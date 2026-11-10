package co.edu.uceva.numerico.historial.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Una corrida de un método numérico sobre una función (RF-07) con sus tiempos (RF-08).
 * "usuario" es el claim "sub" del token de quien la ejecutó (RA-06): el historial propio
 * se filtra por ese valor, nunca por algo que mande el cliente.
 */
@Entity
@Table(name = "corrida")
public class Corrida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "funcion_id", nullable = false)
    private Long funcionId;

    @Column(nullable = false, length = 40)
    private String metodo;

    @Column(nullable = false)
    private boolean convergio;

    /** Nula cuando el método no convergió (RF-06). */
    @Column
    private Double raiz;

    @Column(nullable = false)
    private int iteraciones;

    /** Tiempo medido solo en la parte nativa (C), en milisegundos. RQ-01: objetivo < 50 ms. */
    @Column(name = "tiempo_nativo_ms", nullable = false)
    private double tiempoNativoMs;

    /** Tiempo de extremo a extremo, en milisegundos. RQ-01: objetivo < 500 ms. */
    @Column(name = "tiempo_total_ms", nullable = false)
    private double tiempoTotalMs;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Corrida() {
        // requerido por JPA
    }

    public Corrida(Long funcionId, String metodo, boolean convergio, Double raiz, int iteraciones,
                   double tiempoNativoMs, double tiempoTotalMs, String usuario) {
        this.funcionId = funcionId;
        this.metodo = metodo;
        this.convergio = convergio;
        this.raiz = raiz;
        this.iteraciones = iteraciones;
        this.tiempoNativoMs = tiempoNativoMs;
        this.tiempoTotalMs = tiempoTotalMs;
        this.usuario = usuario;
        this.creadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public Long getFuncionId() { return funcionId; }
    public String getMetodo() { return metodo; }
    public boolean isConvergio() { return convergio; }
    public Double getRaiz() { return raiz; }
    public int getIteraciones() { return iteraciones; }
    public double getTiempoNativoMs() { return tiempoNativoMs; }
    public double getTiempoTotalMs() { return tiempoTotalMs; }
    public String getUsuario() { return usuario; }
    public Instant getCreadoEn() { return creadoEn; }
}
