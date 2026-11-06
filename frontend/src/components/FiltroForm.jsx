import { useState } from 'react'
import { Button, Col, Form, Row } from 'react-bootstrap'

// Formulario de filtros de búsqueda.
// No llama al backend: le pasa los filtros a onBuscar, que es la función buscar del hook.

const VACIOS = { ciudad: '', nombre: '', correo: '', telefono: '' }

export default function FiltroForm({ onBuscar, deshabilitado = false }) {
  const [filtros, setFiltros] = useState(VACIOS)

  // Cambia solo el filtro que se está escribiendo
  const actualizarFiltro = (evento) => {
    const { name, value } = evento.target

    setFiltros((filtrosActuales) => ({
      ...filtrosActuales,
      [name]: value,
    }))
  }

  // Se ejecuta con el botón Buscar o al presionar Enter
  const enviarBusqueda = (evento) => {
    evento.preventDefault()
    onBuscar(filtros)
  }

  // Borra los campos y vuelve a buscar sin filtros
  const limpiarFiltros = () => {
    setFiltros(VACIOS)
    onBuscar(VACIOS)
  }

  // Crea cada campo. xs/sm/lg es lo responsive: 1 campo por fila en celular,
  // 2 en tablet y 4 en pantalla grande
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
    <Form onSubmit={enviarBusqueda}>
      <Row className="g-3">
        {crearCampoFiltro('ciudad', 'Ciudad', { placeholder: 'Ej: Bogotá' })}
        {crearCampoFiltro('nombre', 'Nombre o apellido', { placeholder: 'Ej: Ana' })}
        {crearCampoFiltro('correo', 'Correo', { placeholder: 'Ej: ana@correo.com' })}
        {crearCampoFiltro('telefono', 'Teléfono', { placeholder: 'Ej: 3001234567', inputMode: 'tel' })}

        <Col xs={12} className="d-flex flex-column flex-sm-row gap-2 justify-content-sm-end">
          <Button type="button" variant="outline-secondary" onClick={limpiarFiltros} disabled={deshabilitado}>
            Limpiar
          </Button>
          <Button type="submit" variant="primary" disabled={deshabilitado}>
            Buscar
          </Button>
        </Col>
      </Row>
    </Form>
  )
}
