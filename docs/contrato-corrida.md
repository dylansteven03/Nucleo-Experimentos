# Contrato de la corrida

| Fase | Responsable | Fecha de vencimiento |
|---|---|---|
| 2 Contrato de la corrida | Valentina | 24 oct |

> **Estado: BORRADOR para revisión.** Lo revisan Valentina, Cristóbal (C) y Jonatan (Java/JNA).
> Los puntos marcados **[DECIDIR]** están abiertos y hay que cerrarlos antes de empezar la Fase 3.
> Cuando el contrato quede acordado, se congela: cambiarlo después requiere avisar en la bitácora.

## 1. Convenciones generales

- Formato JSON, campos en `camelCase`, fechas en ISO-8601 UTC (`2026-10-12T22:30:00Z`).
- Tiempos en milisegundos (`...Ms`), como número decimal.
- El JSON **nunca** contiene `NaN` ni `Infinity`. Un valor que no existe se envía como `null`.
- La no convergencia **no es un error**: responde `200` con `estado: "NO_CONVERGIO"`. Nunca `500`.
- Nombres de método: `BISECCION`, `NEWTON`, `SECANTE`.
- **Orden de coeficientes [DECIDIR]:** se propone `coeficientes[i]` = coeficiente de `x^i`
  (de menor a mayor grado), con longitud `grado + 1`. Ejemplo: `x³ − 2x − 5` → `[-5, -2, 0, 1]`.
  Es la misma convención que usa la biblioteca en C (`coef`, `grado`).

## 2. Entrada: `POST /corridas`

```json
{
  "funcionId": 12,
  "metodos": ["BISECCION", "NEWTON", "SECANTE"],
  "parametros": {
    "BISECCION": { "a": 0.0, "b": 5.0 },
    "NEWTON":    { "x0": 3.0 },
    "SECANTE":   { "x0": 1.0, "x1": 3.0 }
  },
  "tolerancia": 1e-10,
  "maxIteraciones": 100
}
```

| Campo | Obligatorio | Default | Regla |
|---|---|---|---|
| `funcionId` | sí | — | Debe existir en funcion-service, si no → `404`. |
| `metodos` | sí | — | Al menos uno, sin repetidos, solo valores válidos. |
| `parametros.BISECCION` | si se pide | — | `a` y `b`, con `a < b`. |
| `parametros.NEWTON` | si se pide | — | `x0`. |
| `parametros.SECANTE` | si se pide | — | `x0` y `x1`, distintos. |
| `tolerancia` | no | `1e-6` | Mayor que 0. |
| `maxIteraciones` | no | `100` | Entre 1 y 1000 **[DECIDIR el tope]**; define el tamaño del buffer en Java. |

Si algo de esto falla antes de calcular → `400` con un mensaje que diga qué campo está mal.

## 3. Salida: respuesta de la corrida

```json
{
  "runId": "b3f1c2e0-6a52-4d7e-9a3b-2d9f1d1c7a10",
  "usuarioSub": "7c1e...-sub-del-token",
  "fecha": "2026-10-12T22:30:00Z",
  "versionMotor": "nativo-1.0.0",
  "persistido": true,
  "codigoPersistencia": null,
  "funcionId": 12,
  "tolerancia": 1e-10,
  "maxIteraciones": 100,
  "resultados": [
    {
      "metodo": "NEWTON",
      "estado": "CONVERGIO",
      "codigoCausa": null,
      "raiz": 2.0,
      "iteraciones": [
        { "n": 1, "x": 2.1666666667, "fx": 0.6944444444, "error": 0.8333333333 },
        { "n": 2, "x": 2.0064102564, "fx": 0.0256821170, "error": 0.1602564103 }
      ],
      "tiempoNativoMs": 0.02,
      "tiempoTotalMs": 3.4
    },
    {
      "metodo": "BISECCION",
      "estado": "CONVERGIO",
      "codigoCausa": null,
      "raiz": 2.00000000004,
      "iteraciones": [
        { "n": 1, "x": 2.5, "fx": 2.25, "error": 2.5, "intervalo": { "a": 0.0, "b": 5.0 } },
        { "n": 2, "x": 1.25, "fx": -2.4375, "error": 1.25, "intervalo": { "a": 0.0, "b": 2.5 } }
      ],
      "tiempoNativoMs": 0.03,
      "tiempoTotalMs": 3.9
    },
    {
      "metodo": "SECANTE",
      "estado": "NO_CONVERGIO",
      "codigoCausa": "DIVISION_BY_ZERO",
      "raiz": null,
      "iteraciones": [],
      "tiempoNativoMs": 0.01,
      "tiempoTotalMs": 2.1
    }
  ]
}
```

(En el ejemplo se recortan las filas de `iteraciones`. En la respuesta real van todas.)

- `iteraciones[]` va **completo aun si no convergió**: trae las filas que C alcanzó a escribir
  antes de fallar (puede ser lista vacía).
