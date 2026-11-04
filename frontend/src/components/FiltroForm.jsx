import { useState } from 'react'
import { Button, Col, Form, Row } from 'react-bootstrap'

// Objeto inicial: representa los cuatro filtros vacíos.
const VACIOS = { ciudad: '', nombre: '', correo: '', telefono: '' }

/* Componente que permite escribir filtros y enviarlos a la página principal. */
export default function FiltrosForm({ onBuscar, deshabilitado = false }) {
  // Guarda los textos escritos en cada campo de filtro.
  const [filtros, setFiltros] = useState(VACIOS)

  // Actualiza solo el filtro que cambió el usuario.
  const cambiar = (evento) => {
    const { name, value } = evento.target

    setFiltros((filtrosActuales) => ({
      ...filtrosActuales,
      [name]: value,
    }))
  }

  // Se ejecuta al pulsar el botón Buscar o presionar Enter.
  const enviar = (evento) => {
    evento.preventDefault()

    // Envía los filtros actuales al hook que realiza la búsqueda.
    onBuscar(filtros)
  }

  /* Borra los campos y vuelve a buscar sin filtros para mostrar todos los contactos. */
  const limpiar = () => {
    setFiltros(VACIOS)
    onBuscar(VACIOS)
  }

  /* Ayuda a crear los cuatro campos sin repetir el mismo código JSX. */
  const campo = (nombre, etiqueta, extra = {}) => (
    <Col xs={12} sm={6} lg={3}>
      <Form.Group controlId={`filtro-${nombre}`}>
        <Form.Label className="small fw-semibold text-secondary mb-1">{etiqueta}</Form.Label>
        <Form.Control
          name={nombre}
          value={filtros[nombre]}
          onChange={cambiar}
          {...extra}
        />
      </Form.Group>
    </Col>
  )

  return (
    // El formulario ejecuta enviar cuando se presiona Buscar o Enter.
    <Form onSubmit={enviar}>
      <Row className="g-3">
        {/* Cada llamada crea una columna con un campo de filtro diferente. */}
        {campo('ciudad', 'Ciudad', { placeholder: 'Ej: Bogotá' })}
        {campo('nombre', 'Nombre o apellido', { placeholder: 'Ej: Ana' })}
        {campo('correo', 'Correo', { placeholder: 'Ej: ana@correo.com' })}
        {campo('telefono', 'Teléfono', { placeholder: 'Ej: 3001234567', inputMode: 'tel' })}

        {/* Contenedor de los botones al final del formulario. */}
        <Col xs={12} className="d-flex flex-column flex-sm-row gap-2 justify-content-sm-end">
          {/* Limpia los filtros y solicita una búsqueda sin condiciones. */}
          <Button type="button" variant="outline-secondary" onClick={limpiar} disabled={deshabilitado}>
            Limpiar
          </Button>

          {/* type="submit" permite enviar el formulario al ejecutar el botón. */}
          <Button type="submit" variant="primary" disabled={deshabilitado}>
            Buscar
          </Button>
        </Col>
      </Row>
    </Form>
  )
}
