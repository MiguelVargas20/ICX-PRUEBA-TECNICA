import { Button, Table } from 'react-bootstrap'

const vacio = (valor) => valor || '—'

export default function ContactosTabla({ contactos, onEditar, onEliminar }) {
  return (
    <Table striped hover responsive className="align-middle">
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Ciudad</th>
          <th>Correo</th>
          <th>Teléfono</th>
          <th className="text-end">Acciones</th>
        </tr>
      </thead>
      <tbody>
        {contactos.map((c) => (
          <tr key={c.id}>
            <td className="text-nowrap">{`${c.nombre ?? ''} ${c.apellido ?? ''}`.trim()}</td>
            <td>{vacio(c.ciudad)}</td>
            <td>{vacio(c.correo)}</td>
            <td className="text-nowrap">{vacio(c.telefono)}</td>
            <td className="text-end text-nowrap">
              <Button size="sm" variant="outline-primary" className="me-2" onClick={() => onEditar(c)}>
                Editar
              </Button>
              <Button size="sm" variant="outline-danger" onClick={() => onEliminar(c)}>
                Eliminar
              </Button>
            </td>
          </tr>
        ))}
      </tbody>
    </Table>
  )
}
