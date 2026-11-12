/* =====================================================================
   Dulce Mayor — comportamiento común de las vistas.
   Archivo IDÉNTICO en admin-web e institucional-web. Sin dependencias.
   Todo es mejora progresiva: los formularios funcionan aunque falle el JS
   (salvo los selects de ubicación en cascada, que lo necesitan).
   ===================================================================== */
(function () {
    "use strict";

    /* Menú lateral como cajón en celulares (admin-web). */
    function iniciarMenu() {
        var sidebar = document.querySelector("[data-sidebar]");
        var velo = document.querySelector("[data-velo]");
        if (!sidebar) return;
        function alternar(abrir) {
            sidebar.classList.toggle("abierto", abrir);
            if (velo) velo.classList.toggle("visible", abrir);
            document.body.style.overflow = abrir ? "hidden" : "";
        }
        document.querySelectorAll("[data-abrir-menu]").forEach(function (boton) {
            boton.addEventListener("click", function () { alternar(true); });
        });
        if (velo) velo.addEventListener("click", function () { alternar(false); });
        document.addEventListener("keydown", function (e) { if (e.key === "Escape") alternar(false); });
    }

    /* Mostrar u ocultar la contraseña. */
    function iniciarVerClave() {
        document.querySelectorAll("[data-ver-clave]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var campo = document.getElementById(boton.getAttribute("data-ver-clave"));
                if (!campo) return;
                var visible = campo.type === "text";
                campo.type = visible ? "password" : "text";
                boton.setAttribute("aria-pressed", String(!visible));
                boton.setAttribute("aria-label", visible ? "Mostrar contraseña" : "Ocultar contraseña");
            });
        });
    }

    /* Confirmación antes de acciones destructivas (data-confirmar="mensaje"). */
    function iniciarConfirmaciones() {
        document.querySelectorAll("form[data-confirmar]").forEach(function (form) {
            form.addEventListener("submit", function (e) {
                if (!window.confirm(form.getAttribute("data-confirmar"))) e.preventDefault();
            });
        });
    }

    /* Evita el doble envío de formularios. */
    function iniciarEnvioUnico() {
        document.querySelectorAll("form[data-envio-unico]").forEach(function (form) {
            form.addEventListener("submit", function (e) {
                if (e.defaultPrevented) return; /* el usuario canceló la confirmación */
                form.querySelectorAll("button[type=submit]").forEach(function (b) {
                    window.setTimeout(function () { b.disabled = true; }, 0);
                });
            });
        });
    }

    /* Campos que dependen del rol elegido: data-segun-rol="PREVENTISTA DISTRIBUIDOR". */
    function iniciarCamposPorRol() {
        var selector = document.querySelector("[data-selector-rol]");
        if (!selector) return;
        function actualizar() {
            var rol = selector.value;
            document.querySelectorAll("[data-segun-rol]").forEach(function (bloque) {
                var roles = bloque.getAttribute("data-segun-rol").split(/\s+/);
                var visible = roles.indexOf(rol) >= 0;
                bloque.hidden = !visible;
                bloque.querySelectorAll("input, select, textarea").forEach(function (c) { c.disabled = !visible; });
            });
        }
        selector.addEventListener("change", actualizar);
        actualizar();
    }

    /* Selects en cascada: <select data-cascada="url-con-{id}" data-hijo="idDelSelectHijo">. */
    function iniciarCascadas() {
        function limpiar(select, texto) {
            select.innerHTML = "";
            var opcion = document.createElement("option");
            opcion.value = "";
            opcion.textContent = texto;
            select.appendChild(opcion);
            select.disabled = true;
            var nieto = select.getAttribute("data-hijo") && document.getElementById(select.getAttribute("data-hijo"));
            if (nieto) limpiar(nieto, nieto.getAttribute("data-vacio") || "Seleccioná una opción");
        }
        document.querySelectorAll("select[data-cascada]").forEach(function (padre) {
            padre.addEventListener("change", function () {
                var hijo = document.getElementById(padre.getAttribute("data-hijo"));
                if (!hijo) return;
                limpiar(hijo, "Cargando…");
                if (!padre.value) { limpiar(hijo, hijo.getAttribute("data-vacio") || "Seleccioná una opción"); return; }
                var url = padre.getAttribute("data-cascada").replace("{id}", encodeURIComponent(padre.value));
                fetch(url, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
                    .then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); })
                    .then(function (opciones) {
                        limpiar(hijo, opciones.length ? "Seleccioná una opción" : "No hay opciones cargadas");
                        opciones.forEach(function (o) {
                            var opcion = document.createElement("option");
                            opcion.value = o.id;
                            opcion.textContent = o.codigoPostal ? o.nombre + " (CP " + o.codigoPostal + ")" : o.nombre;
                            hijo.appendChild(opcion);
                        });
                        hijo.disabled = opciones.length === 0;
                    })
                    .catch(function () { limpiar(hijo, "No se pudieron cargar las opciones"); });
            });
        });
    }

    /* Envía automáticamente un formulario de filtros al cambiar un select. */
    function iniciarAutoEnvio() {
        document.querySelectorAll("[data-auto-envio]").forEach(function (control) {
            control.addEventListener("change", function () { if (control.form) control.form.submit(); });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        iniciarMenu();
        iniciarVerClave();
        iniciarConfirmaciones();
        iniciarEnvioUnico();
        iniciarCamposPorRol();
        iniciarCascadas();
        iniciarAutoEnvio();
    });
})();
