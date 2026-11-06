import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { esNoEncontrado, mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Encabezado } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import { clientes, puntosVenta } from '../servicios.js'

const VACIO = { nombre: '', apellido: '', clave: '', confirmacionClave: '', tipoDocumento: 'DNI', numeroDocumento: '' }

/**
 * Alta de cliente desde el celular del preventista (paso 1 de 2).
 * Primero se busca por correo: si ya tiene cuenta se le asocia el comercio; si no, el propio cliente
 * escribe su correo y su contraseña en este teléfono (la cuenta queda con rol CLIENTE).
 */
export default function NuevoCliente() {
  const navegar = useNavigate()
  const [correo, setCorreo] = useState('')
  const [encontrado, setEncontrado] = useState(null) // { cliente, puntos }
  const [alta, setAlta] = useState(false)
  const [datos, setDatos] = useState(VACIO)
  const [verClave, setVerClave] = useState(false)
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
        clave: datos.clave,
        confirmacionClave: datos.confirmacionClave,
        tipoDocumento: datos.numeroDocumento.trim() ? datos.tipoDocumento : null,
        numeroDocumento: datos.numeroDocumento.trim() || null,
      })
      navegar('/puntos/nuevo', { state: { cliente } })
    } catch (err) {
      setError(mensajeDeError(err, 'No se pudo registrar al cliente.'))
      setDatos((d) => ({ ...d, clave: '', confirmacionClave: '' }))
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
            <Alerta tipo="info">No hay una cuenta con ese correo. Pedile al cliente que complete sus datos y elija su contraseña.</Alerta>
            <div className="fila-campos">
              <label className="campo campo-requerido"><span>Nombre</span>
                <input className="control" autoComplete="off" maxLength={80} required value={datos.nombre} onChange={cambiar('nombre')} /></label>
              <label className="campo campo-requerido"><span>Apellido</span>
                <input className="control" autoComplete="off" maxLength={80} required value={datos.apellido} onChange={cambiar('apellido')} /></label>
            </div>
            <div className="fila-campos">
              <label className="campo"><span>Documento</span>
                <select className="control" value={datos.tipoDocumento} onChange={cambiar('tipoDocumento')}>
                  <option value="DNI">DNI</option><option value="CUIL">CUIL</option><option value="CUIT">CUIT</option><option value="PASAPORTE">Pasaporte</option>
                </select></label>
              <label className="campo"><span>Número</span>
                <input className="control" inputMode="numeric" maxLength={15} value={datos.numeroDocumento} onChange={cambiar('numeroDocumento')} /></label>
            </div>
            <label className="campo campo-requerido"><span>Contraseña (la escribe el cliente)</span>
              <span className="control-con-accion">
                <input className="control" type={verClave ? 'text' : 'password'} autoComplete="new-password" minLength={8} maxLength={72} required
                  value={datos.clave} onChange={cambiar('clave')} />
                <button type="button" className="boton-texto" onClick={() => setVerClave((v) => !v)}>{verClave ? 'Ocultar' : 'Ver'}</button>
              </span>
              <small className="ayuda">Entre 8 y 72 caracteres, con letras y números. No se guarda en este teléfono.</small>
            </label>
            <label className="campo campo-requerido"><span>Repetir contraseña</span>
              <input className="control" type={verClave ? 'text' : 'password'} autoComplete="new-password" minLength={8} maxLength={72} required
                value={datos.confirmacionClave} onChange={cambiar('confirmacionClave')} /></label>
            <button className="boton boton-primario" type="submit" disabled={ocupado}>{ocupado ? 'Registrando…' : 'Crear cuenta y seguir'}</button>
          </form>
        )}
      </main>
    </div>
  )
}
