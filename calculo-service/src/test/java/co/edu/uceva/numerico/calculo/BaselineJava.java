package co.edu.uceva.numerico.calculo;

import java.util.ArrayList;
import java.util.List;

/**
 * Línea base en Java puro (RN-05): los mismos tres métodos que la biblioteca en C,
 * con las mismas reglas del contrato (códigos 0-4, filas desde n=1).
 * Vive en src/test: solo sirve para comparar y medir contra C, no se despliega.
 */
final class BaselineJava {

    private static final int GRADO_MAX = 10;
    private static final double DERIVADA_NULA = 1e-14;

    /** codigo: 0 convergió, 1 tope, 2 derivada nula, 3 overflow/NaN, 4 parámetros inválidos. */
    record Resultado(int codigo, List<FilaIteracion> filas) {
        int n() {
            return filas.size();
        }

        double ultimoX() {
            return filas.get(filas.size() - 1).x();
        }
    }

    private BaselineJava() {
    }

    static double eval(double[] coef, int grado, double x) {
        double r = coef[grado];
        for (int i = grado - 1; i >= 0; i--) {
            r = r * x + coef[i];
        }
        return r;
    }

    static double deriv(double[] coef, int grado, double x) {
        if (grado == 0) {
            return 0.0;
        }
        double r = grado * coef[grado];
        for (int i = grado - 1; i >= 1; i--) {
            r = r * x + i * coef[i];
        }
        return r;
    }

    private static boolean finito(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    private static boolean invalidos(double[] coef, int grado, double tol, int maxIter) {
        return coef == null || grado < 0 || grado > GRADO_MAX || coef.length < grado + 1
                || maxIter <= 0 || !finito(tol) || tol <= 0.0;
    }

    static Resultado biseccion(double[] coef, int grado, double a, double b, double tol, int maxIter) {
        List<FilaIteracion> filas = new ArrayList<>();
        if (invalidos(coef, grado, tol, maxIter) || !finito(a) || !finito(b) || a >= b) {
            return new Resultado(4, filas);
        }
        double fa = eval(coef, grado, a);
        double fb = eval(coef, grado, b);
        if (!finito(fa) || !finito(fb)) {
            return new Resultado(3, filas);
        }
        if (!((fa < 0 && fb > 0) || (fa > 0 && fb < 0))) {
            return new Resultado(4, filas);
        }
        for (int i = 1; i <= maxIter; i++) {
            double m = a + (b - a) / 2.0;
            double fm = eval(coef, grado, m);
            double err = (b - a) / 2.0;
            filas.add(new FilaIteracion(i, m, fm, err));
            if (!finito(fm)) {
                return new Resultado(3, filas);
            }
            if (fm == 0.0 || err < tol) {
                return new Resultado(0, filas);
            }
            if ((fa < 0) == (fm < 0)) {
                a = m;
                fa = fm;
            } else {
                b = m;
            }
        }
        return new Resultado(1, filas);
    }

    static Resultado newton(double[] coef, int grado, double x0, double tol, int maxIter) {
        List<FilaIteracion> filas = new ArrayList<>();
        if (invalidos(coef, grado, tol, maxIter) || !finito(x0)) {
            return new Resultado(4, filas);
        }
        double x = x0;
        for (int i = 1; i <= maxIter; i++) {
            double fx = eval(coef, grado, x);
            double d = deriv(coef, grado, x);
            if (!finito(d) || Math.abs(d) < DERIVADA_NULA) {
                return new Resultado(2, filas);
            }
            double xNuevo = x - fx / d;
            double fNuevo = eval(coef, grado, xNuevo);
            double err = Math.abs(xNuevo - x);
            filas.add(new FilaIteracion(i, xNuevo, fNuevo, err));
            if (!finito(xNuevo) || !finito(fNuevo)) {
                return new Resultado(3, filas);
            }
            if (fNuevo == 0.0 || err < tol) {
                return new Resultado(0, filas);
            }
            x = xNuevo;
        }
        return new Resultado(1, filas);
    }

    static Resultado secante(double[] coef, int grado, double x0, double x1, double tol, int maxIter) {
        List<FilaIteracion> filas = new ArrayList<>();
        if (invalidos(coef, grado, tol, maxIter) || !finito(x0) || !finito(x1) || x0 == x1) {
            return new Resultado(4, filas);
        }
        double xa = x0;
        double xb = x1;
        double fa = eval(coef, grado, xa);
        double fb = eval(coef, grado, xb);
        if (!finito(fa) || !finito(fb)) {
            return new Resultado(3, filas);
        }
        for (int i = 1; i <= maxIter; i++) {
            if (fb == fa) {
                return new Resultado(3, filas);
            }
            double xNuevo = xb - fb * (xb - xa) / (fb - fa);
            double fNuevo = eval(coef, grado, xNuevo);
            double err = Math.abs(xNuevo - xb);
            filas.add(new FilaIteracion(i, xNuevo, fNuevo, err));
            if (!finito(xNuevo) || !finito(fNuevo)) {
                return new Resultado(3, filas);
            }
            if (fNuevo == 0.0 || err < tol) {
                return new Resultado(0, filas);
            }
            xa = xb;
            fa = fb;
            xb = xNuevo;
            fb = fNuevo;
        }
        return new Resultado(1, filas);
    }
}
