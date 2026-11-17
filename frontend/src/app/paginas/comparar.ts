import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { GraficaError } from '../componentes/grafica-error';
import { CalculoApi, FuncionesApi, HistorialApi } from '../core/api.service';
import { Funcion, Metodo, NOMBRE_METODO, ResultadoCalculo, SerieError } from '../models';
import { formatoNumero, formatoPolinomio, leerError } from '../util';

interface FilaComparacion {
  metodo: Metodo;
  nombre: string;
  resultado: ResultadoCalculo | null;
  totalMs: number;
  error: string;
}

const ORDEN: Metodo[] = ['biseccion', 'newton', 'secante'];

/**
 * HU-04: los tres métodos sobre la misma función, uno tras otro (no en paralelo, para que el tiempo
 * de cada uno no se contamine con los otros), con una tabla comparativa y las tres curvas del error en la misma gráfica.
 */
@Component({
  selector: 'app-comparar',
  imports: [FormsModule, GraficaError],
  template: `
    <h1>Comparar métodos</h1>

    <section class="tarjeta">
      <form (ngSubmit)="comparar()">
        <label>Función
          <select name="funcion" [ngModel]="funcionId()" (ngModelChange)="elegirFuncion($event)">
            <option [ngValue]="null">— elija una —</option>
            @for (f of funciones(); track f.id) {
              <option [ngValue]="f.id">{{ f.nombre }}</option>
            }
          </select>
        </label>

        @if (seleccionada(); as f) {
          <p class="nota">Polinomio: <code>{{ polinomio(f) }}</code></p>
        }

        <div class="fila">
          <label>a (bisección y secante, x0) <input name="a" [ngModel]="a()" (ngModelChange)="a.set($event)" /></label>
          <label>b (bisección y secante, x1) <input name="b" [ngModel]="b()" (ngModelChange)="b.set($event)" /></label>
          <label>x0 de Newton <input name="x0" [ngModel]="x0()" (ngModelChange)="x0.set($event)" /></label>
          <label>Tolerancia <input name="tol" [ngModel]="tol()" (ngModelChange)="tol.set($event)" /></label>
          <label>Máx. iteraciones <input name="max" [ngModel]="maxIter()" (ngModelChange)="maxIter.set($event)" /></label>
        </div>

        <button type="submit" [disabled]="comparando() || seleccionada() === null">
          {{ comparando() ? 'Comparando…' : 'Comparar los tres métodos' }}
        </button>
      </form>

      @if (error()) {
        <div class="aviso error">{{ error() }}</div>
      }
    </section>

    @if (filas().length > 0) {
      <section class="tarjeta">
        <h2>Resultados</h2>
        <table>
          <thead>
            <tr>
              <th>Método</th><th>Estado</th><th>Raíz</th><th>Iteraciones</th><th>Nativo (ms)</th><th>Total (ms)</th>
            </tr>
          </thead>
          <tbody>
            @for (f of filas(); track f.metodo) {
              <tr>
                <td>{{ f.nombre }}</td>
                @if (f.resultado; as r) {
                  <td>{{ r.estado === 'CONVERGIO' ? 'Convergió' : 'No convergió' + (r.codigoCausa ? ' (' + r.codigoCausa + ')' : '') }}</td>
                  <td>{{ r.estado === 'CONVERGIO' ? numero(r.raiz) : '—' }}</td>
                  <td>{{ r.totalIteraciones }}</td>
                  <td>{{ numero(r.tiempoNativoMs) }}</td>
                  <td>{{ numero(f.totalMs) }}</td>
                } @else {
                  <td colspan="5" class="texto-error">{{ f.error }}</td>
                }
              </tr>
            }
          </tbody>
        </table>
        @if (mejor(); as m) {
          <p class="nota">Menos iteraciones: <b>{{ m.nombre }}</b> ({{ m.resultado?.totalIteraciones }}).</p>
        }
      </section>

      <section class="tarjeta">
        <h2>Caída del error</h2>
        <app-grafica-error [series]="series()" [tolerancia]="tolUsada()" />
      </section>
    }
  `,
  styles: `
    .texto-error { color: var(--error-texto); }
  `,
})
export class CompararPagina implements OnInit {
  private readonly funcionesApi = inject(FuncionesApi);
  private readonly calculo = inject(CalculoApi);
  private readonly historial = inject(HistorialApi);

