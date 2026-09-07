package com.tpa.testing.jUnit2021;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CalculadoraTest {
    
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
    public void testSumar() {
        int a=2, b=3, c=5, resultado=0;
        
        Calculadora calc = new Calculadora();
        resultado = calc.sumar(a, b);
        
        System.out.println("resultado " + resultado);
    }
    
    @Test
    public void testSumar1() {
        int a=2, b=3, c=5, resultado=0;
        
        Calculadora calc = new Calculadora();
        resultado = calc.sumar(a, b);
        
        System.out.println("resultado " + resultado);
    }
}
