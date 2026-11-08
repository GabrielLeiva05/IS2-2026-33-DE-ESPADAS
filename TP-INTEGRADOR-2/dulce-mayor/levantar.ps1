<#
.SYNOPSIS
    Levanta o detiene todas las aplicaciones de Dulce Mayor con sus requisitos.

.DESCRIPTION
    Prepara el entorno y arranca en un solo comando:
      - PostgreSQL (Docker Compose)                localhost:5432 (o POSTGRES_PORT del .env)
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

# Comprueba el puerto en IPv4 y en IPv6: Vite escucha en [::1] aunque la URL diga "localhost".
# Se crea un socket por familia: un TcpClient IPv4 no puede conectar a una dirección IPv6.
function Puerto-Abierto([int]$puerto) {
    foreach ($direccion in @([System.Net.IPAddress]::Loopback, [System.Net.IPAddress]::IPv6Loopback)) {
        $cliente = $null
        try {
            $cliente = New-Object System.Net.Sockets.TcpClient($direccion.AddressFamily)
            $conexion = $cliente.BeginConnect($direccion, $puerto, $null, $null)
            if ($conexion.AsyncWaitHandle.WaitOne(400, $false)) {
                $cliente.EndConnect($conexion)
                return $true
            }
        } catch {
        } finally {
            if ($cliente) { $cliente.Close() }
        }
    }
    return $false
}

# PID del proceso que escucha en el puerto (0 si no se puede determinar)
function Proceso-DuenoDePuerto([int]$puerto) {
    try {
        $conexion = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction Stop | Select-Object -First 1
        if ($conexion) { return [int]$conexion.OwningProcess }
    } catch {
    }
    return 0
}

# Sube por la cadena de padres hasta el cmd.exe que ejecuta uno de los envoltorios de este script.
# Devuelve 0 si el proceso no lo lanzó este script.
function Envoltorio-Propio([int]$pidProceso) {
    $actual = $pidProceso
    for ($i = 0; $i -lt 6 -and $actual -gt 0; $i++) {
        $proceso = Get-CimInstance Win32_Process -Filter ('ProcessId={0}' -f $actual) -ErrorAction SilentlyContinue
        if (-not $proceso) { break }
        if ($proceso.Name -eq 'cmd.exe' -and $proceso.CommandLine -match 'iniciar-[\w-]+\.cmd') {
            return [int]$proceso.ProcessId
        }
        $actual = [int]$proceso.ParentProcessId
    }
    return 0
}

