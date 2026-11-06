const modo = new URLSearchParams(location.search).get('modo') === 'despacho' ? 'despacho' : 'preventor';
const $ = id => document.getElementById(id);
$('titulo').textContent = modo === 'preventor' ? 'Preventor · ruta de visitas' : 'Despacho · ruta de entregas';
$('btnCompletar').textContent = modo === 'preventor' ? 'Pedido tomado ✔' : 'Entregado ✔';
if (modo === 'preventor') $('alta').hidden = false;

const PLAZA = {lat: -32.8895, lng: -68.8458};   // SOLO ultimo recurso si no hay ubicacion
let origen = null;       // punto de partida de la ruta
let gps = null;          // ultima posicion real del dispositivo
let watchId = null;
let marcadorYo = null;
let eligiendoInicio = false;
let siguiente = null;

const map = L.map('map').setView([PLAZA.lat, PLAZA.lng], 12);
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',
  {maxZoom: 19, attribution: '&copy; Colaboradores de <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'}).addTo(map);
const capa = L.layerGroup().addTo(map);

const pin = (txt, cls) => L.divIcon({className: '', html: `<div class="pin ${cls || ''}">${txt}</div>`, iconSize: [28, 28], iconAnchor: [14, 14]});
const estado = t => { $('gpsEstado').textContent = t; };

// ---------------- Geolocalizacion (pide permiso al navegador) ----------------
function iniciarGPS() {
  if (!navigator.geolocation) {
    estado('Tu navegador no soporta geolocalización. Elegí el inicio en el mapa.');
    return usarFallback();
  }
  if (!window.isSecureContext) {
    estado('La ubicación requiere HTTPS (o localhost). Elegí el inicio en el mapa.');
    return usarFallback();
  }
  if (watchId !== null) navigator.geolocation.clearWatch(watchId);
  eligiendoInicio = false;
  estado('📍 Solicitando ubicación… aceptá el permiso del navegador.');
  watchId = navigator.geolocation.watchPosition(onPosicion, onErrorGPS,
    {enableHighAccuracy: true, maximumAge: 10000, timeout: 15000});
}

function onPosicion(pos) {
  const primera = gps === null;
  gps = {lat: pos.coords.latitude, lng: pos.coords.longitude};
  estado(`📍 Ubicación en vivo (±${Math.round(pos.coords.accuracy)} m)`);
  if (!marcadorYo) marcadorYo = L.marker([gps.lat, gps.lng], {icon: pin('Yo', 'me'), zIndexOffset: 1000}).addTo(map);
  else marcadorYo.setLatLng([gps.lat, gps.lng]);
  if (primera) { origen = {...gps}; cargar(); }   // no recalcula en cada movimiento: evita gastar consultas de ruta
}

function onErrorGPS(err) {
  if (gps) return;   // ya hubo una posicion valida: ignorar timeouts intermitentes
  const msg = {
    1: 'Permiso de ubicación denegado. Habilitalo desde el candado de la barra de direcciones y tocá "Usar mi ubicación".',
    2: 'No se pudo determinar la ubicación. Revisá el GPS o elegí el inicio en el mapa.',
    3: 'La ubicación tardó demasiado. Reintentá o elegí el inicio en el mapa.'
  }[err.code] || 'Error de ubicación.';
  estado('⚠️ ' + msg);
  usarFallback();
}

function usarFallback() {
  if (origen) return;
  origen = {...PLAZA};
  estado($('gpsEstado').textContent + ' (Ruta calculada desde Plaza Independencia como referencia.)');
  cargar();
}

