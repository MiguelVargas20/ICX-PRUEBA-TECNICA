import ContactosPage from './pages/ContactosPage.jsx'

/*
 * Componente raíz que selecciona qué página se muestra.
 * Se conecta con main.jsx, que monta App dentro de la página HTML index.html.
 */
export default function App() {
  // React recibe el resultado y lo renderiza dentro de esta etiqueta.
  // ContactosPage reúne el encabezado, filtros, tabla, modales y mensajes.
  return <ContactosPage />
}
