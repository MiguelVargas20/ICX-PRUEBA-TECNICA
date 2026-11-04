package com.icx.prueba.exception;

/*
 * Se lanza SOLO cuando pedimos un contacto concreto (editar o eliminar)
 * y Oracle responde 404: ahí sí sabemos que el contacto no existe.
 * Cualquier otro 404 de Oracle se trata como error de configuración.
 */
public class ContactoNoEncontradoException extends RuntimeException {

    public ContactoNoEncontradoException(long id) {
        super("No existe el contacto con id " + id);
    }
}
