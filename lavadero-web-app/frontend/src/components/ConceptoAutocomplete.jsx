import { useEffect, useRef, useState } from 'react';

/**
 * Campo de texto con lista de sugerencias (sustituye a <datalist>, cuya lista
 * dibuja el navegador y no se puede agrandar ni dar estilo).
 *
 * - Se puede escribir libremente (p. ej. "Lavado Completo Turismo - 1234ABC").
 * - La lista se filtra mientras se escribe (sin distinguir mayúsculas ni acentos).
 * - Teclado: ↑ ↓ para moverse, Enter para elegir, Escape para cerrar.
 * - La lista usa posición fija: no la recorta el scroll del formulario y se abre
 *   hacia arriba si no cabe debajo.
 *
 * Props:
 *   name, value, placeholder, className  → como un <input> normal
 *   onChange(e)   Recibe { target: { name, value } } (compatible con los handlers existentes)
 *   onBlur(e)     Se llama al salir del campo y también al elegir una opción
 *   opciones      [{ valor, descripcion, precio }]
 */

const normalizar = (txt) =>
  (txt || '').toString().normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

const formatearPrecio = (precio) =>
  typeof precio === 'number'
    ? new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precio)
    : '';

const ConceptoAutocomplete = ({ name, value, onChange, onBlur, opciones = [], placeholder, className = '' }) => {
  const inputRef = useRef(null);
  const listaRef = useRef(null);
  const [abierto, setAbierto] = useState(false);
  const [resaltado, setResaltado] = useState(-1);
  const [posicion, setPosicion] = useState(null);

  // Si el texto coincide exactamente con una opción (o está vacío), se muestran todas
  const texto = normalizar(value);
  const coincideExacto = opciones.some(o => normalizar(o.descripcion) === texto);
  const filtradas = !texto || coincideExacto
    ? opciones
    : opciones.filter(o => normalizar(o.descripcion).includes(texto));

  // Coloca la lista junto al campo (se llama desde eventos, no durante el render)
  const calcularPosicion = () => {
    const el = inputRef.current;
    if (!el) return;
    const r = el.getBoundingClientRect();
    const margen = 8;
    const espacioAbajo = window.innerHeight - r.bottom - margen;
    const espacioArriba = r.top - margen;
    const haciaArriba = espacioAbajo < 260 && espacioArriba > espacioAbajo;
    const maxHeight = Math.min(384, haciaArriba ? espacioArriba : espacioAbajo);
    setPosicion({
      left: r.left,
      width: r.width,
      maxHeight,
      ...(haciaArriba
        ? { bottom: window.innerHeight - r.top + 4 }
        : { top: r.bottom + 4 }),
    });
  };

  const abrir = () => {
    calcularPosicion();
    setAbierto(true);
  };

  // Mientras está abierta, recolocar la lista al hacer scroll o cambiar el tamaño de la ventana
  useEffect(() => {
    if (!abierto) return;
    const recolocar = () => {
      const el = inputRef.current;
      if (!el) return;
      const r = el.getBoundingClientRect();
      const margen = 8;
      const espacioAbajo = window.innerHeight - r.bottom - margen;
      const espacioArriba = r.top - margen;
      const haciaArriba = espacioAbajo < 260 && espacioArriba > espacioAbajo;
      setPosicion({
        left: r.left,
        width: r.width,
        maxHeight: Math.min(384, haciaArriba ? espacioArriba : espacioAbajo),
        ...(haciaArriba ? { bottom: window.innerHeight - r.top + 4 } : { top: r.bottom + 4 }),
      });
    };
    window.addEventListener('scroll', recolocar, true);
    window.addEventListener('resize', recolocar);
    return () => {
      window.removeEventListener('scroll', recolocar, true);
      window.removeEventListener('resize', recolocar);
    };
  }, [abierto]);

  // Mantener visible la opción resaltada al moverse con el teclado
  useEffect(() => {
    if (resaltado < 0 || !listaRef.current) return;
    listaRef.current.children[resaltado]?.scrollIntoView({ block: 'nearest' });
  }, [resaltado]);

  const elegir = (opcion) => {
    const evento = { target: { name, value: opcion.descripcion } };
    onChange?.(evento);
    onBlur?.(evento); // rellena el precio al momento, sin esperar a salir del campo
    setAbierto(false);
    setResaltado(-1);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (!abierto) abrir();
      setResaltado(i => Math.min(i + 1, filtradas.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setResaltado(i => Math.max(i - 1, 0));
    } else if (e.key === 'Enter' && abierto && resaltado >= 0 && filtradas[resaltado]) {
      e.preventDefault(); // no enviar el formulario
      elegir(filtradas[resaltado]);
    } else if (e.key === 'Escape') {
      setAbierto(false);
      setResaltado(-1);
    }
  };

  return (
    <div className="relative">
      <input
        ref={inputRef}
        type="text"
        name={name}
        value={value}
        autoComplete="off"
        placeholder={placeholder}
        className={`${className} pr-10`}
        role="combobox"
        aria-expanded={abierto}
        aria-autocomplete="list"
        onChange={(e) => {
          onChange?.(e);
          abrir();
          setResaltado(-1);
        }}
        onFocus={abrir}
        onClick={abrir}
        onBlur={(e) => {
          setAbierto(false);
          setResaltado(-1);
          onBlur?.(e);
        }}
        onKeyDown={handleKeyDown}
      />
      <button
        type="button"
        tabIndex={-1}
        aria-label="Mostrar servicios"
        onMouseDown={(e) => e.preventDefault()}
        onClick={() => {
          if (abierto) setAbierto(false);
          else abrir();
          inputRef.current?.focus();
        }}
        className="absolute right-2 top-1/2 -translate-y-1/2 px-2 text-gray-500 hover:text-gray-800"
      >
        ▾
      </button>

      {abierto && posicion && filtradas.length > 0 && (
        <ul
          ref={listaRef}
          role="listbox"
          className="fixed z-[70] bg-white border border-gray-200 rounded-lg shadow-2xl overflow-y-auto py-1"
          style={posicion}
        >
          {filtradas.map((opcion, i) => (
            <li
              key={opcion.valor}
              role="option"
              aria-selected={i === resaltado}
              // mousedown + preventDefault: elegir sin que el input pierda el foco antes
              onMouseDown={(e) => {
                e.preventDefault();
                elegir(opcion);
              }}
              onMouseEnter={() => setResaltado(i)}
              className={`flex items-center justify-between gap-4 px-4 py-3 text-base cursor-pointer ${
                i === resaltado ? 'bg-blue-50 text-blue-900' : 'text-gray-800'
              }`}
            >
              <span>{opcion.descripcion}</span>
              {opcion.precio != null && (
                <span className="shrink-0 text-sm font-semibold text-gray-500">
                  {formatearPrecio(opcion.precio)}
                </span>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ConceptoAutocomplete;