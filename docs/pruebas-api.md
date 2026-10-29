# Pruebas de la API de Oracle Service Cloud

Registro de las pruebas que hice en Postman para entender la API antes de construir la aplicación.

- Entorno: `https://imaginecx--tst2.custhelp.com`
- Versión de la API: `v1.4` (ruta base `/services/rest/connect/v1.4`, abreviada como `{base}`)
- Fecha: 29/10/2026
- Herramienta: Postman

> Los nombres, correos y teléfonos de los ejemplos no son reales.

## Configuración usada en todas las pruebas

- Autenticación: Basic Auth con el usuario y la clave del entorno de pruebas.
- Encabezado `OSvC-CREST-Application-Context` con un texto descriptivo (por ejemplo, `prueba tecnica`).
- En las peticiones con cuerpo (POST y PATCH): `Content-Type: application/json`.

## Resumen

| 1 | Listar contactos | `GET {base}/contacts?limit=3` | 200 OK |
| 2 | Consultar un contacto | `GET {base}/contacts/{id}` | 200 OK |
| 3 | Consultar correos de un contacto | `GET {base}/contacts/{id}/emails` | 200 OK |
| 4 | Consultar teléfonos de un contacto | `GET {base}/contacts/{id}/phones` | 200 OK |
| 5 | Búsqueda base (ROQL) | `GET {base}/queryResults` | 200 OK |
| 6 | Búsqueda por ciudad | `GET {base}/queryResults` | 200 OK |
| 7 | Búsqueda por correo | `GET {base}/queryResults` | 200 OK |
| 8 | Búsqueda por teléfono | `GET {base}/queryResults` | 200 OK |
| 9 | Crear contacto con correo y teléfono | `POST {base}/contacts` | 201 Created |
| 10 | Verificar el contacto creado | `GET {base}/contacts/{id}` | 200 OK |
| 11 | Actualizar contacto | `PATCH {base}/contacts/{id}` | 200 OK |
| 12 | Eliminar contacto | `DELETE {base}/contacts/{id}` | Correcto |
| 13 | Casos de error | varias | 400, 404 y 401 |

---

## 1. Listar contactos

- **Petición:** `GET {base}/contacts?limit=3`
- **Resultado obtenido:** `200 OK`. Devolvió 3 contactos con su id, nombre completo y fechas, y un enlace para pedir los siguientes.

```json
{
  "items": [
    { "id": 14, "lookupName": "Ana Ejemplo", "createdTime": "...", "updatedTime": "..." }
  ],
  "hasMore": true
}
```

**Observaciones**

- Solo trae un resumen: no incluye ciudad, correo ni teléfono.

## 2. Consultar un contacto por ID

- **Petición:** `GET {base}/contacts/16`
- **Resultado obtenido:** `200 OK`. Devolvió todos los datos del contacto.

Datos que usaré en la aplicación:

| ID | `id` |
| Nombre y apellido | `name.first` y `name.last` |
| Ciudad | `address.city` |
| Dirección | `address.street` |
| Correo | enlace `emails` |
| Teléfono | enlace `phones` |

**Observaciones**

- El correo y el teléfono no vienen dentro del contacto, solo como enlaces. Hay que pedirlos por aparte.
- La respuesta es muy grande y casi todo son enlaces o campos vacíos.

## 3. Consultar los correos de un contacto

- **Petición:** `GET {base}/contacts/16/emails` y luego `GET {base}/contacts/16/emails/0`
- **Resultado obtenido:** `200 OK` en ambas. La primera devolvió solo el enlace a cada correo; la segunda, el correo.

```json
{
  "address": "ana.ejemplo@correo.test",
  "addressType": { "id": 0, "lookupName": "Email - Primary" },
  "invalid": false
}
```

**Observaciones**

- Primero se pide la lista de enlaces y después cada correo.
- Cada correo tiene un tipo; el principal es el `0`.

## 4. Consultar los teléfonos de un contacto

- **Petición:** `GET {base}/contacts/16/phones` y luego `GET {base}/contacts/16/phones/1`
- **Resultado obtenido:** `200 OK` en ambas, con el mismo funcionamiento que los correos.

