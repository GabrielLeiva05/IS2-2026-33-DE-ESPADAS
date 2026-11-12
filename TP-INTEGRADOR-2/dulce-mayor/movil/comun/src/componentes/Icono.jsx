// Íconos de trazo propios (SVG en línea), mismos trazos que el sitio y la administración.
const TRAZOS = {
  ruta: 'M6 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm12-10a2 2 0 1 0 0-4 2 2 0 0 0 0 4ZM6 15V9a4 4 0 0 1 4-4h2m-4 14h6a4 4 0 0 0 4-4V9',
  pedido: 'M9 4h6l1 2h3v14H5V6h3l1-2Zm-1 7h8m-8 4h5',
  usuario: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8a7 7 0 0 1 14 0',
  catalogo: 'M4 5h7v6H4zM13 5h7v6h-7zM4 13h7v6H4zM13 13h7v6h-7z',
  camion: 'M3 7h11v9H3zM14 10h4l3 3v3h-7M7 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm10 0a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z',
  volver: 'M15 18l-6-6 6-6',
  flecha: 'M9 6l6 6-6 6',
  mas: 'M12 5v14M5 12h14',
  menos: 'M5 12h14',
  buscar: 'M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14Zm9 2-4-4',
  ok: 'M5 12.5l4.5 4.5L19 7.5',
  alerta: 'M12 4l9 16H3L12 4Zm0 6v4m0 3v.01',
  info: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Zm0-11v6m0-9v.01',
  ubicacion: 'M12 21s-7-6.2-7-11.5A7 7 0 0 1 19 9.5C19 14.8 12 21 12 21Zm0-9a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z',
  tienda: 'M4 9l1.5-5h13L20 9M4 9h16v11H4zM9 20v-6h6v6',
  salir: 'M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10',
  llave: 'M8 15a4 4 0 1 1 3.5-6H21v3h-2v3h-3v-3h-4.5A4 4 0 0 1 8 15Z',
  carrito: 'M3 4h2l2.5 11h10L20 8H7M10 20a1 1 0 1 0 0-2 1 1 0 0 0 0 2Zm7 0a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z',
  gps: 'M12 19a7 7 0 1 0 0-14 7 7 0 0 0 0 14Zm0-4a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM12 2v3m0 14v3M2 12h3m14 0h3',
  reloj: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Zm0-13v4l3 2',
  caramelo: 'M8 12a4 4 0 1 0 8 0 4 4 0 0 0-8 0Zm0 0L4 9v6l4-3Zm8 0 4-3v6l-4-3Z',
}

export default function Icono({ nombre, tamanio = 22, className = '' }) {
  return (
    <svg className={`icono ${className}`} width={tamanio} height={tamanio} viewBox="0 0 24 24" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={TRAZOS[nombre] ?? TRAZOS.info} />
    </svg>
  )
}
