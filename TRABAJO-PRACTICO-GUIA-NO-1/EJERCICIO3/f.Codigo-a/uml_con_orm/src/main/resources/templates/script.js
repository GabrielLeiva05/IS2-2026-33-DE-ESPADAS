const API_URL = 'http://localhost:8080';

// ===================== ESTADO =====================
let editandoCliente = false;
let editandoVenta = false;
let idClienteEditando = null;
let idVentaEditando = null;

// ===================== ELEMENTOS: CLIENTE =====================
const formularioCliente = document.querySelector('#formulario-cliente');
const nombreInput = document.querySelector('#nombre');
const documentoInput = document.querySelector('#documento');
const telefonoInput = document.querySelector('#telefono');
const emailInput = document.querySelector('#email');
const direccionInput = document.querySelector('#direccion');
const btnAgregarCliente = document.querySelector('#btn-agregar-cliente');
const divClientes = document.querySelector('#div-clientes');

// ===================== ELEMENTOS: VENTA =====================
const formularioVenta = document.querySelector('#formulario-venta');
const idClienteVentaSelect = document.querySelector('#idClienteVenta');
const fechaInput = document.querySelector('#fecha');
const precioTotalInput = document.querySelector('#precioTotal');
const estadoInput = document.querySelector('#estado');
const btnAgregarVenta = document.querySelector('#btn-agregar-venta');
const divVentas = document.querySelector('#div-ventas');

// ===================== TABS =====================
function cambiarTab(tab) {
    const seccionClientes = document.querySelector('#seccion-clientes');
    const seccionVentas = document.querySelector('#seccion-ventas');
    const tabClientes = document.querySelector('#tab-clientes');
    const tabVentas = document.querySelector('#tab-ventas');

    if (tab === 'clientes') {
        seccionClientes.classList.remove('oculto');
        seccionVentas.classList.add('oculto');
        tabClientes.classList.add('activo');
        tabVentas.classList.remove('activo');
    } else {
        seccionVentas.classList.remove('oculto');
        seccionClientes.classList.add('oculto');
        tabVentas.classList.add('activo');
        tabClientes.classList.remove('activo');
    }
}

// ===================== INICIO =====================
document.addEventListener('DOMContentLoaded', () => {
    cargarClientes();
    cargarVentas();
});

// ===================== CLIENTES =====================

formularioCliente.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (nombreInput.value === '' || documentoInput.value === '') {
        alert('Nombre y documento son obligatorios.');
        return;
    }

    const cliente = {
        nombre: nombreInput.value,
        documento: documentoInput.value,
        telefono: telefonoInput.value,
        email: emailInput.value,
        direccion: direccionInput.value
    };

    if (editandoCliente) {
        await actualizarCliente(idClienteEditando, cliente);
    } else {
        await agregarCliente(cliente);
    }

    formularioCliente.reset();
    editandoCliente = false;
    idClienteEditando = null;
    btnAgregarCliente.textContent = 'Agregar';
});

async function agregarCliente(cliente) {
    try {
        const res = await fetch(`${API_URL}/clientes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cliente)
        });
        if (!res.ok) throw new Error('No se pudo crear el cliente.');
        await cargarClientes();
    } catch (error) {
        alert(error.message);
    }
}

async function actualizarCliente(id, cliente) {
    try {
        const res = await fetch(`${API_URL}/clientes/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cliente)
        });
        if (!res.ok) throw new Error('No se pudo actualizar el cliente.');
        await cargarClientes();
    } catch (error) {
        alert(error.message);
    }
}

async function eliminarCliente(id) {
    try {
        const res = await fetch(`${API_URL}/clientes/${id}`, { method: 'DELETE' });

        if (!res.ok) {
            // El backend devuelve 409 con {"error": "..."} si el cliente tiene ventas
            const data = await res.json().catch(() => null);
            const mensaje = data && data.error
                ? data.error
                : 'No se pudo eliminar el cliente.';
            alert(mensaje);
            return;
        }

        await cargarClientes();
        await cargarVentas();
    } catch (error) {
        alert('Error de conexion con el servidor.');
    }
}

function cargarEmpleadoCliente(cliente) {
    nombreInput.value = cliente.nombre;
    documentoInput.value = cliente.documento;
    telefonoInput.value = cliente.telefono;
    emailInput.value = cliente.email;
    direccionInput.value = cliente.direccion;

    idClienteEditando = cliente.idCliente;
    editandoCliente = true;

    btnAgregarCliente.textContent = 'Actualizar';
}

async function cargarClientes() {
    try {
        const res = await fetch(`${API_URL}/clientes`);
        const clientes = await res.json();

        mostrarClientes(clientes);
        llenarSelectClientes(clientes);
    } catch (error) {
        divClientes.innerHTML = '<p>No se pudo conectar con el servidor.</p>';
    }
}