```json
{
  "number": "300 0000000",
  "rawNumber": "3000000000",
  "phoneType": { "id": 1, "lookupName": "Mobile Phone" }
}
```

**Observaciones**

- El teléfono viene con formato (`number`) y sin formato (`rawNumber`).
- En este contacto el enlace terminó en `/phones/1`, no en `/phones/0`, así que hay que usar el enlace que devuelve la lista.

## 5. Búsqueda base

- **Petición:** `GET {base}/queryResults`
- **Parámetro `query`:** `SELECT ID, Name.First, Name.Last, Address.City FROM Contacts LIMIT 5`
- **Resultado obtenido:** `200 OK`. Devolvió 5 contactos.

```json
{
  "items": [
    {
      "tableName": "contacts",
      "count": 5,
      "columnNames": ["id", "first", "last", "city"],
      "rows": [
        ["14", "Ana", "Ejemplo", "Bogotá"],
        ["15", "Luis", "Prueba", "Bogotá"]
      ]
    }
  ]
}
```

**Observaciones**

- La respuesta viene en dos partes: la lista de nombres de columna y la lista de filas. Hay que unirlas para obtener cada contacto.
- Los nombres de columna salen en minúscula y sin prefijo (`first`, `last`, `city`).
- El `id` llega como texto.

## 6. Búsqueda por ciudad

- **Petición:** `GET {base}/queryResults`
- **Parámetro `query`:** `SELECT ID, Name.First, Name.Last, Address.City FROM Contacts WHERE Address.City LIKE '%25Bogot%25' LIMIT 5`
- **Resultado obtenido:** `200 OK`. Devolvió 5 contactos de Bogotá.

**Observaciones**

- `LIKE` permite buscar por una parte del texto.
- En Postman tuve que escribir `%25` en lugar de `%`; con `%` solo daba error de sintaxis.

## 7. Búsqueda por correo

- **Petición:** `GET {base}/queryResults`
- **Parámetro `query`:** `SELECT ID, Name.First, Emails.Address FROM Contacts WHERE Emails.Address LIKE '%25@%25' LIMIT 5`
- **Resultado obtenido:** `200 OK`. Devolvió 5 contactos con su correo.

```json
{
  "columnNames": ["id", "first", "address"],
  "rows": [["14", "Ana", "ana.ejemplo@correo.test"]]
}
```

**Observaciones**

- La columna del correo se llama `address`.
- Esta búsqueda sí trae el correo, a diferencia del listado simple.

## 8. Búsqueda por teléfono

- **Petición:** `GET {base}/queryResults`
- **Parámetro `query`:** `SELECT ID, Name.First, Phones.Number FROM Contacts WHERE Phones.Number LIKE '%255%25' LIMIT 5`
- **Resultado obtenido:** `200 OK`. Devolvió 5 contactos con su teléfono.

```json
{
  "columnNames": ["id", "first", "number"],
  "rows": [["210", "Luis", "3000000000"]]
}
```

**Observaciones**

- La columna del teléfono se llama `number`.
- Los teléfonos están guardados en formatos distintos: con `+57`, con espacios, con extensión o solo números.

## 9. Crear un contacto con correo y teléfono

- **Petición:** `POST {base}/contacts`
- **Body enviado:**

```json
{
  "name": { "first": "TEST_Miguel3", "last": "Completo" },
  "address": { "city": "Bogota", "street": "Calle 100" },
  "emails": [ { "address": "test.miguel3@ejemplo.com", "addressType": { "id": 0 } } ],
  "phones": [ { "number": "3001234567", "phoneType": { "id": 0 } } ]
}
```

- **Resultado obtenido:** `201 Created`. Se creó el contacto `18326` (`TEST_Miguel3 Completo`) y la respuesta devolvió todos sus datos.

**Observaciones**

- Con una sola petición se crea el contacto junto con su correo y su teléfono.
- La respuesta trae el `id` nuevo, así que no hace falta consultarlo después.
- El país quedó vacío porque no lo envié.
- Antes hice una creación básica (nombre, ciudad y calle) que creó el contacto `18325` y también respondió `201`.

