import { useState } from 'react'
import { Alert, Button, Container, Modal } from 'react-bootstrap'
import ContactoFormModal from '../components/ContactoFormModal.jsx'
import ContactosTabla from '../components/ContactosTabla.jsx'
import FiltroForm from '../components/FiltroForm.jsx'
import { useContactos } from '../hooks/useContactos.js'

// Pantalla principal de contactos.
// Toma los datos y acciones del hook useContactos y se los pasa a los componentes
// (filtros, tabla, formulario). También decide qué avisos y modales se muestran.

// Tiene que ser el mismo número que LIMITE en ContactoService.java
const LIMITE_BACKEND = 50

// Pasa la fecha del respaldo a un texto legible, con la hora de Colombia del navegador
const formatearFecha = (iso) =>
  iso
    ? new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
    : 'fecha desconocida'

export default function ContactosPage() {
  const { contactos, cargando, error, respaldo, buscar, crear, actualizar, eliminar, limpiarError } =
    useContactos()

  // Si los datos vienen del respaldo solo se puede consultar.
  // Crear, editar y eliminar necesitan que Oracle esté funcionando.
  const soloLectura = Boolean(respaldo)

  // null = cerrado | { contacto: null } = crear | { contacto } = editar
  const [formulario, setFormulario] = useState(null)
  // El contacto que se va a eliminar (si tiene valor se abre la confirmación)
  const [aEliminar, setAEliminar] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  // Mensaje verde después de crear, editar o eliminar
  const [aviso, setAviso] = useState(null)

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

  // La llama el formulario al guardar. Si hay contacto es editar, si no es crear.
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
    setAEliminar(null)
    // Si falla, el error ya quedó guardado en el hook y sale la alerta roja
    if (ok) setAviso('Contacto eliminado correctamente.')
  }

  // Para mostrar "Cargando..." solo la primera vez y no "No se encontraron contactos"
  const primeraCarga = cargando && contactos.length === 0

  return (
    <>
      {/* Encabezado */}
      <header className="encabezado-icx">
        <Container className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3">
          <div>
            <h1 className="h2 mb-1">Contactos ICX</h1>
            <p className="mb-0">Consulta, crea y actualiza tus contactos desde aquí.</p>
          </div>
          <Button
            className="btn-nuevo"
            size="lg"
            onClick={abrirCrear}
            disabled={soloLectura}
            title={soloLectura ? 'No disponible sin conexión con Oracle' : undefined}
          >
            Agregar contacto
          </Button>
        </Container>
      </header>

      <Container className="pb-5">
        {/* Filtros */}
        <section className="tarjeta tarjeta-filtros p-3 p-md-4 mb-4" aria-label="Filtros de búsqueda">
          <FiltroForm onBuscar={buscar} deshabilitado={cargando} />
        </section>

        {/* Aviso amarillo cuando Oracle no responde y se muestra el respaldo */}
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

        {/* Si el formulario está abierto, el error se muestra dentro del modal y no aquí */}
        {error && !formulario && (
          <Alert variant="danger" dismissible onClose={limpiarError}>
            {error}
          </Alert>
        )}

        {aviso && (
          <Alert variant="success" dismissible onClose={() => setAviso(null)} className="mb-4">
            {aviso}
          </Alert>
        )}

        {/* Resultados */}
        <section className="tarjeta p-3 p-md-4" aria-label="Resultados">
          {primeraCarga ? (
            <div className="text-center py-5">
              <p className="mb-0 text-muted">Cargando contactos...</p>
            </div>
          ) : contactos.length === 0 ? (
            !error && (
              <div className="text-center text-muted py-5">
                <p className="h5 mb-1">No se encontraron contactos</p>
                <p className="mb-0">Cambia los filtros o pulsa «Limpiar» para volver a buscar.</p>
              </div>
            )
          ) : (
            <>
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h2 className="h6 text-uppercase text-secondary mb-0">
                  {contactos.length} {contactos.length === 1 ? 'resultado' : 'resultados'}
                </h2>
                {cargando && <span className="small text-muted">Actualizando...</span>}
              </div>
              <div>
                <ContactosTabla
                  contactos={contactos}
                  onEditar={abrirEditar}
                  onEliminar={setAEliminar}
                  soloLectura={soloLectura}
                />
              </div>
              {/* Si llegaron 50 es porque puede haber más, entonces aviso que usen los filtros */}
              {contactos.length >= LIMITE_BACKEND && (
                <p className="small text-muted mt-3 mb-0">
                  Mostrando los primeros {LIMITE_BACKEND} resultados. Usa los filtros para afinar la búsqueda.
                </p>
              )}
            </>
          )}
        </section>
      </Container>

      {/* El modal solo se monta cuando está abierto, así siempre arranca con los datos correctos */}
      {formulario && (
        <ContactoFormModal
          contacto={formulario.contacto}
          onGuardar={guardar}
          onCerrar={cerrarFormulario}
          error={error}
        />
      )}

      {/* Confirmación antes de eliminar */}
      <Modal
        show={Boolean(aEliminar)}
        onHide={eliminando ? undefined : () => setAEliminar(null)}
        animation={false}
        centered
      >
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
            {eliminando ? 'Eliminando...' : 'Eliminar'}
          </Button>
        </Modal.Footer>
      </Modal>

    </>
  )
}
