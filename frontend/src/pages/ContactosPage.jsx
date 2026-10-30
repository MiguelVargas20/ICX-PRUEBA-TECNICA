import { useState } from 'react'
import { Alert, Button, Container, Modal, Spinner } from 'react-bootstrap'
import ContactoFormModal from '../components/ContactoFormModal.jsx'
import ContactosTabla from '../components/ContactosTabla.jsx'
import FiltroForm from '../components/FiltroForm.jsx'
import { useContactos } from '../hooks/useContactos.js'

export default function ContactosPage() {
  const { contactos, cargando, error, buscar, crear, actualizar, eliminar, limpiarError } = useContactos()

  // null = cerrado | { contacto: null } = crear | { contacto } = editar
  const [formulario, setFormulario] = useState(null)
  const [aEliminar, setAEliminar] = useState(null)
  const [eliminando, setEliminando] = useState(false)

  const abrirCrear = () => {
    limpiarError()
    setFormulario({ contacto: null })
  }
  const abrirEditar = (contacto) => {
    limpiarError()
    setFormulario({ contacto })
  }
  const cerrarFormulario = () => {
    limpiarError()
    setFormulario(null)
  }

  const guardar = (datos) =>
    formulario.contacto ? actualizar(formulario.contacto.id, datos) : crear(datos)

  const confirmarEliminar = async () => {
    setEliminando(true)
    await eliminar(aEliminar.id)
    setEliminando(false)
    setAEliminar(null) // si falló, el mensaje queda en la alerta roja de la página
  }

  return (
    <Container className="py-4">
      <div className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2 mb-4">
        <h1 className="h3 mb-0">Contactos ICX</h1>
        <Button variant="success" onClick={abrirCrear}>
          Nuevo contacto
        </Button>
      </div>

      <FiltroForm onBuscar={buscar} deshabilitado={cargando} />

      {/* Con el formulario abierto, el error se muestra dentro del modal */}
      {error && !formulario && (
        <Alert variant="danger" dismissible onClose={limpiarError}>
          {error}
        </Alert>
      )}

      {cargando && (
        <div className="text-center py-5">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Cargando...</span>
          </Spinner>
        </div>
      )}

      {!cargando && contactos.length === 0 && !error && (
        <p className="text-center text-muted py-4">No se encontraron contactos.</p>
      )}

      {!cargando && contactos.length > 0 && (
        <ContactosTabla contactos={contactos} onEditar={abrirEditar} onEliminar={setAEliminar} />
      )}

      {formulario && (
        <ContactoFormModal
          contacto={formulario.contacto}
          onGuardar={guardar}
          onCerrar={cerrarFormulario}
          error={error}
        />
      )}

      <Modal show={Boolean(aEliminar)} onHide={eliminando ? undefined : () => setAEliminar(null)} centered>
        <Modal.Header closeButton={!eliminando}>
          <Modal.Title>Eliminar contacto</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          ¿Seguro que quieres eliminar a{' '}
          <strong>{`${aEliminar?.nombre ?? ''} ${aEliminar?.apellido ?? ''}`.trim()}</strong>? Esta acción no se
          puede deshacer.
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setAEliminar(null)} disabled={eliminando}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={confirmarEliminar} disabled={eliminando}>
            {eliminando && <Spinner size="sm" className="me-2" />}
            Eliminar
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  )
}
