package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.FotoPacienteService;
import com.incisog.Clinica.models.FotoPaciente;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * La foto es un "sub-recurso" del paciente: /api/pacientes/{pacienteId}/foto
 * No viaja como JSON: se sube como archivo (multipart) y se devuelve como imagen.
 */
@RestController
@RequestMapping("/api/pacientes/{pacienteId}/foto")
public class FotoPacienteController {

    private final FotoPacienteService service;

    public FotoPacienteController(FotoPacienteService service) {
        this.service = service;
    }

    // multipart/form-data: el formato que usan los formularios HTML para mandar archivos.
    // @RequestParam("archivo"): el nombre del campo del formulario que trae el archivo.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> subir(@PathVariable String pacienteId,
                                      @RequestParam("archivo") MultipartFile archivo) throws ErrorServiceException {
        service.subir(pacienteId, archivo);
        return ResponseEntity.noContent().build();
    }

    // Devuelve los bytes de la imagen con su tipo (image/jpeg, image/png...):
    // el navegador la muestra directamente, por ejemplo en un <img src="...">.
    @GetMapping
    public ResponseEntity<byte[]> ver(@PathVariable String pacienteId) throws ErrorServiceException {
        FotoPaciente foto = service.buscarPorPaciente(pacienteId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getMime()))
                .body(foto.getContenido());
    }
}
