import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CONFIG } from '../config';
import { Corrida, CorridaRequest, Funcion, FuncionRequest, Metodo, ResultadoCalculo } from '../models';

@Injectable({ providedIn: 'root' })
export class FuncionesApi {
  private readonly http = inject(HttpClient);

  listar(): Observable<Funcion[]> {
    return this.http.get<Funcion[]>(`${CONFIG.apiUrl}/funciones`);
  }

  registrar(peticion: FuncionRequest): Observable<Funcion> {
    return this.http.post<Funcion>(`${CONFIG.apiUrl}/funciones`, peticion);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${CONFIG.apiUrl}/funciones/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class CalculoApi {
  private readonly http = inject(HttpClient);

  /** GET /calculo/{metodo}. Los coeficientes van de menor a mayor grado. */
  ejecutar(
    metodo: Metodo,
    coeficientes: number[],
    parametros: Record<string, number>,
  ): Observable<ResultadoCalculo> {
    let params = new HttpParams().set('coef', coeficientes.join(','));
    for (const [nombre, valor] of Object.entries(parametros)) {
      params = params.set(nombre, String(valor));
    }
    return this.http.get<ResultadoCalculo>(`${CONFIG.apiUrl}/calculo/${metodo}`, { params });
  }
}

@Injectable({ providedIn: 'root' })
export class HistorialApi {
  private readonly http = inject(HttpClient);

  registrar(corrida: CorridaRequest): Observable<Corrida> {
    return this.http.post<Corrida>(`${CONFIG.apiUrl}/historial`, corrida);
  }

  /** Solo lo propio: el backend filtra por el "sub" del token. */
  propio(): Observable<Corrida[]> {
    return this.http.get<Corrida[]>(`${CONFIG.apiUrl}/historial/mio`);
  }

  /** Todo el historial: solo el docente (si no, 403). */
  todo(): Observable<Corrida[]> {
    return this.http.get<Corrida[]>(`${CONFIG.apiUrl}/historial`);
  }
}