# Envoltorios de este script que siguen vivos
function Envoltorios-Vivos {
    return @(Get-CimInstance Win32_Process -Filter "Name='cmd.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -match '\.run[\\/]scripts[\\/]iniciar-[\w-]+\.cmd' })
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
    $detenidos = @()
    if (Test-Path $archivoPids) {
        $registro = Get-Content $archivoPids -Raw | ConvertFrom-Json
        foreach ($servicio in $registro) {
            $pidProceso = [int]$servicio.pid
            $vivo = Get-Process -Id $pidProceso -ErrorAction SilentlyContinue
            if ($vivo) {
                # /T termina también los hijos (java y node que lanzan mvnw/npm)
                & taskkill /PID $pidProceso /T /F *> $null
                $detenidos += $pidProceso
                Escribir-Ok ('{0} detenido (PID {1})' -f $servicio.nombre, $pidProceso)
            } else {
                Escribir-Aviso ('{0} ya no estaba en ejecución' -f $servicio.nombre)
            }
        }
        Remove-Item $archivoPids -Force -ErrorAction SilentlyContinue
    } else {
        Escribir-Aviso 'No hay procesos registrados por este script (.run\pids.json)'
    }

    # Barrido de envoltorios que quedaron vivos aunque no estén registrados (reinicios parciales)
    foreach ($envoltorio in (Envoltorios-Vivos)) {
        if ($detenidos -contains [int]$envoltorio.ProcessId) { continue }
        & taskkill /PID ([int]$envoltorio.ProcessId) /T /F *> $null
        $detenidos += [int]$envoltorio.ProcessId
        Escribir-Ok ('Proceso residual detenido (PID {0})' -f $envoltorio.ProcessId)
    }

    # Si algo quedó escuchando en los puertos del sistema, se informa con nombre y PID
    foreach ($puertoServicio in @(8080, 8081, 8082, 5173, 5174)) {
        if (-not (Puerto-Abierto $puertoServicio)) { continue }
        $dueno = Proceso-DuenoDePuerto $puertoServicio
        $nombreDueno = ''
        if ($dueno -gt 0) { $nombreDueno = (Get-Process -Id $dueno -ErrorAction SilentlyContinue).ProcessName }
        Escribir-Aviso ('El puerto {0} sigue ocupado (PID {1}, {2}): no lo lanzó este script.' -f $puertoServicio, $dueno, $nombreDueno)
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

# Puerto publicado por el contenedor: se toma de POSTGRES_PORT en el .env de la raíz.
$puertoBase = 5432
$envRaiz = Join-Path $raiz '.env'
if (Test-Path $envRaiz) {
    foreach ($linea in Get-Content $envRaiz) {
        if ($linea -match '^\s*POSTGRES_PORT\s*=\s*(\d+)') { $puertoBase = [int]$matches[1] }
    }
}

# Si el contenedor del proyecto ya está corriendo, el puerto es suyo.
$compose = Join-Path $raiz 'docker-compose.yml'
$idContenedorBase = ''
if ($dockerDisponible) {
    $idContenedorBase = (& docker compose -f $compose ps -q postgres 2>&1 | Out-String).Trim()
}
# Si hay otro PostgreSQL escuchando (instalación local) se reutiliza ese.
if ($dockerDisponible -and -not $idContenedorBase -and (Puerto-Abierto $puertoBase) -and -not $LimpiarBase) {
    $dueno = ''
    try {
        $conexion = Get-NetTCPConnection -LocalPort $puertoBase -State Listen -ErrorAction Stop | Select-Object -First 1
        if ($conexion) { $dueno = (Get-Process -Id $conexion.OwningProcess -ErrorAction SilentlyContinue).ProcessName }
    } catch { }
    $sufijo = ''
    if ($dueno) { $sufijo = " (proceso: $dueno)" }
    Escribir-Aviso ("Ya hay algo escuchando en el puerto {0} que no es el contenedor del proyecto{1}: se usa ese." -f $puertoBase, $sufijo)
    if ($dueno -and $dueno -notmatch 'docker|com.docker|vpnkit') {
        Escribir-Aviso ('Ese proceso no es Docker: definí POSTGRES_PORT con otro puerto en el .env de la raíz (por ejemplo 5433), ajustá DB_URL en backend/.env y volvé a ejecutar.')
    }
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
        Escribir-Ok ("PostgreSQL listo en localhost:{0}" -f $puertoBase)
    } else {
        Escribir-Aviso 'PostgreSQL no reportó "healthy": sigo igual y verifico la conexión al arrancar la API.'
    }
} else {
    Escribir-Aviso ("Sin Docker propio: se usa el PostgreSQL que ya esté escuchando en localhost:{0}" -f $puertoBase)
}

# Si backend/.env apunta a otro usuario/base que el contenedor, -LimpiarBase no afecta los datos reales
$envBackend = Join-Path $raiz 'backend\.env'
if (Test-Path $envBackend) {
    $usuarioApp = 'dulcemayor'
    $puertoApp = $puertoBase
    foreach ($linea in Get-Content $envBackend) {
        if ($linea -match '^\s*DB_USERNAME\s*=\s*(.+)$') { $usuarioApp = $matches[1].Trim() }
        if ($linea -match '^\s*DB_URL\s*=.*:(\d+)/') { $puertoApp = [int]$matches[1] }
    }
    if ($puertoApp -ne $puertoBase) {
        Escribir-Aviso ("backend/.env apunta a la base en el puerto {0} pero el contenedor publica el {1}: la API NO usa la base de Docker." -f $puertoApp, $puertoBase)
        Escribir-Aviso ('Ajustá DB_URL en backend/.env a jdbc:postgresql://localhost:{0}/<base> o cambiá POSTGRES_PORT en el .env de la raíz.' -f $puertoBase)
    }
    $idContenedor = (& docker compose -f (Join-Path $raiz 'docker-compose.yml') ps -q postgres 2>&1 | Out-String).Trim()
    if ($idContenedor) {
        $variables = (& docker inspect --format '{{range .Config.Env}}{{println .}}{{end}}' $idContenedor 2>&1 | Out-String)
        $usuarioContenedor = ''
        foreach ($linea in ($variables -split "`n")) {
            if ($linea -match '^POSTGRES_USER=(.+)$') { $usuarioContenedor = $matches[1].Trim() }
        }
        if ($usuarioContenedor -and $usuarioApp -ne $usuarioContenedor) {
            Escribir-Aviso ('backend/.env usa DB_USERNAME={0} y el contenedor {1}: la API NO usa la base de Docker.' -f $usuarioApp, $usuarioContenedor)
            Escribir-Aviso 'En ese caso -LimpiarBase borra el volumen de Docker pero no los datos que ve la API. Ajustá DB_USERNAME/DB_PASSWORD en backend/.env o detené el PostgreSQL externo.'
        }
    }
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
        # Puede ser una instancia anterior de este mismo script: se registra para poder detenerla
        $dueno = Proceso-DuenoDePuerto $servicio.Puerto
        $envoltorio = 0
        if ($dueno -gt 0) { $envoltorio = Envoltorio-Propio $dueno }
        if ($envoltorio -gt 0) {
            $lanzados += @{ nombre = $servicio.Nombre; pid = $envoltorio; puerto = $servicio.Puerto }
            Escribir-Ok ('{0} ya estaba en ejecución en el puerto {1} (PID {2})' -f $servicio.Etiqueta, $servicio.Puerto, $envoltorio)
        } else {
            $nombreDueno = ''
            if ($dueno -gt 0) { $nombreDueno = (Get-Process -Id $dueno -ErrorAction SilentlyContinue).ProcessName }
            Escribir-Aviso ('{0}: el puerto {1} lo ocupa otro programa (PID {2}, {3}); se omite. Detenelo o usá -Detener para liberar los del script.' -f $servicio.Etiqueta, $servicio.Puerto, $dueno, $nombreDueno)
        }
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

# Código de salida explícito: lo usan levantar.cmd y las tareas de CI
$fallidos = @($resultados | Where-Object { -not $_.listo })
if ($fallidos.Count -gt 0) {
    exit 1
}
exit 0
