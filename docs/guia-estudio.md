# Guia de estudio: ICX Contactos

## 1. Idea general del proyecto

ICX Contactos es una aplicacion sencilla para consultar y administrar contactos de Oracle Service Cloud. Tiene un frontend hecho con React y Vite, y un backend hecho con Spring Boot y Java 21.

La relacion principal es:

1. El usuario escribe o consulta informacion en el frontend.
2. React envia una peticion HTTP al backend.
3. Spring Boot recibe la peticion, valida los datos y ejecuta la logica.
4. El backend llama a Oracle Service Cloud mediante REST.
5. Si Oracle funciona, entrega los datos nuevos.
6. Si Oracle falla, el backend puede leer el ultimo resultado guardado en una base H2.
7. El frontend muestra datos, mensajes de error o el aviso de modo respaldo.

## 2. Arquitectura

### Frontend

El frontend esta dentro de `frontend/src`.

- `App.jsx` es el componente raiz.
- `main.jsx` monta React dentro del elemento `root` del HTML.
- `pages/ContactosPage.jsx` organiza la pagina principal.
- `components/` contiene filtros, tabla y formulario modal.
- `hooks/useContactos.js` mantiene el estado de la interfaz y las operaciones CRUD.
- `services/contactosApi.js` centraliza las llamadas HTTP al backend.

El frontend no habla directamente con Oracle. Esta separacion hace que las credenciales y detalles de Oracle queden en el backend.

### Backend

El backend esta dentro de `backend/src/main/java/com/icx/prueba`.

- `controller` recibe las peticiones HTTP.
- `service` contiene la logica principal.
- `client` realiza las llamadas REST a Oracle.
- `config` configura el cliente REST y las propiedades de Oracle.
- `dto` representa datos que entran o salen de la API.
- `exception` convierte excepciones en respuestas JSON claras.
- `respaldo` guarda y lee la ultima consulta en H2.

### Oracle y H2

Oracle Service Cloud es la fuente principal de datos. El backend usa consultas ROQL para buscar contactos y endpoints REST para crear, editar y eliminar.

H2 es una fuente secundaria. Solo se usa cuando Oracle no puede atender una consulta. Guarda una copia de la ultima busqueda exitosa para que el usuario pueda seguir consultando.

## 3. Recorrido completo de una busqueda

1. La pagina se carga y `useContactos` ejecuta una primera busqueda sin filtros.
2. `contactosApi.js` hace una peticion `GET /api/contactos`.
3. `ContactoController.buscar()` recibe los parametros opcionales.
4. El controlador llama al metodo `buscar()` de `ContactoService`.
5. `ContactoService` construye una consulta ROQL.
6. `OracleClient.consultar()` envia la consulta a Oracle.
7. Oracle devuelve filas con nombres de columnas y datos.
8. `ContactoService.convertirRespuestaOracle()` transforma las filas en objetos `ContactoDto`.
9. Si Oracle devuelve varias filas para el mismo contacto, porque tiene varios correos o telefonos, el metodo `combinarFilasDelContacto()` conserva un solo registro.
10. La respuesta exitosa se guarda en H2 mediante `RespaldoService`.
11. El backend devuelve una lista y la informacion de que los datos son en vivo.
12. El hook recibe la respuesta y actualiza React.
13. `ContactosTabla` muestra los registros.

## 4. Crear un contacto

1. El usuario pulsa `Nuevo contacto`.
2. `ContactosPage` abre `ContactoFormModal`.
3. React guarda los valores escritos en el estado del modal.
4. Antes de enviar, `validarDatos()` revisa nombre, apellido, ciudad, correo y telefono.
5. Si todo esta bien, el modal envia los datos a `guardar()` de la pagina.
6. La pagina llama a `crear()` del hook.
7. El hook llama a `POST /api/contactos`.
8. `ContactoController.crear()` convierte el JSON en un `ContactoRequest`.
9. Las anotaciones `@Valid` vuelven a validar los datos.
10. `ContactoService.crear()` prepara el cuerpo JSON para Oracle.
11. Oracle crea el contacto y devuelve el identificador.
12. El backend devuelve el nuevo contacto al frontend.
13. El hook recarga la ultima busqueda para mostrar la lista actualizada.
14. La pagina muestra un aviso verde de operacion correcta.

## 5. Editar un contacto

1. El usuario pulsa `Editar` en una fila.
2. `ContactosPage` abre el modal con los datos actuales.
3. El formulario valida los valores nuevos.
4. `PUT /api/contactos/{id}` envia el identificador y los campos al backend.
5. `ContactoController.actualizar()` recibe el `ContactoUpdate`.
6. `ContactoService.actualizar()` prepara un cuerpo PATCH.
7. `OracleClient.actualizar()` envia `PATCH /contacts/{id}` a Oracle.
8. Oracle responde sin contenido cuando la operacion funciona.
9. El frontend vuelve a buscar la lista con los filtros actuales.
10. Si Oracle devuelve 404 en la operacion de este contacto, el backend transforma el error en una excepcion de contacto inexistente.

## 6. Eliminar un contacto

1. El usuario pulsa `Eliminar`.
2. Aparece una ventana de confirmacion.
3. Si confirma, la pagina llama al metodo `eliminar()` del hook.
4. El frontend envia `DELETE /api/contactos/{id}`.
5. El backend pasa el identificador a `ContactoService`.
6. `OracleClient.eliminar()` ejecuta la peticion DELETE a Oracle.
7. Oracle responde 204 si el contacto fue eliminado.
8. El frontend recarga la lista y muestra un aviso correcto.
9. Si Oracle informa que el contacto ya no existe, se muestra el mensaje claro de contacto inexistente.

