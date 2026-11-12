import { Injectable, computed, signal } from '@angular/core';
import Keycloak from 'keycloak-js';
import { CONFIG } from '../config';

/**
 * Inicio de sesión con Keycloak por Authorization Code + PKCE (RA-02): cliente público, sin secreto.
 * Los roles salen del claim anidado realm_access.roles del token.
 * OJO: ocultar botones según el rol es solo comodidad; quien decide es el backend (403).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = new Keycloak({
    url: CONFIG.keycloakUrl,
    realm: CONFIG.realm,
    clientId: CONFIG.clientId,
  });

  readonly usuario = signal('');
  readonly roles = signal<string[]>([]);
  readonly esDocente = computed(() => this.roles().includes('docente'));
  readonly rolVisible = computed(() => (this.esDocente() ? 'docente' : 'estudiante'));

  /** Se ejecuta antes de que arranque la aplicación. Sin sesión, redirige al login de Keycloak. */
  async iniciar(): Promise<void> {
    await this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });
    const token = this.keycloak.tokenParsed;
    this.usuario.set((token?.['preferred_username'] as string | undefined) ?? '');
    this.roles.set(token?.realm_access?.roles ?? []);
  }

  /** Devuelve un token vigente: lo renueva si le quedan menos de 30 s (RA-07). Si no puede, pide login de nuevo. */
  async tokenVigente(): Promise<string> {
    try {
      await this.keycloak.updateToken(30);
    } catch {
      await this.keycloak.login();
    }
    return this.keycloak.token ?? '';
  }

  salir(): Promise<void> {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