## 10. Verificar el contacto creado

- **Petición:** `GET {base}/contacts/18325` y `GET {base}/contacts/18326/emails/0`
- **Resultado obtenido:** `200 OK` en ambas. Los datos coincidían con lo que envié, incluido el correo `test.miguel3@ejemplo.com` como correo principal. El teléfono también respondió correctamente al consultarlo.

**Observaciones**

- Con esta consulta confirmé que el contacto y su correo quedaron guardados.

## 11. Actualizar un contacto

- **Petición:** `PATCH {base}/contacts/18324`
- **Body enviado:**

```json
{ "name": { "last": "Prueba Edi" } }
```

- **Resultado obtenido:** `200 OK`, con la respuesta vacía.
- **Verificación:** `GET {base}/contacts/18324` respondió `200 OK` con el nombre completo actualizado a `TEST_Miguel2 Prueba Edi` y la fecha de modificación nueva.

**Observaciones**

- Solo hay que enviar los campos que cambian.
- La respuesta no devuelve el contacto; para mostrarlo actualizado hay que consultarlo otra vez.

## 12. Eliminar un contacto

- **Petición:** `DELETE {base}/contacts/{id}`, sobre un contacto de prueba creado por mí.
- **Resultado obtenido:** la eliminación se realizó correctamente. Código de respuesta:  200

**Observaciones**

- Solo eliminé contactos de prueba con el prefijo `TEST_`, porque el entorno lo usan otras personas.

## 13. Casos de error

| Sin encabezado | `GET {base}/contacts/18326/emails/0` sin el encabezado de contexto | 400 Bad Request | `OSC-CREST-00025`: Request does not contain OSvC-CREST-Application-Context header |
| ID inexistente | `GET` de un contacto con un id que no existe | 404 Not Found | `OSC-CREST-00013`: Resource with id ... not found |
| Credenciales incorrectas | `GET` dejando la contraseña vacía | 401 Unauthorized | `OSC-CREST-00014`: You are not authorized to use this site |

**Observaciones**

- Todos los errores llegan con la misma forma: código de estado, un detalle y un código de error de Oracle.
- Con eso el backend puede mostrar un mensaje claro según el caso.

---

## Hallazgos

1. **Encabezado obligatorio.** Todas las llamadas, incluso las de lectura, deben llevar `OSvC-CREST-Application-Context`. Si falta, el servidor responde con error 400.
2. **Credenciales protegidas.** El usuario y la clave solo deben estar en el backend, como variables de entorno, y nunca en el frontend.
3. **Para buscar se usa ROQL.** El listado simple de contactos solo trae nombre y fechas y no permite filtrar. Las búsquedas por ciudad, nombre, correo y teléfono se hacen con `queryResults`.
4. **Respuesta en columnas y filas.** ROQL devuelve los nombres de columna por un lado y los valores por otro, así que el backend debe unirlos para armar cada contacto. Los nombres salen en minúscula y sin prefijo.
5. **El `id` cambia de tipo.** Llega como texto en las búsquedas y como número en las consultas directas. Conviene unificarlo en el backend.
6. **Correo y teléfono van aparte.** El detalle del contacto solo trae enlaces. Se obtienen con ROQL o pidiendo primero la lista de enlaces y luego cada uno.
7. **Crear con todo en una petición.** Un solo `POST` puede incluir correo y teléfono. El tipo se envía como número (correo principal = 0; en un teléfono existente vi el tipo móvil = 1).
9. **Crear devuelve el contacto completo.** Incluye el `id`. Los campos que no se envían, como el país, quedan vacíos.
10. **Teléfonos con formatos distintos.** Hay números con `+57`, con espacios y con extensión. El formulario debe validar el teléfono y el backend no debe asumir un formato único.
11. **Errores con formato fijo.** Los errores traen estado, detalle y código de Oracle. Los más comunes son 400 (falta el encabezado), 404 (contacto inexistente) y 401 (credenciales incorrectas).


