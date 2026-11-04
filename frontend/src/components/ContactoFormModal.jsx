import { useState } from 'react'
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap'

/* Expresiones simples para comprobar que correo y teléfono tengan formatos válidos. */
const REGEX_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const REGEX_TELEFONO = /^[0-9+()\-\s]{7,20}$/

/* Valida los campos antes de enviar información al backend. */
/* original = el contacto antes de editar (null al crear). */
function validar(valores, original) {
  const errores = {}
  const nombre = valores.nombre.trim()
  const apellido = valores.apellido.trim()

  if (!nombre) errores.nombre = 'El nombre es obligatorio.'
  else if (nombre.length > 80) errores.nombre = 'El nombre no puede pasar de 80 caracteres.'

  if (!apellido) errores.apellido = 'El apellido es obligatorio.'
  else if (apellido.length > 80) errores.apellido = 'El apellido no puede pasar de 80 caracteres.'

  if (valores.ciudad.trim().length > 80) errores.ciudad = 'La ciudad no puede pasar de 80 caracteres.'

  const correo = valores.correo.trim()
  const telefono = valores.telefono.trim()

  if (correo && !REGEX_CORREO.test(correo)) errores.correo = 'El correo no tiene un formato válido.'
  // Al editar no se permite vaciar un dato que ya existía (Oracle no lo borra con PATCH).
  else if (!correo && original?.correo) errores.correo = 'El contacto ya tiene correo; no se puede dejar vacío.'

  if (telefono && !REGEX_TELEFONO.test(telefono)) {
    errores.telefono = 'Solo números, espacios, + - ( ) y entre 7 y 20 caracteres.'
  } else if (!telefono && original?.telefono) {
    errores.telefono = 'El contacto ya tiene teléfono; no se puede dejar vacío.'
  }
  return errores
}

/* Prepara el objeto JSON que se envía al backend. Crear y editar envían los mismos campos. */
function prepararDatos(valores) {
  return {
    nombre: valores.nombre.trim(),
    apellido: valores.apellido.trim(),
    ciudad: valores.ciudad.trim(),
    correo: valores.correo.trim(),
    telefono: valores.telefono.trim(),
  }
}

/*
 * Sirve para crear (contacto = null) y para editar (contacto = el contacto).
 * Al editar se pueden cambiar todos los campos; correo y teléfono no se pueden vaciar si ya existían.
 * La página lo monta solo cuando está abierto, así el estado inicial siempre es el correcto.
 */
export default function ContactoFormModal({ contacto, onGuardar, onCerrar, error }) {
  // Si existe contacto recibido, el formulario está en modo edición.
  const editando = Boolean(contacto)

  // Guarda los valores escritos en cada campo.
  const [valores, setValores] = useState({
    nombre: contacto?.nombre ?? '',
    apellido: contacto?.apellido ?? '',
    ciudad: contacto?.ciudad ?? '',
    correo: contacto?.correo ?? '',
    telefono: contacto?.telefono ?? '',
  })
  // Guarda los mensajes de validación por nombre de campo.
  const [errores, setErrores] = useState({})
  // Evita repetir acciones mientras la petición está en curso.
  const [guardando, setGuardando] = useState(false)

  // Actualiza solo el campo que originó el evento.
  const cambiar = (evento) => {
    const { name, value } = evento.target
    setValores((valoresActuales) => ({ ...valoresActuales, [name]: value }))
  }

  // Se ejecuta cuando el usuario envía el formulario.
  const enviar = async (evento) => {
    evento.preventDefault()

    // Validar primero evita enviar datos incorrectos al backend.
    const encontrados = validar(valores, contacto)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

       // Crear y editar envían los mismos campos.
    const datos = prepararDatos(valores)

    // Bloquea las acciones mientras espera la respuesta de crear o actualizar.
    setGuardando(true)
    const ok = await onGuardar(datos)
    setGuardando(false)

    // Solo se cierra si la operación fue exitosa.
    if (ok) onCerrar()
  }

  /* Ayuda a mostrar campos repetidos con menos código. */
  const campo = (nombre, etiqueta, extra = {}) => (
    <Form.Group className="mb-3" controlId={`campo-${nombre}`}>
      <Form.Label>{etiqueta}</Form.Label>
      <Form.Control
        name={nombre}
        value={valores[nombre]}
        onChange={cambiar}
        isInvalid={Boolean(errores[nombre])}
        {...extra}
      />
      <Form.Control.Feedback type="invalid">{errores[nombre]}</Form.Control.Feedback>
    </Form.Group>
  )

  return (
    // Modal centrado: no se puede seguir usando la página hasta cerrarlo.
    <Modal show onHide={guardando ? undefined : onCerrar} centered>
      <Form onSubmit={enviar} noValidate>
        <Modal.Header closeButton={!guardando}>
          <Modal.Title>{editando ? 'Editar contacto' : 'Nuevo contacto'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {/* Muestra aquí el mismo error enviado por el backend. */}
          {error && <Alert variant="danger">{error}</Alert>}

          {/* Los campos se crean con la función campo para mantener el JSX simple. */}
          {campo('nombre', 'Nombre *', { autoFocus: true })}
          {campo('apellido', 'Apellido *')}
          {campo('ciudad', 'Ciudad')}
          {campo('correo', 'Correo', { type: 'email' })}
          {campo('telefono', 'Teléfono', { type: 'tel' })}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary" disabled={guardando}>
            {guardando && <Spinner size="sm" className="me-2" />}
            {editando ? 'Guardar cambios' : 'Crear contacto'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  )
}
