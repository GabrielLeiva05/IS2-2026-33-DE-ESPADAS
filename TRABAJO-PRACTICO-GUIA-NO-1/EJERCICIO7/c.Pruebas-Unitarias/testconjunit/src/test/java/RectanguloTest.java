/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

import com.mycompany.testconjunit.entities.Rectangulo;
import com.mycompany.testconjunit.service.RectanguloService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Gabriel
 */
public class RectanguloTest {
    RectanguloService rs = new RectanguloService();
    public RectanguloTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void deberiaInicializarConColor() {
        assertNotNull(new Rectangulo(10,10).getColor());
    }
    
    @Test
    public void deberiaCalcularArea() {
        assertEquals(100, rs.calcularArea(new Rectangulo(10,10)),0);
        assertEquals(20, rs.calcularArea(new Rectangulo(4,5)),0);
        assertEquals(1, rs.calcularArea(new Rectangulo(1,1)),0);
    }
    
    @Test
    public void deberiaCalcularPerimetro() {
        assertEquals(8, rs.calcularPerimetro(new Rectangulo(2,2)),0);
        assertEquals(8, rs.calcularPerimetro(new Rectangulo(2,2)),0);
        assertEquals(8, rs.calcularPerimetro(new Rectangulo(4,1)),0);
    }
    
    @Test
    public void deberiaActivarODesactivar() {
        Rectangulo r = new Rectangulo(5,5);
        assertTrue(r.isActivo());
        r.setActivo(false);
        assertFalse(r.isActivo());
    }
}