// ---------------- Ruta ----------------
async function cargar() {
  if (!origen) return;
  capa.clearLayers();
  const [ruta, todos] = await Promise.all([
    fetch(`/api/ruta/${modo}?lat=${origen.lat}&lng=${origen.lng}`).then(r => r.json()),
    fetch('/api/puntos').then(r => r.json())
  ]);
  const idsRuta = new Set(ruta.orden.map(p => p.id));

  todos.filter(p => !idsRuta.has(p.id)).forEach(p =>
    L.circleMarker([p.lat, p.lng], {radius: 6, color: '#868e96', fillOpacity: .6}).bindTooltip(p.nombre).addTo(capa));

  if (ruta.geometria.length) L.polyline(ruta.geometria, {color: '#1971c2', weight: 5, opacity: .75}).addTo(capa);
  if (!gps) L.marker([origen.lat, origen.lng], {icon: pin('Inicio', 'me')}).addTo(capa);

  const ol = $('lista'); ol.innerHTML = '';
  ruta.orden.forEach((p, i) => {
    L.marker([p.lat, p.lng], {icon: pin(i + 1, i === 0 ? 'next' : '')})
      .bindPopup(`<b>${esc(p.nombre)}</b><br>${esc(p.direccion || '')}`).addTo(capa);
    const li = document.createElement('li');
    li.textContent = `${p.nombre} — ${p.direccion || ''}`;
    if (i === 0) li.style.fontWeight = '700';
    ol.appendChild(li);
  });

  siguiente = ruta.orden[0] || null;
  $('btnCompletar').disabled = !siguiente;
  $('resumen').textContent = siguiente
    ? `${ruta.orden.length} puntos · ${ruta.km.toFixed(1)} km · ~${Math.round(ruta.minutos)} min · ${ruta.fuente}`
    : 'No hay puntos pendientes. ¡Ruta finalizada!';
  const b = L.latLngBounds([[origen.lat, origen.lng], ...ruta.orden.map(p => [p.lat, p.lng])]);
  if (b.isValid()) map.fitBounds(b, {padding: [40, 40]});
}

$('btnCompletar').onclick = async () => {
  if (!siguiente) return;
  const r = await fetch(`/api/ruta/${modo}/completar/${siguiente.id}`, {method: 'POST'}).then(r => r.json());
  $('msg').textContent = r.mensaje;
  // Con GPS: la nueva ruta parte de donde estoy ahora. Sin GPS: del punto recien atendido.
  origen = gps ? {...gps} : {lat: r.completado.lat, lng: r.completado.lng};
  await cargar();
};

$('btnRecalcular').onclick = () => { if (gps) { origen = {...gps}; cargar(); } else iniciarGPS(); };
$('btnUbicar').onclick = () => { gps = null; origen = null; iniciarGPS(); };
$('btnManual').onclick = () => {
  eligiendoInicio = true;
  estado('👆 Tocá el mapa para marcar el punto de partida.');
};

// ---------------- Alta de cliente (solo preventor): Nominatim + clic en mapa ----------------
let marcadorNuevo = null;
function ubicarNuevo(lat, lng) {
  if (!marcadorNuevo) marcadorNuevo = L.marker([lat, lng], {draggable: true}).addTo(map);
  else marcadorNuevo.setLatLng([lat, lng]);
  $('btnGuardar').disabled = false;
}
map.on('click', e => {
  if (eligiendoInicio) {                       // inicio manual: se deja de seguir el GPS
    eligiendoInicio = false;
    if (watchId !== null) { navigator.geolocation.clearWatch(watchId); watchId = null; }
    if (marcadorYo) { map.removeLayer(marcadorYo); marcadorYo = null; }
    gps = null;
    origen = {lat: e.latlng.lat, lng: e.latlng.lng};
    estado('Inicio elegido manualmente.');
    cargar();
  } else if (!$('alta').hidden && $('alta').open) {
    ubicarNuevo(e.latlng.lat, e.latlng.lng);
  }
});
$('btnBuscar').onclick = async () => {
  const q = $('nDir').value.trim(); if (!q) return;
  const r = await fetch('https://nominatim.openstreetmap.org/search?format=json&limit=1&countrycodes=ar&q=' + encodeURIComponent(q)).then(r => r.json());
  if (!r.length) return alert('No se encontró la dirección. Tocá el mapa para ubicarla.');
  ubicarNuevo(parseFloat(r[0].lat), parseFloat(r[0].lon));
  map.setView([r[0].lat, r[0].lon], 16);
};
$('btnGuardar').onclick = async () => {
  const ll = marcadorNuevo.getLatLng();
  const res = await fetch('/api/puntos', {method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({nombre: $('nNombre').value, direccion: $('nDir').value, email: $('nMail').value, lat: ll.lat, lng: ll.lng})});
  if (!res.ok) return alert('Datos inválidos (nombre y ubicación son obligatorios)');
  map.removeLayer(marcadorNuevo); marcadorNuevo = null; $('btnGuardar').disabled = true;
  ['nNombre', 'nDir', 'nMail'].forEach(i => $(i).value = '');
  $('msg').textContent = 'Cliente dado de alta';
  cargar();
};

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

iniciarGPS();