  protected readonly funciones = signal<Funcion[]>([]);
  protected readonly funcionId = signal<number | null>(null);
  protected readonly a = signal('');
  protected readonly b = signal('');
  protected readonly x0 = signal('');
  protected readonly tol = signal('1e-6');
  protected readonly maxIter = signal('100');

  protected readonly comparando = signal(false);
  protected readonly error = signal('');
  protected readonly filas = signal<FilaComparacion[]>([]);
  protected readonly tolUsada = signal<number | null>(null);

  protected readonly seleccionada = computed(
    () => this.funciones().find((f) => f.id === this.funcionId()) ?? null,
  );

  protected readonly series = computed<SerieError[]>(() =>
    this.filas()
      .filter((f) => f.resultado !== null)
      .map((f) => ({ nombre: f.nombre, filas: f.resultado!.iteraciones })),
  );

  /** El método convergente que usó menos iteraciones. */
  protected readonly mejor = computed(() => {
    const convergentes = this.filas().filter((f) => f.resultado?.estado === 'CONVERGIO');
    if (convergentes.length === 0) {
      return null;
    }
    return convergentes.reduce((m, f) =>
      f.resultado!.totalIteraciones < m.resultado!.totalIteraciones ? f : m,
    );
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

  /** Se precargan el intervalo y la tolerancia de la función; Newton parte del punto medio. */
  protected elegirFuncion(id: number | null): void {
    this.funcionId.set(id);
    const f = this.funciones().find((x) => x.id === id);
    if (f) {
      this.a.set(String(f.intervaloA));
      this.b.set(String(f.intervaloB));
      this.x0.set(String((f.intervaloA + f.intervaloB) / 2));
      this.tol.set(String(f.toleranciaPorDefecto));
    }
    this.filas.set([]);
  }

  protected async comparar(): Promise<void> {
    const f = this.seleccionada();
    if (!f) {
      return;
    }
    this.error.set('');

    const a = Number(this.a());
    const b = Number(this.b());
    const x0 = Number(this.x0());
    const tol = Number(this.tol());
    const maxIter = Number(this.maxIter());
    if (![a, b, x0].every(Number.isFinite) || this.a().trim() === '' || this.b().trim() === '' || this.x0().trim() === '') {
      this.error.set('Los valores iniciales deben ser numéricos.');
      return;
    }
    if (!Number.isFinite(tol) || tol <= 0) {
      this.error.set('La tolerancia debe ser un número mayor que 0.');
      return;
    }
    if (!Number.isInteger(maxIter) || maxIter < 1) {
      this.error.set('El máximo de iteraciones debe ser un entero positivo.');
      return;
    }

    const parametros: Record<Metodo, Record<string, number>> = {
      biseccion: { a, b, tol, maxIter },
      newton: { x0, tol, maxIter },
      secante: { x0: a, x1: b, tol, maxIter },
    };

    this.comparando.set(true);
    this.tolUsada.set(tol);
    const acumuladas: FilaComparacion[] = [];
    this.filas.set([]);
    for (const metodo of ORDEN) {
      const inicio = performance.now();
      try {
        const r = await firstValueFrom(this.calculo.ejecutar(metodo, f.coeficientes, parametros[metodo]));
        const total = performance.now() - inicio;
        acumuladas.push({ metodo, nombre: NOMBRE_METODO[metodo], resultado: r, totalMs: total, error: '' });
        this.registrar(f.id, metodo, r, total);
      } catch (e) {
        acumuladas.push({ metodo, nombre: NOMBRE_METODO[metodo], resultado: null, totalMs: 0, error: leerError(e).mensaje });
      }
      this.filas.set([...acumuladas]);
    }
    this.comparando.set(false);
  }

  /** Cada corrida de la comparación también queda en el historial (HU-08). Si falla, no estorba a la comparación. */
  private registrar(funcionId: number, metodo: Metodo, r: ResultadoCalculo, total: number): void {
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
      .subscribe({ error: () => undefined });
  }
}
