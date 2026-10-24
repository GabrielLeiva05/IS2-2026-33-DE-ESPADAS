/*!
* Start Bootstrap - Shop Homepage v5.0.6 (https://startbootstrap.com/template/shop-homepage)
* Copyright 2013-2023 Start Bootstrap
* Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-shop-homepage/blob/master/LICENSE)
*/
window.addEventListener('DOMContentLoaded', async () => {
    const grid = document.querySelector('#videojuegos-grid');
    if (!grid) {
        return;
    }

    const status = grid.querySelector('[data-catalog-status]');
    const empty = grid.querySelector('[data-catalog-empty]');
    const apiUrl = new URL(grid.dataset.apiUrl, window.location.origin);
    const query = grid.dataset.query || '';

    if (query.trim()) {
        apiUrl.searchParams.set('query', query);
    }

    try {
        const response = await fetch(apiUrl, {
            headers: { Accept: 'application/json' }
        });
        if (!response.ok) {
            throw new Error(`La API respondió con estado ${response.status}.`);
        }

        const videojuegos = await response.json();
        if (!Array.isArray(videojuegos)) {
            throw new Error('La respuesta de la API no tiene el formato esperado.');
        }

        const activos = videojuegos.filter(videojuego => videojuego.activo === true);
        for (const videojuego of activos) {
            grid.appendChild(crearTarjeta(videojuego, grid.dataset, grid.dataset.cardLayout));
        }

        status.hidden = true;
        empty.hidden = activos.length > 0;
    } catch (error) {
        status.textContent = 'No se pudieron cargar los videojuegos. Intenta nuevamente más tarde.';
        status.setAttribute('role', 'alert');
        console.error('Error al consultar la API de videojuegos:', error);
    }
});

function crearTarjeta(videojuego, config, layout) {
    const columna = document.createElement('div');
    columna.className = layout === 'shop' ? 'col mb-5' : 'col-12 col-md-3';

    const tarjeta = document.createElement('article');
    tarjeta.className = layout === 'shop' ? 'card h-100' : 'card my-2';

    if (videojuego.imagen) {
        const imagen = document.createElement('img');
        imagen.className = 'card-img-top';
        imagen.src = config.imageBase + encodeURIComponent(videojuego.imagen);
        imagen.alt = videojuego.titulo || 'Videojuego';
        imagen.loading = 'lazy';
        tarjeta.appendChild(imagen);
    }

    const cuerpo = document.createElement('div');
    cuerpo.className = layout === 'shop' ? 'card-body p-4' : 'card-body';

    const contenido = document.createElement('div');
    contenido.className = layout === 'shop' ? 'text-center' : '';

    const titulo = document.createElement('h5');
    titulo.className = layout === 'shop' ? 'fw-bolder' : 'card-title';
    titulo.textContent = videojuego.titulo || 'Sin título';
    contenido.appendChild(titulo);

    if (videojuego.categoria && videojuego.categoria.nombre) {
        const categoria = document.createElement(layout === 'shop' ? 'span' : 'p');
        categoria.className = layout === 'shop' ? 'text-muted' : 'card-text';
        categoria.textContent = videojuego.categoria.nombre;
        contenido.appendChild(categoria);
    }

    const precio = document.createElement('div');
    precio.textContent = `$${videojuego.precio}`;
    contenido.appendChild(precio);
    cuerpo.appendChild(contenido);
    tarjeta.appendChild(cuerpo);

    columna.appendChild(tarjeta);
    return columna;
}