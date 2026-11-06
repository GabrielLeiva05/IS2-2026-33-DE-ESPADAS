import { useEffect, useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Encabezado } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import SelectorUbicacion from '@comun/componentes/SelectorUbicacion.jsx'
import { mapas, puntosVenta, ubicaciones } from '../servicios.js'

const PROVINCIA = 'Mendoza'
const CONDICIONES = [
  ['RESPONSABLE_INSCRIPTO', 'Responsable inscripto'], ['MONOTRIBUTISTA', 'Monotributista'],
  ['EXENTO', 'Exento'], ['CONSUMIDOR_FINAL', 'Consumidor final'],
]

const normalizar = (t) => (t ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim()

/**
 * HU-17 (según backlog): alta de punto de venta con datos del comercio (nombre de fantasía, razón social,
 * CUIT, condición de IVA), su responsable (cliente del paso 1) y la ubicación marcada en el mapa.
 * La ubicación sale de: buscar la dirección (Nominatim, vía backend), tocar el mapa o el GPS del teléfono.
 * Sin ubicación elegida no se puede guardar: no se usan coordenadas por defecto.
 */
export default function NuevoPuntoVenta() {
  const { state } = useLocation()
  const navegar = useNavigate()
  const cliente = state?.cliente
  const [datos, setDatos] = useState({
    nombreFantasia: '', razonSocial: '', cuit: '', condicionIva: 'MONOTRIBUTISTA',
    calle: '', numeracion: '', barrio: '', referencia: '', idDepartamento: '', idLocalidad: '',
  })
  const [departamentos, setDepartamentos] = useState([])
  const [localidades, setLocalidades] = useState([])
  const [posicion, setPosicion] = useState(null)
  const [descripcion, setDescripcion] = useState(null)
  const [resultados, setResultados] = useState(null)
  const [buscando, setBuscando] = useState(false)
  const [avisoMapa, setAvisoMapa] = useState(null)
  const [error, setError] = useState(null)
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    ubicaciones.provincia(PROVINCIA)
      .then((p) => ubicaciones.departamentos(p.id))
      .then((lista) => setDepartamentos([...lista].sort((a, b) => a.nombre.localeCompare(b.nombre))))
      .catch((e) => setError(mensajeDeError(e, 'No se pudieron cargar los departamentos.')))
  }, [])

  useEffect(() => {
    if (!datos.idDepartamento) return
    ubicaciones.localidades(datos.idDepartamento)
      .then((lista) => {
        setLocalidades(lista)
        // Si el departamento tiene una sola localidad cargada, se elige sola.
        if (lista.length === 1) setDatos((d) => ({ ...d, idLocalidad: lista[0].id }))
      })
      .catch(() => setLocalidades([]))
  }, [datos.idDepartamento])

  if (!cliente) return <Navigate to="/clientes/nuevo" replace />

  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }))
  const elegirDepartamento = (e) => {
    setLocalidades([])
    setDatos((d) => ({ ...d, idDepartamento: e.target.value, idLocalidad: '' }))
  }

  const departamento = departamentos.find((d) => d.id === datos.idDepartamento)
  const localidad = localidades.find((l) => l.id === datos.idLocalidad)

  function ubicar(latitud, longitud, texto) {
    setPosicion({ latitud, longitud })
    setDescripcion(texto ?? null)
    setResultados(null)
    setAvisoMapa(null)
  }

  async function tocarMapa(latitud, longitud) {
    ubicar(latitud, longitud)
    try {
      const encontrado = await mapas.inversa(latitud, longitud)
      setDescripcion(encontrado.descripcion)
    } catch {
      setDescripcion(null) // la ubicación tocada vale igual, aunque no se encuentre la dirección
    }
  }

  async function buscarDireccion() {
    if (!datos.calle.trim()) {
      setAvisoMapa('Escribí al menos la calle para buscarla en el mapa.')
      return
    }
    const texto = [`${datos.calle} ${datos.numeracion}`.trim(), localidad?.nombre, departamento?.nombre, PROVINCIA]
      .filter(Boolean).join(', ')
    setBuscando(true)
    setAvisoMapa(null)
    try {
      const lista = await mapas.geocodificar(texto)
      setResultados(lista)
      if (lista.length === 0) setAvisoMapa('No encontramos esa dirección. Tocá el mapa en el lugar del comercio.')
    } catch (e) {
      setAvisoMapa(mensajeDeError(e, 'No se pudo buscar la dirección. Tocá el mapa en el lugar del comercio.'))
    } finally {
      setBuscando(false)
    }
  }

  function elegirResultado(r) {
    ubicar(r.latitud, r.longitud, r.descripcion)
    // Completa el departamento si el resultado lo informa y todavía no se eligió.
    if (!datos.idDepartamento && r.departamento) {
      const coincide = departamentos.find((d) => normalizar(r.departamento).includes(normalizar(d.nombre)))
      if (coincide) setDatos((d) => ({ ...d, idDepartamento: coincide.id, idLocalidad: '' }))
    }
    if (!datos.numeracion && r.numeracion) setDatos((d) => ({ ...d, numeracion: r.numeracion }))
  }

  function usarGps() {
    if (!('geolocation' in navigator)) {
      setAvisoMapa('Este teléfono no informa su ubicación. Tocá el mapa en el lugar del comercio.')
      return
    }
    setAvisoMapa('Buscando tu ubicación…')
    navigator.geolocation.getCurrentPosition(
      (p) => tocarMapa(p.coords.latitude, p.coords.longitude),
      () => setAvisoMapa('No pudimos obtener tu ubicación. Tocá el mapa en el lugar del comercio.'),
      { enableHighAccuracy: true, timeout: 15000 },
    )
  }

  async function guardar(e) {
    e.preventDefault()
    if (!posicion) {
      setError('Ubicá el comercio en el mapa: buscá la dirección, usá tu ubicación o tocá el mapa.')
      return
    }
    setGuardando(true)
    setError(null)
    try {
      const creado = await puntosVenta.alta({
        nombreFantasia: datos.nombreFantasia.trim(),
        razonSocial: datos.razonSocial.trim(),
        cuit: datos.cuit.replace(/\D/g, ''),
        condicionIva: datos.condicionIva,
        idCliente: cliente.id,
        direccion: {
          calle: datos.calle.trim(), numeracion: datos.numeracion.trim() || null, barrio: datos.barrio.trim() || null,
          referencia: datos.referencia.trim() || null, latitud: posicion.latitud, longitud: posicion.longitud,
          idLocalidad: datos.idLocalidad,
        },
      })
      navegar(`/puntos/${creado.id}`, { replace: true })
    } catch (err) {
      setError(mensajeDeError(err, 'No se pudo guardar el punto de venta.'))
      setGuardando(false)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  return (
    <div className="pantalla">
      <Encabezado titulo="Nuevo punto de venta" subtitulo={`Paso 2 de 2 · ${cliente.nombre} ${cliente.apellido}`} volver="/clientes/nuevo" />
      <main className="contenido">
        <div className="pasos" aria-hidden="true"><span className="hecho" /><span className="hecho" /></div>
        <Alerta>{error}</Alerta>
        <form className="formulario" onSubmit={guardar}>
          <section className="tarjeta formulario">
            <h2>Comercio</h2>
            <label className="campo campo-requerido"><span>Nombre de fantasía</span>
              <input className="control" maxLength={120} required value={datos.nombreFantasia} onChange={cambiar('nombreFantasia')} placeholder="Kiosco El Sol" /></label>
            <label className="campo campo-requerido"><span>Razón social</span>
              <input className="control" maxLength={150} required value={datos.razonSocial} onChange={cambiar('razonSocial')} /></label>
            <div className="fila-campos">
              <label className="campo campo-requerido"><span>CUIT</span>
                <input className="control" inputMode="numeric" maxLength={13} required value={datos.cuit} onChange={cambiar('cuit')} placeholder="20123456786" /></label>
              <label className="campo campo-requerido"><span>Condición IVA</span>
                <select className="control" value={datos.condicionIva} onChange={cambiar('condicionIva')}>
                  {CONDICIONES.map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
                </select></label>
            </div>
          </section>

          <section className="tarjeta formulario">
            <h2>Dirección del comercio</h2>
            <div className="fila-campos" style={{ gridTemplateColumns: '2fr 1fr' }}>
              <label className="campo campo-requerido"><span>Calle</span>
                <input className="control" maxLength={120} required value={datos.calle} onChange={cambiar('calle')} /></label>
              <label className="campo"><span>Número</span>
                <input className="control" inputMode="numeric" maxLength={10} value={datos.numeracion} onChange={cambiar('numeracion')} /></label>
            </div>
            <div className="fila-campos">
              <label className="campo campo-requerido"><span>Departamento</span>
                <select className="control" required value={datos.idDepartamento} onChange={elegirDepartamento}>
                  <option value="">Elegí…</option>
                  {departamentos.map((d) => <option key={d.id} value={d.id}>{d.nombre}</option>)}
                </select></label>
              <label className="campo campo-requerido"><span>Localidad</span>
                <select className="control" required value={datos.idLocalidad} onChange={cambiar('idLocalidad')} disabled={!localidades.length}>
                  <option value="">Elegí…</option>
                  {localidades.map((l) => <option key={l.id} value={l.id}>{l.nombre}</option>)}
                </select></label>
            </div>
            <label className="campo"><span>Barrio</span>
              <input className="control" maxLength={80} value={datos.barrio} onChange={cambiar('barrio')} /></label>
            <label className="campo"><span>Referencia</span>
              <input className="control" maxLength={150} value={datos.referencia} onChange={cambiar('referencia')} placeholder="Frente a la plaza, portón verde" /></label>
          </section>

          <section className="tarjeta formulario">
            <div className="tarjeta-titulo" style={{ marginBottom: 0 }}>
              <h2>Ubicación en el mapa</h2>
              {posicion ? <span className="insignia insignia-verde">Marcada</span> : <span className="insignia insignia-naranja">Falta</span>}
            </div>
            <div className="grupo-botones">
              <button type="button" className="boton boton-secundario boton-chico" onClick={buscarDireccion} disabled={buscando}>
                <Icono nombre="buscar" tamanio={18} />{buscando ? 'Buscando…' : 'Buscar dirección'}
              </button>
              <button type="button" className="boton boton-fantasma boton-chico" onClick={usarGps}>
                <Icono nombre="gps" tamanio={18} />Mi ubicación
              </button>
            </div>
            {avisoMapa && <Alerta tipo="aviso">{avisoMapa}</Alerta>}
            {resultados?.length > 0 && (
              <ul className="lista">
                {resultados.map((r) => (
                  <li key={`${r.latitud},${r.longitud}`}>
                    <button type="button" className="item" style={{ width: '100%', textAlign: 'left', cursor: 'pointer' }} onClick={() => elegirResultado(r)}>
                      <Icono nombre="ubicacion" />
                      <span className="item-cuerpo"><small style={{ whiteSpace: 'normal' }}>{r.descripcion}</small></span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
            <SelectorUbicacion posicion={posicion} alCambiar={tocarMapa} />
            <p className="ayuda">
              {posicion
                ? `Ubicación: ${posicion.latitud.toFixed(5)}, ${posicion.longitud.toFixed(5)}${descripcion ? ` · ${descripcion}` : ''}. Podés arrastrar el pin para corregirla.`
                : 'Tocá el mapa en el lugar exacto del comercio, o buscá la dirección.'}
            </p>
          </section>

          <button className="boton boton-primario" type="submit" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar punto de venta'}</button>
        </form>
      </main>
    </div>
  )
}