## 7. Como funciona el respaldo

La clase `RespaldoService` administra H2.

- `guardar()` reemplaza el ultimo respaldo.
- Primero elimina las filas anteriores.
- Luego guarda los contactos nuevos.
- Guarda la fecha y los filtros de la consulta.
- `@Transactional` indica que todo el cambio debe hacerse como una sola operacion.
- Si falla al borrar, guardar o actualizar, la transaccion se deshace.

`leer()` busca la fila unica de consulta con id 1. Despues obtiene todos los contactos guardados ordenados por su posicion y devuelve un `ResultadoBusqueda` con datos de respaldo.

El respaldo se usa cuando:

- La conexion con Oracle falla.
- Oracle responde 401 o 403 por autenticacion o autorizacion.
- Oracle responde un estado 5xx.
- Oracle devuelve HTML en vez de JSON.

No se usa para 400 ni 404 basicos, porque eso significa que Oracle si respondio, pero los datos o la operacion no eran validos.

Cuando hay respaldo activo, la interfaz entra en modo solo lectura: no permite crear, editar ni eliminar.

## 8. Manejo de errores

### Backend apagado

Si el frontend recibe un fallo de red o el proxy no puede llegar al backend, `contactosApi.js` muestra un mensaje que pide verificar que el backend este encendido.

### Oracle caido o sin conexion

`sinConexion()` convierte una excepcion de acceso en un estado HTTP 503. El backend intenta usar el ultimo respaldo antes de devolver este error.

### Oracle devuelve HTML

`BufferingClientHttpRequestFactory` ayuda a controlar el envio del cuerpo de las peticiones REST. Si la respuesta de Oracle contiene HTML en vez de JSON, el backend reconoce que no tiene el formato esperado y puede recurrir al respaldo.

### Datos invalidos

Las anotaciones `@Valid`, `@NotBlank`, `@Email`, `@Pattern` y `@Size` revisan las reglas. `ApiExceptionHandler` convierte los campos invalidos en un error HTTP 400 con texto claro.

### Contacto inexistente

Cuando editar o eliminar recibe 404 para el identificador solicitado, `ContactoService` crea una `ContactoNoEncontradoException`. El manejador devuelve HTTP 404 y recomienda actualizar la lista.

## 9. Decisiones tomadas

### Separacion por capas

El controlador recibe HTTP, el servicio aplica reglas y el cliente comunica con Oracle. Esto facilita explicar y cambiar cada parte sin mezclar responsabilidades.

### DTO separados

`ContactoRequest` representa crear y `ContactoUpdate` representa editar. Los DTO permiten validar datos y mantener el contrato JSON estable.

### H2 como respaldo

H2 sirve para demostrar recuperacion ante fallos sin reemplazar Oracle como fuente principal. Guarda solo la ultima consulta, por eso es suficiente para consulta temporal y sencilla.

### Respuesta unica de error

Todos los errores usan la forma `ErrorApi` con estado, mensaje y codigo de Oracle. El frontend no tiene que adivinar diferentes formatos.

### Solucion de filas repetidas

Oracle puede devolver varias filas de un contacto al unir varios correos o telefonos. El metodo de combinacion conserva el primer contacto e intenta completar correo y telefono sin duplicar registros.

## 10. Posibles preguntas de entrevista

**¿Por que el frontend no llama directamente a Oracle?**

Porque las credenciales y la configuracion deben quedar protegidas en el backend. Ademas, el backend puede validar, transformar y manejar errores con una sola estrategia.

**¿Que hace Spring Boot en este proyecto?**

Recibe peticiones REST, usa inyeccion de dependencias, configura el cliente REST y administra las entidades JPA e H2.

**¿Que es un DTO?**

Es un objeto que representa datos de entrada o salida. No contiene la logica principal de Oracle ni JPA. Sirve para mantener un contrato claro.

**¿Que diferencia hay entre GET, POST, PUT y DELETE?**

GET consulta, POST crea, PUT/PATCH actualiza y DELETE elimina. El frontend usa PUT en el proyecto, aunque Oracle recibe PATCH internamente.

**¿Que hace `@Valid`?**

Revisa las reglas del DTO antes de ejecutar el metodo del controlador. Si encuentra un campo invalido, Spring inicia el manejo de errores.

**¿Por que se usa BufferingClientHttpRequestFactory?**

Permite que el cliente REST envie el cuerpo completo de la peticion con una longitud conocida. Esto evita problemas con cuerpos HTTP enviados por partes en Oracle.

**¿Que hace una transaccion de respaldo?**

Agrupa la limpieza y el guardado como una sola operacion. Si alguna parte falla, la base vuelve al estado anterior.

**¿Por que leer H2 cuando Oracle devuelve 401 o 403?**

Porque esos estados significan que la autenticacion o la autorizacion no funcionaron y puede ser una interrupcion temporal. H2 permite continuar consultando mientras Oracle vuelve.

**¿Por que no usar respaldo ante un 404 de editar o eliminar?**

Porque el 404 de ese recurso indica que el contacto solicitado no existe. Usar respaldo no convertiria el recurso eliminado en existente.

**¿Que hace ROQL?**

ROQL es el lenguaje de consulta de Oracle Service Cloud. El backend lo construye con los filtros y luego lo envia al endpoint de query results.

**¿Que pasa si Oracle devuelve varias filas del mismo contacto?**

El backend las agrupa por el id. Conserva los datos principales y completa correo o telefono si estaban en otras filas.

**¿Que mostraras si te preguntan por el flujo completo?**

Explicaria que React inicia la accion, el controlador recibe la peticion, el servicio prepara la regla o consulta, Oracle responde, el DTO normaliza los datos y finalmente el hook actualiza la interfaz.
