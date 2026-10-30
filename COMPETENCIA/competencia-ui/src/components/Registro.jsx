import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Registro() {
  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    fechaNacimiento: '',
    direccion: '',
    correo: '',
    password: '',
    confirmPassword: ''
  });
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje({ texto: '', tipo: '' });
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });

      const data = await response.text(); // O response.json() según cómo devuelva el mensaje tu backend

      if (!response.ok) {
        // Mostramos la alerta exacta que mandó el backend
        setMensaje({ texto: data || 'Error en el registro', tipo: 'error' });
        return;
      }

      // Si el backend valida todo correctamente
      setMensaje({ texto: 'Registro exitoso. Redirigiendo...', tipo: 'success' });
      setTimeout(() => navigate('/login'), 2000);

    } catch (err) {
      setMensaje({ texto: 'No se pudo conectar con el servidor', tipo: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-xl border border-slate-100">
        <h2 className="text-center text-3xl font-extrabold text-slate-900">Crea tu cuenta</h2>
        
        {/* Alerta dinámica basada en la respuesta del backend */}
        {mensaje.texto && (
          <div className={`p-3 rounded-lg text-sm text-center ${mensaje.tipo === 'error' ? 'bg-red-50 text-red-500' : 'bg-green-50 text-green-600'}`}>
            {mensaje.texto}
          </div>
        )}

        <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
          <div className="grid grid-cols-2 gap-4">
            <input name="nombre" type="text" required placeholder="Nombre" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input name="apellido" type="text" required placeholder="Apellido" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <input name="dni" type="text" required placeholder="DNI" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          <input name="fechaNacimiento" type="date" required onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          <input name="direccion" type="text" required placeholder="Dirección" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          <input name="correo" type="email" required placeholder="Correo Electrónico" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          <input name="password" type="password" required placeholder="Contraseña" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          <input name="confirmPassword" type="password" required placeholder="Confirmar Contraseña" onChange={handleChange} className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />

          <button type="submit" disabled={loading} className="w-full py-3 px-4 rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 font-medium transition shadow-md disabled:opacity-50">
            {loading ? 'Procesando...' : 'Registrarse'}
          </button>
        </form>
      </div>
    </div>
  );
}