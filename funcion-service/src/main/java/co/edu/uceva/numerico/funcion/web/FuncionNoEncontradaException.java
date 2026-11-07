package co.edu.uceva.numerico.funcion.web;

public class FuncionNoEncontradaException extends RuntimeException {

    public FuncionNoEncontradaException(Long id) {
        super("No existe una función con id " + id);
    }
}
