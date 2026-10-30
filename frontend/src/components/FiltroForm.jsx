import { useState } from 'react'
import { Button, Col, Form, Row } from 'react-bootstrap'

const VACIOS = { ciudad: '', nombre: '', correo: '', telefono: '' }

export default function FiltrosForm({ onBuscar, deshabilitado = false }) {
  const [filtros, setFiltros] = useState(VACIOS)

  const cambiar = (e) => setFiltros({ ...filtros, [e.target.name]: e.target.value })

  const enviar = (e) => {
    e.preventDefault()
    onBuscar(filtros)
  }

  return (
    <Form onSubmit={enviar} className="mb-4">
      <Row className="g-2">
        <Col xs={12} sm={6} lg={3}>
          <Form.Control name="ciudad" placeholder="Ciudad" value={filtros.ciudad} onChange={cambiar} aria-label="Ciudad" />
        </Col>
        <Col xs={12} sm={6} lg={3}>
          <Form.Control name="nombre" placeholder="Nombre" value={filtros.nombre} onChange={cambiar} aria-label="Nombre" />
        </Col>
        <Col xs={12} sm={6} lg={3}>
          <Form.Control name="correo" placeholder="Correo" value={filtros.correo} onChange={cambiar} aria-label="Correo" />
        </Col>
        <Col xs={12} sm={6} lg={3}>
          <Form.Control name="telefono" placeholder="Teléfono" value={filtros.telefono} onChange={cambiar} aria-label="Teléfono" />
        </Col>
        <Col xs={12} className="d-grid d-sm-block">
          <Button type="submit" variant="primary" disabled={deshabilitado}>
            Buscar
          </Button>
        </Col>
      </Row>
    </Form>
  )
}
