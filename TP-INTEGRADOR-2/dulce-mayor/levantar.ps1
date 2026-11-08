<#
.SYNOPSIS
    Levanta o detiene todas las aplicaciones de Dulce Mayor con sus requisitos.

.DESCRIPTION
    Prepara el entorno y arranca en un solo comando:
      - PostgreSQL (Docker Compose)                localhost:5432
      - API REST             (backend)             http://localhost:8080
      - Panel administrativo (admin-web)           http://localhost:8081
      - Sitio institucional  (institucional-web)   http://localhost:8082
      - App del preventista  (movil/preventista)   http://localhost:5173
      - App del distribuidor (movil/distribuidor)  http://localhost:5174

    Verifica requisitos (Java 25, Node 20+, Docker), crea los .env que falten a partir de los
    .env.example, espera a que cada servicio responda por HTTP y deja los logs en .run\logs.
    Los procesos se lanzan desacoplados y sobreviven al cierre del script.

.PARAMETER LimpiarBase
    Borra el volumen de PostgreSQL y lo recrea vacío antes de arrancar.

.PARAMETER ConDatosDemo
    Fuerza DATOS_DEMO_HABILITADO=true para la API en esta ejecución.

.PARAMETER SinDatosDemo
    Fuerza DATOS_DEMO_HABILITADO=false para la API en esta ejecución.

.PARAMETER SinMoviles
    No arranca las dos apps React.

.PARAMETER AbrirNavegador
    Abre las cuatro interfaces web en el navegador predeterminado.

.PARAMETER Detener
    Detiene los procesos que levantó este script.

.PARAMETER IncluirBase
    Junto con -Detener, también detiene el contenedor de PostgreSQL.

.PARAMETER Verificar
    Solo comprueba requisitos y configuración, sin arrancar nada.

.PARAMETER Solo`r`n    Arranca solo los servicios indicados, por ejemplo: -Solo backend,admin-web`r`n`r`n.PARAMETER EsperaMaxima
    Segundos máximos de espera por servicio (por defecto 300).

.EXAMPLE
    .\levantar.ps1 -LimpiarBase -ConDatosDemo -AbrirNavegador

.EXAMPLE
    .\levantar.ps1 -Verificar

.EXAMPLE
    .\levantar.ps1 -Detener -IncluirBase
#>
[CmdletBinding()]
param(
    [switch]$LimpiarBase,
    [switch]$ConDatosDemo,
    [switch]$SinDatosDemo,
    [switch]$SinMoviles,
    [switch]$AbrirNavegador,
    [switch]$Detener,
    [switch]$IncluirBase,
    [switch]$Verificar,
    [string[]]$Solo,
    [int]$EsperaMaxima = 300
)

# 'Continue' evita que el stderr de comandos nativos (java -version, docker info) corte el script;
# los fallos relevantes se controlan con $LASTEXITCODE y throw explícito.
$ErrorActionPreference = 'Continue'

$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
$carpetaRun = Join-Path $raiz '.run'
$carpetaLogs = Join-Path $carpetaRun 'logs'
$carpetaScripts = Join-Path $carpetaRun 'scripts'
$archivoPids = Join-Path $carpetaRun 'pids.json'

# ----------------------------------------------------------------------------- utilidades

function Escribir-Paso($texto) { Write-Host "`n==> $texto" -ForegroundColor Cyan }
function Escribir-Ok($texto) { Write-Host "    [ok] $texto" -ForegroundColor Green }
function Escribir-Aviso($texto) { Write-Host "    [!]  $texto" -ForegroundColor Yellow }
function Escribir-Error($texto) { Write-Host "    [x]  $texto" -ForegroundColor Red }

function Probar-Comando($nombre) {
    return [bool](Get-Command $nombre -ErrorAction SilentlyContinue)
}

function Obtener-VersionJava {
    if (-not (Probar-Comando 'java')) { return $null }
    $salida = & java -version 2>&1 | Out-String
    $m = [regex]::Match($salida, 'version "(\d+)')
    if ($m.Success) { return [int]$m.Groups[1].Value }
    return $null
}

