import { useEffect, useState } from 'react'
import { usuariosApi } from '../api/api'
import { useAuth } from '../context/AuthContext'
import Alerta from './Alerta'
import Campo from './Campo'
import { formatearFecha } from './formato'

const VACIO = {
  nombre: '', apellido: '', dni: '', fechaNacimiento: '', direccion: '', correo: '', rol: 'USUARIO', clave: '',
}

/**
 * ABM de usuarios (solo ADMIN). Consume los endpoints heredados del BaseController
 * del backend (patron Template): GET/POST /api/usuarios, PUT/DELETE /api/usuarios/{id}.
 */
export default function AdminUsuarios() {
  const { usuario: yo } = useAuth()
  const [usuarios, setUsuarios] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [editando, setEditando] = useState(null)      // null = cerrado, {} = alta, {id..} = modificacion
  const [confirmarBaja, setConfirmarBaja] = useState(null)

  const [version, setVersion] = useState(0)          // se incrementa para volver a pedir la lista
  const cargar = () => setVersion((v) => v + 1)

  useEffect(() => {
    usuariosApi.listar()
      .then(setUsuarios)
      .catch((err) => setError(err.message))
      .finally(() => setCargando(false))
  }, [version])

  const darDeBaja = async (id) => {
    setError(''); setExito('')
    try {
      await usuariosApi.eliminar(id)
      setExito('El usuario fue dado de baja.')
      setConfirmarBaja(null)
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  const alGuardar = (mensaje) => {
    setEditando(null)
    setExito(mensaje)
    cargar()
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">Usuarios</h1>
          <p className="text-slate-500 text-sm mt-1">Alta, baja y modificación de usuarios del sistema.</p>
        </div>
        {!editando && (
          <button onClick={() => { setExito(''); setError(''); setEditando({}) }} className="btn-primary">
            + Nuevo usuario
          </button>
        )}
      </div>

      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="exito">{exito}</Alerta>

      {editando && (
        <FormularioUsuario
          inicial={editando}
          onCancelar={() => setEditando(null)}
          onGuardado={alGuardar}
        />
      )}

      <div className="bg-white rounded-2xl shadow-xl border border-slate-100 overflow-hidden">
        {cargando ? (
          <p className="p-8 text-center text-slate-400 text-sm">Cargando usuarios...</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wide text-left">
                <tr>
                  <th className="px-5 py-3 font-medium">Nombre</th>
                  <th className="px-5 py-3 font-medium">Correo</th>
                  <th className="px-5 py-3 font-medium">DNI</th>
                  <th className="px-5 py-3 font-medium">Nacimiento</th>
                  <th className="px-5 py-3 font-medium">Rol</th>
                  <th className="px-5 py-3 font-medium text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {usuarios.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/60">
                    <td className="px-5 py-4 font-semibold text-slate-800 whitespace-nowrap">
                      {u.nombre} {u.apellido !== '-' ? u.apellido : ''}
                      {u.proveedor === 'GOOGLE' && <span className="ml-2 text-[10px] font-bold text-slate-400">GOOGLE</span>}
                    </td>
                    <td className="px-5 py-4 text-slate-600">{u.correo}</td>
                    <td className="px-5 py-4 text-slate-600">{u.dni || '—'}</td>
                    <td className="px-5 py-4 text-slate-600 whitespace-nowrap">{formatearFecha(u.fechaNacimiento) || '—'}</td>
                    <td className="px-5 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                        u.rol === 'ADMIN' ? 'bg-indigo-100 text-indigo-700' : 'bg-slate-100 text-slate-600'}`}>
                        {u.rol}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-right whitespace-nowrap">
                      {confirmarBaja === u.id ? (
                        <span className="inline-flex items-center gap-2">
                          <span className="text-xs text-slate-500">¿Dar de baja?</span>
                          <button onClick={() => darDeBaja(u.id)} className="px-3 py-1.5 rounded-lg text-xs font-semibold text-white bg-red-600 hover:bg-red-700 cursor-pointer">Sí</button>
                          <button onClick={() => setConfirmarBaja(null)} className="px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer">No</button>
                        </span>
                      ) : (
                        <span className="inline-flex gap-2">
                          <button onClick={() => { setExito(''); setError(''); setEditando(u) }}
                                  className="px-3 py-1.5 rounded-lg text-xs font-semibold text-indigo-700 hover:bg-indigo-50 cursor-pointer">
                            Editar
                          </button>
                          <button onClick={() => setConfirmarBaja(u.id)} disabled={u.id === yo.id}
                                  title={u.id === yo.id ? 'No podés darte de baja a vos mismo' : ''}
                                  className="px-3 py-1.5 rounded-lg text-xs font-semibold text-red-600 hover:bg-red-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer">
                            Eliminar
                          </button>
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

function FormularioUsuario({ inicial, onCancelar, onGuardado }) {
  const esAlta = !inicial.id
  const [form, setForm] = useState({
    ...VACIO,
    ...Object.fromEntries(Object.entries(inicial).filter(([k]) => k in VACIO).map(([k, v]) => [k, v ?? ''])),
    clave: '',
  })
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setGuardando(true)
    // Campos vacios -> null (en la modificacion, clave null = se conserva la actual)
    const datos = Object.fromEntries(Object.entries(form).map(([k, v]) => [k, v === '' ? null : v]))
    try {
      if (esAlta) {
        await usuariosApi.crear(datos)
        onGuardado('Usuario creado correctamente.')
      } else {
        await usuariosApi.modificar(inicial.id, datos)
        onGuardado('Usuario modificado correctamente.')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setGuardando(false)
    }
  }

  const campo = (name, label, props = {}) => (
    <Campo name={name} label={label} value={form[name]} onChange={handleChange} {...props} />
  )

  return (
    <form onSubmit={handleSubmit} noValidate
          className="bg-white rounded-2xl shadow-xl border border-slate-100 p-6 md:p-8 space-y-5">
      <h2 className="text-xl font-bold text-slate-800 uppercase tracking-wider border-b pb-2">
        {esAlta ? 'Nuevo usuario' : `Editar usuario #${inicial.id}`}
      </h2>
      <Alerta tipo="error">{error}</Alerta>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {campo('nombre', 'Nombre')}
        {campo('apellido', 'Apellido')}
        {campo('dni', 'DNI', { inputMode: 'numeric', maxLength: 8 })}
        {campo('fechaNacimiento', 'Fecha de nacimiento', { type: 'date' })}
        {campo('direccion', 'Dirección', { className: 'lg:col-span-2' })}
        {campo('correo', 'Correo (usuario)', { type: 'email', className: 'sm:col-span-2' })}
        <div>
          <label htmlFor="rol" className="label">Rol</label>
          <select id="rol" name="rol" value={form.rol} onChange={handleChange} className="input">
            <option value="USUARIO">USUARIO</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
        {campo('clave', esAlta ? 'Contraseña' : 'Nueva contraseña (opcional)', {
          type: 'password', autoComplete: 'new-password',
          placeholder: esAlta ? 'Mínimo 6 caracteres' : 'Dejar vacío para no cambiarla',
          className: 'sm:col-span-2',
        })}
      </div>
      <div className="flex justify-end gap-3">
        <button type="button" onClick={onCancelar} className="btn-secondary">Cancelar</button>
        <button type="submit" disabled={guardando} className="btn-primary">
          {guardando ? 'Guardando...' : esAlta ? 'Crear usuario' : 'Guardar cambios'}
        </button>
      </div>
    </form>
  )
}
