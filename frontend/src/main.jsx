import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Primero Bootstrap y después mis estilos, para poder cambiar cosas de Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import './estilos.css'
import App from './App.jsx'

// Punto de entrada del frontend.
// React dibuja toda la app dentro del <div id="root"> que está en index.html
createRoot(document.getElementById('root')).render(
  // StrictMode solo hace revisiones extra mientras se desarrolla
  <StrictMode>
    <App />
  </StrictMode>,
)
