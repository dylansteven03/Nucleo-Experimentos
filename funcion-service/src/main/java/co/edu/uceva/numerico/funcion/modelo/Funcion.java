package co.edu.uceva.numerico.funcion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

/**
 * Un polinomio registrado en el catálogo.
 * Convención del contrato: coeficientes[i] es el coeficiente de x^i (de menor a mayor grado).
 */
@Entity
@Table(name = "funcion")
public class Funcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Convert(converter = CoeficientesConverter.class)
    @Column(nullable = false, length = 1000)
    private List<Double> coeficientes;

    @Column(name = "intervalo_a", nullable = false)
    private double intervaloA;

    @Column(name = "intervalo_b", nullable = false)
    private double intervaloB;

    @Column(name = "tolerancia_por_defecto", nullable = false)
    private double toleranciaPorDefecto;

    /** Claim "sub" del token de quien la registró (RA-06). */
    @Column(name = "creado_por", nullable = false, length = 100)
    private String creadoPor;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Funcion() {
        // requerido por JPA
    }

    public Funcion(String nombre, List<Double> coeficientes, double intervaloA, double intervaloB,
                   double toleranciaPorDefecto, String creadoPor) {
        this.nombre = nombre;
        this.coeficientes = coeficientes;
        this.intervaloA = intervaloA;
        this.intervaloB = intervaloB;
        this.toleranciaPorDefecto = toleranciaPorDefecto;
        this.creadoPor = creadoPor;
        this.creadoEn = Instant.now();
    }

    public int getGrado() {
        return coeficientes.size() - 1;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public List<Double> getCoeficientes() { return coeficientes; }
    public double getIntervaloA() { return intervaloA; }
    public double getIntervaloB() { return intervaloB; }
    public double getToleranciaPorDefecto() { return toleranciaPorDefecto; }
    public String getCreadoPor() { return creadoPor; }
    public Instant getCreadoEn() { return creadoEn; }
}
