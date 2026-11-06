package com.dulcemayor.facturacion.service;

import com.dulcemayor.facturacion.model.Pedido;
import com.dulcemayor.facturacion.repository.PedidoRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private final PedidoRepository pedidoRepository;

    public FacturaService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public byte[] generarFacturaPdf(Long pedidoId) throws Exception {
        Pedido pedido = obtenerPedidoPorId(pedidoId);

        InputStream jrxmlStream = new ClassPathResource("reports/factura_dulce_mayor.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        List<Map<String, Object>> detallesData = pedido.getDetalles().stream().map(detalle -> {
            Map<String, Object> map = new HashMap<>();
            map.put("producto", detalle.getProducto());
            map.put("cantidad", detalle.getCantidad());
            map.put("precioUnitario", detalle.getPrecioUnitario());
            map.put("subtotalItem", detalle.getSubtotal());
            return map;
        }).collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detallesData);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("RAZON_SOCIAL_CLIENTE", pedido.getCliente().getRazonSocial());
        parameters.put("CUIT_CLIENTE", pedido.getCliente().getCuit());
        parameters.put("DOMICILIO_CLIENTE", pedido.getCliente().getDomicilio() + ", " + pedido.getCliente().getLocalidad());
        parameters.put("CONDICION_IVA_CLIENTE", pedido.getCliente().getCondicionIva());
        parameters.put("NUMERO_FACTURA", pedido.getNumeroFactura());
        parameters.put("FECHA_EMISION", pedido.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        parameters.put("MEDIO_PAGO", pedido.getMedioPago());
        parameters.put("SUBTOTAL", pedido.getSubtotal());
        parameters.put("IVA", pedido.getIva());
        parameters.put("TOTAL", pedido.getTotal());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
