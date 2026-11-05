import { useState } from 'react'
import { Button, Col, Form, Row } from 'react-bootstrap'

/*
 * Recoge los filtros de búsqueda y los envía a ContactosPage.jsx.
 * No consulta datos directamente: useContactos.js recibe los valores y llama al backend.
 */

// Objeto inicial: representa los cuatro filtros vacíos.
const VACIOS = { ciudad: '', nombre: '', correo: '', telefono: '' }

/* Componente que permite escribir filtros y enviarlos a la página principal. */
export default function FiltrosForm({ onBuscar, deshabilitado = false }) {
  // Guarda los textos escritos en cada campo de filtro.
  const [filtros, setFiltros] = useState(VACIOS)

  // Actualiza solo el filtro que cambió el usuario.
  const actualizarFiltro = (evento) => {
    const { name, value } = evento.target

    setFiltros((filtrosActuales) => ({
      ...filtrosActuales,
      [name]: value,
    }))
  }

  // Se ejecuta al pulsar el botón Buscar o presionar Enter.
  const enviarBusqueda = (evento) => {
    evento.preventDefault()

    // Envía los filtros actuales al hook que realiza la búsqueda.
    onBuscar(filtros)
  }

  /* Borra los campos y vuelve a buscar sin filtros para mostrar todos los contactos. */
  const limpiarFiltros = () => {
    setFiltros(VACIOS)
    onBuscar(VACIOS)
  }

  /* Ayuda a crear los cuatro campos sin repetir el mismo código JSX. */
  const crearCampoFiltro = (nombre, etiqueta, extra = {}) => (
    <Col xs={12} sm={6} lg={3}>
      <Form.Group controlId={`filtro-${nombre}`}>
        <Form.Label className="small fw-semibold text-secondary mb-1">{etiqueta}</Form.Label>
        <Form.Control
          name={nombre}
          value={filtros[nombre]}
          onChange={actualizarFiltro}
          {...extra}
        />
      </Form.Group>
    </Col>
  )

  return (
    // El formulario ejecuta enviar cuando se presiona Buscar o Enter.
    <Form onSubmit={enviarBusqueda}>
      <Row className="g-3">
        {/* Cada llamada crea una columna con un campo de filtro diferente. */}
        {crearCampoFiltro('ciudad', 'Ciudad', { placeholder: 'Ej: Bogotá' })}
        {crearCampoFiltro('nombre', 'Nombre o apellido', { placeholder: 'Ej: Ana' })}
        {crearCampoFiltro('correo', 'Correo', { placeholder: 'Ej: ana@correo.com' })}
        {crearCampoFiltro('telefono', 'Teléfono', { placeholder: 'Ej: 3001234567', inputMode: 'tel' })}

        {/* Contenedor de los botones al final del formulario. */}
        <Col xs={12} className="d-flex flex-column flex-sm-row gap-2 justify-content-sm-end">
          {/* Limpia los filtros y solicita una búsqueda sin condiciones. */}
          <Button type="button" variant="outline-secondary" onClick={limpiarFiltros} disabled={deshabilitado}>
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
