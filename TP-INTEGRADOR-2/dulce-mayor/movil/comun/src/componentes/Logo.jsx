// Marca de Dulce Mayor (caramelo con degradado de la identidad visual).
export default function Logo({ chico = false }) {
  return (
    <span className={`logo${chico ? ' logo-chico' : ''}`}>
      <svg viewBox="0 0 48 48" aria-hidden="true" className="logo-icono">
        <defs>
          <linearGradient id="dm-grad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stopColor="#d10f8c" /><stop offset=".55" stopColor="#7a35e0" /><stop offset="1" stopColor="#1f6feb" />
          </linearGradient>
        </defs>
        <rect x="4" y="4" width="40" height="40" rx="12" fill="url(#dm-grad)" />
        <circle cx="24" cy="24" r="8.5" fill="#fff" />
        <path d="M15.5 24 9 18v12zM32.5 24 39 18v12z" fill="#fff" opacity=".9" />
        <path d="M20 21a5 5 0 0 1 8 0" stroke="#ffc52e" strokeWidth="2.4" fill="none" strokeLinecap="round" />
      </svg>
      <span className="logo-texto"><b>Dulce</b><b>Mayor</b></span>
    </span>
  )
}
