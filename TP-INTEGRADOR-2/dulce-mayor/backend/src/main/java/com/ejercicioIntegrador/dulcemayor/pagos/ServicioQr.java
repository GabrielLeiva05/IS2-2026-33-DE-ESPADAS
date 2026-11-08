package com.ejercicioIntegrador.dulcemayor.pagos;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Genera la imagen PNG del QR (ZXing) a partir del contenido que devuelve Mercado Pago. La imagen
 * viaja incrustada en el correo, así que el cliente no necesita abrir otra página ni descargar nada.
 */
@Service
public class ServicioQr {

    private static final int LADO_PX = 320;

    public byte[] generarPng(String contenido) throws MiException {
        if (contenido == null || contenido.isBlank()) {
            throw new MiException("El contenido del QR está vacío");
        }
        Map<EncodeHintType, Object> pistas = new EnumMap<>(EncodeHintType.class);
        pistas.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        pistas.put(EncodeHintType.MARGIN, 1);
        pistas.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, LADO_PX, LADO_PX, pistas);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida, new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF));
            return salida.toByteArray();
        } catch (WriterException | IOException e) {
            throw new MiException("No se pudo generar la imagen del QR");
        }
    }

    /** Data URI para mostrar el mismo QR en la aplicación o en el panel sin otro endpoint. */
    public String dataUri(String contenido) throws MiException {
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(generarPng(contenido));
    }
}
