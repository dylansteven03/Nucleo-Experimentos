import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../core/auth.service';
import { HistorialApi } from '../core/api.service';
import { Corrida } from '../models';
import { formatoNumero, leerError } from '../util';

/** HU-07: el estudiante ve solo lo suyo; el docente puede ver también todo el historial. */
@Component({
  selector: 'app-historial',
  imports: [DatePipe],
  template: `
    <h1>Historial de corridas</h1>

    <section class="tarjeta">
      <div class="pestanas">
        <button type="button" [class.activa]="!verTodo()" (click)="elegir(false)">Mi historial</button>
        @if (auth.esDocente()) {
          <button type="button" [class.activa]="verTodo()" (click)="elegir(true)">Todo el historial</button>
        }
      </div>

      @if (cargando()) {
        <p>Cargando…</p>
      } @else if (corridas().length === 0) {
        <p>No hay corridas todavía.</p>
      } @else {
        <table>
          <thead>
            <tr>
              <th>Fecha</th><th>Método</th><th>Función</th><th>Convergió</th><th>Raíz</th>
              <th>Iter.</th><th>Nativo (ms)</th><th>Total (ms)</th>
              @if (verTodo()) { <th>Usuario</th> }
            </tr>
          </thead>
          <tbody>
            @for (c of corridas(); track c.id) {
              <tr>
                <td>{{ c.creadoEn | date: 'dd/MM/yyyy HH:mm:ss' }}</td>
                <td>{{ c.metodo }}</td>
                <td>#{{ c.funcionId }}</td>
                <td>{{ c.convergio ? 'Sí' : 'No' }}</td>
                <td>{{ numero(c.raiz) }}</td>
                <td>{{ c.iteraciones }}</td>
                <td>{{ numero(c.tiempoNativoMs) }}</td>
                <td>{{ numero(c.tiempoTotalMs) }}</td>
                @if (verTodo()) { <td><small>{{ c.usuario }}</small></td> }
              </tr>
            }
          </tbody>
        </table>
      }
      @if (error()) {
        <div class="aviso error">{{ error() }}</div>
      }
    </section>
  `,
})
export class HistorialPagina implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(HistorialApi);

  protected readonly corridas = signal<Corrida[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly verTodo = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  protected numero(valor: number | null): string {
    return formatoNumero(valor);
  }

  protected elegir(todo: boolean): void {
    this.verTodo.set(todo);
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    const consulta = this.verTodo() ? this.api.todo() : this.api.propio();
    consulta.subscribe({
      next: (lista) => {
        this.corridas.set(lista);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(leerError(e).mensaje);
      },
    });
  }
}
