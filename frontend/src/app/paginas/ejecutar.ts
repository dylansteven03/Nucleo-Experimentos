import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CalculoApi, FuncionesApi, HistorialApi } from '../core/api.service';
import { Funcion, Metodo, ResultadoCalculo } from '../models';
import { formatoNumero, formatoPolinomio, leerError } from '../util';

/**
 * HU-02, HU-03, HU-06: configurar una corrida, ejecutarla y ver la tabla de iteraciones.
 * Al terminar, registra la corrida en el historial con el tiempo nativo (lo que mide el backend)
 * y el tiempo total (lo que mide este navegador de extremo a extremo) -> HU-08.
 */
@Component({
  selector: 'app-ejecutar',
  imports: [FormsModule],
  template: `
    <h1>Ejecutar un método</h1>

    <section class="tarjeta">
      <form (ngSubmit)="ejecutar()">
        <div class="fila">
          <label>Función
            <select name="funcion" [ngModel]="funcionId()" (ngModelChange)="elegirFuncion($event)">
              <option [ngValue]="null">— elija una —</option>
              @for (f of funciones(); track f.id) {
                <option [ngValue]="f.id">{{ f.nombre }}</option>
              }
            </select>
          </label>
          <label>Método
            <select name="metodo" [ngModel]="metodo()" (ngModelChange)="metodo.set($event)">
              <option value="biseccion">Bisección</option>
              <option value="newton">Newton</option>
              <option value="secante">Secante</option>
            </select>
          </label>
        </div>

        @if (seleccionada(); as f) {
          <p class="nota">Polinomio: <code>{{ polinomio(f) }}</code> · intervalo de trabajo [{{ f.intervaloA }}, {{ f.intervaloB }}]</p>
        }

        <div class="fila">
          @if (metodo() === 'biseccion') {
            <label>a <input name="p1" [ngModel]="p1()" (ngModelChange)="p1.set($event)" /></label>
            <label>b <input name="p2" [ngModel]="p2()" (ngModelChange)="p2.set($event)" /></label>
          } @else if (metodo() === 'newton') {
            <label>x0 <input name="p1" [ngModel]="p1()" (ngModelChange)="p1.set($event)" /></label>
          } @else {
            <label>x0 <input name="p1" [ngModel]="p1()" (ngModelChange)="p1.set($event)" /></label>
            <label>x1 <input name="p2" [ngModel]="p2()" (ngModelChange)="p2.set($event)" /></label>
          }
          <label>Tolerancia <input name="tol" [ngModel]="tol()" (ngModelChange)="tol.set($event)" /></label>
          <label>Máx. iteraciones <input name="max" [ngModel]="maxIter()" (ngModelChange)="maxIter.set($event)" /></label>
        </div>

        <button type="submit" [disabled]="ejecutando() || seleccionada() === null">Ejecutar</button>
      </form>

      @if (error()) {
        <div class="aviso error">{{ error() }}</div>
      }
    </section>

    @if (resultado(); as r) {
      <section class="tarjeta">
        <h2>Resultado</h2>
        @if (r.estado === 'CONVERGIO') {
          <div class="aviso ok">
            Convergió. Raíz aproximada: <b>{{ numero(r.raiz) }}</b> en {{ r.totalIteraciones }} iteraciones.
          </div>
        } @else {
          <div class="aviso error">
            No convergió{{ r.codigoCausa ? ' — causa: ' + r.codigoCausa : '' }}.
            Última iteración: {{ r.totalIteraciones }}.
          </div>
        }
        <p class="nota">
          Tiempo nativo: <b>{{ numero(r.tiempoNativoMs) }} ms</b> ·
          Tiempo total (navegador → gateway → servicio → C): <b>{{ numero(tiempoTotalMs()) }} ms</b>
        </p>
        @if (guardado()) {
          <p class="nota">{{ guardado() }}</p>
        }

        @if (columnas().length > 0) {
          <table>
            <thead>
              <tr>@for (c of columnas(); track c) { <th>{{ c }}</th> }</tr>
            </thead>
            <tbody>
              @for (fila of r.iteraciones; track $index) {
                <tr>@for (c of columnas(); track c) { <td>{{ numero(fila[c]) }}</td> }</tr>
              }
            </tbody>
          </table>
        }
      </section>
    }
  `,
})
export class EjecutarPagina implements OnInit {
  private readonly funcionesApi = inject(FuncionesApi);
  private readonly calculo = inject(CalculoApi);
  private readonly historial = inject(HistorialApi);

