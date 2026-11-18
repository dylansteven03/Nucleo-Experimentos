import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { NEVER, catchError, from, switchMap, throwError } from 'rxjs';
import { CONFIG } from '../config';
import { AuthService } from './auth.service';

/**
 * RA-03: el token viaja como Bearer en cada petición al backend, y SOLO al backend (no a otros dominios).
 * RA-07: si el backend responde 401 (token vencido justo en el camino), se vuelve al login una vez
 * en lugar de mostrar un error sin explicación.
 */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  if (!peticion.url.startsWith(CONFIG.apiUrl)) {
    return siguiente(peticion);
  }
  const auth = inject(AuthService);
  return from(auth.tokenVigente()).pipe(
    switchMap((token) => siguiente(peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } }))),
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && auth.reautenticar()) {
        return NEVER; // la página está yendo al login
      }
      return throwError(() => error);
    }),
  );
};
