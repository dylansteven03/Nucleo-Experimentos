package co.edu.uceva.numerico.funcion.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/** Todo error de datos sale como 400 con un mensaje que dice qué está mal (RF-01). */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> alValidar(MethodArgumentNotValidException ex) {
        List<ErrorCampo> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorCampo(e.getField(), e.getDefaultMessage()))
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud tiene datos inválidos", detalles);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorRespuesta> alValidarNegocio(ValidacionException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrores());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> alLeerCuerpo(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo no es un JSON válido, o algún valor numérico (por ejemplo un coeficiente) no es un número",
                List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> alConvertirParametro(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' no tiene el formato correcto",
                List.of());
    }

    @ExceptionHandler(FuncionNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> alNoEncontrar(FuncionNoEncontradaException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje, List<ErrorCampo> detalles) {
        return ResponseEntity.status(estado)
                .body(new ErrorRespuesta(estado.value(), estado.getReasonPhrase(), mensaje, detalles));
    }
}