function mostrarClientes(clientes) {
    limpiarHTML(divClientes);

    clientes.forEach(cliente => {
        const { idCliente, nombre, documento, telefono, email, direccion } = cliente;

        const parrafo = document.createElement('p');
        parrafo.textContent = `#${idCliente} - ${nombre} - Doc: ${documento} - Tel: ${telefono} - ${email} - ${direccion} `;

        const editarBoton = document.createElement('button');
        editarBoton.onclick = () => cargarEmpleadoCliente(cliente);
        editarBoton.textContent = 'Editar';
        editarBoton.classList.add('btn', 'btn-editar');
        parrafo.append(editarBoton);

        const eliminarBoton = document.createElement('button');
        eliminarBoton.onclick = () => eliminarCliente(idCliente);
        eliminarBoton.textContent = 'Eliminar';
        eliminarBoton.classList.add('btn', 'btn-eliminar');
        parrafo.append(eliminarBoton);

        divClientes.appendChild(parrafo);
        divClientes.appendChild(document.createElement('hr'));
    });
}

function llenarSelectClientes(clientes) {
    idClienteVentaSelect.innerHTML = '<option value="">Seleccione un cliente</option>';

    clientes.forEach(cliente => {
        const option = document.createElement('option');
        option.value = cliente.idCliente;
        option.textContent = `#${cliente.idCliente} - ${cliente.nombre}`;
        idClienteVentaSelect.appendChild(option);
    });
}

// ===================== VENTAS =====================

formularioVenta.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (idClienteVentaSelect.value === '' || fechaInput.value === '' || estadoInput.value === '') {
        alert('Cliente, fecha y estado son obligatorios.');
        return;
    }

    if (Number.isNaN(parseInt(idClienteVentaSelect.value))) {
        alert('Seleccioná un cliente valido de la lista.');
        return;
    }

    if (editandoVenta) {
        // Ya no mandamos el objeto cliente en el body: viaja por la URL, igual que en el alta
        const venta = {
            fecha: fechaInput.value,
            precioTotal: parseFloat(precioTotalInput.value) || 0,
            estado: estadoInput.value
        };
        await actualizarVenta(idVentaEditando, venta, idClienteVentaSelect.value);
    } else {
        const venta = {
            fecha: fechaInput.value,
            precioTotal: parseFloat(precioTotalInput.value) || 0,
            estado: estadoInput.value
        };
        await agregarVenta(venta, idClienteVentaSelect.value);
    }

    formularioVenta.reset();
    editandoVenta = false;
    idVentaEditando = null;
    btnAgregarVenta.textContent = 'Agregar';
});

async function agregarVenta(venta, idCliente) {
    try {
        const res = await fetch(`${API_URL}/ventas/${idCliente}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(venta)
        });
        if (!res.ok) throw new Error('No se pudo crear la venta.');
        await cargarVentas();
    } catch (error) {
        alert(error.message);
    }
}

async function actualizarVenta(id, venta, idCliente) {
    try {
        const res = await fetch(`${API_URL}/ventas/${id}/${idCliente}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(venta)
        });
        if (!res.ok) throw new Error('No se pudo actualizar la venta.');
        await cargarVentas();
    } catch (error) {
        alert(error.message);
    }
}

async function eliminarVenta(id) {
    try {
        const res = await fetch(`${API_URL}/ventas/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('No se pudo eliminar la venta.');
        await cargarVentas();
    } catch (error) {
        alert(error.message);
    }
}

function cargarEmpleadoVenta(venta) {
    idClienteVentaSelect.value = venta.cliente ? venta.cliente.idCliente : '';
    fechaInput.value = venta.fecha;
    precioTotalInput.value = venta.precioTotal;
    estadoInput.value = venta.estado;

    idVentaEditando = venta.idVenta;
    editandoVenta = true;

    btnAgregarVenta.textContent = 'Actualizar';
}

async function cargarVentas() {
    try {
        const res = await fetch(`${API_URL}/ventas`);
        const ventas = await res.json();
        mostrarVentas(ventas);
    } catch (error) {
        divVentas.innerHTML = '<p>No se pudo conectar con el servidor.</p>';
    }
}

function mostrarVentas(ventas) {
    limpiarHTML(divVentas);

    ventas.forEach(venta => {
        const { idVenta, fecha, precioTotal, estado, cliente } = venta;
        const nombreCliente = cliente ? cliente.nombre : 'Sin cliente';

        const parrafo = document.createElement('p');
        parrafo.textContent = `#${idVenta} - ${fecha} - $${precioTotal} - ${estado} - Cliente: ${nombreCliente} `;

        const editarBoton = document.createElement('button');
        editarBoton.onclick = () => cargarEmpleadoVenta(venta);
        editarBoton.textContent = 'Editar';
        editarBoton.classList.add('btn', 'btn-editar');
        parrafo.append(editarBoton);

        const eliminarBoton = document.createElement('button');
        eliminarBoton.onclick = () => eliminarVenta(idVenta);
        eliminarBoton.textContent = 'Eliminar';
        eliminarBoton.classList.add('btn', 'btn-eliminar');
        parrafo.append(eliminarBoton);

        divVentas.appendChild(parrafo);
        divVentas.appendChild(document.createElement('hr'));
    });
}

// ===================== UTILIDAD =====================
function limpiarHTML(contenedor) {
    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }
}