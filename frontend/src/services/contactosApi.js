/*
 * Centraliza las llamadas HTTP hacia el backend /api/contactos.
 * Se conecta con useContactos.js y convierte respuestas o fallos de red
 * en errores que la interfaz puede mostrar de forma clara.
 */
const BASE = '/api/contactos'

/* Error con la misma forma que ErrorApi del backend. */
export class ErrorApi extends Error {
  constructor(mensaje, estado = 0, codigoOracle = null) {
    super(mensaje)
    this.name = 'ErrorApi'
    this.estado = estado
    this.codigoOracle = codigoOracle
  }
}

const MENSAJE_SIN_SERVIDOR =
  'No se pudo comunicar con el servidor. Verifica que el backend esté encendido.'

/* Hace el fetch y convierte cualquier fallo en un ErrorApi. */
async function realizarPeticion(url, opciones) {
  let respuesta
  try {
    respuesta = await fetch(url, opciones)
  } catch {
    // Falla de red: el fetch ni siquiera llegó al servidor.
    throw new ErrorApi(MENSAJE_SIN_SERVIDOR)
  }

  if (respuesta.ok) return respuesta

  // Respuesta de error: se intenta leer el { mensaje } que manda el backend.
  let cuerpo = null
  try {
    cuerpo = await respuesta.json()
  } catch {
    // Sin JSON (por ejemplo, el proxy de Vite cuando el backend está apagado).
  }
  if (cuerpo?.mensaje) {
    throw new ErrorApi(cuerpo.mensaje, respuesta.status, cuerpo.codigoOracle ?? null)
  }
  throw new ErrorApi(
    respuesta.status >= 500 ? MENSAJE_SIN_SERVIDOR : `Error inesperado (${respuesta.status}).`,
    respuesta.status,
  )
}

/* Lee el JSON de la respuesta. Si llegó otra cosa (ej. HTML del proxy mal configurado), lanza un error entendible. */
async function leerJson(respuesta) {
  const tipo = respuesta.headers.get('content-type') ?? ''
  if (!tipo.includes('application/json')) {
    throw new ErrorApi(
      'El servidor respondió algo que no es JSON. Revisa que el proxy /api apunte al backend (puerto 8080).',
      respuesta.status,
    )
  }
  return respuesta.json()
}

const JSON_HEADERS = { 'Content-Type': 'application/json' }

/*
 * GET /api/contactos?ciudad=&nombre=&correo=&telefono=
 * Devuelve { contactos, desdeRespaldo, fechaRespaldo, filtrosRespaldo }.
 * desdeRespaldo = true significa que Oracle no respondió y los datos vienen de la BD de respaldo.
 */
export async function buscar(filtros = {}) {
  const params = new URLSearchParams()
  Object.entries(filtros).forEach(([clave, valor]) => {
    if (valor && valor.trim()) params.set(clave, valor.trim())
  })
  const consulta = params.toString()
  const respuesta = await realizarPeticion(consulta ? `${BASE}?${consulta}` : BASE)
  return leerJson(respuesta)
}

/* POST /api/contactos → 201 con el contacto creado */
export async function crear(datos) {
  const respuesta = await realizarPeticion(BASE, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(datos),
  })
  return leerJson(respuesta)
}

/* PUT /api/contactos/{id} → 204 (solo nombre, apellido y ciudad) */
export async function actualizar(id, datos) {
  await realizarPeticion(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: JSON_HEADERS,
    body: JSON.stringify(datos),
  })
}

/* DELETE /api/contactos/{id} → 204 */
export async function eliminar(id) {
  await realizarPeticion(`${BASE}/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
