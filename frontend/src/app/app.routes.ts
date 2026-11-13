import { Routes } from '@angular/router';
import { EjecutarPagina } from './paginas/ejecutar';
import { FuncionesPagina } from './paginas/funciones';
import { HistorialPagina } from './paginas/historial';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'funciones' },
  { path: 'funciones', component: FuncionesPagina },
  { path: 'ejecutar', component: EjecutarPagina },
  { path: 'historial', component: HistorialPagina },
  { path: '**', redirectTo: 'funciones' },
];
