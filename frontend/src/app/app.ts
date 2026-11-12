import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="barra">
      <span class="marca">Núcleo <small>banco de métodos numéricos</small></span>
      <nav>
        <a routerLink="/funciones" routerLinkActive="activo">Funciones</a>
        <a routerLink="/ejecutar" routerLinkActive="activo">Ejecutar</a>
        <a routerLink="/historial" routerLinkActive="activo">Historial</a>
      </nav>
      <span class="sesion">
        {{ auth.usuario() }} <span class="rol">{{ auth.rolVisible() }}</span>
        <button type="button" (click)="auth.salir()">Salir</button>
      </span>
    </header>
    <main class="contenido">
      <router-outlet />
    </main>
  `,
})
export class App {
  protected readonly auth = inject(AuthService);
}
