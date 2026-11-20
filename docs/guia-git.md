# Guía básica de Git para el equipo

Esta guía es de resumen para aportar cambios al proyecto.

## Flujo de trabajo

1. Cambiarse a develop: `git checkout develop`
2. Traer los cambios nuevos: `git pull origin develop`
3. Crear una rama propia: `git checkout -b feature/nombre-cambio`
4. Hacer los cambios en el editor.
5. Revisar los cambios: `git status`
6. Agregar los cambios: `git add .`
7. Crear el commit: `git commit -m "feat: descripcion del cambio"`
8. Subir la rama: `git push origin feature/nombre-cambio`
9. Abrir un Pull Request hacia `develop` en GitHub.

## Convención de mensajes de commit

- `feat:` nueva funcionalidad
- `fix:` corrección de un error
- `docs:` cambios en documentación

## Recomendaciones

- No trabajar directamente en `develop`.
- Hacer `git pull` antes de empezar a trabajar.
- Hacer commits pequeños y con mensajes claros.

## Después de fusionar el Pull Request

Una vez que el Pull Request haya sido revisado y fusionado:

1. Cambiarse a la rama develop: `git checkout develop`
2. Actualizar los cambios del repositorio: `git pull origin develop`
3. Eliminar la rama local que ya fue fusionada: `git branch -d feature/nombre-cambio`
4. Comprobar que el repositorio esté actualizado: `git status`

## Buenas prácticas adicionales

- Crear una rama diferente para cada aporte.
- Revisar los cambios antes de realizar un commit.
- Solicitar la revisión de otro integrante antes de fusionar un Pull Request.
- No eliminar ramas que todavía tengan cambios pendientes de integrar.