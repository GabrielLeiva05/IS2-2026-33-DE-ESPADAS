import { formatearFecha, iniciales } from './formato'

/**
 * Tarjeta "Mi perfil" (diseño original del frontend), ahora con los datos REALES
 * del usuario logueado que llegan de GET /api/perfil.
 * `children` es el contenido de la columna derecha (datos, edicion o cambio de clave).
 */
export default function ResumeView({ user, children }) {
  return (
    <div className="bg-white shadow-2xl rounded-3xl overflow-hidden border border-slate-100 grid grid-cols-1 md:grid-cols-3">
      {/* Sidebar / Avatar y contacto */}
      <div className="bg-indigo-700 p-8 text-white flex flex-col items-center justify-center text-center">
        <div className="w-36 h-36 rounded-full bg-white/15 border-4 border-white/20 shadow-lg mb-4 flex items-center justify-center text-5xl font-bold tracking-wide">
          {iniciales(user)}
        </div>
        <h1 className="text-2xl font-bold">{user.nombre} {user.apellido !== '-' ? user.apellido : ''}</h1>
        <p className="text-indigo-200 text-sm mt-1 break-all">{user.correo}</p>
        <div className="mt-4 flex flex-wrap justify-center gap-2 text-xs font-semibold">
          <span className="px-3 py-1 rounded-full bg-white/15">{user.rol === 'ADMIN' ? 'Administrador' : 'Usuario'}</span>
          {user.proveedor !== 'LOCAL' && (
            <span className="px-3 py-1 rounded-full bg-white/15">Cuenta {user.proveedor === 'GOOGLE' ? 'Google' : user.proveedor}</span>
          )}
        </div>
        {user.fechaAlta && (
          <p className="text-indigo-200/80 text-xs mt-6">Miembro desde {formatearFecha(user.fechaAlta)}</p>
        )}
      </div>

      {/* Contenido principal */}
      <div className="col-span-2 p-8 md:p-12">{children}</div>
    </div>
  )
}

/** Vista de solo lectura de los datos personales. */
export function DetallesPersonales({ user }) {
  const dato = (valor) => valor || <span className="text-slate-400 font-normal italic">Sin completar</span>
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 uppercase tracking-wider border-b pb-2">Sobre Mí</h2>
        <p className="text-slate-600 mt-3 text-sm leading-relaxed">
          ¡Hola! Este es mi perfil personal registrado en el sistema. A continuación se detallan mis datos validados por el sistema.
        </p>
      </div>
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-slate-800">Detalles Personales</h3>
        <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
          <Item titulo="Nombre">{dato(user.nombre)}</Item>
          <Item titulo="Apellido">{dato(user.apellido !== '-' ? user.apellido : null)}</Item>
          <Item titulo="DNI">{dato(user.dni)}</Item>
          <Item titulo="Fecha de Nacimiento">{dato(formatearFecha(user.fechaNacimiento))}</Item>
          <Item titulo="Dirección" ancho>{dato(user.direccion)}</Item>
          <Item titulo="Correo (usuario)" ancho>{dato(user.correo)}</Item>
        </ul>
      </div>
    </div>
  )
}

function Item({ titulo, ancho, children }) {
  return (
    <li className={`bg-slate-50 p-4 rounded-xl border border-slate-100 ${ancho ? 'sm:col-span-2' : ''}`}>
      <span className="block text-slate-400 font-medium text-xs uppercase">{titulo}</span>
      <span className="text-slate-800 font-semibold text-base break-all">{children}</span>
    </li>
  )
}
