import { createContext, useCallback, useContext, useMemo, useState } from 'react'

const CarritoContext = createContext(null)

/**
 * Pedido en armado (solo en memoria, para un punto de venta a la vez). Los precios que se muestran son
 * los del catálogo al momento de elegir; el backend recalcula con el precio vigente y controla el stock.
 */
export function CarritoProvider({ children }) {
  const [idPuntoVenta, setIdPuntoVenta] = useState(null)
  const [lineas, setLineas] = useState({})

  const iniciar = useCallback((id) => {
    if (id !== idPuntoVenta) {
      setIdPuntoVenta(id)
      setLineas({})
    }
  }, [idPuntoVenta])

  const cambiarCantidad = useCallback((producto, cantidad) => {
    setLineas((actuales) => {
      const limite = Math.max(0, Math.min(cantidad, producto.stockDisponible))
      const siguientes = { ...actuales }
      if (limite === 0) delete siguientes[producto.id]
      else siguientes[producto.id] = { producto, cantidad: limite }
      return siguientes
    })
  }, [])

  const vaciar = useCallback(() => setLineas({}), [])

  const valor = useMemo(() => {
    const lista = Object.values(lineas)
    return {
      idPuntoVenta,
      lineas: lista,
      cantidadDe: (idProducto) => lineas[idProducto]?.cantidad ?? 0,
      bultos: lista.reduce((suma, l) => suma + l.cantidad, 0),
      total: lista.reduce((suma, l) => suma + l.cantidad * l.producto.precio, 0),
      iniciar,
      cambiarCantidad,
      vaciar,
    }
  }, [idPuntoVenta, lineas, iniciar, cambiarCantidad, vaciar])

  return <CarritoContext.Provider value={valor}>{children}</CarritoContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCarrito() {
  return useContext(CarritoContext)
}
