import { Component, computed, input } from '@angular/core';
import { MARCO, construirModelo } from '../grafica';
import { SerieError } from '../models';

/**
 * HU-05: caída del error por iteración, en escala logarítmica (un método que converge bien dibuja una recta que baja).
 * Acepta una o varias curvas: con varias sirve para comparar métodos (HU-04). Es SVG puro, sin librerías.
 */
@Component({
  selector: 'app-grafica-error',
  template: `
    @if (modelo(); as m) {
      @if (m.vacio) {
        <p class="nota">No hay errores positivos para graficar.</p>
      } @else {
        <svg [attr.viewBox]="'0 0 ' + marco.ancho + ' ' + marco.alto" role="img"
             aria-label="Error por iteración en escala logarítmica" class="grafica">
          @for (t of m.ticksY; track t.etiqueta) {
            <line [attr.x1]="marco.izq" [attr.x2]="marco.ancho - marco.der" [attr.y1]="t.px" [attr.y2]="t.px" class="rejilla" />
            <text [attr.x]="marco.izq - 8" [attr.y]="t.px + 4" text-anchor="end" class="etiqueta">{{ t.etiqueta }}</text>
          }
          @for (t of m.ticksX; track t.etiqueta) {
            <text [attr.x]="t.px" [attr.y]="marco.alto - marco.aba + 18" text-anchor="middle" class="etiqueta">{{ t.etiqueta }}</text>
          }
          <line [attr.x1]="marco.izq" [attr.x2]="marco.izq" [attr.y1]="marco.arr" [attr.y2]="marco.alto - marco.aba" class="eje" />
          <line [attr.x1]="marco.izq" [attr.x2]="marco.ancho - marco.der" [attr.y1]="marco.alto - marco.aba"
                [attr.y2]="marco.alto - marco.aba" class="eje" />
          @if (m.lineaTolerancia !== null) {
            <line [attr.x1]="marco.izq" [attr.x2]="marco.ancho - marco.der" [attr.y1]="m.lineaTolerancia"
                  [attr.y2]="m.lineaTolerancia" class="tolerancia" />
            <text [attr.x]="marco.ancho - marco.der" [attr.y]="m.lineaTolerancia - 5" text-anchor="end" class="etiqueta">tolerancia</text>
          }
          @for (c of m.curvas; track c.nombre) {
            <path [attr.d]="c.ruta" fill="none" [attr.stroke]="c.color" stroke-width="2" />
            @for (p of c.puntos; track $index) {
              <circle [attr.cx]="p.cx" [attr.cy]="p.cy" r="3.5" [attr.fill]="c.color">
                <title>{{ c.nombre }} · iteración {{ p.x }} · error {{ p.y }}</title>
              </circle>
            }
          }
          <text [attr.x]="(marco.izq + marco.ancho - marco.der) / 2" [attr.y]="marco.alto - 6" text-anchor="middle" class="etiqueta">
            iteración
          </text>
        </svg>
        <div class="leyenda">
          @for (c of m.curvas; track c.nombre) {
            <span><i [style.background]="c.color"></i>{{ c.nombre }}</span>
          }
        </div>
        <p class="nota">Eje vertical en escala logarítmica. Las iteraciones con error 0 no se dibujan.</p>
      }
    }
  `,
  styles: `
    .grafica { width: 100%; height: auto; max-width: 720px; display: block; }
    .rejilla { stroke: var(--linea); stroke-width: 1; }
    .eje { stroke: var(--suave); stroke-width: 1; }
    .tolerancia { stroke: var(--error-texto); stroke-width: 1; stroke-dasharray: 5 4; }
    .etiqueta { fill: var(--suave); font-size: 11px; }
    .leyenda { display: flex; gap: 1rem; flex-wrap: wrap; font-size: .9rem; margin-top: .25rem; }
    .leyenda i { display: inline-block; width: .8rem; height: .8rem; border-radius: 50%; margin-right: .35rem; }
  `,
})
export class GraficaError {
  readonly series = input.required<SerieError[]>();
  readonly tolerancia = input<number | null>(null);

  protected readonly marco = MARCO;
  protected readonly modelo = computed(() => construirModelo(this.series(), this.tolerancia()));
}
