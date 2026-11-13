import { useAuth } from '../auth/AuthContext.jsx'
import { BotonEnlace, Encabezado } from '../componentes/Interfaz.jsx'
import Icono from '../componentes/Icono.jsx'

const ROLES = { PREVENTISTA: 'Preventista', DISTRIBUIDOR: 'Distribuidor' }

export default function Perfil() {
  const { usuario, salir } = useAuth()
  const iniciales = `${usuario.nombre?.[0] ?? ''}${usuario.apellido?.[0] ?? ''}`.toUpperCase()
  return (
    <div className="pantalla">
      <Encabezado titulo="Perfil" />
      <main className="contenido">
        <section className="tarjeta perfil">
          <span className="avatar avatar-grande">{iniciales}</span>
          <div>
            <h2>{usuario.nombre} {usuario.apellido}</h2>
            <p className="texto-secundario">{usuario.correo}</p>
            <span className="insignia insignia-violeta">{ROLES[usuario.rol] ?? usuario.rol}</span>
          </div>
        </section>
        <BotonEnlace a="/cambiar-clave" variante="secundario"><Icono nombre="llave" />Cambiar contraseña</BotonEnlace>
        <button type="button" className="boton boton-peligro" onClick={salir}><Icono nombre="salir" />Cerrar sesión</button>
      </main>
    </div>
  )
}
