import { useState } from 'react'
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap'

// Modal con el formulario para crear o editar un contacto.
// Si recibe contacto = null es para crear, si recibe un contacto es para editarlo.
// Valida los datos aquí antes de mandarlos, con las mismas reglas que el backend.

const REGEX_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const REGEX_TELEFONO = /^[0-9+()\-\s]{7,20}$/

// Revisa los campos y devuelve un objeto con los errores, ej: { nombre: 'El nombre es obligatorio.' }
// original es el contacto antes de editar (null cuando se está creando)
function validarDatos(valores, original) {
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

  // Al editar no dejo vaciar el correo o el teléfono si ya tenía,
  // porque el backend no los borra en Oracle (solo los cambia)
  if (correo && !REGEX_CORREO.test(correo)) errores.correo = 'El correo no tiene un formato válido.'
  else if (!correo && original?.correo) errores.correo = 'El contacto ya tiene correo; no se puede dejar vacío.'

  if (telefono && !REGEX_TELEFONO.test(telefono)) {
    errores.telefono = 'Solo números, espacios, + - ( ) y entre 7 y 20 caracteres.'
  } else if (!telefono && original?.telefono) {
    errores.telefono = 'El contacto ya tiene teléfono; no se puede dejar vacío.'
  }
  return errores
}

// Arma el objeto que se manda al backend, sin espacios de más
function prepararDatosParaEnviar(valores) {
  return {
    nombre: valores.nombre.trim(),
    apellido: valores.apellido.trim(),
    ciudad: valores.ciudad.trim(),
    correo: valores.correo.trim(),
    telefono: valores.telefono.trim(),
  }
}

export default function ContactoFormModal({ contacto, onGuardar, onCerrar, error }) {
  const editando = Boolean(contacto)

  // Valores de los campos. Si es editar, arrancan con los datos del contacto.
  const [valores, setValores] = useState({
    nombre: contacto?.nombre ?? '',
    apellido: contacto?.apellido ?? '',
    ciudad: contacto?.ciudad ?? '',
    correo: contacto?.correo ?? '',
    telefono: contacto?.telefono ?? '',
  })
  const [errores, setErrores] = useState({})
  // Mientras guarda bloqueo los botones para que no se envíe dos veces
  const [guardando, setGuardando] = useState(false)

  // Cambia solo el campo que se está escribiendo (usa el name del input)
  const actualizarCampo = (evento) => {
    const { name, value } = evento.target
    setValores((valoresActuales) => ({ ...valoresActuales, [name]: value }))
  }

  const guardarFormulario = async (evento) => {
    evento.preventDefault() // para que no se recargue la página

    const encontrados = validarDatos(valores, contacto)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    const datos = prepararDatosParaEnviar(valores)

    setGuardando(true)
    const ok = await onGuardar(datos)
    setGuardando(false)

    // Solo se cierra si salió bien. Si falló, se queda abierto mostrando el error.
    if (ok) onCerrar()
  }

  // Para no repetir el mismo bloque de JSX en cada campo
  const crearCampoFormulario = (nombre, etiqueta, extra = {}) => (
    <Form.Group className="mb-3" controlId={`campo-${nombre}`}>
      <Form.Label>{etiqueta}</Form.Label>
      <Form.Control
        name={nombre}
        value={valores[nombre]}
        onChange={actualizarCampo}
        isInvalid={Boolean(errores[nombre])}
        {...extra}
      />
      <Form.Control.Feedback type="invalid">{errores[nombre]}</Form.Control.Feedback>
    </Form.Group>
  )

  return (
    <Modal show onHide={guardando ? undefined : onCerrar} centered>
      {/* noValidate: uso mis validaciones y no las del navegador */}
      <Form onSubmit={guardarFormulario} noValidate>
        <Modal.Header closeButton={!guardando}>
          <Modal.Title>{editando ? 'Editar contacto' : 'Nuevo contacto'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {/* Error que mandó el backend (por ejemplo si Oracle rechazó los datos) */}
          {error && <Alert variant="danger">{error}</Alert>}

          {crearCampoFormulario('nombre', 'Nombre *', { autoFocus: true })}
          {crearCampoFormulario('apellido', 'Apellido *')}
          {crearCampoFormulario('ciudad', 'Ciudad')}
          {crearCampoFormulario('correo', 'Correo', { type: 'email' })}
          {crearCampoFormulario('telefono', 'Teléfono', { type: 'tel' })}
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
