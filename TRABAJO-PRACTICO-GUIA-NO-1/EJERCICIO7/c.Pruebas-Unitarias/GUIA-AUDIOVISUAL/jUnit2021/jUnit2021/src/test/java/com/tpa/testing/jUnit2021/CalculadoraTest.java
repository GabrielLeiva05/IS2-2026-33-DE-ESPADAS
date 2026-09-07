package com.tpa.testing.jUnit2021;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.OrderAnnotation.class)
public class CalculadoraTest {
    Calculadora calc = new Calculadora();
    //Tanto BeforeAll como AfterAll deben ser static
    @BeforeAll
    public static void beforeAll() {
        System.out.println("beforeAll");
    }
    
    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll");
    }
    
    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach");
    }
    
    @AfterEach
    public void afterEach() {
        System.out.println("afterEach");
    }
    
    @Test
    @Order(1)
    public void testSumar() {
        int a=2, b=3, c=5, resultado=0;
        resultado = calc.sumar(a, b);
        
        System.out.println("resultado " + resultado);
    }
    
    @Test
    @Order(2)
    public void testDividirDenominadorCero() {
        Double a=2.0,b=3.0,c=5.0, resultado = 0.0 ,esperado = 0.6666666666666666;
        try {
            resultado = calc.dividir(a, b);
        } catch (Exception ex) {
            System.getLogger(CalculadoraTest.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
        Assertions.assertEquals(esperado, resultado);
        
        System.out.println("resultado " + resultado);
    }
    
    @Test
    @Order(3)
    public void testDividirAssertTrue() {
        Double a=2.0,b=3.0,c=5.0, resultado = 0.0 ,esperado = 0.6666666666666666;
        try {
            resultado = calc.dividir(a, b);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        Assertions.assertTrue(esperado != resultado);
        
        System.out.println("resultado " + resultado);
    }
}
