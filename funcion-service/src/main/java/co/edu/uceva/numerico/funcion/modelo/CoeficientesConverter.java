package co.edu.uceva.numerico.funcion.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** Guarda la lista de coeficientes como texto "a;b;c" para no depender de arreglos de la base de datos. */
@Converter
public class CoeficientesConverter implements AttributeConverter<List<Double>, String> {

    @Override
    public String convertToDatabaseColumn(List<Double> coeficientes) {
        if (coeficientes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coeficientes.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(coeficientes.get(i));
        }
        return sb.toString();
    }

    @Override
    public List<Double> convertToEntityAttribute(String texto) {
        List<Double> lista = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return lista;
        }
        for (String parte : texto.split(";")) {
            lista.add(Double.valueOf(parte));
        }
        return lista;
    }
}
