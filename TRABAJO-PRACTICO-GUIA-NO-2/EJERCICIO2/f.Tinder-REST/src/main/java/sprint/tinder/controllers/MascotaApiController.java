package sprint.tinder.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sprint.tinder.dtos.MascotaDto;
import sprint.tinder.dtos.MascotaRequest;
import sprint.tinder.entities.Mascota;
import sprint.tinder.entities.Usuario;
import sprint.tinder.enumerations.Tipo;
import sprint.tinder.errors.ErrorServicio;
import sprint.tinder.mappers.MascotaMapper;
import sprint.tinder.repositories.UsuarioRepository;
import sprint.tinder.services.MascotaService;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaApiController {
    private final MascotaService mascotaService;
    private final UsuarioRepository usuarioRepository;
    private final MascotaMapper mascotaMapper;

    public MascotaApiController(MascotaService mascotaService,
                                UsuarioRepository usuarioRepository,
                                MascotaMapper mascotaMapper) {
        this.mascotaService = mascotaService;
        this.usuarioRepository = usuarioRepository;
        this.mascotaMapper = mascotaMapper;
    }

    @GetMapping("/mias")
    public List<MascotaDto> listarMias(Authentication authentication) throws ErrorServicio {
        return mascotaMapper.toDtos(mascotaService.listarMascotaPorUsuario(usuarioActual(authentication).getId()));
    }

    @GetMapping("/baja")
    public List<MascotaDto> listarDeBaja(Authentication authentication) throws ErrorServicio {
        return mascotaMapper.toDtos(mascotaService.listarMascotaDeBaja(usuarioActual(authentication).getId()));
    }

    @GetMapping("/explorar")
    public List<MascotaDto> explorar(Authentication authentication,
                                     @RequestParam(required = false) Tipo tipo) throws ErrorServicio {
        Collection<Mascota> mascotas = mascotaService.listarMascotasPorTipo(
                usuarioActual(authentication).getId(), tipo);
        return mascotaMapper.toDtos(mascotas);
    }

    @GetMapping("/{id}")
    public MascotaDto buscar(@PathVariable String id, Authentication authentication) throws ErrorServicio {
        Mascota mascota = mascotaService.buscarMascota(id);
        if (mascota == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la mascota indicada");
        }
        if (!mascota.getUsuario().getId().equals(usuarioActual(authentication).getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usted no es dueño de esa mascota");
        }
        return mascotaMapper.toDto(mascota);
    }

    @PostMapping
    public ResponseEntity<MascotaDto> crear(@RequestBody MascotaRequest request,
                                             Authentication authentication) throws ErrorServicio {
        Mascota mascota = mascotaService.crearApi(usuarioActual(authentication).getId(),
                request.getNombre(), request.getSexo(), request.getTipo());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(mascota.getId()).toUri();
        return ResponseEntity.created(location).body(mascotaMapper.toDto(mascota));
    }

    @PutMapping("/{id}")
    public MascotaDto modificar(@PathVariable String id,
                                @RequestBody MascotaRequest request,
                                Authentication authentication) throws ErrorServicio {
        Mascota mascota = mascotaService.modificarApi(usuarioActual(authentication).getId(), id,
                request.getNombre(), request.getSexo(), request.getTipo());
        return mascotaMapper.toDto(mascota);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id,
                                         Authentication authentication) throws ErrorServicio {
        mascotaService.eliminar(usuarioActual(authentication).getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restaurar")
    public ResponseEntity<Void> restaurar(@PathVariable String id,
                                          Authentication authentication) throws ErrorServicio {
        mascotaService.agregarMascota(usuarioActual(authentication).getId(), id);
        return ResponseEntity.noContent().build();
    }

    private Usuario usuarioActual(Authentication authentication) {
        Usuario usuario = usuarioRepository.buscarPorMail(authentication.getName());
        if (usuario == null || usuario.isEliminado() || usuario.getBaja() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido");
        }
        return usuario;
    }
}
