package co.edu.uceva.numerico.calculo;

import com.sun.jna.Native;
import org.springframework.stereotype.Service;

@Service
public class NativoService {

    private NumericoLib lib;

    private synchronized NumericoLib lib() {
        if (lib == null) {
            lib = Native.load("numerico", NumericoLib.class);
        }
        return lib;
    }

    public double suma(double a, double b) {
        return lib().nm_suma(a, b);
    }
}