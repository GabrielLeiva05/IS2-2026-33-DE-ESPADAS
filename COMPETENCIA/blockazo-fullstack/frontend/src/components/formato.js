// yyyy-MM-dd (formato del backend) -> dd/MM/yyyy
export function formatearFecha(iso) {
  if (!iso) return null
  const [anio, mes, dia] = String(iso).slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

export function iniciales(usuario) {
  const n = usuario?.nombre?.[0] || ''
  const a = usuario?.apellido?.[0] && usuario.apellido !== '-' ? usuario.apellido[0] : ''
  return (n + a).toUpperCase() || '?'
}
