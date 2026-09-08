package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.Libro;
import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class CatalogoController {
    private final CatalogoService catalogoService;

    @Autowired
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(
            summary = "Obtener un libro",
            description = "Este endpoint permite consultar un libro específico por su ID. "
                    + "Es necesario que el libro exista en la base de datos."
    )
    @GetMapping("/{id}")
    public Libro obtenerLibro(@PathVariable String id) {
        return catalogoService.obtenerLibro(new LibroId(id));
    }

    @Operation(
            summary = "Consultar disponibilidad de un libro",
            description = "Este endpoint indica si un libro está disponible para préstamo. "
                    + "Devuelve true si el libro existe y está disponible."
    )
    @GetMapping("/{id}/disponible")
    public boolean isLibroDisponible(@PathVariable String id) {
        Libro libro = catalogoService.obtenerLibro(new LibroId(id));
        return libro != null && libro.isDisponible();
    }

    @Operation(
            summary = "Actualizar disponibilidad de un libro",
            description = "Este endpoint permite cambiar la disponibilidad de un libro (disponible o no). "
                    + "Se utiliza al prestar o devolver un libro."
    )
    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public void actualizarDisponibilidad(@PathVariable String id, @RequestBody boolean disponible) {
        catalogoService.actualizarDisponibilidad(new LibroId(id), disponible);
    }

    @Operation(
            summary = "Buscar libros por criterio",
            description = "Este endpoint permite buscar libros según un criterio de búsqueda "
                    + "(por título, autor, categoría, etc.)."
    )
    @GetMapping("/buscar")
    public List<Libro> buscarLibros(@RequestParam String criterio) {
        return catalogoService.buscarLibros(criterio);
    }
}
