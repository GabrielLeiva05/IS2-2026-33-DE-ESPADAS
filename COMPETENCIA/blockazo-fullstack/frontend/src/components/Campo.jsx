// Input con etiqueta y mensaje de error por campo (los errores vienen del backend en "errores")
export default function Campo({ label, error, className = '', ...props }) {
  return (
    <div className={className}>
      {label && <label htmlFor={props.name} className="label">{label}</label>}
      <input id={props.name} className={`input ${error ? 'input-error' : ''}`} {...props} />
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
}
