package co.edu.uceva.numerico.funcion.web;

import co.edu.uceva.numerico.funcion.modelo.Funcion;
import co.edu.uceva.numerico.funcion.modelo.FuncionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FuncionService {

    static final int GRADO_MAXIMO = 10;
    static final double TOLERANCIA_POR_DEFECTO = 1e-6;

    private final FuncionRepository repositorio;

    public FuncionService(FuncionRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Funcion registrar(FuncionRequest peticion, String sub) {
        List<ErrorCampo> errores = new ArrayList<>();
        validar(peticion, errores);
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        double tolerancia = peticion.toleranciaPorDefecto() == null
                ? TOLERANCIA_POR_DEFECTO : peticion.toleranciaPorDefecto();
        Funcion funcion = new Funcion(peticion.nombre().trim(), List.copyOf(peticion.coeficientes()),
                peticion.intervaloA(), peticion.intervaloB(), tolerancia, sub);
        return repositorio.save(funcion);
    }

    @Transactional(readOnly = true)
    public List<Funcion> listar() {
        return repositorio.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Funcion obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new FuncionNoEncontradaException(id));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new FuncionNoEncontradaException(id);
        }
        repositorio.deleteById(id);
    }

    private void validar(FuncionRequest p, List<ErrorCampo> errores) {
        List<Double> coef = p.coeficientes();
        if (coef.size() - 1 > GRADO_MAXIMO) {
            errores.add(new ErrorCampo("coeficientes", "El grado máximo es " + GRADO_MAXIMO));
        }
        for (int i = 0; i < coef.size(); i++) {
            if (!Double.isFinite(coef.get(i))) {
                errores.add(new ErrorCampo("coeficientes[" + i + "]", "Debe ser un número finito"));
            }
        }
        if (coef.get(coef.size() - 1) == 0.0) {
            errores.add(new ErrorCampo("coeficientes",
                    "El último coeficiente (el de mayor grado) no puede ser 0"));
        }
        if (!Double.isFinite(p.intervaloA()) || !Double.isFinite(p.intervaloB())) {
            errores.add(new ErrorCampo("intervaloA", "Los extremos del intervalo deben ser números finitos"));
        } else if (p.intervaloA() >= p.intervaloB()) {
            errores.add(new ErrorCampo("intervaloB", "El fin del intervalo debe ser mayor que el inicio"));
        }
        if (p.toleranciaPorDefecto() != null
                && (!Double.isFinite(p.toleranciaPorDefecto()) || p.toleranciaPorDefecto() <= 0)) {
            errores.add(new ErrorCampo("toleranciaPorDefecto", "La tolerancia debe ser mayor que 0"));
        }
    }
}
