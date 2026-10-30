import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Registro from './components/Registro';
import ResumeView from './components/ReviewView';

export default function App() {
  // Estado para almacenar los datos del usuario cuando el backend valida y autoriza el login
  const [user, setUser] = useState(null);

  return (
    <BrowserRouter>
      <Routes>
        {/* Ruta para el formulario de Registro */}
        <Route path="/registro" element={<Registro />} />

        {/* Ruta para el Login, pasándole la función setUser para guardar la sesión */}
        <Route path="/login" element={<Login setUser={setUser} />} />

        {/* Ruta protegida: si el usuario está autenticado muestra sus datos, sino redirige al login */}
        <Route 
          path="/mis-datos" 
          element={user ? <ResumeView user={user} setUser={setUser} /> : <Navigate to="/login" />} 
        />

        {/* Redirección por defecto para cualquier otra ruta no encontrada */}
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}