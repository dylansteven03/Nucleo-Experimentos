import { HttpErrorResponse } from '@angular/common/http';
import { ErrorApi, ErrorCampo } from './models';

export interface CoeficientesLeidos {
  valores: number[];
  error: string | null;
}

/** Lee "-4, 0, 1" (de menor a mayor grado). Acepta comas, punto y coma o espacios. */
export function parsearCoeficientes(texto: string): CoeficientesLeidos {
  const partes = texto.split(/[,;\s]+/).filter((p) => p.length > 0);
  if (partes.length === 0) {
    return { valores: [], error: 'Escriba al menos un coeficiente' };
  }
  const valores: number[] = [];
  for (const parte of partes) {
    const numero = Number(parte);
    if (!Number.isFinite(numero)) {
      return { valores: [], error: `"${parte}" no es un número` };
    }
    valores.push(numero);
  }
  return { valores, error: null };
}

/** [-4, 0, 1] -> "x^2 - 4" */
export function formatoPolinomio(coeficientes: number[]): string {
  let texto = '';
  for (let grado = coeficientes.length - 1; grado >= 0; grado--) {
    const c = coeficientes[grado];
    if (c === 0) {
      continue;
    }
    const signo = c < 0 ? '-' : '+';
    const magnitud = Math.abs(c);
    let termino: string;
    if (grado === 0) {
      termino = String(magnitud);
    } else {
      const coef = magnitud === 1 ? '' : String(magnitud);
      const variable = grado === 1 ? 'x' : `x^${grado}`;
      termino = coef + variable;
    }
    if (texto === '') {
      texto = (signo === '-' ? '-' : '') + termino;
    } else {
      texto += ` ${signo} ${termino}`;
    }
  }
  return texto === '' ? '0' : texto;
}

/** Número legible: 10 cifras significativas y sin ceros de relleno. */
export function formatoNumero(valor: number | string | null): string {
  if (valor === null || valor === undefined) {
    return '';
  }
  if (typeof valor === 'number') {
    return String(Number(valor.toPrecision(10)));
  }
  return valor;
}

export interface ErrorLegible {
  mensaje: string;
  detalles: ErrorCampo[];
}

/** Traduce un error HTTP en algo que se pueda mostrar a una persona. */
export function leerError(e: unknown): ErrorLegible {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) {
      return { mensaje: 'No hay conexión con el servidor. ¿Está arriba el api-gateway?', detalles: [] };
    }
    if (e.status === 401) {
      return { mensaje: 'La sesión expiró o el token no es válido. Vuelva a iniciar sesión.', detalles: [] };
    }
    if (e.status === 403) {
      return { mensaje: 'Su rol no tiene permiso para esta acción.', detalles: [] };
    }
    const cuerpo = e.error as Partial<ErrorApi> | null;
    if (cuerpo && typeof cuerpo === 'object' && cuerpo.mensaje) {
      return { mensaje: cuerpo.mensaje, detalles: cuerpo.detalles ?? [] };
    }
    return { mensaje: `Error ${e.status} del servidor.`, detalles: [] };
  }
  return { mensaje: 'Ocurrió un error inesperado.', detalles: [] };
}
