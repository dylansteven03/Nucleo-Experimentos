import { FilaIteracion, SerieError } from './models';

export interface PuntoError {
  x: number;
  y: number;
}

/** La columna del error se busca por nombre (empieza por "err"): el backend define las columnas. */
export function columnaError(filas: FilaIteracion[]): string | null {
  if (filas.length === 0) {
    return null;
  }
  const claves = Object.keys(filas[0]);
  return claves.find((k) => /^err/i.test(k)) ?? claves.find((k) => /err/i.test(k)) ?? null;
}

export function columnaIteracion(filas: FilaIteracion[]): string | null {
  if (filas.length === 0) {
    return null;
  }
  const claves = Object.keys(filas[0]);
  return claves.find((k) => /^iter/i.test(k)) ?? claves.find((k) => /^(n|k|i)$/i.test(k)) ?? null;
}

/**
 * Puntos (iteración, error) que se pueden dibujar. La escala es logarítmica, así que se descartan los
 * errores que no son positivos y finitos (por ejemplo, un error exactamente 0 en la última iteración).
 */
export function puntosDeError(filas: FilaIteracion[]): PuntoError[] {
  const columnaE = columnaError(filas);
  if (!columnaE) {
    return [];
  }
  const columnaI = columnaIteracion(filas);
  const puntos: PuntoError[] = [];
  filas.forEach((fila, indice) => {
    const y = Number(fila[columnaE]);
    const bruto = columnaI ? Number(fila[columnaI]) : Number.NaN;
    const x = Number.isFinite(bruto) ? bruto : indice + 1;
    if (Number.isFinite(y) && y > 0) {
      puntos.push({ x, y });
    }
  });
  return puntos;
}

export const COLORES_GRAFICA = ['#1b5e8c', '#c2571a', '#2e8b57', '#7b3fa0'];

export const MARCO = { ancho: 680, alto: 320, izq: 64, der: 20, arr: 16, aba: 44 };

export interface PuntoDibujado {
  cx: number;
  cy: number;
  x: number;
  y: number;
}

export interface CurvaDibujada {
  nombre: string;
  color: string;
  ruta: string;
  puntos: PuntoDibujado[];
}

export interface ModeloGrafica {
  vacio: boolean;
  curvas: CurvaDibujada[];
  ticksY: { px: number; etiqueta: string }[];
  ticksX: { px: number; etiqueta: string }[];
  lineaTolerancia: number | null;
}

const MODELO_VACIO: ModeloGrafica = { vacio: true, curvas: [], ticksY: [], ticksX: [], lineaTolerancia: null };

const redondear = (n: number): string => n.toFixed(1);

/** Calcula, sin tocar el DOM, todo lo que hace falta para dibujar el error por iteración en escala log. */
export function construirModelo(series: SerieError[], tolerancia: number | null): ModeloGrafica {
  const conPuntos = series.map((s) => ({ nombre: s.nombre, puntos: puntosDeError(s.filas) }));
  const todos = conPuntos.flatMap((s) => s.puntos);
  if (todos.length === 0) {
    return MODELO_VACIO;
  }

  const ys = todos.map((p) => p.y);
  let minE = Math.floor(Math.log10(Math.min(...ys)));
  let maxE = Math.ceil(Math.log10(Math.max(...ys)));
  const hayTolerancia = tolerancia !== null && Number.isFinite(tolerancia) && tolerancia > 0;
  if (hayTolerancia) {
    minE = Math.min(minE, Math.floor(Math.log10(tolerancia)));
    maxE = Math.max(maxE, Math.ceil(Math.log10(tolerancia)));
  }
  if (maxE === minE) {
    maxE = minE + 1;
  }

  const xs = todos.map((p) => p.x);
  const minX = Math.min(...xs);
  let maxX = Math.max(...xs);
  if (maxX === minX) {
    maxX = minX + 1;
  }

  const anchoUtil = MARCO.ancho - MARCO.izq - MARCO.der;
  const altoUtil = MARCO.alto - MARCO.arr - MARCO.aba;
  const px = (x: number) => MARCO.izq + ((x - minX) / (maxX - minX)) * anchoUtil;
  const py = (y: number) => MARCO.arr + ((maxE - Math.log10(y)) / (maxE - minE)) * altoUtil;

  const curvas: CurvaDibujada[] = conPuntos
    .filter((s) => s.puntos.length > 0)
    .map((s, indice) => {
      const puntos = s.puntos.map((p) => ({ cx: px(p.x), cy: py(p.y), x: p.x, y: p.y }));
      const ruta = puntos.map((p, i) => `${i === 0 ? 'M' : 'L'}${redondear(p.cx)} ${redondear(p.cy)}`).join(' ');
      return { nombre: s.nombre, color: COLORES_GRAFICA[indice % COLORES_GRAFICA.length], ruta, puntos };
    });

  const ticksY: { px: number; etiqueta: string }[] = [];
  const pasoY = Math.max(1, Math.ceil((maxE - minE + 1) / 8));
  for (let e = minE; e <= maxE; e += pasoY) {
    ticksY.push({ px: py(Math.pow(10, e)), etiqueta: `1e${e}` });
  }

  const ticksX: { px: number; etiqueta: string }[] = [];
  const pasoX = Math.max(1, Math.ceil((maxX - minX + 1) / 10));
  for (let x = Math.ceil(minX); x <= maxX; x += pasoX) {
    ticksX.push({ px: px(x), etiqueta: String(x) });
  }

  return { vacio: false, curvas, ticksY, ticksX, lineaTolerancia: hayTolerancia ? py(tolerancia) : null };
}
