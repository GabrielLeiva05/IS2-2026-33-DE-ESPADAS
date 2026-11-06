import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Cargando, Encabezado, InsigniaPedido, Vacio } from '@comun/componentes/Interfaz.jsx'
import { formatoFechaHora, formatoMoneda } from '@comun/utilidades/formato.js'
import { entregas } from '../servicios.js'

/** Pedidos asignados al distribuidor que todavía no se entregaron (incluye los reprogramados de días anteriores). */
export default function Pendientes() {
  const [lista, setLista] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    entregas.pendientes().then(setLista).catch((e) => { setError(mensajeDeError(e)); setLista([]) })
  }, [])

  return (
    <div className="pantalla">
      <Encabezado titulo="Pedidos asignados" subtitulo="Pendientes de entrega" />
      <main className="contenido">
        <Alerta>{error}</Alerta>
        {lista === null && <Cargando />}
        {lista?.length === 0 && !error && <Vacio icono="camion" titulo="No tenés pedidos pendientes" />}
        <ul className="lista">
          {lista?.map((p) => (
            <li key={p.id}>
              <Link to={`/entregas/${p.idPuntoVenta}`} className="item">
                <span className="item-cuerpo">
                  <strong>N.º {p.numeroPedido} · {p.nombreFantasia}</strong>
                  <small>{p.direccion}</small>
                  <small>Confirmado {formatoFechaHora(p.fechaConfirmacion)} · {formatoMoneda(p.total)}</small>
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
