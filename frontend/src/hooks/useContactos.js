import { useCallback, useEffect, useRef, useState } from 'react'
import * as api from '../services/contactosApi'

// Hook con el estado y las acciones de los contactos.
// ContactosPage lo usa y no tiene que saber cómo se llama al backend.
// Guarda: la lista, si está cargando, el error y si los datos vienen del respaldo.
export function useContactos() {
  const [contactos, setContactos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  // null = datos de Oracle | { fecha, filtros } = datos del respaldo H2
  const [respaldo, setRespaldo] = useState(null)
  // Los últimos filtros. Uso useRef y no useState porque cambiarlos no tiene que
  // volver a pintar la pantalla, solo los necesito para repetir la búsqueda
  const filtrosActuales = useRef({})

  // Guarda en el estado lo que respondió el backend
  const mostrarResultado = useCallback((resultado) => {
    setContactos(resultado.contactos ?? [])
    setRespaldo(
      resultado.desdeRespaldo
        ? { fecha: resultado.fechaRespaldo, filtros: resultado.filtrosRespaldo }
        : null,
    )
  }, [])

  // Primera carga al abrir la página, sin filtros.
  // "activo" es para no cambiar el estado si el componente ya se desmontó
  useEffect(() => {
    let activo = true
    api
      .buscar({})
      .then((resultado) => activo && mostrarResultado(resultado))
      .catch((e) => activo && setError(e.message))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [mostrarResultado])

  // Busca con los filtros que le pasen. Si no le paso nada, repite la última búsqueda
  const buscar = useCallback(
    async (filtros = filtrosActuales.current) => {
      filtrosActuales.current = filtros
      setCargando(true)
      setError(null)
      try {
        mostrarResultado(await api.buscar(filtros))
      } catch (e) {
        setError(e.message)
      } finally {
        // finally se ejecuta siempre, salga bien o mal
        setCargando(false)
      }
    },
    [mostrarResultado],
  )

  // La uso para crear, editar y eliminar.
  // Si sale bien vuelvo a buscar para que la tabla quede actualizada.
  // Devuelve true o false para que el formulario sepa si se cierra o no.
  const ejecutarAccion = useCallback(
    async (accion) => {
      setError(null)
      try {
        await accion()
      } catch (e) {
        setError(e.message)
        return false
      }
      await buscar()
      return true
    },
    [buscar],
  )

  const crear = useCallback((datos) => ejecutarAccion(() => api.crear(datos)), [ejecutarAccion])
  const actualizar = useCallback((id, datos) => ejecutarAccion(() => api.actualizar(id, datos)), [ejecutarAccion])
  const eliminar = useCallback((id) => ejecutarAccion(() => api.eliminar(id)), [ejecutarAccion])
  const limpiarError = useCallback(() => setError(null), [])

  return { contactos, cargando, error, respaldo, buscar, crear, actualizar, eliminar, limpiarError }
}
