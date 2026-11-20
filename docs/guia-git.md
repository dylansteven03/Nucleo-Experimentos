# Guía básica de Git para el equipo

Esta guía es de resumen  para aportar cambios al proyecto 

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