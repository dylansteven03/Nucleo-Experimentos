import { Routes } from '@angular/router';
import { CompararPagina } from './paginas/comparar';
import { EjecutarPagina } from './paginas/ejecutar';
import { FuncionesPagina } from './paginas/funciones';
import { HistorialPagina } from './paginas/historial';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'funciones' },
  { path: 'funciones', component: FuncionesPagina },
  { path: 'ejecutar', component: EjecutarPagina },
  { path: 'comparar', component: CompararPagina },
  { path: 'historial', component: HistorialPagina },
  { path: '**', redirectTo: 'funciones' },
];
