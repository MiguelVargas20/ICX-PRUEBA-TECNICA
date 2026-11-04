import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Carga primero Bootstrap; después estilos.css podrá sobrescribir sus clases.
import 'bootstrap/dist/css/bootstrap.min.css'
import './estilos.css'
import App from './App.jsx'

// Busca el div con id="root" del index.html. Ahí React dibuja toda la interfaz.
createRoot(document.getElementById('root')).render(
  // StrictMode activa comprobaciones adicionales durante el desarrollo.
  <StrictMode>
    {/* App contiene la página principal que el usuario verá. */}
    <App />
  </StrictMode>,
)