# Comprueba el puerto en IPv4 y en IPv6: Vite escucha en ::1 aunque la URL diga "localhost"
function Puerto-Abierto([int]$puerto) {
    foreach ($host0 in @('127.0.0.1', '::1')) {
        $cliente = New-Object System.Net.Sockets.TcpClient
        try {
            $conexion = $cliente.BeginConnect($host0, $puerto, $null, $null)
            if ($conexion.AsyncWaitHandle.WaitOne(400, $false)) {
                $cliente.EndConnect($conexion)
                return $true
            }
        } catch {
        } finally {
            $cliente.Close()
        }
    }
    return $false
}

function Url-Responde([string]$url) {
    try {
        $null = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 4 -ErrorAction Stop
        return $true
    } catch {
        # Un 401/403/302 también indica que el servicio está escuchando
        if ($_.Exception.Response) { return $true }
        return $false
    }
}

function Esperar-Servicio([hashtable]$servicio, [int]$segundosMaximos, [string]$log) {
    $limite = (Get-Date).AddSeconds($segundosMaximos)
    while ((Get-Date) -lt $limite) {
        if (Url-Responde $servicio.Salud) { return $true }
        if ($log -and (Test-Path $log)) {
            $contenido = Get-Content $log -Raw -ErrorAction SilentlyContinue
            if ($contenido -and $contenido -match '(?i)(APPLICATION FAILED TO START|BUILD FAILURE|error when starting dev server|EADDRINUSE)') {
                return $false
            }
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

<#
    Lanza el servicio desacoplado (sobrevive al cierre de este script) con su log redirigido.
    Se usa un envoltorio .cmd y Win32_Process.Create: el proceso creado así no pertenece al árbol
    del script. Como WMI no hereda el PATH de esta sesión, se escribe la ruta absoluta del
    ejecutable y se fija el PATH dentro del propio envoltorio.
#>
function Iniciar-Servicio([hashtable]$servicio, [string]$directorio, [string]$salida) {
    $ejecutableLocal = Join-Path $directorio $servicio.Ejecutable
    $ejecutable = $servicio.Ejecutable
    if (Test-Path $ejecutableLocal) {
        $ejecutable = $ejecutableLocal
    } else {
        $comando = Get-Command $servicio.Ejecutable -ErrorAction SilentlyContinue
        if ($comando) { $ejecutable = $comando.Source }
    }
    $argumentos = ($servicio.Argumentos -join ' ')
    $rutaHerramienta = Split-Path -Parent $ejecutable

    $envoltorio = Join-Path $carpetaScripts ('iniciar-{0}.cmd' -f $servicio.Nombre)
    $lineas = @()
    $lineas += '@echo off'
    $lineas += ('set PATH={0};{1};%PATH%' -f $rutaHerramienta, (Join-Path $directorio 'node_modules\.bin'))
    $lineas += ('cd /d "{0}"' -f $directorio)
    $lineas += ('"{0}" {1} 1> "{2}" 2>&1' -f $ejecutable, $argumentos, $salida)
    Set-Content -Path $envoltorio -Value ($lineas -join "`r`n") -Encoding ASCII

    try {
        $resultado = Invoke-CimMethod -ClassName Win32_Process -MethodName Create `
            -Arguments @{ CommandLine = ('cmd.exe /c "{0}"' -f $envoltorio); CurrentDirectory = $directorio } -ErrorAction Stop
        if ($resultado -and $resultado.ReturnValue -eq 0) { return [int]$resultado.ProcessId }
    } catch {
    }

    # Alternativa si WMI no está disponible
    $proceso = Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', ('"{0}"' -f $envoltorio) `
        -WorkingDirectory $directorio -WindowStyle Hidden -PassThru
    if ($proceso) { return [int]$proceso.Id }
    return 0
}

# ----------------------------------------------------------------------------- detener

if ($Detener) {
    Escribir-Paso 'Deteniendo servicios'
    if (Test-Path $archivoPids) {
        $registro = Get-Content $archivoPids -Raw | ConvertFrom-Json
        foreach ($servicio in $registro) {
            $pidProceso = [int]$servicio.pid
            $vivo = Get-Process -Id $pidProceso -ErrorAction SilentlyContinue
            if ($vivo) {
                # /T termina también los hijos (java y node que lanzan mvnw/npm)
                & taskkill /PID $pidProceso /T /F *> $null
                Escribir-Ok ('{0} detenido (PID {1})' -f $servicio.nombre, $pidProceso)
            } else {
                Escribir-Aviso ('{0} ya no estaba en ejecución' -f $servicio.nombre)
            }
        }
        Remove-Item $archivoPids -Force -ErrorAction SilentlyContinue
    } else {
        Escribir-Aviso 'No hay procesos registrados por este script (.run\pids.json)'
    }
    if ($IncluirBase -and (Probar-Comando 'docker')) {
        & docker compose -f (Join-Path $raiz 'docker-compose.yml') stop postgres *> $null
        Escribir-Ok 'PostgreSQL detenido'
    }
    Escribir-Paso 'Listo'
    return
}

# ----------------------------------------------------------------------------- requisitos

Escribir-Paso 'Verificando requisitos'

$problemas = @()

$versionJava = Obtener-VersionJava
if ($versionJava -eq $null) {
    $problemas += 'Java no está instalado o no está en el PATH (se requiere JDK 25).'
} elseif ($versionJava -lt 25) {
    $problemas += ('Java {0} detectado: el backend requiere JDK 25 o superior.' -f $versionJava)
} else {
    Escribir-Ok ('Java {0}' -f $versionJava)
}

if (-not (Probar-Comando 'node')) {
    $problemas += 'Node.js no está instalado (se requiere 20 o superior).'
} else {
    $versionNode = (& node --version).Trim()
    $mayorNode = [int](($versionNode -replace '^v', '') -split '\.')[0]
    if ($mayorNode -lt 20) {
        $problemas += ('Node {0} detectado: se requiere 20 o superior.' -f $versionNode)
    } else {
        Escribir-Ok ('Node {0} / npm {1}' -f $versionNode, (& npm --version).Trim())
    }
}

$dockerDisponible = $false
if (-not (Probar-Comando 'docker')) {
    Escribir-Aviso 'Docker no está instalado: se asume que PostgreSQL ya corre en el puerto 5432.'
} else {
    $null = & docker info --format '{{.ServerVersion}}' 2>&1
    if ($LASTEXITCODE -eq 0) {
        $dockerDisponible = $true
        Escribir-Ok 'Docker disponible'
    } else {
        Escribir-Aviso 'El demonio de Docker no responde: iniciá Docker Desktop o usá un PostgreSQL existente.'
    }
}

$archivosEnv = @(
    @{ Ruta = 'backend'; Etiqueta = 'API' },
    @{ Ruta = 'admin-web'; Etiqueta = 'Panel' },
    @{ Ruta = 'institucional-web'; Etiqueta = 'Sitio' }
)
foreach ($item in $archivosEnv) {
    $envDestino = Join-Path (Join-Path $raiz $item.Ruta) '.env'
    $envEjemplo = Join-Path (Join-Path $raiz $item.Ruta) '.env.example'
    if (Test-Path $envDestino) {
        Escribir-Ok ('{0}: .env presente' -f $item.Etiqueta)
    } elseif (Test-Path $envEjemplo) {
        Copy-Item $envEjemplo $envDestino
        if ($item.Ruta -eq 'backend') {
            Escribir-Aviso 'API: .env creado desde .env.example (definí JWT_SECRET y, si usás Mercado Pago, las credenciales)'
        } else {
            Escribir-Aviso ('{0}: .env creado desde .env.example (revisá puertos y URLs)' -f $item.Etiqueta)
        }
    } else {
        $problemas += ('Falta {0}\.env y su .env.example' -f $item.Ruta)
    }
}

$envPreventista = Join-Path $raiz 'movil\preventista\.env'
if (-not (Test-Path $envPreventista)) {
    Escribir-Aviso 'App preventista sin .env: usa el proxy a http://localhost:8080 y el cobro con tarjeta queda deshabilitado (falta VITE_MP_PUBLIC_KEY).'
}

if ($problemas.Count -gt 0) {
    Escribir-Paso 'Requisitos faltantes'
    foreach ($problema in $problemas) { Escribir-Error $problema }
    throw 'No se puede continuar: resolvé los puntos anteriores.'
}

if ($Verificar) {
    Escribir-Paso 'Verificación completada (no se arrancó ningún servicio)'
    return
}

# ----------------------------------------------------------------------------- base de datos

# Si ya hay un PostgreSQL escuchando (instalación local u otro contenedor) se reutiliza.
if ($dockerDisponible -and (Puerto-Abierto 5432) -and -not $LimpiarBase) {
    Escribir-Aviso 'Ya hay un PostgreSQL escuchando en el puerto 5432: se usa ese y no se inicia el contenedor.'
    $dockerDisponible = $false
}
if (-not $dockerDisponible -and $LimpiarBase) {
    Escribir-Aviso 'La base no la gestiona este proyecto con Docker: -LimpiarBase no puede recrearla. Usá una base local o detené el servicio externo.'
    $LimpiarBase = $false
}

if ($dockerDisponible) {
    $compose = Join-Path $raiz 'docker-compose.yml'
    if ($LimpiarBase) {
        Escribir-Paso 'Reinicializando la base de datos (se borra el volumen)'
        & docker compose -f $compose down -v *> $null
        Escribir-Ok 'Volumen eliminado'
    } else {
        Escribir-Paso 'Iniciando PostgreSQL'
    }
    & docker compose -f $compose up -d postgres *> $null
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar PostgreSQL con Docker Compose.' }

    $limite = (Get-Date).AddSeconds(90)
    $baseLista = $false
    while ((Get-Date) -lt $limite -and -not $baseLista) {
        $estado = (& docker compose -f $compose ps postgres 2>&1 | Out-String)
        if ($estado -match 'healthy') { $baseLista = $true }
        if (-not $baseLista) { Start-Sleep -Seconds 2 }
    }
    if ($baseLista) {
        Escribir-Ok 'PostgreSQL listo en localhost:5432'
    } else {
        Escribir-Aviso 'PostgreSQL no reportó "healthy": sigo igual y verifico la conexión al arrancar la API.'
    }
} else {
    Escribir-Aviso 'Sin Docker propio: se usa el PostgreSQL que ya esté escuchando en localhost:5432'
}

# ----------------------------------------------------------------------------- datos demo

if ($ConDatosDemo -and $SinDatosDemo) {
    throw 'Elegí -ConDatosDemo o -SinDatosDemo, no ambos.'
}
if ($ConDatosDemo) {
    $env:DATOS_DEMO_HABILITADO = 'true'
    Escribir-Ok 'Datos de demostración forzados: DATOS_DEMO_HABILITADO=true'
} elseif ($SinDatosDemo) {
    $env:DATOS_DEMO_HABILITADO = 'false'
    Escribir-Ok 'Datos de demostración deshabilitados para esta ejecución'
}
if ($LimpiarBase -and $SinDatosDemo) {
    Escribir-Aviso 'Base vacía y sin datos demo: el sistema arranca sin catálogo ni usuarios.'
}

# ----------------------------------------------------------------------------- servicios

$servicios = @(
    @{ Nombre = 'backend'; Ruta = 'backend'; Puerto = 8080; Ejecutable = 'mvnw.cmd'; Argumentos = @('-B', 'spring-boot:run'); Etiqueta = 'API REST'; Salud = 'http://localhost:8080/api/catalogo' },
    @{ Nombre = 'admin-web'; Ruta = 'admin-web'; Puerto = 8081; Ejecutable = 'mvnw.cmd'; Argumentos = @('-B', 'spring-boot:run'); Etiqueta = 'Panel administrativo'; Salud = 'http://localhost:8081/login' },
    @{ Nombre = 'institucional-web'; Ruta = 'institucional-web'; Puerto = 8082; Ejecutable = 'mvnw.cmd'; Argumentos = @('-B', 'spring-boot:run'); Etiqueta = 'Sitio institucional'; Salud = 'http://localhost:8082/' }
)
if (-not $SinMoviles) {
    $servicios += @{ Nombre = 'preventista'; Ruta = 'movil\preventista'; Puerto = 5173; Ejecutable = 'npm.cmd'; Argumentos = @('run', 'dev'); Etiqueta = 'App del preventista'; Salud = 'http://localhost:5173/' }
    $servicios += @{ Nombre = 'distribuidor'; Ruta = 'movil\distribuidor'; Puerto = 5174; Ejecutable = 'npm.cmd'; Argumentos = @('run', 'dev'); Etiqueta = 'App del distribuidor'; Salud = 'http://localhost:5174/' }
}

if ($Solo -and $Solo.Count -gt 0) {
    # Se acepta -Solo backend,admin-web y también -Solo "backend,admin-web"
    $pedidos = @()
    foreach ($entrada in $Solo) {
        foreach ($nombre in ($entrada -split ",")) {
            $limpio = $nombre.Trim()
            if ($limpio) { $pedidos += $limpio }
        }
    }
    $servicios = $servicios | Where-Object { $pedidos -contains $_.Nombre }
    if (@($servicios).Count -eq 0) {
        throw ("Ninguno de los servicios indicados existe. Opciones: backend, admin-web, institucional-web, preventista, distribuidor")
    }
}

New-Item -ItemType Directory -Force -Path $carpetaLogs | Out-Null
New-Item -ItemType Directory -Force -Path $carpetaScripts | Out-Null

Escribir-Paso 'Iniciando servicios'
$lanzados = @()
foreach ($servicio in $servicios) {
    $directorio = Join-Path $raiz $servicio.Ruta
    $salida = Join-Path $carpetaLogs ('{0}.log' -f $servicio.Nombre)

    if (Puerto-Abierto $servicio.Puerto) {
        Escribir-Aviso ('{0}: el puerto {1} ya está en uso, se omite' -f $servicio.Etiqueta, $servicio.Puerto)
        continue
    }

    if ($servicio.Ejecutable -eq 'npm.cmd' -and -not (Test-Path (Join-Path $directorio 'node_modules'))) {
        Escribir-Aviso ('{0}: instalando dependencias (npm install)…' -f $servicio.Etiqueta)
        Push-Location $directorio
        try {
            & npm install --no-audit --no-fund *> $salida
            if ($LASTEXITCODE -ne 0) { throw ('npm install falló en {0} (ver {1})' -f $servicio.Ruta, $salida) }
        } finally {
            Pop-Location
        }
    }

    Remove-Item $salida -Force -ErrorAction SilentlyContinue
    $pidNuevo = Iniciar-Servicio -servicio $servicio -directorio $directorio -salida $salida
    if ($pidNuevo -le 0) {
        Escribir-Error ('{0}: no se pudo lanzar {1}' -f $servicio.Etiqueta, $servicio.Ejecutable)
        continue
    }
    $lanzados += @{ nombre = $servicio.Nombre; pid = $pidNuevo; puerto = $servicio.Puerto }
    Escribir-Ok ('{0} lanzado (PID {1})' -f $servicio.Etiqueta, $pidNuevo)
}

if ($lanzados.Count -eq 0) {
    Escribir-Aviso 'No se lanzó ningún proceso nuevo.'
} else {
    # Se acumulan los PIDs de ejecuciones anteriores para que -Detener los alcance a todos
    $previos = @()
    if (Test-Path $archivoPids) {
        try {
            $previos = @(Get-Content $archivoPids -Raw | ConvertFrom-Json)
        } catch {
            $previos = @()
        }
    }
    $todos = @()
    foreach ($previo in $previos) {
        $sigueVivo = Get-Process -Id ([int]$previo.pid) -ErrorAction SilentlyContinue
        $reemplazado = $lanzados | Where-Object { $_.nombre -eq $previo.nombre }
        if ($sigueVivo -and -not $reemplazado) {
            $todos += @{ nombre = $previo.nombre; pid = [int]$previo.pid; puerto = [int]$previo.puerto }
        }
    }
    $todos += $lanzados
    # PowerShell 5.1 no tiene -AsArray: se fuerza el formato de arreglo a mano
    $json = ConvertTo-Json -InputObject @($todos) -Depth 4
    if ($json -notmatch '^\s*\[') { $json = '[' + "`r`n" + $json + "`r`n]" }
    Set-Content -Path $archivoPids -Value $json -Encoding UTF8
}

# ----------------------------------------------------------------------------- espera

Escribir-Paso 'Esperando a que los servicios respondan'
$resultados = @()
foreach ($registro in $lanzados) {
    $servicio = $servicios | Where-Object { $_.Nombre -eq $registro.nombre }
    $log = Join-Path $carpetaLogs ('{0}.log' -f $registro.nombre)

    $listo = Esperar-Servicio -servicio $servicio -segundosMaximos $EsperaMaxima -log $log
    if ($listo) {
        Escribir-Ok ('{0} responde en {1}' -f $servicio.Etiqueta, $servicio.Salud)
    } else {
        Escribir-Error ('{0} no respondió en {1} s. Revisá {2}' -f $servicio.Etiqueta, $EsperaMaxima, $log)
    }
    $resultados += @{ servicio = $servicio; listo = $listo }
}

foreach ($servicio in $servicios) {
    $yaConsiderado = $resultados | Where-Object { $_.servicio.Nombre -eq $servicio.Nombre }
    if (-not $yaConsiderado -and (Url-Responde $servicio.Salud)) {
        $resultados += @{ servicio = $servicio; listo = $true }
    }
}

# ----------------------------------------------------------------------------- resumen

$urls = @{
    'backend'           = 'http://localhost:8080'
    'admin-web'         = 'http://localhost:8081'
    'institucional-web' = 'http://localhost:8082'
    'preventista'       = 'http://localhost:5173'
    'distribuidor'      = 'http://localhost:5174'
}

Escribir-Paso 'Sistema disponible'
Write-Host ''
Write-Host ('  {0,-24} {1,-26} {2}' -f 'SERVICIO', 'URL', 'ESTADO') -ForegroundColor White
Write-Host ('  {0,-24} {1,-26} {2}' -f ('-' * 24), ('-' * 26), ('-' * 7))
foreach ($servicio in $servicios) {
    $resultado = $resultados | Where-Object { $_.servicio.Nombre -eq $servicio.Nombre }
    $estado = 'sin arrancar'
    if ($resultado -and $resultado.listo) { $estado = 'ok' }
    elseif ($resultado) { $estado = 'revisar log' }
    Write-Host ('  {0,-24} {1,-26} {2}' -f $servicio.Etiqueta, $urls[$servicio.Nombre], $estado)
}

Write-Host ''
Write-Host '  Credenciales de prueba' -ForegroundColor Cyan
Write-Host '    Jefe          jefe@dulcemayor.com.ar       / dulcemayor   (se cambia en el primer ingreso)'
Write-Host '    Administrativo admin@dulcemayor.test      / dulcemayor'
Write-Host '    Preventista   preventista@dulcemayor.test  / dulcemayor'
Write-Host '    Distribuidor  distribuidor@dulcemayor.test / dulcemayor'
Write-Host '    Cliente       kiosco.demo@dulcemayor.test  (registrate en el sitio con ese correo)'
Write-Host ''
Write-Host ('  Logs:     {0}' -f $carpetaLogs)
Write-Host '  Detener:  .\detener.cmd   (o .\levantar.ps1 -Detener -IncluirBase)'
Write-Host ''

if ($AbrirNavegador) {
    foreach ($nombre in @('admin-web', 'institucional-web', 'preventista', 'distribuidor')) {
        $resultado = $resultados | Where-Object { $_.servicio.Nombre -eq $nombre -and $_.listo }
        if ($resultado) { Start-Process $urls[$nombre] }
    }
}
