package com.ejercicioIntegrador.dulcemayor.service.promociones;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoValorDescuento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.CodigoDescuento;
import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.model.PromocionPorcentual;
import com.ejercicioIntegrador.dulcemayor.model.PromocionVolumen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Cálculo de descuentos con BigDecimal (sin acceso a la base: recibe las promociones ya cargadas).
 *
 * Reglas de combinación (acordadas con el equipo):
 *  1. Promociones automáticas: en cada renglón se aplica SOLO la mejor entre las porcentuales y las de volumen
 *     que alcanzan al producto. No se suman entre sí.
 *  2. Código de descuento (uno por pedido): se aplica después, sobre el importe que quedó en cada renglón.
 *     Si el código NO es acumulable, no se aplica sobre los renglones que ya tienen una promoción automática.
 *  3. Monto mínimo del código: se compara con el total después de las promociones automáticas.
 *  4. Importe fijo: nunca supera el importe de los renglones alcanzados (el total no puede quedar negativo).
 * Los importes se redondean a centavos (HALF_UP) en cada descuento.
 */
public final class CalculadorDescuentos {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadorDescuentos() {
    }

    static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** porcentaje % de base, redondeado a centavos. */
    public static BigDecimal porcentajeDe(BigDecimal base, BigDecimal porcentaje) {
        return dinero(base.multiply(porcentaje).divide(CIEN, 6, RoundingMode.HALF_UP));
    }

    /**
     * @param automaticas promociones porcentuales y por volumen vigentes (las demás se ignoran)
     * @param codigo      código ya validado (existe, activo, vigente, con usos); null si no se usa
     * @throws MiException si el código no cumple sus condiciones para ESTE pedido (monto mínimo, productos)
     */
    public static ResultadoDescuentos calcular(List<RenglonDescuento> renglones, List<? extends Promocion> automaticas,
                                               CodigoDescuento codigo) throws MiException {
        List<DescuentoCalculado> descuentos = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal[] neto = new BigDecimal[renglones.size()];
        boolean[] conPromocion = new boolean[renglones.size()];

        for (int i = 0; i < renglones.size(); i++) {
            RenglonDescuento renglon = renglones.get(i);
            BigDecimal base = renglon.subtotal();
            subtotal = subtotal.add(base);
            neto[i] = base;
            Promocion mejor = null;
            BigDecimal mejorPorcentaje = BigDecimal.ZERO;
            for (Promocion promocion : automaticas) {
                BigDecimal porcentaje = porcentajeAutomatico(promocion, renglon);
                if (porcentaje.compareTo(mejorPorcentaje) > 0) {
                    mejor = promocion;
                    mejorPorcentaje = porcentaje;
                }
            }
            if (mejor != null) {
                BigDecimal importe = porcentajeDe(base, mejorPorcentaje);
                if (importe.signum() > 0) {
                    descuentos.add(new DescuentoCalculado(i, mejor, mejorPorcentaje, importe));
                    neto[i] = base.subtract(importe);
                    conPromocion[i] = true;
                }
            }
        }

        if (codigo != null) {
            aplicarCodigo(codigo, renglones, neto, conPromocion, descuentos);
        }

        BigDecimal totalDescuento = descuentos.stream().map(DescuentoCalculado::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal.subtract(totalDescuento).max(BigDecimal.ZERO);
        return new ResultadoDescuentos(dinero(subtotal), List.copyOf(descuentos), dinero(totalDescuento), dinero(total),
                codigo);
    }

    /** Porcentaje que la promoción automática le da a ese renglón (0 si no lo alcanza). */
    public static BigDecimal porcentajeAutomatico(Promocion promocion, RenglonDescuento renglon) {
        if (!promocion.alcanza(renglon.producto())) {
            return BigDecimal.ZERO;
        }
        if (promocion instanceof PromocionPorcentual porcentual) {
            return porcentual.getPorcentaje();
        }
        if (promocion instanceof PromocionVolumen volumen) {
            return volumen.porcentajePara(renglon.cantidad());
        }
        return BigDecimal.ZERO;
    }

    private static void aplicarCodigo(CodigoDescuento codigo, List<RenglonDescuento> renglones, BigDecimal[] neto,
                                      boolean[] conPromocion, List<DescuentoCalculado> descuentos) throws MiException {
        BigDecimal totalNeto = BigDecimal.ZERO;
        for (BigDecimal valor : neto) {
            totalNeto = totalNeto.add(valor);
        }
        if (codigo.getMontoMinimo() != null && totalNeto.compareTo(codigo.getMontoMinimo()) < 0) {
            throw new MiException("El código " + codigo.getCodigo() + " requiere una compra mínima de $ "
                    + dinero(codigo.getMontoMinimo()).toPlainString() + " (sin IVA). El pedido suma $ "
                    + dinero(totalNeto).toPlainString());
        }
        List<Integer> alcanzados = new ArrayList<>();
        BigDecimal netoAlcanzado = BigDecimal.ZERO;
        for (int i = 0; i < renglones.size(); i++) {
            boolean bloqueadoPorPromocion = conPromocion[i] && !codigo.isAcumulable();
            if (codigo.alcanza(renglones.get(i).producto()) && !bloqueadoPorPromocion && neto[i].signum() > 0) {
                alcanzados.add(i);
                netoAlcanzado = netoAlcanzado.add(neto[i]);
            }
        }
        if (alcanzados.isEmpty()) {
            throw new MiException(codigo.isAcumulable()
                    ? "El código " + codigo.getCodigo() + " no se aplica a ningún producto del pedido"
                    : "El código " + codigo.getCodigo() + " no es acumulable y los productos alcanzados ya tienen una promoción");
        }
        if (codigo.getTipoValor() == TipoValorDescuento.PORCENTAJE) {
            for (int i : alcanzados) {
                BigDecimal importe = porcentajeDe(neto[i], codigo.getValor());
                if (importe.signum() > 0) {
                    descuentos.add(new DescuentoCalculado(i, codigo, codigo.getValor(), importe));
                    neto[i] = neto[i].subtract(importe);
                }
            }
        } else {
            BigDecimal importe = dinero(codigo.getValor().min(netoAlcanzado));
            descuentos.add(new DescuentoCalculado(DescuentoCalculado.PEDIDO_COMPLETO, codigo, null, importe));
        }
    }
}
