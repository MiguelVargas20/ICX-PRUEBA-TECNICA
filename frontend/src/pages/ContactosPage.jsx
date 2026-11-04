import { useState } from 'react'
import { Alert, Button, Container, Modal, Spinner, Toast, ToastContainer } from 'react-bootstrap'
import ContactoFormModal from '../components/ContactoFormModal.jsx'
import ContactosTabla from '../components/ContactosTabla.jsx'
import FiltroForm from '../components/FiltroForm.jsx'
import { useContactos } from '../hooks/useContactos.js'

/* Debe ser igual al LIMITE de ContactoService.java (el backend corta la lista ahí). */
const LIMITE_BACKEND = 50

/* "2026-11-04T16:30:00Z" → "4 nov 2026, 11:30 a. m." (en la hora local del navegador). */
const formatearFecha = (iso) =>
  iso
    ? new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
    : 'fecha desconocida'

export default function ContactosPage() {
  const { contactos, cargando, error, respaldo, buscar, crear, actualizar, eliminar, limpiarError } =
    useContactos()

  // Sin conexión con Oracle: solo se puede consultar, no crear / editar / eliminar.
  const soloLectura = Boolean(respaldo)

  // null = cerrado | { contacto: null } = crear | { contacto } = editar
  const [formulario, setFormulario] = useState(null)
  const [aEliminar, setAEliminar] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  const [aviso, setAviso] = useState(null) // mensaje verde de "todo salió bien"

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

  const guardar = async (datos) => {
    const editando = Boolean(formulario.contacto)
    const ok = editando ? await actualizar(formulario.contacto.id, datos) : await crear(datos)
    if (ok) setAviso(editando ? 'Contacto actualizado correctamente.' : 'Contacto creado correctamente.')
    return ok
  }

  const confirmarEliminar = async () => {
    setEliminando(true)
    const ok = await eliminar(aEliminar.id)
    setEliminando(false)
    setAEliminar(null) // si falló, el mensaje queda en la alerta roja de la página
    if (ok) setAviso('Contacto eliminado correctamente.')
  }

  // Spinner grande solo en la primera carga; después la tabla se queda visible (un poco opaca).
  const primeraCarga = cargando && contactos.length === 0

  return (
    <>
      {/* ---------- Encabezado ---------- */}
      <header className="encabezado-icx">
        <Container className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3">
          <div>
            <h1 className="h2 mb-1">Contactos ICX</h1>
            <p className="mb-0">Consulta y administra los contactos de Oracle Service Cloud</p>
          </div>
          <Button
            className="btn-nuevo"
            size="lg"
            onClick={abrirCrear}
            disabled={soloLectura}
            title={soloLectura ? 'No disponible sin conexión con Oracle' : undefined}
          >
            + Nuevo contacto
          </Button>
        </Container>
      </header>

      <Container className="pb-5">
        {/* ---------- Filtros ---------- */}
        <section className="tarjeta tarjeta-filtros p-3 p-md-4 mb-4" aria-label="Filtros de búsqueda">
          <FiltroForm onBuscar={buscar} deshabilitado={cargando} />
        </section>

        {/* ---------- Aviso de modo respaldo (Oracle desconectado) ---------- */}
        {respaldo && (
          <Alert variant="warning" className="d-flex flex-column flex-md-row gap-2 justify-content-between">
            <div>
              <strong>Sin conexión con Oracle Service Cloud.</strong> Estás viendo la última consulta
              guardada ({respaldo.filtros}), del {formatearFecha(respaldo.fecha)}. Mientras no vuelva la
              conexión no se pueden crear, editar ni eliminar contactos, y los filtros nuevos no se aplican.
            </div>
            <Button
              variant="outline-dark"
              size="sm"
              className="align-self-md-center text-nowrap"
              onClick={() => buscar()}
              disabled={cargando}
            >
              Reintentar
            </Button>
          </Alert>
        )}

        {/* Con el formulario abierto, el error se muestra dentro del modal */}
        {error && !formulario && (
          <Alert variant="danger" dismissible onClose={limpiarError}>
            {error}
          </Alert>
        )}

        {/* ---------- Resultados ---------- */}
        <section className="tarjeta p-3 p-md-4" aria-label="Resultados">
          {primeraCarga ? (
            <div className="text-center py-5">
              <Spinner animation="border" role="status" variant="warning">
                <span className="visually-hidden">Cargando...</span>
              </Spinner>
            </div>
          ) : contactos.length === 0 ? (
            !error && (
              <div className="text-center text-muted py-5">
                <p className="h5 mb-1">No se encontraron contactos</p>
                <p className="mb-0">Prueba con otros filtros o pulsa «Limpiar».</p>
              </div>
            )
          ) : (
            <>
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h2 className="h6 text-uppercase text-secondary mb-0">
                  {contactos.length} {contactos.length === 1 ? 'resultado' : 'resultados'}
                </h2>
                {cargando && <Spinner size="sm" animation="border" variant="warning" />}
              </div>
              <div style={{ opacity: cargando ? 0.5 : 1, transition: 'opacity .2s' }}>
                <ContactosTabla
                  contactos={contactos}
                  onEditar={abrirEditar}
                  onEliminar={setAEliminar}
                  soloLectura={soloLectura}
                />
              </div>
              {contactos.length >= LIMITE_BACKEND && (
                <p className="small text-muted mt-3 mb-0">
                  Mostrando los primeros {LIMITE_BACKEND} resultados. Usa los filtros para afinar la búsqueda.
                </p>
              )}
            </>
          )}
        </section>
      </Container>

      {formulario && (
        <ContactoFormModal
          contacto={formulario.contacto}
          onGuardar={guardar}
          onCerrar={cerrarFormulario}
          error={error}
        />
      )}

      {/* ---------- Confirmar eliminación ---------- */}
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

      {/* ---------- Aviso de éxito (se cierra solo) ---------- */}
      <ToastContainer position="bottom-end" className="p-3 position-fixed">
        <Toast bg="success" show={Boolean(aviso)} onClose={() => setAviso(null)} delay={3500} autohide>
          <Toast.Body className="text-white fw-semibold">{aviso}</Toast.Body>
        </Toast>
      </ToastContainer>
    </>
  )
}
