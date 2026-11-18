import { Injectable, computed, signal } from '@angular/core';
import Keycloak from 'keycloak-js';
import { CONFIG } from '../config';

/** Para no entrar en un bucle de redirecciones si el 401 no se debe a un token vencido. */
const CLAVE_REINICIO = 'nucleo.ultimoReinicioDeSesion';
const ESPERA_ENTRE_REINICIOS_MS = 15_000;

/**
 * Inicio de sesión con Keycloak por Authorization Code + PKCE (RA-02): cliente público, sin secreto.
 * Los roles salen del claim anidado realm_access.roles del token.
 * OJO: ocultar botones según el rol es solo comodidad; quien decide es el backend (403).
 *
 * Expiración y cierre de sesión (RA-07):
 * - El token se renueva solo cuando le quedan menos de 30 s (tokenVigente). Si no se puede renovar,
 *   se vuelve al login de Keycloak en lugar de mostrar un error.
 * - Salir llama al cierre de sesión de Keycloak (invalida la sesión allá, no solo en el navegador).
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
    // Si Keycloak no consigue renovar (refresh token vencido, sesión cerrada desde otro lado),
    // se lleva a la persona al login en vez de dejarla con una pantalla que falla.
    this.keycloak.onAuthRefreshError = () => {
      void this.keycloak.login();
    };

    await this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });
    const token = this.keycloak.tokenParsed;
    this.usuario.set((token?.['preferred_username'] as string | undefined) ?? '');
    this.roles.set(token?.realm_access?.roles ?? []);

    // Al volver con el botón «atrás» el navegador puede mostrar la página desde su memoria (bfcache)
    // sin ejecutarla otra vez. Se fuerza la recarga para que se vuelva a comprobar la sesión.
    window.addEventListener('pageshow', (evento) => {
      if (evento.persisted) {
        window.location.reload();
      }
    });
  }

  /** Devuelve un token vigente: lo renueva si le quedan menos de 30 s (RA-07). Si no puede, pide login de nuevo. */
  async tokenVigente(): Promise<string> {
    try {
      await this.keycloak.updateToken(30);
    } catch {
      await this.keycloak.login();
      // La página se está redirigiendo: la petición no debe salir con un token vacío ni mostrar un error.
      return new Promise<string>(() => undefined);
    }
    return this.keycloak.token ?? '';
  }

  /**
   * Para un 401 inesperado del backend: vuelve al login una vez. Devuelve false si ya se hizo hace
   * poco (para no entrar en un bucle) y entonces el error debe mostrarse normalmente.
   */
  reautenticar(): boolean {
    const ahora = Date.now();
    try {
      const ultimo = Number(sessionStorage.getItem(CLAVE_REINICIO) ?? 0);
      if (ahora - ultimo < ESPERA_ENTRE_REINICIOS_MS) {
        return false;
      }
      sessionStorage.setItem(CLAVE_REINICIO, String(ahora));
    } catch {
      // Sin sessionStorage no se puede proteger del bucle: mejor no redirigir.
      return false;
    }
    void this.keycloak.login();
    return true;
  }

  salir(): Promise<void> {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
