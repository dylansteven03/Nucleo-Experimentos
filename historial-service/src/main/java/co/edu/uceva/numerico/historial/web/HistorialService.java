package co.edu.uceva.numerico.historial.web;

import co.edu.uceva.numerico.historial.modelo.Corrida;
import co.edu.uceva.numerico.historial.modelo.CorridaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistorialService {

    private final CorridaRepository repositorio;

    public HistorialService(CorridaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** El dueño es siempre el "sub" del token (RA-06). */
    public Corrida registrar(CorridaRequest p, String usuario) {
        return repositorio.save(new Corrida(p.funcionId(), p.metodo().trim(), p.convergio(),
                Boolean.TRUE.equals(p.convergio()) ? p.raiz() : null,
                p.iteraciones(), p.tiempoNativoMs(), p.tiempoTotalMs(), usuario));
    }

    public List<Corrida> propias(String usuario) {
        return repositorio.findByUsuarioOrderByCreadoEnDesc(usuario);
    }

    public List<Corrida> todas() {
        return repositorio.findAllByOrderByCreadoEnDesc();
    }
}
