# ICX Contactos

Aplicación web para consultar y administrar contactos de Oracle Service Cloud usando su API REST.

Prueba técnica Imagine CX - Miguel Vargas

## Funcionalidades

- Buscar contactos por ciudad, nombre, correo o teléfono.
- Ver los resultados en una tabla.
- Crear, editar y eliminar contactos.
- Si Oracle no responde, se muestra la última consulta guardada en una base de datos de respaldo (H2).

## Tecnologías

- **Frontend:** React, Vite, Bootstrap (React Bootstrap)
- **Backend:** Java 21, Spring Boot, Spring Data JPA
- **Base de datos de respaldo:** H2
- **API:** Oracle Service Cloud REST (v1.4)

## Cómo ejecutarlo

### Backend

1. Crear el archivo `backend/.env` con base en `backend/.env.template`:

```
ORACLE_BASE_URL=
ORACLE_USER=
ORACLE_PASS=
```

2. Desde la carpeta `backend`:

```bash
./mvnw spring-boot:run
```

El backend queda en `http://localhost:8080`.

> Importante: hay que ejecutarlo desde la carpeta `backend`, si no, no encuentra el `.env`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

La app queda en `http://localhost:5173`.

## Base de datos de respaldo

- Cada vez que una búsqueda en Oracle sale bien, el resultado se guarda en H2.
- Si Oracle no responde (sin conexión, error del servidor o acceso deshabilitado), el backend devuelve lo último que se guardó.
- En ese caso la app muestra un aviso amarillo con la fecha de esa consulta y desactiva crear, editar y eliminar.
- La base se guarda en `backend/data/`, así no se pierde al reiniciar.
- Para ver las tablas: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/respaldo-contactos`, usuario `sa`, sin clave).

## Manejo de errores

Todos los errores del backend responden con el mismo formato:

```json
{ "estado": 503, "mensaje": "...", "codigoOracle": null }
```

Así el frontend siempre muestra un mensaje claro al usuario. Además, los formularios validan los datos antes de enviarlos.

## Decisiones

- Sin conexión con Oracle la app es de solo lectura. Guardar cambios offline y sincronizarlos después quedaría como mejora futura.
- Al editar, el correo y el teléfono no se pueden dejar vacíos si el contacto ya los tenía.
- Se maneja solo el correo y teléfono principal (tipo 0).

## Flujo de trabajo con Git

- `main`: versiones entregadas (con tags `v1.0.0`, `v1.0.1`).
- `develop`: integración.
- Una rama por tarea (`feat/`, `fix/`, `docs/`, `chore/`) que se une a `develop` con Pull Request.

## Pruebas de la API

Las pruebas que hice en Postman están en [docs/pruebas-api.md](docs/pruebas-api.md).
