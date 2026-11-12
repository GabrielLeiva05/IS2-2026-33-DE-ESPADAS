/* =====================================================================
   Gráficos del panel (HU-43) con Chart.js 4 (copia local en js/vendor, licencia MIT).
   Los datos ya llegan agregados desde el backend (window.datosPanel); acá solo se dibujan.
   ===================================================================== */
(function () {
    "use strict";
    if (typeof Chart === "undefined" || !window.datosPanel) return;

    var estilos = getComputedStyle(document.documentElement);
    function token(nombre, porDefecto) { return (estilos.getPropertyValue(nombre) || "").trim() || porDefecto; }
    // Paleta de la identidad de Dulce Mayor (mismos colores que el resto del panel)
    var PALETA = ["#d10f8c", "#1f6feb", "#7a35e0", "#ffc52e", "#16935a", "#ff7ac6", "#5aa2ff", "#b38cf2", "#f08c00", "#0c8599"];
    var texto = token("--dm-texto-2", "#5d5b70");
    var borde = token("--dm-borde", "#e4e4ee");
    Chart.defaults.font.family = token("--dm-fuente", "Inter, system-ui, sans-serif");
    Chart.defaults.color = texto;
    Chart.defaults.maintainAspectRatio = false;
    var numero = new Intl.NumberFormat("es-AR");

    // ---------------- Barras: pedidos por preventista y por día (solo jefe) ----------------
    var lienzoPedidos = document.getElementById("graficoPedidos");
    var pedidos = window.datosPanel.pedidos;
    if (lienzoPedidos && pedidos && pedidos.series.length) {
        new Chart(lienzoPedidos, {
            type: "bar",
            data: {
                labels: pedidos.dias.map(function (d) { return String(d); }),
                datasets: pedidos.series.map(function (s, i) {
                    return {
                        label: s.preventista + " (" + s.total + ")",
                        data: s.cantidades,
                        backgroundColor: PALETA[i % PALETA.length],
                        borderRadius: 4,
                        maxBarThickness: 22
                    };
                })
            },
            options: {
                responsive: true,
                interaction: { mode: "index", intersect: false },
                plugins: {
                    legend: { position: "bottom", labels: { boxWidth: 12, boxHeight: 12 } },
                    tooltip: {
                        callbacks: {
                            title: function (items) { return "Día " + items[0].label + " de " + pedidos.mes; },
                            label: function (c) { return c.dataset.label.replace(/ \(\d+\)$/, "") + ": " + c.parsed.y + " pedidos"; }
                        },
                        filter: function (c) { return c.parsed.y > 0; }
                    }
                },
                scales: {
                    x: { title: { display: true, text: "Día del mes" }, grid: { display: false } },
                    y: { beginAtZero: true, ticks: { precision: 0 }, title: { display: true, text: "Pedidos" }, grid: { color: borde } }
                }
            }
        });
    }

    // ---------------- Torta: stock por categoría (administración y jefe) ----------------
    var lienzoStock = document.getElementById("graficoStock");
    if (!lienzoStock) return;
    function datosTorta(lista) {
        return {
            labels: lista.map(function (s) { return s.categoria; }),
            datasets: [{
                data: lista.map(function (s) { return s.bultos; }),
                productos: lista.map(function (s) { return s.productos; }),
                backgroundColor: lista.map(function (_, i) { return PALETA[i % PALETA.length]; }),
                borderColor: "#fff",
                borderWidth: 2
            }]
        };
    }
    var torta = new Chart(lienzoStock, {
        type: "pie",
        data: datosTorta(window.datosPanel.stock || []),
        options: {
            responsive: true,
            plugins: {
                legend: { position: "bottom", labels: { boxWidth: 12, boxHeight: 12 } },
                tooltip: {
                    callbacks: {
                        label: function (c) {
                            var total = c.dataset.data.reduce(function (a, b) { return a + b; }, 0) || 1;
                            var productos = c.dataset.productos[c.dataIndex];
                            return c.label + ": " + numero.format(c.parsed) + " bultos (" +
                                Math.round(c.parsed * 100 / total) + "%) · " + productos + " productos";
                        }
                    }
                }
            }
        }
    });

    // Refresco: cada minuto y al volver a la pestaña, sin recargar la página.
    function refrescar() {
        if (document.visibilityState !== "visible") return;
        fetch(window.datosPanel.urlStock, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
            .then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); })
            .then(function (lista) { torta.data = datosTorta(lista); torta.update(); })
            .catch(function () { /* se reintenta en el próximo ciclo */ });
    }
    setInterval(refrescar, 60000);
    document.addEventListener("visibilitychange", refrescar);
})();
