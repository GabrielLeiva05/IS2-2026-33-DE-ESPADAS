package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.entities.Persona;
import ar.com.biblioteca.server.repositories.PersonaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);
    private static final String ZONA = "America/Argentina/Buenos_Aires";

    private final PersonaRepository personaRepository;
    private final MailService mailService;
    private final String urlFacultad;

    public ScheduleTask(PersonaRepository personaRepository, MailService mailService,
                        @Value("${biblioteca.facultad.url}") String urlFacultad) {
        this.personaRepository = personaRepository;
        this.mailService = mailService;
        this.urlFacultad = urlFacultad;
    }

    /** Todos los días: avisa de los libros que vencen mañana. */
    @Scheduled(cron = "${biblioteca.mail.cron}", zone = ZONA)
    @Transactional(readOnly = true)
    public void avisarVencimientos() {
        LocalDate manana = LocalDate.now(ZoneId.of(ZONA)).plusDays(1);
        for (Persona p : personaRepository.findConLibrosQueVencenEl(manana)) {
            List<Libro> libros = p.getLibros().stream()
                    .filter(l -> manana.equals(l.getFechaDevolucion()))
                    .toList();
            mailService.enviarHtml(p.getEmail(), "Recordatorio: devolución de libro",
                    htmlVencimiento(p, libros, manana));
        }
        log.info("Avisos de vencimiento procesados para {}", manana);
    }

    /** Todos los días: saluda a quienes cumplen años. */
    @Scheduled(cron = "${biblioteca.mail.cron}", zone = ZONA)
    @Transactional(readOnly = true)
    public void saludarCumpleanios() {
        LocalDate hoy = LocalDate.now(ZoneId.of(ZONA));
        for (Persona p : personaRepository.findCumpleanieros(hoy.getMonthValue(), hoy.getDayOfMonth())) {
            mailService.enviarHtml(p.getEmail(), "¡Feliz cumpleaños, " + p.getNombre() + "!",
                    htmlCumpleanios(p));
        }
    }

    private String htmlVencimiento(Persona p, List<Libro> libros, LocalDate fecha) {
        String items = libros.stream()
                .map(l -> "<li>" + HtmlUtils.htmlEscape(l.getTitulo()) + "</li>")
                .collect(Collectors.joining());
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto">
              <h2>Hola %s:</h2>
              <p>Mañana (%s) vence el plazo de devolución de:</p>
              <ul>%s</ul>
              <p>¡Gracias!</p>
            </div>
            """.formatted(HtmlUtils.htmlEscape(p.getNombre()),
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), items);
    }

    private String htmlCumpleanios(Persona p) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;text-align:center">
              <h1>🎂 ¡Feliz cumpleaños, %s!</h1>
              <p>Te deseamos un día lleno de alegría.</p>
              <a href="%s" style="display:inline-block;padding:12px 24px;background:#0d6efd;
                 color:#fff;text-decoration:none;border-radius:6px">Visitá la página de la Facultad</a>
            </div>
            """.formatted(HtmlUtils.htmlEscape(p.getNombre()), urlFacultad);
    }
}