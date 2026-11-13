package com.ejercicioIntegrador.dulcemayor.service.pagos;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/** Genera el PNG de un QR (con ZXing) para adjuntarlo en el correo de pago. */
@Component
public class GeneradorQrPng {

    private static final int TAMANIO = 320;

    public byte[] generar(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El contenido del QR no puede estar vacío");
        }
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Map<EncodeHintType, Object> pistas = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, TAMANIO, TAMANIO, pistas);
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return salida.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("No se pudo generar la imagen del QR", e);
        }
    }
}
