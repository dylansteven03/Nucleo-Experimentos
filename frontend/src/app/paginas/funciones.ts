import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/auth.service';
import { FuncionesApi } from '../core/api.service';
import { ErrorCampo, Funcion } from '../models';
import { formatoPolinomio, leerError, parsearCoeficientes } from '../util';

/** HU-01: registrar una función (solo docente). Cualquier rol puede ver el catálogo. */
@Component({
  selector: 'app-funciones',
  imports: [FormsModule],
  template: `
    <h1>Funciones</h1>

    @if (auth.esDocente()) {
      <section class="tarjeta">
        <h2>Registrar una función</h2>
        <form (ngSubmit)="registrar()">
          <label>Nombre
            <input name="nombre" [ngModel]="nombre()" (ngModelChange)="nombre.set($event)" placeholder="x^2 - 4" />
          </label>
          <label>Coeficientes, de <b>menor a mayor</b> grado
            <input name="coef" [ngModel]="coeficientes()" (ngModelChange)="coeficientes.set($event)"
                   placeholder="-4, 0, 1" />
            <small>x^2 - 4 se escribe: -4, 0, 1. Grado máximo: 10 (11 coeficientes).</small>
          </label>
          <div class="fila">
            <label>Intervalo: inicio (a)
              <input name="a" [ngModel]="a()" (ngModelChange)="a.set($event)" placeholder="0" />
            </label>
            <label>Intervalo: fin (b)
              <input name="b" [ngModel]="b()" (ngModelChange)="b.set($event)" placeholder="5" />
            </label>
            <label>Tolerancia (opcional)
              <input name="tol" [ngModel]="tolerancia()" (ngModelChange)="tolerancia.set($event)"
                     placeholder="1e-6" />
            </label>
          </div>
          <button type="submit" [disabled]="enviando()">Guardar función</button>
        </form>

        @if (error()) {
          <div class="aviso error">
            {{ error() }}
            @if (detalles().length > 0) {
              <ul>
                @for (d of detalles(); track d.campo + d.mensaje) {
                  <li><b>{{ d.campo }}:</b> {{ d.mensaje }}</li>
                }
              </ul>
            }
          </div>
        }
        @if (exito()) {
          <div class="aviso ok">{{ exito() }}</div>
        }
      </section>
    }

    <section class="tarjeta">
      <h2>Catálogo</h2>
      @if (cargando()) {
        <p>Cargando…</p>
      } @else if (funciones().length === 0) {
        <p>Todavía no hay funciones registradas.</p>
      } @else {
        <table>
          <thead>
            <tr>
              <th>Nombre</th><th>Polinomio</th><th>Grado</th><th>Intervalo</th><th>Tolerancia</th>
              @if (auth.esDocente()) { <th></th> }
            </tr>
          </thead>
          <tbody>
            @for (f of funciones(); track f.id) {
              <tr>
                <td>{{ f.nombre }}</td>
                <td><code>{{ polinomio(f) }}</code></td>
                <td>{{ f.grado }}</td>
                <td>[{{ f.intervaloA }}, {{ f.intervaloB }}]</td>
                <td>{{ f.toleranciaPorDefecto }}</td>
                @if (auth.esDocente()) {
                  <td><button type="button" class="peligro" (click)="eliminar(f)">Eliminar</button></td>
                }
              </tr>
            }
          </tbody>
        </table>
      }
      @if (errorLista()) {
        <div class="aviso error">{{ errorLista() }}</div>
      }
    </section>
  `,
})
export class FuncionesPagina implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(FuncionesApi);

  protected readonly funciones = signal<Funcion[]>([]);
  protected readonly cargando = signal(true);
  protected readonly errorLista = signal('');

  protected readonly nombre = signal('');
  protected readonly coeficientes = signal('');
  protected readonly a = signal('');
  protected readonly b = signal('');
  protected readonly tolerancia = signal('');
  protected readonly enviando = signal(false);
  protected readonly error = signal('');
  protected readonly detalles = signal<ErrorCampo[]>([]);
  protected readonly exito = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  protected polinomio(f: Funcion): string {
    return formatoPolinomio(f.coeficientes);
  }

  private cargar(): void {
    this.cargando.set(true);
    this.api.listar().subscribe({
      next: (lista) => {
        this.funciones.set(lista);
        this.cargando.set(false);
        this.errorLista.set('');
      },
      error: (e) => {
        this.cargando.set(false);
        this.errorLista.set(leerError(e).mensaje);
      },
    });
  }

  protected registrar(): void {
    this.error.set('');
    this.detalles.set([]);
    this.exito.set('');

    const leidos = parsearCoeficientes(this.coeficientes());
    if (leidos.error) {
      this.error.set(leidos.error);
      return;
    }
    const a = Number(this.a());
    const b = Number(this.b());
    if (this.a().trim() === '' || this.b().trim() === '' || !Number.isFinite(a) || !Number.isFinite(b)) {
      this.error.set('El intervalo (a y b) debe ser numérico.');
      return;
    }
    let tolerancia: number | null = null;
    if (this.tolerancia().trim() !== '') {
      tolerancia = Number(this.tolerancia());
      if (!Number.isFinite(tolerancia)) {
        this.error.set('La tolerancia debe ser numérica.');
        return;
      }
    }

    this.enviando.set(true);
    this.api
      .registrar({
        nombre: this.nombre(),
        coeficientes: leidos.valores,
        intervaloA: a,
        intervaloB: b,
        toleranciaPorDefecto: tolerancia,
      })
      .subscribe({
        next: (creada) => {
          this.enviando.set(false);
          this.exito.set(`Función "${creada.nombre}" guardada (grado ${creada.grado}).`);
          this.nombre.set('');
          this.coeficientes.set('');
          this.a.set('');
          this.b.set('');
          this.tolerancia.set('');
          this.cargar();
        },
        error: (e) => {
          this.enviando.set(false);
          const err = leerError(e);
          this.error.set(err.mensaje);
          this.detalles.set(err.detalles);
        },
      });
  }

  protected eliminar(f: Funcion): void {
    if (!window.confirm(`¿Eliminar la función "${f.nombre}"?`)) {
      return;
    }
    this.api.eliminar(f.id).subscribe({
      next: () => this.cargar(),
      error: (e) => this.errorLista.set(leerError(e).mensaje),
    });
  }
}
