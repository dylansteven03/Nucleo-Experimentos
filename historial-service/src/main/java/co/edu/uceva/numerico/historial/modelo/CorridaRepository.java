package co.edu.uceva.numerico.historial.modelo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorridaRepository extends JpaRepository<Corrida, Long> {

    List<Corrida> findByUsuarioOrderByCreadoEnDesc(String usuario);

    List<Corrida> findAllByOrderByCreadoEnDesc();
}
