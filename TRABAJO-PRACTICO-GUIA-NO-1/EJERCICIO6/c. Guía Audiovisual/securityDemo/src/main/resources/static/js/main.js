document.addEventListener("DOMContentLoaded", () => {
    const links = document.querySelectorAll(".menu-link");
    const sections = document.querySelectorAll(".section");

    links.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();

            // Quitar active de todos los enlaces
            links.forEach(l => l.classList.remove("active"));

            // Activar el enlace seleccionado
            this.classList.add("active");

            // Ocultar todas las secciones
            sections.forEach(s => s.classList.remove("active"));

            // Mostrar la sección seleccionada
            const sectionId = this.dataset.section;
            const targetSection = document.getElementById(sectionId);
            if (targetSection) {
                targetSection.classList.add("active");
            }
        });
    });
});