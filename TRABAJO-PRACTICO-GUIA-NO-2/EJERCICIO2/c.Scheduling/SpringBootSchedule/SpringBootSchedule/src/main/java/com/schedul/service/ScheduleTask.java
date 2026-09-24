package com.schedul.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
 - fixedRate: Ejecuta la tarea a intervalos fijos desde el inicio de cada ejecución, sin importar cuánto tiempo tarde en completarse la tarea anterior.
 - fixedDelay: Ejecuta la tarea a intervalos fijos desde el final de la ejecución de la tarea anterior, asegurando que haya un retraso constante entre las ejecuciones.
 - initialDelay: Retrasa la primera ejecución de la tarea programada por un período de tiempo especificado después de que la aplicación se haya iniciado.
 - cron: Ejecuta según una expresión cron, que permite programar tareas en momentos específicos del día, semana o mes.
   CRON es una nomenclatura que sirve para expresar periodos de tiempo.
 - zone: Permite especificar la zona horaria en la que se debe ejecutar la tarea programada, lo que es útil para aplicaciones que operan en múltiples zonas horarias.
*/

@Component 
public class ScheduleTask {
    
    @Scheduled(cron = "0 0 15 * * 1,3,5", zone = "America/Argentina/Buenos_Aires")// Ejecuta la tarea a las 15:00 horas los lunes, miércoles y viernes.
    public void scheduleMessage() throws InterruptedException {
        System.out.println("Scheduled task executed at: " + new java.util.Date());
    }
}
