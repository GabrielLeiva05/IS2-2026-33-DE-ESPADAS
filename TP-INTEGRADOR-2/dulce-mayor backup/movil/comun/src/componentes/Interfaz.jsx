import { Link, NavLink, useNavigate } from 'react-router'
import { ESTADOS_PEDIDO, RESULTADOS } from '../utilidades/formato.js'
import Icono from './Icono.jsx'

/** Barra superior de cada pantalla, con volver opcional. */
export function Encabezado({ titulo, subtitulo, volver, accion }) {
  const navegar = useNavigate()
  return (
    <header className="encabezado">
      {volver && (
        <button type="button" className="encabezado-volver" aria-label="Volver"
          onClick={() => (typeof volver === 'string' ? navegar(volver) : navegar(-1))}>
          <Icono nombre="volver" />
        </button>
      )}
      <div className="encabezado-textos">
        <h1>{titulo}</h1>
        {subtitulo && <p>{subtitulo}</p>}
      </div>
      {accion}
    </header>
  )
}

/** Navegación inferior tipo app. `items`: [{ a, texto, icono, fin }] */
export function BarraInferior({ items }) {
  return (
    <nav className="barra-inferior" aria-label="Secciones">
      {items.map((item) => (
        <NavLink key={item.a} to={item.a} end={item.fin} className={({ isActive }) => (isActive ? 'activo' : undefined)}>
          <Icono nombre={item.icono} />
          <span>{item.texto}</span>
        </NavLink>
      ))}
    </nav>
  )
}

export function Cargando({ texto = 'Cargando…' }) {
  return <div className="cargando" role="status"><span className="girador" aria-hidden="true" />{texto}</div>
}

export function Alerta({ tipo = 'error', children }) {
  if (!children) return null
  return (
    <div className={`alerta alerta-${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      <Icono nombre={tipo === 'exito' ? 'ok' : tipo === 'info' ? 'info' : 'alerta'} />
      <span>{children}</span>
    </div>
  )
}

export function Vacio({ icono = 'info', titulo, children, accion }) {
  return (
    <div className="vacio">
      <span className="vacio-icono"><Icono nombre={icono} /></span>
      <h2>{titulo}</h2>
      {children && <p>{children}</p>}
      {accion}
    </div>
  )
}

export function InsigniaResultado({ resultado }) {
  const r = RESULTADOS[resultado] ?? RESULTADOS.PENDIENTE
  return <span className={`insignia insignia-${r.tono}`}>{r.texto}</span>
}

export function InsigniaPedido({ estado }) {
  const e = ESTADOS_PEDIDO[estado] ?? { texto: estado, tono: 'gris' }
  return <span className={`insignia insignia-${e.tono}`}>{e.texto}</span>
}

/** Botón-enlace grande, usado para acciones principales. */
export function BotonEnlace({ a, children, variante = 'primario', ...resto }) {
  return <Link to={a} className={`boton boton-${variante}`} {...resto}>{children}</Link>
}

/** Hoja inferior (modal) para pedir un dato corto, como el motivo de una visita sin pedido. */
export function Hoja({ abierta, titulo, children, alCerrar }) {
  if (!abierta) return null
  return (
    <div className="hoja-fondo" role="presentation" onClick={alCerrar}>
      <section className="hoja" role="dialog" aria-modal="true" aria-label={titulo} onClick={(e) => e.stopPropagation()}>
        <div className="hoja-asa" aria-hidden="true" />
        <h2>{titulo}</h2>
        {children}
      </section>
    </div>
  )
}
