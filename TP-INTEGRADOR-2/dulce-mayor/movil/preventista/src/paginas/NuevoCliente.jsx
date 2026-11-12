import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { esNoEncontrado, mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Encabezado } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import { clientes, puntosVenta } from '../servicios.js'

const VACIO = { nombre: '', apellido: '', tipoDocumento: 'DNI', numeroDocumento: '', fechaNacimiento: '' }

// Fecha máxima de nacimiento: mayor de edad (la API vuelve a validarlo).
const hoy = new Date()
const MAYOR_DE_EDAD = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate()).toISOString().slice(0, 10)

/**
 * Alta de cliente desde el celular del preventista (paso 1 de 2).
 * Primero se busca por correo: si ya tiene cuenta se le asocia el comercio; si no, se registra con documento y
 * fecha de nacimiento obligatorios. La cuenta (rol CLIENTE) nace con la contraseña provisoria "dulcemayor" y el
 * cliente debe cambiarla en su primer ingreso al sitio de Dulce Mayor (lo exige la API).
 */
export default function NuevoCliente() {
  const navegar = useNavigate()
  const [correo, setCorreo] = useState('')
  const [encontrado, setEncontrado] = useState(null) // { cliente, puntos }
  const [alta, setAlta] = useState(false)
  const [datos, setDatos] = useState(VACIO)
  const [error, setError] = useState(null)
  const [ocupado, setOcupado] = useState(false)

  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }))

  async function buscar(e) {
    e.preventDefault()
    setOcupado(true)
    setError(null)
    setEncontrado(null)
    setAlta(false)
    try {
      const cliente = await clientes.buscarPorCorreo(correo.trim())
      setEncontrado({ cliente, puntos: await puntosVenta.delCliente(cliente.id) })
    } catch (err) {
      if (esNoEncontrado(err)) setAlta(true)
      else setError(mensajeDeError(err))
    } finally {
      setOcupado(false)
    }
  }

  async function registrar(e) {
    e.preventDefault()
    setOcupado(true)
    setError(null)
    try {
      const cliente = await clientes.altaEnCampo({
        nombre: datos.nombre.trim(),
        apellido: datos.apellido.trim(),
        correo: correo.trim(),
        tipoDocumento: datos.tipoDocumento,
        numeroDocumento: datos.numeroDocumento.trim(),
        fechaNacimiento: datos.fechaNacimiento,
      })
      navegar('/puntos/nuevo', { state: { cliente } })
    } catch (err) {
      setError(mensajeDeError(err, 'No se pudo registrar al cliente.'))
    } finally {
      setOcupado(false)
    }
  }

  return (
    <div className="pantalla">
      <Encabezado titulo="Nuevo cliente" subtitulo="Paso 1 de 2 · Responsable del comercio" volver="/" />
      <main className="contenido">
        <div className="pasos" aria-hidden="true"><span className="hecho" /><span /></div>
        <Alerta>{error}</Alerta>

        <form className="tarjeta formulario" onSubmit={buscar}>
          <label className="campo campo-requerido">
            <span>Correo del cliente</span>
            <input className="control" type="email" inputMode="email" autoComplete="off" required value={correo}
              onChange={(e) => { setCorreo(e.target.value); setEncontrado(null); setAlta(false) }} placeholder="cliente@correo.com" />
            <small className="ayuda">Será su usuario para confirmar pedidos en el sitio de Dulce Mayor.</small>
          </label>
          {!alta && <button className="boton boton-secundario" type="submit" disabled={ocupado}>{ocupado ? 'Buscando…' : 'Continuar'}</button>}
        </form>

        {encontrado && (
          <section className="tarjeta">
            <Alerta tipo="info">Este correo ya tiene cuenta. Podés agregarle otro comercio.</Alerta>
            <p style={{ marginTop: 10 }}><b>{encontrado.cliente.nombre} {encontrado.cliente.apellido}</b></p>
            {encontrado.puntos.length > 0 && (
              <ul className="lista" style={{ marginTop: 10 }}>
                {encontrado.puntos.map((p) => (
                  <li key={p.id}>
                    <Link className="item" to={`/puntos/${p.id}`}>
                      <Icono nombre="tienda" />
                      <span className="item-cuerpo"><strong>{p.nombreFantasia}</strong><small>CUIT {p.cuit}</small></span>
                      <Icono nombre="flecha" />
                    </Link>
                  </li>
                ))}
              </ul>
            )}
            <button className="boton boton-primario" style={{ marginTop: 12 }} type="button"
              onClick={() => navegar('/puntos/nuevo', { state: { cliente: encontrado.cliente } })}>
              Agregar un comercio
            </button>
          </section>
        )}

        {alta && (
          <form className="tarjeta formulario" onSubmit={registrar}>
            <Alerta tipo="info">No hay una cuenta con ese correo. Completá los datos del responsable del comercio.</Alerta>
            <div className="fila-campos">
              <label className="campo campo-requerido"><span>Nombre</span>
                <input className="control" autoComplete="off" maxLength={80} required value={datos.nombre} onChange={cambiar('nombre')} /></label>
              <label className="campo campo-requerido"><span>Apellido</span>
                <input className="control" autoComplete="off" maxLength={80} required value={datos.apellido} onChange={cambiar('apellido')} /></label>
            </div>
            <div className="fila-campos">
              <label className="campo campo-requerido"><span>Documento</span>
                <select className="control" required value={datos.tipoDocumento} onChange={cambiar('tipoDocumento')}>
                  <option value="DNI">DNI</option><option value="CUIL">CUIL</option><option value="CUIT">CUIT</option><option value="PASAPORTE">Pasaporte</option>
                </select></label>
              <label className="campo campo-requerido"><span>Número</span>
                <input className="control" inputMode="numeric" maxLength={15} required value={datos.numeroDocumento}
                  onChange={cambiar('numeroDocumento')} placeholder="Sin puntos" /></label>
            </div>
            <label className="campo campo-requerido"><span>Fecha de nacimiento</span>
              <input className="control" type="date" required max={MAYOR_DE_EDAD} value={datos.fechaNacimiento}
                onChange={cambiar('fechaNacimiento')} />
              <small className="ayuda">Tiene que ser mayor de edad.</small>
            </label>
            <Alerta tipo="info">
              La cuenta se crea con la contraseña provisoria <b>dulcemayor</b>. Avisale al cliente que la va a tener que
              cambiar la primera vez que entre al sitio de Dulce Mayor con su correo.
            </Alerta>
            <button className="boton boton-primario" type="submit" disabled={ocupado}>{ocupado ? 'Registrando…' : 'Crear cuenta y seguir'}</button>
          </form>
        )}
      </main>
    </div>
  )
}
