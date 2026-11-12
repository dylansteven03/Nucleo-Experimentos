export type Metodo = 'biseccion' | 'newton' | 'secante';

/** Coeficientes de MENOR a MAYOR grado: x^2 - 4  ->  [-4, 0, 1] (contrato con el backend y la biblioteca en C). */
export interface Funcion {
  id: number;
  nombre: string;
  grado: number;
  coeficientes: number[];
  intervaloA: number;
  intervaloB: number;
  toleranciaPorDefecto: number;
  creadoPor: string;
  creadoEn: string;
}

export interface FuncionRequest {
  nombre: string;
  coeficientes: number[];
  intervaloA: number;
  intervaloB: number;
  toleranciaPorDefecto: number | null;
}

/** Cada fila de la tabla de iteraciones. Las columnas las define el backend, por eso es un diccionario. */
export type FilaIteracion = Record<string, number | string | null>;

export interface ResultadoCalculo {
  metodo: string;
  estado: string; // CONVERGIO | NO_CONVERGIO
  codigoCausa: string | null;
  codigoNativo: number;
  raiz: number | null;
  totalIteraciones: number;
  iteraciones: FilaIteracion[];
  tiempoNativoMs: number;
}

export interface CorridaRequest {
  funcionId: number;
  metodo: string;
  convergio: boolean;
  raiz: number | null;
  iteraciones: number;
  tiempoNativoMs: number;
  tiempoTotalMs: number;
}

export interface Corrida extends CorridaRequest {
  id: number;
  usuario: string;
  creadoEn: string;
}

export interface ErrorCampo {
  campo: string;
  mensaje: string;
}

/** Formato de error de TODOS los servicios. */
export interface ErrorApi {
  estado: number;
  error: string;
  mensaje: string;
  detalles: ErrorCampo[];
}