- `intervalo` solo existe en bisección.
- `raiz` es `null` cuando `estado` es `NO_CONVERGIO`. Nunca se inventa una raíz.
- `tiempoNativoMs` es el tiempo dentro de la llamada a C; `tiempoTotalMs` incluye todo el servicio.
- `versionMotor` indica qué motor corrió: `nativo-<versión>` o `java-linea-base`.
- Si historial-service no responde: la corrida **igual se devuelve** con
  `persistido: false` y `codigoPersistencia: "PERSIST_TIMEOUT"` **[DECIDIR el nombre del campo]**.

## 4. Definiciones numéricas (para que C y Java den lo mismo)

- **Fila `n`:** empieza en 1. La fila `n` es la aproximación obtenida en el paso `n`.
  El punto inicial (`x0`) no es una fila.
- **Bisección:** `x` es el punto medio del intervalo actual y `error = (b − a) / 2` del intervalo actual.
- **Newton:** `x_n = x_{n-1} − f(x_{n-1}) / f'(x_{n-1})` y `error = |x_n − x_{n-1}|`.
  La derivada se calcula de forma analítica dentro de C.
- **Secante:** `x_n = x_{n-1} − f(x_{n-1})·(x_{n-1} − x_{n-2}) / (f(x_{n-1}) − f(x_{n-2}))` y `error = |x_n − x_{n-1}|`.
- **Criterio de parada:** converge cuando `error < tolerancia` o cuando `f(x) == 0` exacto.
  `raiz` es la `x` de la última fila.
- **Tope:** si se completan `maxIteraciones` filas sin converger → código 1.
- **Derivada nula:** se considera nula si `|f'(x)| < 1e-14` → código 2.

## 5. Mapeo de códigos de la biblioteca C → JSON

| Código C | Significado en C | `estado` | `codigoCausa` |
|---|---|---|---|
| 0 | Convergió | `CONVERGIO` | `null` |
| 1 | Tope de iteraciones | `NO_CONVERGIO` | `MAX_ITERATIONS_REACHED` |
| 2 | Derivada nula | `NO_CONVERGIO` | `ZERO_DERIVATIVE` |
| 3 | Desbordamiento o NaN | `NO_CONVERGIO` | `NUMERICAL_INSTABILITY` o `DIVISION_BY_ZERO` **[DECIDIR]** |
| 4 | Parámetros inválidos | `NO_CONVERGIO` | `DOMAIN_BOUNDS_EXCEEDED` **[DECIDIR]** |

**[DECIDIR] Código 3:** la API tiene 5 causas y C tiene 4 códigos de falla, así que uno se reparte.
Propuesta: `calculo-service` devuelve `DIVISION_BY_ZERO` cuando el método es `SECANTE` y
`f(x_{n-1}) == f(x_{n-2})` (denominador cero), y `NUMERICAL_INSTABILITY` en el resto de casos.
No hay que tocar la cabecera `numerico.h`, que está congelada.

**[DECIDIR] Código 4:** el caso típico es bisección sin cambio de signo (`f(a)·f(b) ≥ 0`).
Propuesta: se devuelve como `NO_CONVERGIO` con `DOMAIN_BOUNDS_EXCEEDED`, con respuesta `200`.
Los datos mal formados (tolerancia ≤ 0, listas vacías, etc.) se rechazan antes con `400` y no llegan a C.

## 6. Otros endpoints

| Endpoint | Para qué | Respuesta |
|---|---|---|
| `POST /corridas/validar` | Revisa **antes** de ejecutar si `f(a)·f(b) < 0` en bisección (RF-02). | `200` con `{ "valida": true/false, "fa": ..., "fb": ..., "mensaje": "..." }` |
| `GET /corridas/{runId}` | Reabre una corrida **sin recalcular**. | Mismo JSON de la sección 3, con `persistido: true`. |
| `GET /corridas` | Historial: docente ve todas; estudiante solo las suyas. | Lista paginada **[DECIDIR en Fase 5]**. |

Errores HTTP:

| Código | Cuándo |
|---|---|
| `200` | Corrida hecha, convergiera o no. |
| `400` | Entrada inválida (con mensaje claro). |
| `401` | Sin token o token con firma alterada. |
| `403` | Rol sin permiso (por ejemplo, estudiante pidiendo una corrida ajena). |
| `404` | `funcionId` o `runId` inexistente. |

## 7. Contrato interno: `calculo-service`

`calculo-service` **no tiene estado** y no conoce el catálogo. El gateway busca la función en
funcion-service y le manda los coeficientes explícitos:

```json
{
  "coeficientes": [-4, 0, 1],
  "metodo": "NEWTON",
  "parametros": { "x0": 3.0 },
  "tolerancia": 1e-10,
  "maxIteraciones": 100
}
```

La respuesta es un elemento de `resultados[]` de la sección 3. El gateway arma la corrida
completa y la guarda **de una sola vez** en historial-service.

