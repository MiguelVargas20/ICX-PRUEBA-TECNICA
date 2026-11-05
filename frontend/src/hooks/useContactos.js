import { useCallback, useEffect, useRef, useState } from 'react'
import * as api from '../services/contactosApi'

/*
 * Guarda y administra el estado de contactos que usa la página.
 * Se conecta con la página ContactosPage.jsx y el servicio contactosApi.js:
 * contiene la lista, el cargando, los errores, el respaldo y las acciones CRUD.
 */
export function useContactos() {
  const [contactos, setContactos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  // null = datos en vivo de Oracle | { fecha, filtros } = datos de la BD de respaldo
  const [respaldo, setRespaldo] = useState(null)
  const filtrosActuales = useRef({})

  /* Pone en el estado lo que respondió el backend (venga de Oracle o del respaldo). */
  const mostrarResultado = useCallback((resultado) => {
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
      .then((resultado) => activo && mostrarResultado(resultado))
      .catch((e) => activo && setError(e.message))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [mostrarResultado])

  /* Busca con los filtros dados; sin argumento repite la última búsqueda. */
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
        setCargando(false)
      }
    },
    [mostrarResultado],
  )

  /* Ejecuta una acción; si sale bien recarga la lista. Devuelve true/false. */
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
