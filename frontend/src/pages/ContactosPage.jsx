import { useState } from 'react'
import { Alert, Button, Container, Modal } from 'react-bootstrap'
import ContactoFormModal from '../components/ContactoFormModal.jsx'
import ContactosTabla from '../components/ContactosTabla.jsx'
import FiltroForm from '../components/FiltroForm.jsx'
import { useContactos } from '../hooks/useContactos.js'

/*
 * Archivo principal de la pantalla de contactos.
 * Une el hook useContactos.js con los componentes de filtros, tabla y formulario.
 * También decide cuándo mostrar avisos, formularios, confirmaciones y resultados.
 */

/*
 * Este valor debe coincidir con LIMITE en ContactoService.java.
 * Se usa para avisar al usuario cuando Oracle entregó el máximo de registros permitido.
 */
const LIMITE_BACKEND = 50

/*
 * Convierte la fecha de H2 a un texto entendido por el usuario.
 * El navegador toma su hora local para mostrar fecha y hora.
 */
const formatearFecha = (iso) =>
  iso
    ? new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
    : 'fecha desconocida'

/*
 * Crea la página completa y guarda en React qué formulario o confirmación está abierto.
 * El hook useContactos administra los datos; esta función solo conecta esa información con los botones.
 */
export default function ContactosPage() {
  const { contactos, cargando, error, respaldo, buscar, crear, actualizar, eliminar, limpiarError } =
    useContactos()

  /*
   * Cuando la respuesta viene de H2, solo permitimos consultar.
   * Crear, editar y eliminar necesitan una conexión activa con Oracle.
   */
  const soloLectura = Boolean(respaldo)

  // null = cerrado | { contacto: null } = crear | { contacto } = editar
  const [formulario, setFormulario] = useState(null)
  const [aEliminar, setAEliminar] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  const [aviso, setAviso] = useState(null)

  /*
   * Limpia un error anterior y abre el modal en modo creación.
   * Al enviar contacto: null, el formulario sabe que debe crear y no editar.
   */
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
    /*
     * Pregunta si el modal estaba editando un registro existente.
     * Después vuelve a cargar la lista con los filtros guardados por el hook.
     */
    const editando = Boolean(formulario.contacto)
    const ok = editando ? await actualizar(formulario.contacto.id, datos) : await crear(datos)
    if (ok) setAviso(editando ? 'Contacto actualizado correctamente.' : 'Contacto creado correctamente.')
    return ok
  }

  const confirmarEliminar = async () => {
    /*
     * Indica que la eliminación está en curso, llama al hook y luego cierra el modal.
     * Si falla, el estado ya contiene el mensaje de error y se muestra como alerta roja.
     */
    setEliminando(true)
    const ok = await eliminar(aEliminar.id)
    setEliminando(false)
    setAEliminar(null)
    if (ok) setAviso('Contacto eliminado correctamente.')
  }

  /*
   * En la primera carga todavía no hay contactos.
   * Con este valor evitamos mostrar el texto de lista vacía mientras estamos esperando.
   */
  const primeraCarga = cargando && contactos.length === 0

  return (
    <>
      {/* ---------- Encabezado ---------- */}
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

        {/* Aviso simple de la última acción realizada. Se cierra al pulsar la X. */}
        {aviso && (
          <Alert variant="success" dismissible onClose={() => setAviso(null)} className="mb-4">
            {aviso}
          </Alert>
        )}

        {/* ---------- Resultados ---------- */}
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
