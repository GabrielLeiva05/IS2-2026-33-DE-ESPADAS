# Dulce Mayor – PoC OpenStreetMap (Spring Boot 3 + Leaflet)

## Requisitos
Java 17+ y Maven 3.9+ (internet para tiles OSM, OSRM y Nominatim).

## Ejecutar
    mvn spring-boot:run
Abrir http://localhost:8080  (la geolocalizacion del navegador funciona en localhost/https).

## Pantallas
- /sucursales.html            Sitio institucional: sucursales en mapa.
- /ruta.html?modo=preventor   Ruta optima de visitas, "Pedido tomado" -> siguiente punto, alta de clientes.
- /ruta.html?modo=despacho    Ruta de entregas, "Entregado" -> siguiente punto + email al proximo cliente.

## API
GET  /api/puntos | POST /api/puntos | GET /api/sucursales
GET  /api/ruta/{preventor|despacho}?lat=&lng=
POST /api/ruta/{modo}/completar/{id}
POST /api/demo/reset

## Email
Sin `spring.mail.host` el envio se simula por log. Configurar SMTP en application.properties para enviar real.

## Antes de produccion
OSRM publico, Nominatim y tiles de tile.openstreetmap.org son solo para demo/uso liviano (ver sus politicas de uso).

## Geolocalizacion
El navegador solo permite geolocalizacion en HTTPS o localhost. Para probar desde un celular en la red local
(http://192.168.x.x:8080) no funcionara: usar un tunel HTTPS (ngrok, cloudflared) o habilitar SSL en Spring Boot
(server.ssl.*). Si no hay ubicacion, la pantalla lo informa y permite elegir el inicio tocando el mapa.
