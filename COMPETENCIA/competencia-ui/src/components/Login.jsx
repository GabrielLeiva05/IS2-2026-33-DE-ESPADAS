import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Login({ setUser }) {
  const [correo, setCorreo] = useState('');
  const [password, setPassword] = useState('');
  const [errorBackend, setErrorBackend] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorBackend('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correo, password })
      });

      if (!response.ok) {
        // Capturamos el mensaje exacto que configuraste en el backend 
        // (ej: si no existe la cuenta o la contraseña falla)
        const errorMessage = await response.text();
        setErrorBackend(errorMessage || 'Credenciales inválidas');
        return;
      }

      const userData = await response.json();
      setUser(userData);
      navigate('/mis-datos');

    } catch (err) {
      setErrorBackend('Error de conexión con el servidor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-xl border border-slate-100">
        <h2 className="text-center text-3xl font-extrabold text-slate-900">Iniciar Sesión</h2>
        
        {/* Alerta única controlada por la respuesta de error del backend */}
        {errorBackend && (
          <div className="bg-red-50 text-red-500 p-3 rounded-lg text-sm text-center font-medium">
            {errorBackend}
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleLogin}>
          <input 
            type="email" 
            required 
            placeholder="Correo Electrónico" 
            value={correo} 
            onChange={(e) => setCorreo(e.target.value)} 
            className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" 
          />
          <input 
            type="password" 
            required 
            placeholder="Contraseña" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            className="w-full px-4 py-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" 
          />
          
          <button 
            type="submit" 
            disabled={loading} 
            className="w-full py-3 px-4 rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 font-medium shadow-md transition disabled:opacity-50"
          >
            {loading ? 'Verificando...' : 'Ingresar'}
          </button>
        </form>
      </div>
    </div>
  );
}