import { useEffect, useState } from 'react'
import { mensajeDeError, urlImagen } from '@comun/api/cliente.js'
import { Alerta, Cargando, Vacio } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import { formatoMoneda, presentacionLegible } from '@comun/utilidades/formato.js'
import { catalogo } from '../servicios.js'

/** Botones − cantidad + (no deja superar el stock disponible). */
export function Stepper({ cantidad, maximo, alCambiar, nombre }) {
  return (
    <span className="stepper">
      <button type="button" aria-label={`Quitar un bulto de ${nombre}`} disabled={cantidad <= 0} onClick={() => alCambiar(cantidad - 1)}>
        <Icono nombre="menos" tamanio={18} />
      </button>
      <output aria-live="polite">{cantidad}</output>
      <button type="button" aria-label={`Agregar un bulto de ${nombre}`} disabled={cantidad >= maximo} onClick={() => alCambiar(cantidad + 1)}>
        <Icono nombre="mas" tamanio={18} />
      </button>
    </span>
  )
}

/**
 * HU-16: catálogo móvil con buscador y filtro por categoría; muestra precio vigente y stock disponible.
 * El backend solo devuelve productos con stock y precio vigente. Con `cantidadDe`/`alCambiar` se usa para tomar pedidos.
 */
export default function ListaProductos({ cantidadDe, alCambiar }) {
  const [categorias, setCategorias] = useState([])
  const [categoria, setCategoria] = useState('')
  const [texto, setTexto] = useState('')
  const [busqueda, setBusqueda] = useState('')
  // Resultado de la última consulta, con la clave (búsqueda + categoría) a la que corresponde.
  const [resultado, setResultado] = useState({ clave: null, productos: [], error: null })

  useEffect(() => {
    catalogo.categorias().then(setCategorias).catch(() => setCategorias([]))
  }, [])

  // Espera a que el usuario deje de escribir antes de consultar.
  useEffect(() => {
    const id = setTimeout(() => setBusqueda(texto.trim()), 350)
    return () => clearTimeout(id)
  }, [texto])

  const clave = `${busqueda}|${categoria}`
  useEffect(() => {
    let vigente = true
    catalogo.productos({ texto: busqueda || undefined, idCategoria: categoria || undefined })
      .then((lista) => { if (vigente) setResultado({ clave, productos: lista, error: null }) })
      .catch((e) => { if (vigente) setResultado({ clave, productos: [], error: mensajeDeError(e, 'No se pudo cargar el catálogo.') }) })
    return () => { vigente = false }
  }, [busqueda, categoria, clave])

  const cargando = resultado.clave !== clave
  const productos = cargando ? null : resultado.productos
  const error = cargando ? null : resultado.error

  return (
    <>
      <div className="buscador">
        <label className="control-con-icono">
          <Icono nombre="buscar" tamanio={18} />
          <span className="oculto-visual">Buscar productos</span>
          <input className="control" type="search" value={texto} onChange={(e) => setTexto(e.target.value)}
            placeholder="Buscar por nombre o marca" enterKeyHint="search" />
        </label>
        <div className="chips" role="group" aria-label="Categorías">
          <button type="button" className={`chip${categoria === '' ? ' activo' : ''}`} onClick={() => setCategoria('')}>Todas</button>
          {categorias.map((c) => (
            <button key={c.id} type="button" className={`chip${categoria === c.id ? ' activo' : ''}`} onClick={() => setCategoria(c.id)}>{c.nombre}</button>
          ))}
        </div>
      </div>
      <Alerta>{error}</Alerta>
      {productos === null && <Cargando texto="Buscando productos…" />}
      {productos?.length === 0 && !error && (
        <Vacio icono="caramelo" titulo="No hay productos para mostrar">Probá con otra búsqueda o categoría.</Vacio>
      )}
      <ul className="lista lista-grilla">
        {productos?.map((p) => (
          <li key={p.id} className="producto">
            <span className="producto-foto">
              {p.idsImagen?.length ? <img src={urlImagen(p.idsImagen[0])} alt="" loading="lazy" /> : <Icono nombre="caramelo" tamanio={30} />}
            </span>
            <div className="producto-datos">
              <strong>{p.nombre}</strong>
              <small>{p.categoria} · {presentacionLegible(p.presentacion, p.unidadesPorPresentacion)}</small>
              <div className="producto-pie">
                <span>
                  <span className="precio">{formatoMoneda(p.precio)}</span>
                  {p.enOferta && <span className="insignia insignia-magenta" style={{ marginLeft: 6 }}>Oferta</span>}
                  <br /><small>Stock: {p.stockDisponible} bultos</small>
                </span>
                {alCambiar && (
                  <Stepper nombre={p.nombre} cantidad={cantidadDe(p.id)} maximo={p.stockDisponible}
                    alCambiar={(n) => alCambiar(p, n)} />
                )}
              </div>
            </div>
          </li>
        ))}
      </ul>
    </>
  )
}
