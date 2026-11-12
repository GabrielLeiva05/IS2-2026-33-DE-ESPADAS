import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Cargando, Encabezado, InsigniaPedido, Vacio } from '@comun/componentes/Interfaz.jsx'
import { formatoFechaHora, formatoMoneda } from '@comun/utilidades/formato.js'
import { pedidos } from '../servicios.js'

const FILTROS = [
  { clave: 'todos', texto: 'Todos' },
  { clave: 'hoy', texto: 'Hoy' },
  { clave: 'semana', texto: 'Esta semana' },
]

function desde(clave) {
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  if (clave === 'hoy') return hoy
  if (clave === 'semana') {
    const lunes = new Date(hoy)
    lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7))
    return lunes
  }
  return null
}

/** Pedidos cargados por el preventista (solo los suyos: el backend filtra por el usuario del token). */
export default function MisPedidos() {
  const [lista, setLista] = useState(null)
  const [error, setError] = useState(null)
  const [filtro, setFiltro] = useState('hoy')

  useEffect(() => {
    pedidos.mios().then(setLista).catch((e) => { setError(mensajeDeError(e)); setLista([]) })
  }, [])

  const visibles = useMemo(() => {
    const limite = desde(filtro)
    return (lista ?? []).filter((p) => !limite || new Date(p.fechaHoraCarga) >= limite)
  }, [lista, filtro])

  return (
    <div className="pantalla">
      <Encabezado titulo="Mis pedidos" />
      <main className="contenido">
        <div className="pestanias" role="tablist">
          {FILTROS.map((f) => (
            <button key={f.clave} type="button" role="tab" aria-selected={filtro === f.clave}
              className={filtro === f.clave ? 'activo' : undefined} onClick={() => setFiltro(f.clave)}>{f.texto}</button>
          ))}
        </div>
        <Alerta>{error}</Alerta>
        {lista === null && <Cargando />}
        {lista && visibles.length === 0 && !error && <Vacio icono="pedido" titulo="No hay pedidos en este período" />}
        <ul className="lista lista-grilla">
          {visibles.map((p) => (
            <li key={p.id}>
              <Link to={`/pedidos/${p.id}`} className="item">
                <span className="item-cuerpo">
                  <strong>N.º {p.numeroPedido} · {p.nombreFantasia}</strong>
                  <small>{formatoFechaHora(p.fechaHoraCarga)} · {formatoMoneda(p.total)}</small>
                </span>
                <span className="item-lado"><InsigniaPedido estado={p.estado} /></span>
              </Link>
            </li>
          ))}
        </ul>
      </main>
    </div>
  )
}
