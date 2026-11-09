# Entradas nuevas para BITACORA.md (agregar a la tabla)

Completen la columna "Quién" con quien tomó la decisión. No pongan a alguien que no participó.

| Fecha | Decisión | Alternativas descartadas | Justificada contra | Quién |
|---|---|---|---|---|
| 2026-11-06 | La autorización se valida en cada servicio (resource server con JWT), además del gateway | Validar solo en el gateway y dejar los servicios internos abiertos | RA-04: cada servicio debe poder defenderse solo si alguien llega a la red interna | — |
| 2026-11-06 | Los roles se leen del claim anidado `realm_access.roles` con un convertidor propio (`RolesRealmConverter`) | Declarar en Keycloak un mapper que publique los roles en el primer nivel del token | Un mapper deja la regla dentro de la configuración de Keycloak y es más difícil de probar; el convertidor se prueba con un test unitario | — |
| 2026-11-06 | El token debe traer la audiencia `numerico-api` (mapper en el cliente `numerico-web`) y cada servicio la comprueba | Aceptar cualquier token firmado por el realm | RA-04: rechazar un token "con destinatario que no le corresponde" | — |
| 2026-11-06 | Las funciones guardan `creadoPor` = claim `sub` del token | Guardar el nombre de usuario visible | RA-06: el `sub` es estable; el nombre puede cambiar | — |
| 2026-11-06 | Los coeficientes se guardan de menor a mayor grado, como el contrato de la corrida y la biblioteca en C | Mayor a menor, como el documento del profesor | Evitar invertir en cada llamada a C. **Pendiente confirmar con el profesor** y avisar a Brian (formulario de HU-01) | — |
