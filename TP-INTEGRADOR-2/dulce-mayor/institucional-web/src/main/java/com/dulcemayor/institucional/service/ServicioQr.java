
package com.dulcemayor.institucional.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class ServicioQr {

    private static final int TAMANIO_QR = 280;

    public String generarBase64(String contenido) {

        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException(
                "El contenido del QR no puede estar vacío"
            );
        }

        try (ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            Map<EncodeHintType, Object> configuracion = Map.of(
                EncodeHintType.MARGIN, 2,
                EncodeHintType.ERROR_CORRECTION,
                ErrorCorrectionLevel.H
            );

            BitMatrix matriz = new QRCodeWriter().encode(
                contenido,
                BarcodeFormat.QR_CODE,
                TAMANIO_QR,
                TAMANIO_QR,
                configuracion
            );

            MatrixToImageWriter.writeToStream(
                matriz,
                "PNG",
                salida
            );

            return Base64.getEncoder()
                .encodeToString(salida.toByteArray());

        } catch (WriterException | IOException e) {
            throw new IllegalStateException(
                "No se pudo generar el código QR", e
            );
        }
    }
}
