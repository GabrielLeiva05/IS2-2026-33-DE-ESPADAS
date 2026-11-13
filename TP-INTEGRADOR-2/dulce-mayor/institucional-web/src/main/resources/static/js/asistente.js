// Asistente de presupuestos (solo se carga con sesión). Habla con /asistente del propio sitio, que reenvía al backend
// con el token de la sesión. Todo el texto se inserta con textContent: nada de lo que llega se interpreta como HTML.
(function () {
    'use strict';
    const panel = document.getElementById('asistente');
    const abrir = document.getElementById('asistente-abrir');
    if (!panel || !abrir) return;

    const base = panel.dataset.url;
    const mensajes = document.getElementById('asistente-mensajes');
    const cuadro = document.getElementById('asistente-presupuesto');
    const formulario = document.getElementById('asistente-form');
    const texto = document.getElementById('asistente-texto');
    const pesos = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' });
    let idConversacion = null;
    let enviando = false;

    try { idConversacion = sessionStorage.getItem('dm-asistente') || null; } catch (e) { /* sin almacenamiento */ }

    function recordar(id) {
        idConversacion = id;
        try {
            if (id) sessionStorage.setItem('dm-asistente', id); else sessionStorage.removeItem('dm-asistente');
        } catch (e) { /* sin almacenamiento */ }
    }

    function agregar(clase, contenido) {
        const p = document.createElement('p');
        p.className = 'asistente-msj ' + clase;
        p.textContent = contenido;
        mensajes.appendChild(p);
        mensajes.scrollTop = mensajes.scrollHeight;
        return p;
    }

    function mostrarOpciones(opciones) {
        if (!opciones || !opciones.length) return;
        const caja = document.createElement('div');
        caja.className = 'asistente-opciones';
        opciones.forEach((o) => {
            const b = document.createElement('button');
            b.type = 'button';
            b.textContent = o.nombre + (o.presentacion ? ' · ' + o.presentacion : '') + ' · ' + pesos.format(o.precio);
            b.addEventListener('click', () => {
                caja.remove();
                enviar('', o.idProducto, o.nombre);
            });
            caja.appendChild(b);
        });
        mensajes.appendChild(caja);
        mensajes.scrollTop = mensajes.scrollHeight;
    }

    function fila(tabla, izquierda, derecha, clase) {
        const tr = tabla.insertRow();
        if (clase) tr.className = clase;
        tr.insertCell().textContent = izquierda;
        tr.insertCell().textContent = derecha;
    }

    function mostrarPresupuesto(p) {
        cuadro.replaceChildren();
        if (!p || !p.renglones || !p.renglones.length) {
            cuadro.hidden = true;
            return;
        }
        const tabla = document.createElement('table');
        p.renglones.forEach((r) => {
            fila(tabla, r.cantidad + ' × ' + r.nombre + (r.presentacion ? ' (' + r.presentacion + ')' : ''), pesos.format(r.subtotal));
            if (Number(r.descuento) > 0) fila(tabla, '  Promoción', '− ' + pesos.format(r.descuento), 'desc');
        });
        fila(tabla, 'Total sin IVA', pesos.format(p.total), 'total');
        if (p.totalUsd != null) fila(tabla, '≈ en dólares', 'US$ ' + Number(p.totalUsd).toFixed(2));
        cuadro.appendChild(tabla);
        cuadro.hidden = false;
    }

    async function enviar(mensaje, idProductoElegido, eco) {
        if (enviando) return;
        enviando = true;
        agregar('asistente-yo', eco || mensaje);
        const espera = agregar('asistente-bot', 'Pensando…');
        try {
            const r = await fetch(base + '/mensajes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'fetch' },
                body: JSON.stringify({ idConversacion, mensaje, idProductoElegido: idProductoElegido || null }),
            });
            const datos = await r.json().catch(() => ({}));
            espera.remove();
            if (!r.ok) {
                agregar('asistente-error', datos.mensaje || 'No se pudo enviar el mensaje.');
                if (r.status === 401) setTimeout(() => { window.location.href = base.replace(/\/asistente$/, '') + '/ingresar'; }, 1500);
                return;
            }
            recordar(datos.idConversacion);
            agregar('asistente-bot', datos.mensaje);
            mostrarOpciones(datos.opciones);
            mostrarPresupuesto(datos.presupuesto);
        } catch (e) {
            espera.remove();
            agregar('asistente-error', 'No hay conexión con el sitio. Probá de nuevo.');
        } finally {
            enviando = false;
            texto.focus();
        }
    }

    function alternar(visible) {
        panel.hidden = !visible;
        abrir.hidden = visible;
        abrir.setAttribute('aria-expanded', String(visible));
        if (visible) texto.focus();
    }

    abrir.addEventListener('click', () => alternar(true));
    document.getElementById('asistente-cerrar').addEventListener('click', () => alternar(false));
    document.getElementById('asistente-nuevo').addEventListener('click', () => {
        if (idConversacion) {
            fetch(base + '/conversaciones/' + encodeURIComponent(idConversacion),
                { method: 'DELETE', headers: { 'X-Requested-With': 'fetch' } }).catch(() => {});
        }
        recordar(null);
        mensajes.querySelectorAll('.asistente-msj:not(:first-child), .asistente-opciones').forEach((n) => n.remove());
        mostrarPresupuesto(null);
    });
    formulario.addEventListener('submit', (e) => {
        e.preventDefault();
        const valor = texto.value.trim();
        if (!valor) return;
        texto.value = '';
        enviar(valor);
    });
})();
