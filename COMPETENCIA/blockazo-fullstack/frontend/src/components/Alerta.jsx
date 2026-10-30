const ESTILOS = {
  error: 'bg-red-50 text-red-600 border-red-100',
  exito: 'bg-green-50 text-green-700 border-green-100',
  aviso: 'bg-amber-50 text-amber-700 border-amber-100',
}

export default function Alerta({ tipo = 'error', children }) {
  if (!children) return null
  return (
    <div role="alert" className={`p-3 rounded-lg border text-sm text-center font-medium ${ESTILOS[tipo]}`}>
      {children}
    </div>
  )
}
