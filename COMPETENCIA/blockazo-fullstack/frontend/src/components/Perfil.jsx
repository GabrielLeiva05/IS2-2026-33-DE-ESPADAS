import { useState } from 'react'
import { perfilApi } from '../api/api'
import { useAuth } from '../context/AuthContext'
import Alerta from './Alerta'
import Campo from './Campo'
import ResumeView, { DetallesPersonales } from './ReviewView'

const PESTANIAS = [
  { id: 'datos', texto: 'Mis datos' },
  { id: 'editar', texto: 'Editar perfil' },
  { id: 'clave', texto: 'Cambiar contraseña' },
]

/** Seccion Perfil: ver mis datos, modificarlos todos y cambiar la contraseña. */
export default function Perfil() {
  const { usuario, bienvenida, ocultarBienvenida } = useAuth()
  // Quien entra con Google llega sin DNI / fecha / direccion: se abre directo la edicion
  const [pestania, setPestania] = useState(usuario.perfilCompleto ? 'datos' : 'editar')
  const [aviso, setAviso] = useState('')

  const cambiar = (id) => {
    setAviso('')
    ocultarBienvenida()
    setPestania(id)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold text-slate-900">Mi perfil</h1>
        <p className="text-slate-500 text-sm mt-1">Consultá y modificá tus datos personales.</p>
      </div>

      {!usuario.perfilCompleto && (
        <Alerta tipo="aviso">Completá tu DNI, fecha de nacimiento y dirección para terminar tu perfil.</Alerta>
      )}
      <Alerta tipo="exito">{aviso || (bienvenida && '¡Bienvenido! Tu cuenta se creó correctamente.')}</Alerta>

      <ResumeView user={usuario}>
        <div className="flex flex-wrap gap-2 mb-8 border-b border-slate-100 pb-4">
          {PESTANIAS.map((p) => (
            <button
              key={p.id}
              onClick={() => cambiar(p.id)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition cursor-pointer ${
                pestania === p.id ? 'bg-indigo-600 text-white shadow' : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {p.texto}
            </button>
          ))}
        </div>

        {pestania === 'datos' && <DetallesPersonales user={usuario} />}
        {pestania === 'editar' && (
          <EditarPerfil onGuardado={() => { setPestania('datos'); setAviso('Tus datos se actualizaron correctamente.') }} />
        )}
        {pestania === 'clave' && (
          <CambiarClave onGuardado={() => { setPestania('datos'); setAviso('La contraseña se modificó correctamente.') }} />
        )}
      </ResumeView>
    </div>
  )
}

function EditarPerfil({ onGuardado }) {
  const { usuario, guardarSesion } = useAuth()
  const [form, setForm] = useState({
    nombre: usuario.nombre || '',
    apellido: usuario.apellido && usuario.apellido !== '-' ? usuario.apellido : '',
    dni: usuario.dni || '',
    fechaNacimiento: usuario.fechaNacimiento || '',
    direccion: usuario.direccion || '',
    correo: usuario.correo || '',
  })
  const [error, setError] = useState('')
  const [errores, setErrores] = useState({})
  const [guardando, setGuardando] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: undefined })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setErrores({})
    setGuardando(true)
    try {
      // El backend devuelve un token NUEVO (el correo es el usuario y pudo haber cambiado)
      const respuesta = await perfilApi.actualizar(form)
      guardarSesion(respuesta)
      onGuardado()
    } catch (err) {
      setError(err.message)
      setErrores(err.errores || {})
    } finally {
      setGuardando(false)
    }
  }

  const campo = (name, label, props = {}) => (
    <Campo name={name} label={label} value={form[name]} onChange={handleChange} error={errores[name]} {...props} />
  )

  return (
    <form onSubmit={handleSubmit} className="space-y-4" noValidate>
      <h2 className="text-xl font-bold text-slate-800 uppercase tracking-wider border-b pb-2">Editar perfil</h2>
      <Alerta tipo="error">{error}</Alerta>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {campo('nombre', 'Nombre')}
        {campo('apellido', 'Apellido')}
        {campo('dni', 'DNI', { inputMode: 'numeric', maxLength: 8, placeholder: 'Sin puntos' })}
        {campo('fechaNacimiento', 'Fecha de nacimiento', { type: 'date' })}
        {campo('direccion', 'Dirección', { className: 'sm:col-span-2' })}
        {campo('correo', 'Correo (es tu usuario para ingresar)', { type: 'email', className: 'sm:col-span-2' })}
      </div>
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={guardando} className="btn-primary">
          {guardando ? 'Guardando...' : 'Guardar cambios'}
        </button>
      </div>
    </form>
  )
}

function CambiarClave({ onGuardado }) {
  const { usuario, setUsuario } = useAuth()
  const [form, setForm] = useState({ claveActual: '', claveNueva: '', confirmacionClave: '' })
  const [error, setError] = useState('')
  const [errores, setErrores] = useState({})
  const [guardando, setGuardando] = useState(false)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setErrores({})
    if (form.claveNueva !== form.confirmacionClave) {
      setErrores({ confirmacionClave: 'Las claves no coinciden' })
      return
    }
    setGuardando(true)
    try {
      await perfilApi.cambiarClave(form)
      setUsuario({ ...usuario, tieneClave: true })
      onGuardado()
    } catch (err) {
      setError(err.message)
      setErrores(err.errores || {})
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-md" noValidate>
      <h2 className="text-xl font-bold text-slate-800 uppercase tracking-wider border-b pb-2">Cambiar contraseña</h2>
      {!usuario.tieneClave && (
        <p className="text-sm text-slate-500">
          Ingresaste con Google y todavía no tenés contraseña. Definí una para poder ingresar también con tu correo.
        </p>
      )}
      <Alerta tipo="error">{error}</Alerta>
      {usuario.tieneClave && (
        <Campo name="claveActual" type="password" label="Contraseña actual" autoComplete="current-password"
               value={form.claveActual} onChange={handleChange} error={errores.claveActual} />
      )}
      <Campo name="claveNueva" type="password" label="Nueva contraseña" autoComplete="new-password"
             placeholder="Mínimo 6 caracteres" value={form.claveNueva} onChange={handleChange} error={errores.claveNueva} />
      <Campo name="confirmacionClave" type="password" label="Confirmar nueva contraseña" autoComplete="new-password"
             value={form.confirmacionClave} onChange={handleChange} error={errores.confirmacionClave} />
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={guardando} className="btn-primary">
          {guardando ? 'Guardando...' : 'Cambiar contraseña'}
        </button>
      </div>
    </form>
  )
}
