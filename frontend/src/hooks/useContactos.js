import { useCallback, useEffect, useRef, useState } from 'react'
import * as api from '../services/contactosApi'

/*
 * Guarda la lista, si está cargando, el mensaje de error y si estamos en "modo respaldo".
 * Cuando crear / actualizar / eliminar terminan bien, vuelve a cargar la lista
 * con los mismos filtros que tenía la última búsqueda.
 */
export function useContactos() {
  const [contactos, setContactos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  // null = datos en vivo de Oracle | { fecha, filtros } = datos de la BD de respaldo
  const [respaldo, setRespaldo] = useState(null)
  const filtrosActuales = useRef({})

  /* Pone en el estado lo que respondió el backend (venga de Oracle o del respaldo). */
  const aplicarResultado = useCallback((resultado) => {
    setContactos(resultado.contactos ?? [])
    setRespaldo(
      resultado.desdeRespaldo
        ? { fecha: resultado.fechaRespaldo, filtros: resultado.filtrosRespaldo }
        : null,
    )
  }, [])

  /* Primera carga (sin filtros). Empieza con cargando = true, por eso no hay setState al inicio. */
  useEffect(() => {
    let activo = true
    api
      .buscar({})
      .then((resultado) => activo && aplicarResultado(resultado))
      .catch((e) => activo && setError(e.message))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [aplicarResultado])

  /* Busca con los filtros dados; sin argumento repite la última búsqueda. */
  const buscar = useCallback(
    async (filtros = filtrosActuales.current) => {
      filtrosActuales.current = filtros
      setCargando(true)
      setError(null)
      try {
        aplicarResultado(await api.buscar(filtros))
      } catch (e) {
        setError(e.message)
      } finally {
        setCargando(false)
      }
    },
    [aplicarResultado],
  )

  /* Ejecuta una acción; si sale bien recarga la lista. Devuelve true/false. */
  const ejecutar = useCallback(
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

  const crear = useCallback((datos) => ejecutar(() => api.crear(datos)), [ejecutar])
  const actualizar = useCallback((id, datos) => ejecutar(() => api.actualizar(id, datos)), [ejecutar])
  const eliminar = useCallback((id) => ejecutar(() => api.eliminar(id)), [ejecutar])
  const limpiarError = useCallback(() => setError(null), [])

  return { contactos, cargando, error, respaldo, buscar, crear, actualizar, eliminar, limpiarError }
}
