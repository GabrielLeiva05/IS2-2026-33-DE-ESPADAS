import { useState } from 'react'
import { Alerta, Hoja } from './Interfaz.jsx'

/** Hoja inferior para registrar por qué no se concretó una visita o una entrega. */
export default function FormularioMotivo({ abierta, titulo, sugerencias, obligatorio = false, textoBoton, alCerrar, alConfirmar }) {
  const [motivo, setMotivo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState(null)

  async function confirmar(e) {
    e.preventDefault()
    setEnviando(true)
    setError(null)
    try {
      await alConfirmar(motivo.trim() || null)
      setMotivo('')
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Hoja abierta={abierta} titulo={titulo} alCerrar={alCerrar}>
      <form className="formulario" onSubmit={confirmar}>
        <Alerta>{error}</Alerta>
        <div className="opciones-motivo">
          {sugerencias.map((s) => (
            <button key={s} type="button" className={`chip${motivo === s ? ' activo' : ''}`} onClick={() => setMotivo(s)}>{s}</button>
          ))}
        </div>
        <label className={`campo${obligatorio ? ' campo-requerido' : ''}`}>
          <span>Motivo</span>
          <textarea className="control" maxLength={255} value={motivo} required={obligatorio}
            onChange={(e) => setMotivo(e.target.value)} placeholder="Escribí o elegí un motivo" />
        </label>
        <button className="boton boton-primario" type="submit" disabled={enviando || (obligatorio && !motivo.trim())}>
          {enviando ? 'Guardando…' : textoBoton}
        </button>
        <button className="boton boton-fantasma" type="button" onClick={alCerrar}>Cancelar</button>
      </form>
    </Hoja>
  )
}
