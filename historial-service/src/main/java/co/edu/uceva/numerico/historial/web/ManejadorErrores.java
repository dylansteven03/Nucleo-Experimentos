package co.edu.uceva.numerico.historial.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Mismo formato de error que funcion-service: {estado, error, mensaje, detalles[{campo, mensaje}]}. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> alValidar(MethodArgumentNotValidException ex) {
        List<ErrorCampo> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorCampo(e.getField(), e.getDefaultMessage()))
                .toList();
        return respuesta("La solicitud tiene datos inválidos", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> alLeerCuerpo(HttpMessageNotReadableException ex) {
        return respuesta("El cuerpo no es un JSON válido, o algún valor numérico no es un número", List.of());
    }

    private ResponseEntity<ErrorRespuesta> respuesta(String mensaje, List<ErrorCampo> detalles) {
        HttpStatus estado = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(estado)
                .body(new ErrorRespuesta(estado.value(), estado.getReasonPhrase(), mensaje, detalles));
    }
}
