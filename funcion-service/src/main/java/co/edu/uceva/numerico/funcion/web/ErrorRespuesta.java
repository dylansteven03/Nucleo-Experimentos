package co.edu.uceva.numerico.funcion.web;

import java.util.List;

public record ErrorRespuesta(int estado, String error, String mensaje, List<ErrorCampo> detalles) {
}
