import React from 'react';

export default function ResumeView({ userData }) {
  // Datos de ejemplo si no vienen por props, simulando el usuario autenticado
  const user = userData || {
    nombre: "Gabriel",
    apellido: "Pérez",
    dni: "38123456",
    fechaNacimiento: "1998-05-12",
    direccion: "Av. Libertador 1234, Mendoza",
    correo: "gabriel@email.com"
  };

  return (
    <div className="bg-slate-50 min-h-screen font-sans">
      <div className="max-w-4xl mx-auto px-6 py-12">
        <div className="bg-white shadow-2xl rounded-3xl overflow-hidden border border-slate-100 grid grid-cols-1 md:grid-cols-3">
          
          {/* Sidebar / Imagen y Contacto */}
          <div className="bg-indigo-700 p-8 text-white flex flex-col items-center justify-center text-center">
            <img 
              src="/assets/img/profile.jpg" 
              alt="Perfil" 
              className="w-36 h-36 rounded-full object-cover border-4 border-white/20 shadow-lg mb-4"
              onError={(e)=>{e.target.src='https://via.placeholder.com/150'}} // Respaldo si no encuentra la imagen local
            />
            <h1 className="text-2xl font-bold">{user.nombre} {user.apellido}</h1>
            <p className="text-indigo-200 text-sm mt-1">{user.correo}</p>
          </div>

          {/* Contenido Principal / Sobre Mí y Datos */}
          <div className="col-span-2 p-8 md:p-12 space-y-6">
            <div>
              <h2 className="text-xl font-bold text-slate-800 uppercase tracking-wider border-b pb-2">Sobre Mí</h2>
              <p className="text-slate-600 mt-3 text-sm leading-relaxed">
                ¡Hola! Este es mi perfil personal registrado en el sistema. A continuación se detallan mis datos puntuales validados por el sistema.
              </p>
            </div>

            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-slate-800">Detalles Personales</h3>
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
                <li className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <span className="block text-slate-400 font-medium text-xs uppercase">DNI</span>
                  <span className="text-slate-800 font-semibold text-base">{user.dni}</span>
                </li>
                <li className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <span className="block text-slate-400 font-medium text-xs uppercase">Fecha de Nacimiento</span>
                  <span className="text-slate-800 font-semibold text-base">{user.fechaNacimiento}</span>
                </li>
                <li className="bg-slate-50 p-4 rounded-xl border border-slate-100 sm:col-span-2">
                  <span className="block text-slate-400 font-medium text-xs uppercase">Dirección</span>
                  <span className="text-slate-800 font-semibold text-base">{user.direccion}</span>
                </li>
              </ul>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}