## 8. Casos de referencia con valor esperado

Valores calculados con las definiciones de la sección 4. Para las pruebas en C y Java:

- `n` (cantidad de iteraciones) debe coincidir **exacto**.
- `raiz`, `x`, `fx`, `error`: comparar con tolerancia relativa `1e-9`.
- Mismos casos en C (`make test`) y en Java: la tabla de JNA debe coincidir fila por fila con la de Java.

Funciones usadas: `x² − 4` → `[-4, 0, 1]`; `x³ − 2x − 5` → `[-5, -2, 0, 1]`; `x² + 1` → `[1, 0, 1]`.
Salvo que se indique otra cosa: `tol = 1e-10` y `max_iter = 100`.

### Casos que convergen (código 0)

| # | Función | Método y parámetros | `n` | Raíz esperada | Primeras filas `(n, x, fx, error)` |
|---|---|---|---|---|---|
| 1 | `x² − 4` | Bisección `[0, 5]` | 36 | ≈ 2.00000000004 | (1, 2.5, 2.25, 2.5) · (2, 1.25, −2.4375, 1.25) · (3, 1.875, −0.484375, 0.625) · (4, 2.1875, 0.78515625, 0.3125). Error decrece monótonamente. |
| 2 | `x² − 4` | Newton `x0 = 3` | 5 | 2 | (1, 2.1666666667, 0.6944444444, 0.8333333333) · (2, 2.0064102564, 0.0256821170, 0.1602564103) · (3, 2.0000102400, 4.096e−5, 0.0064000164) |
| 3 | `x² − 4` | Secante `x0 = 1, x1 = 3` | 7 | 2 | (1, 1.75, −0.9375, 1.25) · (2, 1.9473684211, −0.2077562327, 0.1973684211) · (3, 2.0035587189, 0.0142475399, 0.0561902978) |
| 4 | `x³ − 2x − 5` | Newton `x0 = 2` | 5 | ≈ 2.0945514815 | (1, 2.1, 0.061, 0.1) · (2, 2.0945681211, 1.857e−4, 0.0054318789) |
| 5 | `x³ − 2x − 5` | Bisección `[2, 3]` | 34 | ≈ 2.0945514816 | (1, 2.5, 5.625, 0.5) · (2, 2.25, 1.890625, 0.25) · (3, 2.125, 0.345703125, 0.125) · (4, 2.0625, −0.3513183594, 0.0625) |
| 6 | `x³ − 2x − 5` | Secante `x0 = 2, x1 = 3` | 7 | ≈ 2.0945514815 | (1, 2.0588235294, −0.3907999186, 0.9411764706) · (2, 2.0812636600, −0.1472040596, 0.0224401304) |

### Casos que no convergen (un caso por cada código de falla)

| # | Función | Método y parámetros | Código C | `codigoCausa` | `n` al fallar |
|---|---|---|---|---|---|
| 7 | `x² − 4` | Bisección `[0, 5]`, `max_iter = 3` | 1 | `MAX_ITERATIONS_REACHED` | 3 (la última fila es `x = 1.875`) |
| 8 | `x² − 4` | Newton `x0 = 0` (derivada nula en el punto inicial) | 2 | `ZERO_DERIVATIVE` | 0 |
| 9 | `x² + 1` | Newton `x0 = 1` (no tiene raíz real; el primer paso cae en `x = 0`, donde `f' = 0`) | 2 | `ZERO_DERIVATIVE` | 1 (fila: `x = 0, fx = 1, error = 1`) |
| 10 | `x² − 4` | Secante `x0 = −1, x1 = 1` (`f(x0) = f(x1) = −3`) | 3 | `DIVISION_BY_ZERO` (ver [DECIDIR] de la sección 5) | 0 |
| 11 | `x² − 4` | Bisección `[3, 5]` (`f(3)` y `f(5)` positivos, sin cambio de signo) | 4 | `DOMAIN_BOUNDS_EXCEEDED` | 0 |

Los 11 casos cubren los 5 códigos de retorno (0 a 4) y superan el mínimo de 10 pruebas pedido para la
biblioteca en C.

## 9. Preguntas abiertas para el equipo

1. ¿Orden de coeficientes de menor a mayor grado? (sección 1)
2. ¿Tope de `maxIteraciones` en 1000? (sección 2)
3. ¿Nombre del campo `codigoPersistencia`? (sección 3)
4. ¿Cómo se reparte el código 3 entre `NUMERICAL_INSTABILITY` y `DIVISION_BY_ZERO`? (sección 5)
5. ¿El código 4 se devuelve como `200` con `DOMAIN_BOUNDS_EXCEEDED`? (sección 5)
6. ¿Formato del listado `GET /corridas`? (sección 6, Fase 5)

Cuando se resuelvan, borrar la marca **[DECIDIR]** y anotar cada decisión en `BITACORA.md`.