  protected readonly funciones = signal<Funcion[]>([]);
  protected readonly funcionId = signal<number | null>(null);
  protected readonly metodo = signal<Metodo>('biseccion');
  protected readonly p1 = signal('');
  protected readonly p2 = signal('');
  protected readonly tol = signal('1e-6');
  protected readonly maxIter = signal('100');

  protected readonly ejecutando = signal(false);
  protected readonly error = signal('');
  protected readonly resultado = signal<ResultadoCalculo | null>(null);
  protected readonly tiempoTotalMs = signal(0);
  protected readonly guardado = signal('');

  protected readonly seleccionada = computed(
    () => this.funciones().find((f) => f.id === this.funcionId()) ?? null,
  );

  /** Las columnas salen de la primera fila: así la pantalla sigue las que defina el backend. */
  protected readonly columnas = computed(() => {
    const filas = this.resultado()?.iteraciones ?? [];
    return filas.length > 0 ? Object.keys(filas[0]) : [];
  });

  ngOnInit(): void {
    this.funcionesApi.listar().subscribe({
      next: (lista) => this.funciones.set(lista),
      error: (e) => this.error.set(leerError(e).mensaje),
    });
  }

  protected polinomio(f: Funcion): string {
    return formatoPolinomio(f.coeficientes);
  }

  protected numero(valor: number | string | null): string {
    return formatoNumero(valor);
  }

  /** Al elegir una función se precargan el intervalo y la tolerancia que el docente dejó guardados. */
  protected elegirFuncion(id: number | null): void {
    this.funcionId.set(id);
    const f = this.funciones().find((x) => x.id === id);
    if (f) {
      this.p1.set(String(f.intervaloA));
      this.p2.set(String(f.intervaloB));
      this.tol.set(String(f.toleranciaPorDefecto));
    }
    this.resultado.set(null);
    this.guardado.set('');
  }

  protected ejecutar(): void {
    const f = this.seleccionada();
    if (!f) {
      return;
    }
    this.error.set('');
    this.guardado.set('');

    const tol = Number(this.tol());
    const maxIter = Number(this.maxIter());
    const p1 = Number(this.p1());
    const p2 = Number(this.p2());
    const metodo = this.metodo();
    const usaDos = metodo !== 'newton';
    if (!Number.isFinite(tol) || tol <= 0) {
      this.error.set('La tolerancia debe ser un número mayor que 0.');
      return;
    }
    if (!Number.isInteger(maxIter) || maxIter < 1) {
      this.error.set('El máximo de iteraciones debe ser un entero positivo.');
      return;
    }
    if (!Number.isFinite(p1) || (usaDos && !Number.isFinite(p2))) {
      this.error.set('Los valores iniciales deben ser numéricos.');
      return;
    }

    const parametros: Record<string, number> =
      metodo === 'biseccion'
        ? { a: p1, b: p2, tol, maxIter }
        : metodo === 'newton'
          ? { x0: p1, tol, maxIter }
          : { x0: p1, x1: p2, tol, maxIter };

    this.ejecutando.set(true);
    const inicio = performance.now();
    this.calculo.ejecutar(metodo, f.coeficientes, parametros).subscribe({
      next: (r) => {
        const total = performance.now() - inicio;
        this.ejecutando.set(false);
        this.tiempoTotalMs.set(total);
        this.resultado.set(r);
        this.registrarEnHistorial(f.id, metodo, r, total);
      },
      error: (e) => {
        this.ejecutando.set(false);
        this.resultado.set(null);
        this.error.set(leerError(e).mensaje);
      },
    });
  }

  private registrarEnHistorial(funcionId: number, metodo: Metodo, r: ResultadoCalculo, total: number): void {
    const convergio = r.estado === 'CONVERGIO';
    this.historial
      .registrar({
        funcionId,
        metodo,
        convergio,
        raiz: convergio ? r.raiz : null,
        iteraciones: r.totalIteraciones,
        tiempoNativoMs: r.tiempoNativoMs,
        tiempoTotalMs: total,
      })
      .subscribe({
        next: () => this.guardado.set('La corrida quedó registrada en su historial.'),
        error: (e) => this.guardado.set('No se pudo registrar en el historial: ' + leerError(e).mensaje),
      });
  }
}
