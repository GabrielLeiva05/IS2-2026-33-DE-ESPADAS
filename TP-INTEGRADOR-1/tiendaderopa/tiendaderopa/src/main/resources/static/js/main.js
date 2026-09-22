document.addEventListener("DOMContentLoaded", () => {
    const links = document.querySelectorAll(".menu-link");
    const sections = document.querySelectorAll(".section");

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

    // Evento al hacer clic en un enlace del sidebar
    links.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            const sectionId = this.dataset.section;
            activarSeccion(sectionId);
        });
    });

    // Al cargar o recargar la página (por ejemplo, tras enviar un formulario):
    // Si no hay nada guardado previamente, muestra 'inicio' por defecto
    const seccionGuardada = localStorage.getItem("seccionActiva") || "inicio";
    activarSeccion(seccionGuardada);
});