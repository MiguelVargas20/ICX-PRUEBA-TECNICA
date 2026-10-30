import { useCallback, useEffect, useRef, useState } from 'react'
import * as api from '../services/contactosApi'

/*
 * Guarda la lista, si está cargando y el mensaje de error.
 * Cuando crear / actualizar / eliminar terminan bien, vuelve a cargar la lista
 * con los mismos filtros que tenía la última búsqueda.
 */
export function useContactos() {
  const [contactos, setContactos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const filtrosActuales = useRef({})

  /* Primera carga (sin filtros). Empieza con cargando = true, por eso no hay setState al inicio. */
  useEffect(() => {
    let activo = true
    api
      .buscar({})
      .then((lista) => activo && setContactos(lista))
      .catch((e) => activo && setError(e.message))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [])

  /* Busca con los filtros dados; sin argumento repite la última búsqueda. */
  const buscar = useCallback(async (filtros = filtrosActuales.current) => {
    filtrosActuales.current = filtros
    setCargando(true)
    setError(null)
    try {
      setContactos(await api.buscar(filtros))
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }, [])

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

  return { contactos, cargando, error, buscar, crear, actualizar, eliminar, limpiarError }
}
