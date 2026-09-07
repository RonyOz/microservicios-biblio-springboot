package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.model.Prestamo;
import co.analisys.biblioteca.model.PrestamoId;
import co.analisys.biblioteca.model.UsuarioId;
import co.analisys.biblioteca.service.CirculacionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/circulacion")
public class CirculacionController {
    @Autowired
    private CirculacionService circulacionService;

    @Operation(
            summary = "Prestar un libro",
            description = "Este endpoint permite registrar un préstamo. " +
                    "Es importante que el usuario y el libro estén registrados previamente en la base de datos, " +
                    "de lo contrario no podrá realizarse la operación."
    )
    @PostMapping("/prestar")
    public void prestarLibro(@RequestParam String usuarioId, @RequestParam String libroId) {
        circulacionService.prestarLibro(new UsuarioId(usuarioId), new LibroId(libroId));
    }

    @Operation(
            summary = "Devolver un libro",
            description = "Este endpoint permite registrar la devolución de un libro previamente prestado. " +
                    "Es necesario que el ID del préstamo exista en el sistema."
    )
    @PostMapping("/devolver")
    public void devolverLibro(@RequestParam String prestamoId) {
        circulacionService.devolverLibro(new PrestamoId(prestamoId));
    }

    @Operation(
            summary = "Consultar todos los préstamos",
            description = "Este endpoint permite obtener una lista de todos los préstamos registrados " +
                    "en el sistema. Es importante que el cliente esté registrado previamente en la base de datos, " +
                    "de lo contrario no podrá acceder a esta información."
    )
    @GetMapping("/prestamos")
    public List<Prestamo> obtenerTodosPrestamos() {
        return circulacionService.obtenerTodosPrestamos();
    }
}
