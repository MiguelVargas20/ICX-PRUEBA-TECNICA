/*
 * Único archivo que habla con el backend.
 * El backend siempre responde los errores con la forma { estado, mensaje, codigoOracle }.
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
async function pedir(url, opciones) {
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

const JSON_HEADERS = { 'Content-Type': 'application/json' }

/* GET /api/contactos?ciudad=&nombre=&correo=&telefono= */
export async function buscar(filtros = {}) {
  const params = new URLSearchParams()
  Object.entries(filtros).forEach(([clave, valor]) => {
    if (valor && valor.trim()) params.set(clave, valor.trim())
  })
  const consulta = params.toString()
  const respuesta = await pedir(consulta ? `${BASE}?${consulta}` : BASE)
  return respuesta.json()
}

/* POST /api/contactos → 201 con el contacto creado */
export async function crear(datos) {
  const respuesta = await pedir(BASE, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(datos),
  })
  return respuesta.json()
}

/* PUT /api/contactos/{id} → 204 (solo nombre, apellido y ciudad) */
export async function actualizar(id, datos) {
  await pedir(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: JSON_HEADERS,
    body: JSON.stringify(datos),
  })
}

/* DELETE /api/contactos/{id} → 204 */
export async function eliminar(id) {
  await pedir(`${BASE}/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
