package com.tpa.testing.jUnit2021;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;

public class SumaSampler extends AbstractJavaSamplerClient {

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult resultado = new SampleResult();

        int a = context.getIntParameter("a", 10);
        int b = context.getIntParameter("b", 20);

        resultado.sampleStart();

        Calculadora calculadora = new Calculadora();
        int suma = calculadora.sumar(a, b);

        resultado.sampleEnd();

        resultado.setResponseCodeOK();
        resultado.setResponseMessage("Resultado: " + suma);
        resultado.setResponseData(String.valueOf(suma), "UTF-8");
        resultado.setSuccessful(true);

        return resultado;
    }

    @Override
    public Arguments getDefaultParameters() {

        Arguments args = new Arguments();

        args.addArgument("a", "10");
        args.addArgument("b", "20");

        return args;
    }
}