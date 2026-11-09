package co.edu.uceva.numerico.funcion.web;

import java.util.List;

/** Reglas de negocio que no se pueden expresar con anotaciones. Se responde 400. */
public class ValidacionException extends RuntimeException {

    private final List<ErrorCampo> errores;

    public ValidacionException(List<ErrorCampo> errores) {
        super("La función tiene datos inválidos");
        this.errores = errores;
    }

    public List<ErrorCampo> getErrores() {
        return errores;
    }
}
