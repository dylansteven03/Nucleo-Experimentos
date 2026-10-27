package co.edu.uceva.numerico.calculo;

import java.util.List;

/** Los 11 casos de referencia de la sección 8 del contrato, compartidos por las pruebas. */
final class CasosReferencia {

    static final double TOL = 1e-10;
    static final double[] X2_MENOS_4 = {-4, 0, 1};
    static final double[] X2_MAS_1 = {1, 0, 1};
    static final double[] CUBICA = {-5, -2, 0, 1};

    /**
     * @param nombre         texto para el reporte
     * @param metodo         BISECCION, NEWTON o SECANTE
     * @param p1             a (bisección), x0 (Newton, secante)
     * @param p2             b (bisección), x1 (secante); no se usa en Newton
     * @param codigo         código esperado (0-4)
     * @param n              iteraciones esperadas
     * @param raiz           raíz esperada (solo si codigo == 0)
     */
    record Caso(String nombre, String metodo, double[] coef, double p1, double p2,
                int maxIter, int codigo, int n, double raiz) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    static final List<Caso> TODOS = List.of(
            new Caso("1 bisección x²-4 [0,5]", "BISECCION", X2_MENOS_4, 0, 5, 100, 0, 36, 2.0),
            new Caso("2 Newton x²-4 x0=3", "NEWTON", X2_MENOS_4, 3, 0, 100, 0, 5, 2.0),
            new Caso("3 secante x²-4 (1,3)", "SECANTE", X2_MENOS_4, 1, 3, 100, 0, 7, 2.0),
            new Caso("4 Newton cúbica x0=2", "NEWTON", CUBICA, 2, 0, 100, 0, 5, 2.0945514815),
            new Caso("5 bisección cúbica [2,3]", "BISECCION", CUBICA, 2, 3, 100, 0, 34, 2.0945514815),
            new Caso("6 secante cúbica (2,3)", "SECANTE", CUBICA, 2, 3, 100, 0, 7, 2.0945514815),
            new Caso("7 bisección maxIter=3", "BISECCION", X2_MENOS_4, 0, 5, 3, 1, 3, 0),
            new Caso("8 Newton x0=0", "NEWTON", X2_MENOS_4, 0, 0, 100, 2, 0, 0),
            new Caso("9 Newton x²+1 x0=1", "NEWTON", X2_MAS_1, 1, 0, 100, 2, 1, 0),
            new Caso("10 secante (-1,1)", "SECANTE", X2_MENOS_4, -1, 1, 100, 3, 0, 0),
            new Caso("11 bisección [3,5]", "BISECCION", X2_MENOS_4, 3, 5, 100, 4, 0, 0));

    private CasosReferencia() {
    }

    static BaselineJava.Resultado ejecutarJava(Caso c) {
        int grado = c.coef().length - 1;
        return switch (c.metodo()) {
            case "BISECCION" -> BaselineJava.biseccion(c.coef(), grado, c.p1(), c.p2(), TOL, c.maxIter());
            case "NEWTON" -> BaselineJava.newton(c.coef(), grado, c.p1(), TOL, c.maxIter());
            case "SECANTE" -> BaselineJava.secante(c.coef(), grado, c.p1(), c.p2(), TOL, c.maxIter());
            default -> throw new IllegalArgumentException(c.metodo());
        };
    }
}
