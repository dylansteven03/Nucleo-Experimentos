import { describe, expect, it } from 'vitest';
import { columnaError, columnaIteracion, construirModelo, puntosDeError } from './grafica';
import { FilaIteracion } from './models';

const filas: FilaIteracion[] = [
  { iteracion: 1, a: 0, b: 5, c: 2.5, fc: 2.25, error: 2.5 },
  { iteracion: 2, a: 0, b: 2.5, c: 1.25, fc: -2.4375, error: 1.25 },
  { iteracion: 3, a: 1.25, b: 2.5, c: 1.875, fc: -0.484, error: 0.625 },
];

describe('columnas', () => {
  it('encuentra la columna del error y la de la iteración por nombre', () => {
    expect(columnaError(filas)).toBe('error');
    expect(columnaIteracion(filas)).toBe('iteracion');
  });

  it('acepta variantes del nombre', () => {
    expect(columnaError([{ k: 1, errAbs: 0.1 }])).toBe('errAbs');
    expect(columnaIteracion([{ k: 1, errAbs: 0.1 }])).toBe('k');
  });

  it('sin filas o sin columna de error, no hay columna', () => {
    expect(columnaError([])).toBeNull();
    expect(columnaError([{ iteracion: 1, x: 2 }])).toBeNull();
  });
});

describe('puntosDeError', () => {
  it('toma (iteración, error)', () => {
    expect(puntosDeError(filas)).toEqual([
      { x: 1, y: 2.5 },
      { x: 2, y: 1.25 },
      { x: 3, y: 0.625 },
    ]);
  });

  it('descarta errores 0, negativos o nulos: la escala logarítmica no los admite', () => {
    const r = puntosDeError([
      { iteracion: 1, error: 0.5 },
      { iteracion: 2, error: 0 },
      { iteracion: 3, error: null },
      { iteracion: 4, error: -1 },
    ]);
    expect(r).toEqual([{ x: 1, y: 0.5 }]);
  });

  it('si no hay columna de iteración, numera desde 1', () => {
    expect(puntosDeError([{ error: 1 }, { error: 0.1 }]).map((p) => p.x)).toEqual([1, 2]);
  });
});

describe('construirModelo', () => {
  it('sin datos queda vacío', () => {
    expect(construirModelo([], null).vacio).toBe(true);
    expect(construirModelo([{ nombre: 'x', filas: [{ iteracion: 1, error: 0 }] }], null).vacio).toBe(true);
  });

  it('dibuja una curva con un punto por iteración y los errores grandes arriba', () => {
    const m = construirModelo([{ nombre: 'Bisección', filas }], null);
    expect(m.vacio).toBe(false);
    expect(m.curvas).toHaveLength(1);
    const [p1, p2, p3] = m.curvas[0].puntos;
    expect(p1.cy).toBeLessThan(p2.cy);
    expect(p2.cy).toBeLessThan(p3.cy);
    expect(p1.cx).toBeLessThan(p3.cx);
    expect(m.curvas[0].ruta.startsWith('M')).toBe(true);
  });

  it('dos series conservan sus nombres y usan colores distintos', () => {
    const m = construirModelo(
      [
        { nombre: 'Bisección', filas },
        { nombre: 'Newton', filas: [{ iteracion: 1, error: 1 }, { iteracion: 2, error: 1e-6 }] },
      ],
      null,
    );
    expect(m.curvas.map((c) => c.nombre)).toEqual(['Bisección', 'Newton']);
    expect(m.curvas[0].color).not.toBe(m.curvas[1].color);
  });

  it('la tolerancia ensancha la escala y aparece como línea', () => {
    const sin = construirModelo([{ nombre: 'B', filas }], null);
    const con = construirModelo([{ nombre: 'B', filas }], 1e-10);
    expect(sin.lineaTolerancia).toBeNull();
    expect(con.lineaTolerancia).not.toBeNull();
    expect(con.ticksY.length).toBeGreaterThan(sin.ticksY.length);
  });

  it('una sola iteración no rompe la escala', () => {
    const m = construirModelo([{ nombre: 'N', filas: [{ iteracion: 1, error: 0.3 }] }], null);
    expect(m.vacio).toBe(false);
    expect(Number.isFinite(m.curvas[0].puntos[0].cx)).toBe(true);
    expect(Number.isFinite(m.curvas[0].puntos[0].cy)).toBe(true);
  });
});
