package co.analisys.biblioteca.service;

import co.analisys.biblioteca.client.CatalogoClient;
import co.analisys.biblioteca.dto.NotificacionDTO;
import co.analisys.biblioteca.model.EstadoPrestamo;
import co.analisys.biblioteca.model.FechaDevolucionPrevista;
import co.analisys.biblioteca.model.FechaPrestamo;
import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.model.Prestamo;
import co.analisys.biblioteca.model.PrestamoId;
import co.analisys.biblioteca.model.UsuarioId;
import co.analisys.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CirculacionServiceTest {

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private CatalogoClient catalogoClient;
    @Mock private KafkaTemplate<String, NotificacionDTO> kafkaTemplate;

    @InjectMocks private CirculacionService circulacionService;

    @Test
    void devolverLibro_enviarNotificacionViaKafka() {
        PrestamoId prestamoId = new PrestamoId("p1");
        Prestamo prestamo = new Prestamo(prestamoId, new UsuarioId("u1"), new LibroId("l1"),
                new FechaPrestamo(), new FechaDevolucionPrevista(), EstadoPrestamo.ACTIVO);
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));

        circulacionService.devolverLibro(prestamoId);

        verify(catalogoClient).actualizarDisponibilidad("l1", true);
        verify(kafkaTemplate).send("devolucion-libro", new NotificacionDTO("u1", "Libro devuelto: l1"));
    }
}