import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { CONFIG } from '../config';
import { AuthService } from './auth.service';

/** RA-03: el token viaja como Bearer en cada petición al backend, y SOLO al backend (no a otros dominios). */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  if (!peticion.url.startsWith(CONFIG.apiUrl)) {
    return siguiente(peticion);
  }
  const auth = inject(AuthService);
  return from(auth.tokenVigente()).pipe(
    switchMap((token) => siguiente(peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } }))),
  );
};
