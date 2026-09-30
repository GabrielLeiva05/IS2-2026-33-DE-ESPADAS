document.addEventListener("DOMContentLoaded", () => {
    // Resalta en el sidebar el enlace de página completa que corresponde a la URL actual.
    const currentPath = window.location.pathname;
    document.querySelectorAll(".menu-link:not([data-section])").forEach(link => {
        const linkPath = new URL(link.href, window.location.origin).pathname;
        if (currentPath === linkPath || currentPath.startsWith(linkPath + "/")) {
            link.classList.add("active");
        }
    });

    // Solo los enlaces con data-section son pestañas internas (SPA); el resto
    // son enlaces normales a otras páginas del panel y deben navegar.
    const links = document.querySelectorAll(".menu-link[data-section]");
    const sections = document.querySelectorAll(".section");

    if (links.length === 0) {
        return;
    }

    function activarSeccion(sectionId) {
        // 1. Quitar 'active' de todos los enlaces y secciones
        links.forEach(l => l.classList.remove("active"));
        sections.forEach(s => s.classList.remove("active"));

        // 2. Localizar elementos por id / data-section
        const targetSection = document.getElementById(sectionId);
        const targetLink = document.querySelector(`.menu-link[data-section="${sectionId}"]`);

        // 3. Activar sección y enlace correspondiente
        if (targetSection) {
            targetSection.classList.add("active");
        }
        if (targetLink) {
            targetLink.classList.add("active");
        }

        // 4. Guardar en el almacenamiento local del navegador
        localStorage.setItem("seccionActiva", sectionId);
    }

    // Evento al hacer clic en un enlace del sidebar: si la sección existe en esta
    // página se activa in-place; si no, se navega normalmente al dashboard.
    links.forEach(link => {
        link.addEventListener("click", function (event) {
            const sectionId = this.dataset.section;
            if (document.getElementById(sectionId)) {
                event.preventDefault();
                activarSeccion(sectionId);
            } else {
                localStorage.setItem("seccionActiva", sectionId);
            }
        });
    });

    // Al cargar o recargar la página (por ejemplo, tras enviar un formulario):
    // Si no hay nada guardado previamente, muestra 'inicio' por defecto
    const seccionGuardada = localStorage.getItem("seccionActiva") || "inicio";
    activarSeccion(seccionGuardada);
});