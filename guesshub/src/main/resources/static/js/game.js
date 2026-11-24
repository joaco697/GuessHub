/* GuessHub — game.js (Modo Juego Continuo y Aleatorio) */
(() => {
  'use strict';

  const TIEMPO_MAX = 60;
  const BASE_PUNTOS = 1000;
  const COSTO_PISTA = 200;
  const COSTO_SEGUNDO = 5;

  const MODOS = {
    pelicula: { nombre: 'Películas', icono: '🎬', titulo: '¿Qué película es?', url: '/api/pelicula/random', pista2: 'Director', pista3: 'Año', placeholder: 'Título de la película...', dif: 'FACIL' },
    cancion: { nombre: 'Música', icono: '🎵', titulo: '¿Qué canción es?', url: '/api/random', pista2: 'Álbum', pista3: 'Año', placeholder: 'Nombre de la canción o tema...', dif: 'FACIL' },
    equipo: { nombre: 'Deportes', icono: '⚽', titulo: '¿Qué equipo es?', url: '/equipo/random', pista2: 'Año de fundación', pista3: 'Estadio', placeholder: 'Nombre del equipo o club...', dif: 'FACIL' },
    diario: { nombre: 'Desafío del Día', icono: '👑', titulo: 'Desafío Diario', url: '/api/desafio-diario', pista2: 'Pista 2', pista3: 'Pista 3', placeholder: 'Tu respuesta...', dif: 'MEDIO' },
  };

  const param = (new URLSearchParams(location.search).get('cat') || 'random').toLowerCase();
  const FIJO = {
    diario: 'diario',
    desafio: 'diario',
    dia: 'diario',
    peliculas: 'pelicula',
    pelicula: 'pelicula',
    musica: 'cancion',
    cancion: 'cancion',
    deportes: 'equipo',
    deporte: 'equipo',
    equipo: 'equipo'
  }[param] || null;

  const $ = GH.$;
  const ui = {
    catBadge: $('#game-cat-badge'),
    catIcon: $('#game-cat-icon'),
    catName: $('#game-cat-name'),
    title: $('#q-title'),
    sessionStreak: $('#game-session-streak'),
    timerVal: $('#timer-val'),
    stake: $('#stake'),
    timebarFill: $('#timebar-fill'),
    state: $('#state'),
    stateMsg: $('#state-msg'),
    stateRetry: $('#state-retry'),
    clues: $('#clues'),
    form: $('#ticket'),
    input: $('#answer'),
    submit: $('#btn-enviar'),
    listbox: $('#listbox'),
    guestHint: $('#guest-hint'),
    resultModal: $('#result'),
    resIco: $('#res-ico'),
    resTitle: $('#res-title'),
    resAnswer: $('#res-answer'),
    resPtsNum: $('#res-pts-num'),
    resGuest: $('#res-guest'),
    resNext: $('#res-next'),
  };

  const S = {
    modo: null,
    q: null,
    pistas: 1,
    nueva: 0,
    t0: 0,
    seg: 0,
    timer: null,
    listo: true,
    rachaSesion: 0,
    esDiario: false,
  };

  /* ---------- Selección de Categoría ---------- */
  function elegirModo() {
    if (FIJO) return FIJO;
    const claves = ['pelicula', 'cancion', 'equipo'];
    let siguiente;
    do {
      siguiente = claves[Math.floor(Math.random() * claves.length)];
    } while (siguiente === S.modo && claves.length > 1);
    return siguiente;
  }

  function pintarModo() {
    const m = MODOS[S.modo];
    if (ui.catIcon) ui.catIcon.textContent = m.icono;
    if (ui.catName) ui.catName.textContent = FIJO ? m.nombre : `Aleatorio (${m.nombre})`;
    if (ui.title) ui.title.textContent = m.titulo;
    if (ui.input) ui.input.placeholder = m.placeholder;
    document.title = 'GuessHub — ' + m.nombre;

    if (ui.sessionStreak) {
      if (S.rachaSesion > 0) {
        ui.sessionStreak.textContent = `🔥 Racha en partida: ${S.rachaSesion} acierto${S.rachaSesion > 1 ? 's' : ''} consecutivo${S.rachaSesion > 1 ? 's' : ''}!`;
        ui.sessionStreak.hidden = false;
      } else {
        ui.sessionStreak.hidden = true;
      }
    }
  }

  /* ---------- Puntos y Timer ---------- */
  const puntosEnJuego = () => {
    let pts = BASE_PUNTOS;
    if (S.pistas >= 2) pts -= COSTO_PISTA;
    if (S.pistas >= 3) pts -= COSTO_PISTA;
    pts -= (S.seg * COSTO_SEGUNDO);
    return Math.max(50, pts);
  };

  function pintarPuntos() {
    const restan = Math.max(0, TIEMPO_MAX - S.seg);
    if (ui.stake) ui.stake.textContent = GH.fmt(puntosEnJuego());
    if (ui.timerVal) ui.timerVal.textContent = restan;
    if (ui.timebarFill) {
      const pct = (restan / TIEMPO_MAX) * 100;
      ui.timebarFill.style.width = pct + '%';
      if (restan <= 10) {
        ui.timebarFill.style.backgroundColor = '#FFA726';
      } else {
        ui.timebarFill.style.backgroundColor = '#4CAF50';
      }
    }
  }

  function correrTimer() {
    clearInterval(S.timer);
    S.timer = setInterval(() => {
      const s = Math.min(TIEMPO_MAX, Math.floor((Date.now() - S.t0) / 1000));
      if (s !== S.seg) {
        S.seg = s;
        pintarPuntos();
      }
      if (s >= TIEMPO_MAX) {
        resolver('', true);
      }
    }, 250);
  }

  const detenerTimer = () => clearInterval(S.timer);

  /* ---------- Render de Pistas Progresivas ---------- */
  function renderPistas() {
    if (!ui.clues) return;
    ui.clues.innerHTML = '';
    if (!S.q) {
      ui.clues.innerHTML = '<li class="clue-item" style="padding:0.75rem;background:#F9FAFB;border-radius:8px">Cargando desafío...</li>';
      return;
    }

    const m = MODOS[S.modo];
    for (let i = 1; i <= 3; i++) {
      const li = document.createElement('li');
      li.style.listStyle = 'none';
      li.style.marginBottom = '0.6rem';

      if (i <= S.pistas) {
        // Pista desbloqueada
        const box = document.createElement('div');
        box.style.display = 'flex';
        box.style.alignItems = 'center';
        box.style.gap = '0.75rem';
        box.style.padding = '0.75rem 1rem';
        box.style.borderRadius = '8px';
        box.style.background = '#E8F5E9';
        box.style.border = '1px solid #C8E6C9';
        box.style.color = '#1B5E20';
        box.style.fontWeight = '600';
        box.style.fontSize = '0.95rem';

        box.innerHTML = `<span style="background:#4CAF50;color:#fff;border-radius:50%;width:24px;height:24px;display:flex;align-items:center;justify-content:center;font-size:0.75rem;font-weight:900">${i}</span>
                         <span class="clue-text" style="flex:1">${GH.esc(S.q.pistas[i - 1] || '—')}</span>`;
        li.appendChild(box);
      } else {
        // Pista bloqueada (se puede revelar si es la siguiente)
        const etiqueta = (S.q.etiquetas && S.q.etiquetas[i - 1]) || (i === 2 ? m.pista2 : m.pista3);
        const habilitada = (i === S.pistas + 1) && !S.listo;

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.disabled = !habilitada;
        btn.style.width = '100%';
        btn.style.display = 'flex';
        btn.style.alignItems = 'center';
        btn.style.justifyContent = 'space-between';
        btn.style.padding = '0.75rem 1rem';
        btn.style.borderRadius = '8px';
        btn.style.border = '1px dashed #CBD5E1';
        btn.style.background = habilitada ? '#FFF8E1' : '#F8FAFC';
        btn.style.color = habilitada ? '#B78103' : '#94A3B8';
        btn.style.cursor = habilitada ? 'pointer' : 'not-allowed';
        btn.style.fontWeight = '700';
        btn.style.fontSize = '0.9rem';

        btn.innerHTML = `<span style="display:flex;align-items:center;gap:0.5rem">
                           <span>🔒</span> Revelar Pista ${i}: ${GH.esc(etiqueta)}
                         </span>
                         <span style="font-size:0.8rem;background:rgba(255,167,38,0.2);padding:2px 6px;border-radius:4px;color:#B78103">−${COSTO_PISTA} pts</span>`;

        if (habilitada) {
          btn.addEventListener('click', () => revelarPista(i));
        }
        li.appendChild(btn);
      }

      ui.clues.appendChild(li);
    }
  }

  function revelarPista(i) {
    if (S.listo || i !== S.pistas + 1) return;
    S.pistas = i;
    S.nueva = i;
    renderPistas();
    pintarPuntos();
    if (ui.input) ui.input.focus();
  }

  function bloquear(b) {
    S.listo = b;
    if (ui.input) ui.input.disabled = b;
    if (ui.submit) ui.submit.disabled = b;
    renderPistas();
  }

  /* ---------- Carga de Desafío ---------- */
  function normalizar(modo, data) {
    const d = Array.isArray(data) ? data[0] : data;
    if (!d) return null;
    if (modo === 'diario') {
      return {
        id: d.id,
        pistas: d.pistas || [],
        etiquetas: d.etiquetasPistas || ['Pista 1', 'Pista 2', 'Pista 3'],
        categoriaReal: d.categoria,
        dificultad: d.dificultad || 'MEDIO'
      };
    }
    if (modo === 'equipo') {
      return {
        id: d.id,
        pistas: ['País: ' + d.pais, 'Fundado en: ' + d.fundacion, 'Estadio: ' + d.estadio],
        etiquetas: ['País', 'Año de fundación', 'Estadio local'],
        dificultad: 'FACIL'
      };
    }
    return {
      id: d.id,
      pistas: [d.pista1, d.pista2, d.pista3],
      etiquetas: [modo === 'pelicula' ? 'Género y Sinopsis' : 'Artista / Género', modo === 'pelicula' ? 'Director' : 'Álbum', 'Año'],
      dificultad: 'FACIL'
    };
  }

  function mostrarError(msg) {
    if (ui.stateMsg) ui.stateMsg.textContent = msg;
    if (ui.state) ui.state.hidden = false;
  }

  async function nuevaPregunta() {
    detenerTimer();
    cerrarLista();
    S.modo = elegirModo();
    S.esDiario = (S.modo === 'diario');
    S.q = null;
    S.pistas = 1;
    S.nueva = 0;
    S.seg = 0;

    if (ui.state) ui.state.hidden = true;
    if (ui.input) {
      ui.input.value = '';
      ui.input.classList.remove('is-correct', 'is-wrong');
    }

    pintarModo();
    bloquear(true);
    pintarPuntos();

    try {
      const m = MODOS[S.modo];
      let res = await fetch(m.url, { headers: GH.authHeaders() });
      if (res.status === 401) {
        GH.clearSession();
        GH.refreshChip();
        GH.toast('Sesión expirada. Podés seguir jugando como invitado.', 'info');
        res = await fetch(m.url);
      }
      if (res.status === 204) return mostrarError('No hay preguntas disponibles en este momento.');
      if (!res.ok) throw new Error('Error al cargar pregunta');

      const data = await res.json();
      const q = normalizar(S.modo, data);
      if (!q || q.id == null) return mostrarError('Respuesta inesperada del servidor.');

      S.q = q;
      bloquear(false);
      S.t0 = Date.now();
      pintarPuntos();
      correrTimer();
      if (ui.input) ui.input.focus();
    } catch {
      mostrarError('No pudimos conectar con los servicios en vivo. Reintentá en unos segundos.');
    }
  }

  /* ---------- Enviar y Resolver Partida ---------- */
  async function guardarPartida(correcto, pistas, seg, modo) {
    if (!GH.isLogged()) return null; // Invitado
    try {
      const dif = (S.q && S.q.dificultad) ? S.q.dificultad : (MODOS[modo].dif || 'FACIL');
      const catGuardar = (modo === 'diario' && S.q && S.q.categoriaReal) ? S.q.categoriaReal : modo;

      const res = await fetch('/api/partida', {
        method: 'POST',
        headers: GH.authHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify({
          correcto,
          pistasUsadas: pistas,
          segundosUsados: seg,
          categoria: catGuardar,
          dificultad: dif,
          rachaPartida: S.rachaSesion,
          esDesafioDiario: S.esDiario
        }),
      });
      if (!res.ok) return undefined;
      return await res.json();
    } catch {
      return undefined;
    }
  }

  async function resolver(respuesta, agotado = false) {
    if (S.listo || !S.q) return;
    detenerTimer();
    bloquear(true);
    cerrarLista();

    const { seg, pistas, modo } = { seg: S.seg, pistas: S.pistas, modo: S.modo };

    let r;
    try {
      const res = await fetch('/api/respuesta', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ preguntaId: S.q.id, respuesta }),
      });
      if (!res.ok) throw new Error('respuesta');
      r = await res.json();
    } catch {
      if (!agotado) {
        GH.toast('Error al validar la respuesta. Probá de nuevo.', 'error');
        bloquear(false);
        correrTimer();
        return;
      }
      r = { correcto: false, tituloCorrecto: '—' };
    }

    const correcto = !!r.correcto && !agotado;
    if (correcto) {
      S.rachaSesion++;
    } else {
      S.rachaSesion = 0;
    }

    const resultadoApi = await guardarPartida(correcto, pistas, seg, modo);

    if (resultadoApi && resultadoApi.insigniasNuevas && resultadoApi.insigniasNuevas.length > 0) {
      GH.toast('🏆 ¡Nueva insignia desbloqueada: ' + resultadoApi.insigniasNuevas.join(', ') + '!', 'success');
    }

    const puntos = resultadoApi ? resultadoApi.puntosGanados : (resultadoApi === null ? null : undefined);
    const mult = resultadoApi ? resultadoApi.multiplicador : 1.0;

    setTimeout(() => {
      mostrarResultado({ correcto, agotado, titulo: r.tituloCorrecto, puntos, multiplicador: mult });
    }, correcto ? 300 : 600);
  }

  if (ui.form) {
    ui.form.addEventListener('submit', (e) => {
      e.preventDefault();
      if (S.listo || !S.q) return;
      const val = ui.input.value.trim();
      if (!val) {
        GH.toast('Escribí una respuesta antes de enviar');
        ui.input.focus();
        return;
      }
      resolver(val, false);
    });
  }

  /* ---------- Modal de Resultado ---------- */
  function mostrarResultado({ correcto, agotado, titulo, puntos, multiplicador }) {
    if (ui.resIco) ui.resIco.textContent = correcto ? '🎉' : agotado ? '⏱️' : '💡';
    if (ui.resTitle) ui.resTitle.textContent = correcto ? '¡Correcto!' : agotado ? '¡Se acabó el tiempo!' : 'Respuesta Incorrecta';
    if (ui.resAnswer) ui.resAnswer.textContent = titulo || '—';

    if (ui.resPtsNum) {
      if (typeof puntos === 'number') {
        let texto = (puntos > 0 ? '+' : '') + GH.fmt(puntos) + ' pts';
        if (multiplicador && multiplicador > 1.0) {
          texto += ` (${multiplicador}x)`;
        }
        ui.resPtsNum.textContent = texto;
        if (ui.resGuest) ui.resGuest.hidden = true;
      } else {
        ui.resPtsNum.textContent = 'Partida de Invitado';
        if (ui.resGuest) ui.resGuest.hidden = false;
      }
    }

    GH.openModal('result');
  }

  if (ui.resNext) {
    ui.resNext.addEventListener('click', () => {
      GH.closeModal('result');
      nuevaPregunta();
    });
  }

  if (ui.stateRetry) {
    ui.stateRetry.addEventListener('click', nuevaPregunta);
  }

  /* ---------- Autocompletado ---------- */
  let acTimer = null;
  function cerrarLista() {
    if (!ui.listbox) return;
    ui.listbox.hidden = true;
    ui.listbox.innerHTML = '';
  }

  async function buscar(q) {
    const modoBusqueda = S.modo === 'diario'
      ? (S.q && S.q.categoriaReal ? S.q.categoriaReal : 'pelicula')
      : (S.modo || 'pelicula');

    try {
      const res = await fetch('/api/buscar?q=' + encodeURIComponent(q) + '&modo=' + encodeURIComponent(modoBusqueda));
      if (!res.ok || S.listo) return;
      const data = await res.json();
      if (!Array.isArray(data) || data.length === 0 || S.listo) {
        return cerrarLista();
      }

      ui.listbox.innerHTML = '';
      data.forEach((item) => {
        const li = document.createElement('li');
        li.className = 'autocomplete-item';
        li.textContent = item;
        li.addEventListener('click', () => {
          ui.input.value = item.split(' — ')[0];
          cerrarLista();
          ui.input.focus();
        });
        ui.listbox.appendChild(li);
      });
      ui.listbox.hidden = false;
    } catch { }
  }

  if (ui.input) {
    ui.input.addEventListener('input', () => {
      clearTimeout(acTimer);
      const q = ui.input.value.trim();
      if (q.length < 2) return cerrarLista();
      acTimer = setTimeout(() => buscar(q), 220);
    });
  }

  document.addEventListener('click', (e) => {
    if (!e.target.closest('.answer-box')) cerrarLista();
  });

  // Invitado
  function refrescarInvitado() {
    if (ui.guestHint) ui.guestHint.hidden = GH.isLogged();
  }
  document.addEventListener('gh:auth', refrescarInvitado);
  document.addEventListener('gh:logout', refrescarInvitado);

  /* ---------- Inicialización ---------- */
  refrescarInvitado();
  nuevaPregunta();
})();
