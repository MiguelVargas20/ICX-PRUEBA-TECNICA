import { Badge, Button, Table } from 'react-bootstrap'

const vacio = <span className="text-muted">—</span>

/* Iniciales para el círculo: "Ana Pérez" → "AP". */
const iniciales = (c) => ((c.nombre?.[0] ?? '') + (c.apellido?.[0] ?? '')).toUpperCase() || '?'

/* soloLectura = true cuando Oracle está desconectado: los botones se desactivan. */
export default function ContactosTabla({ contactos, onEditar, onEliminar, soloLectura = false }) {
  return (
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
        {contactos.map((c) => (
          <tr key={c.id}>
            <td className="text-nowrap">
              <div className="d-flex align-items-center gap-3">
                <div className="avatar" aria-hidden="true">
                  {iniciales(c)}
                </div>
                <div>
                  <div className="fw-semibold">{`${c.nombre ?? ''} ${c.apellido ?? ''}`.trim()}</div>
                  <div className="small text-muted">ID {c.id}</div>
                </div>
              </div>
            </td>
            <td>
              {c.ciudad ? (
                <Badge pill bg="none" className="insignia-ciudad">
                  {c.ciudad}
                </Badge>
              ) : (
                vacio
              )}
            </td>
            <td>
              {c.correo ? (
                <a className="enlace-dato" href={`mailto:${c.correo}`}>
                  {c.correo}
                </a>
              ) : (
                vacio
              )}
            </td>
            <td className="text-nowrap">
              {c.telefono ? (
                <a className="enlace-dato" href={`tel:${c.telefono.replace(/[^\d+]/g, '')}`}>
                  {c.telefono}
                </a>
              ) : (
                vacio
              )}
            </td>
            <td className="text-end text-nowrap">
              <Button
                size="sm"
                variant="outline-primary"
                className="me-2"
                onClick={() => onEditar(c)}
                disabled={soloLectura}
              >
                Editar
              </Button>
              <Button size="sm" variant="outline-danger" onClick={() => onEliminar(c)} disabled={soloLectura}>
                Eliminar
              </Button>
            </td>
          </tr>
        ))}
      </tbody>
    </Table>
  )
}
