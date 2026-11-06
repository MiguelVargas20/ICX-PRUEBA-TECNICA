import { Badge, Button, Table } from 'react-bootstrap'

// Tabla de contactos. Solo muestra los datos, las acciones de editar y eliminar
// las recibe por props desde ContactosPage.

// Lo que se muestra cuando falta un dato
const marcadorVacio = <span className="text-muted">—</span>

// Iniciales para el círculo: "Ana Pérez" -> "AP"
const obtenerIniciales = (contacto) =>
  ((contacto.nombre?.[0] ?? '') + (contacto.apellido?.[0] ?? '')).toUpperCase() || '?'

// soloLectura es true cuando se está mostrando el respaldo: se desactivan los botones
export default function ContactosTabla({ contactos, onEditar, onEliminar, soloLectura = false }) {
  return (
    // responsive agrega scroll horizontal en pantallas pequeñas
    <Table hover responsive className="tabla-contactos align-middle mb-0">
      <thead>
        <tr>
          <th>Contacto</th>
          <th>Ciudad</th>
          <th>Correo</th>
          <th>Teléfono</th>
          <th className="text-end">Acciones</th>
        </tr>
      </thead>
      <tbody>
        {contactos.map((contacto) => (
          // key con el id para que React identifique cada fila
          <tr key={contacto.id}>
            <td className="text-nowrap">
              <div className="d-flex align-items-center gap-3">
                <div className="avatar" aria-hidden="true">
                  {obtenerIniciales(contacto)}
                </div>
                <div>
                  <div className="fw-semibold">{`${contacto.nombre ?? ''} ${contacto.apellido ?? ''}`.trim()}</div>
                  <div className="small text-muted">ID {contacto.id}</div>
                </div>
              </div>
            </td>
            <td>
              {contacto.ciudad ? (
                <Badge pill bg="none" className="insignia-ciudad">
                  {contacto.ciudad}
                </Badge>
              ) : (
                marcadorVacio
              )}
            </td>
            <td>
              {contacto.correo ? (
                <a className="enlace-dato" href={`mailto:${contacto.correo}`}>
                  {contacto.correo}
                </a>
              ) : (
                marcadorVacio
              )}
            </td>
            <td className="text-nowrap">
              {/* Para el link tel: dejo solo números y el +, en el celular abre el marcador */}
              {contacto.telefono ? (
                <a className="enlace-dato" href={`tel:${contacto.telefono.replace(/[^\d+]/g, '')}`}>
                  {contacto.telefono}
                </a>
              ) : (
                marcadorVacio
              )}
            </td>
            <td className="text-end text-nowrap">
              <Button
                size="sm"
                variant="outline-primary"
                className="me-2"
                onClick={() => onEditar(contacto)}
                disabled={soloLectura}
              >
                Editar
              </Button>
              <Button size="sm" variant="outline-danger" onClick={() => onEliminar(contacto)} disabled={soloLectura}>
                Eliminar
              </Button>
            </td>
          </tr>
        ))}
      </tbody>
    </Table>
  )
}
