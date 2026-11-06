// Aquí están todas las llamadas al backend (/api/contactos).
// Es el único archivo que usa fetch, los demás llaman estas funciones.
// /api no apunta directo al 8080: en desarrollo pasa por el proxy de Vite (vite.config.js)
const BASE = '/api/contactos'

// Error con los mismos campos que ErrorApi del backend (mensaje, estado, codigoOracle)
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

// Hace el fetch y si algo sale mal lanza un ErrorApi con un mensaje que se pueda mostrar
async function realizarPeticion(url, opciones) {
  let respuesta
  try {
    respuesta = await fetch(url, opciones)
  } catch {
    // El fetch ni siquiera llegó: backend apagado o sin red
    throw new ErrorApi(MENSAJE_SIN_SERVIDOR)
  }

  // ok = códigos 200 a 299
  if (respuesta.ok) return respuesta

  // Si es un error, intento leer el { mensaje } que manda el backend
  let cuerpo = null
  try {
    cuerpo = await respuesta.json()
  } catch {
    // A veces no viene JSON, por ejemplo cuando el backend está apagado y responde el proxy
  }
  if (cuerpo?.mensaje) {
    throw new ErrorApi(cuerpo.mensaje, respuesta.status, cuerpo.codigoOracle ?? null)
  }
  throw new ErrorApi(
    respuesta.status >= 500 ? MENSAJE_SIN_SERVIDOR : `Error inesperado (${respuesta.status}).`,
    respuesta.status,
  )
}

// Lee la respuesta como JSON, pero primero reviso que sí sea JSON.
// Si llega HTML casi siempre es porque el proxy no está apuntando al backend.
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

// GET /api/contactos?ciudad=&nombre=&correo=&telefono=
// Devuelve { contactos, desdeRespaldo, fechaRespaldo, filtrosRespaldo }.
// Si desdeRespaldo es true, Oracle no respondió y los datos son los guardados en H2.
export async function buscar(filtros = {}) {
  // Solo mando los filtros que tienen texto
  const params = new URLSearchParams()
  Object.entries(filtros).forEach(([clave, valor]) => {
    if (valor && valor.trim()) params.set(clave, valor.trim())
  })
  const consulta = params.toString()
  const respuesta = await realizarPeticion(consulta ? `${BASE}?${consulta}` : BASE)
  return leerJson(respuesta)
}

// POST /api/contactos -> responde 201 con el contacto creado
export async function crear(datos) {
  const respuesta = await realizarPeticion(BASE, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(datos),
  })
  return leerJson(respuesta)
}

// PUT /api/contactos/{id} -> responde 204 (sin cuerpo)
export async function actualizar(id, datos) {
  await realizarPeticion(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: JSON_HEADERS,
    body: JSON.stringify(datos),
  })
}

// DELETE /api/contactos/{id} -> responde 204
export async function eliminar(id) {
  await realizarPeticion(`${BASE}/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
