package com.icx.prueba.exception;

/*
 * Se lanza SOLO cuando pedimos un contacto concreto (editar o eliminar)
 * y Oracle responde 404: ahí sí sabemos que el contacto no existe.
 * Cualquier otro 404 de Oracle se trata como error de configuración.
 */
public class ContactoNoEncontradoException extends RuntimeException {

    public ContactoNoEncontradoException(long id) {
        /*
         * super() guarda el mensaje técnico dentro de la excepción.
         * El texto visible para el usuario puede cambiarse después en
         * ApiExceptionHandler sin modificar esta clase.
         */
        super("No existe el contacto con id " + id);
    }
}
