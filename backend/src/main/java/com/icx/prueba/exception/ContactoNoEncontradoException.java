package com.icx.prueba.exception;

// Error propio para cuando se intenta editar o eliminar un contacto que no existe.
// Lo lanza ContactoService cuando Oracle responde 404 para ese id.
// Así diferencio "el contacto no existe" de otros 404 (por ejemplo una URL mal configurada).
public class ContactoNoEncontradoException extends RuntimeException {

    public ContactoNoEncontradoException(long id) {
        // Este mensaje va al log. Lo que ve el usuario se arma en ApiExceptionHandler.
        super("No existe el contacto con id " + id);
    }
}
