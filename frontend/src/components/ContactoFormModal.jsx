import { useState } from 'react'
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap'

/* Mismas reglas del backend (ContactoRequest / ContactoUpdate). */
const REGEX_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const REGEX_TELEFONO = /^[0-9+()\-\s]{7,20}$/

function validar(valores, editando) {
  const errores = {}
  const nombre = valores.nombre.trim()
  const apellido = valores.apellido.trim()

  if (!nombre) errores.nombre = 'El nombre es obligatorio.'
  else if (nombre.length > 80) errores.nombre = 'El nombre no puede pasar de 80 caracteres.'

  if (!apellido) errores.apellido = 'El apellido es obligatorio.'
  else if (apellido.length > 80) errores.apellido = 'El apellido no puede pasar de 80 caracteres.'

  if (valores.ciudad.trim().length > 80) errores.ciudad = 'La ciudad no puede pasar de 80 caracteres.'

  if (!editando) {
    const correo = valores.correo.trim()
    const telefono = valores.telefono.trim()
    if (correo && !REGEX_CORREO.test(correo)) errores.correo = 'El correo no tiene un formato válido.'
    if (telefono && !REGEX_TELEFONO.test(telefono)) {
      errores.telefono = 'Solo números, espacios, + - ( ) y entre 7 y 20 caracteres.'
    }
  }
  return errores
}

/*
 * Sirve para crear (contacto = null) y para editar (contacto = el contacto).
 * Al editar solo se muestran nombre, apellido y ciudad.
 * La página lo monta solo cuando está abierto, así el estado inicial siempre es el correcto.
 */
export default function ContactoFormModal({ contacto, onGuardar, onCerrar, error }) {
  const editando = Boolean(contacto)
  const [valores, setValores] = useState({
    nombre: contacto?.nombre ?? '',
    apellido: contacto?.apellido ?? '',
    ciudad: contacto?.ciudad ?? '',
    correo: '',
    telefono: '',
  })
  const [errores, setErrores] = useState({})
  const [guardando, setGuardando] = useState(false)

  const cambiar = (e) => setValores({ ...valores, [e.target.name]: e.target.value })

  const enviar = async (e) => {
    e.preventDefault()
    const encontrados = validar(valores, editando)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    const datos = editando
      ? { nombre: valores.nombre.trim(), apellido: valores.apellido.trim(), ciudad: valores.ciudad.trim() }
      : {
          nombre: valores.nombre.trim(),
          apellido: valores.apellido.trim(),
          ciudad: valores.ciudad.trim(),
          correo: valores.correo.trim(),
          telefono: valores.telefono.trim(),
        }

    setGuardando(true)
    const ok = await onGuardar(datos)
    setGuardando(false)
    if (ok) onCerrar()
  }

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
    <Modal show onHide={guardando ? undefined : onCerrar} centered>
      <Form onSubmit={enviar} noValidate>
        <Modal.Header closeButton={!guardando}>
          <Modal.Title>{editando ? 'Editar contacto' : 'Nuevo contacto'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          {campo('nombre', 'Nombre *', { autoFocus: true })}
          {campo('apellido', 'Apellido *')}
          {campo('ciudad', 'Ciudad')}
          {!editando && (
            <>
              {campo('correo', 'Correo', { type: 'email' })}
              {campo('telefono', 'Teléfono', { type: 'tel' })}
            </>
          )}
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
