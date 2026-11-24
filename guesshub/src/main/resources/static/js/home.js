/* GuessHub — home.js (Desafío Diario con Inicio a Elección del Usuario y Top 3 de Hoy) */
(() => {
  'use strict';

  const TIEMPO_MAX = 60;
  const BASE_PUNTOS = 1000;
  const COSTO_PISTA = 200;
  const COSTO_SEGUNDO = 5;

  const S = {
    desafio: null,
    pistasReveladas: 1,
    segundos: 0,
    timer: null,
    jugando: false,
    terminado: false,
    t0: 0,
  };

  const ui = {
    startView: GH.$('#daily-start-view'),
    playView: GH.$('#daily-play-view'),
    btnEmpezar: GH.$('#btn-empezar-desafio'),
    previewIcon: GH.$('#daily-preview-icon'),
    previewTitle: GH.$('#daily-preview-title'),
    previewSub: GH.$('#daily-preview-sub'),
    streakBadge: GH.$('#daily-streak-badge'),
    streakNum: GH.$('#daily-streak-num'),
    top3List: GH.$('#home-top3-list'),

    // Vista de juego activa
    activeIcon: GH.$('#daily-active-icon'),
    activeCat: GH.$('#daily-active-cat'),
    qTitle: GH.$('#daily-q-title'),
    timerVal: GH.$('#timer-val'),
    stakeVal: GH.$('#stake-val'),
    timebar: GH.$('#daily-timebar'),
    cluesBox: GH.$('#daily-clues'),
    form: GH.$('#form-daily-game'),
    input: GH.$('#daily-input'),
    btnSubmit: GH.$('#btn-daily-submit'),
    autocomplete: GH.$('#daily-autocomplete'),
  };

  /* ---------- Cálculo de Puntos y HUD ---------- */
  const calcularPuntos = () => {
    let pts = BASE_PUNTOS;
    if (S.pistasReveladas >= 2) pts -= COSTO_PISTA;
    if (S.pistasReveladas >= 3) pts -= COSTO_PISTA;
    pts -= (S.segundos * COSTO_SEGUNDO);
    return Math.max(50, pts);
  };

  function actualizarHud() {
    const restan = Math.max(0, TIEMPO_MAX - S.segundos);
    ui.timerVal.textContent = restan;
    ui.stakeVal.textContent = GH.fmt(calcularPuntos());

    const pct = (restan / TIEMPO_MAX) * 100;
    ui.timebar.style.width = pct + '%';

    const timerBadge = GH.$('#hud-timer');
    if (restan <= 10) {
      timerBadge.style.color = 'var(--warn)';
      timerBadge.style.borderColor = 'var(--warn)';
    } else {
      timerBadge.style.color = '';
      timerBadge.style.borderColor = '';
    }
  }

  function iniciarTimer() {
    clearInterval(S.timer);
    S.t0 = Date.now();
    S.timer = setInterval(() => {
      if (S.terminado) return;
      const s = Math.min(TIEMPO_MAX, Math.floor((Date.now() - S.t0) / 1000));
      if (s !== S.segundos) {
        S.segundos = s;
        actualizarHud();
      }
      if (s >= TIEMPO_MAX) {
        finalizarPartida('', true);
      }
    }, 250);
  }

  function detenerTimer() {
    clearInterval(S.timer);
  }

  /* ---------- Renderizado de Pistas ---------- */
  function renderPistas() {
    if (!S.desafio) return;
    ui.cluesBox.innerHTML = '';
    const pistas = S.desafio.pistas || [];
    const etiquetas = S.desafio.etiquetasPistas || ['Pista inicial', 'Segunda pista', 'Pista final'];

    for (let i = 1; i <= 3; i++) {
      const li = document.createElement('li');
      li.className = 'clue-item' + (i <= S.pistasReveladas ? ' revealed' : '');

      if (i <= S.pistasReveladas) {
        li.innerHTML = `
          <div class="clue-num">${i}</div>
          <div class="clue-content">${GH.esc(pistas[i - 1] || '—')}</div>
        `;
      } else {
        const puedeRevelar = (i === S.pistasReveladas + 1) && !S.terminado;
        li.innerHTML = `
          <div class="clue-num">${i}</div>
          <div class="clue-content" style="display:flex;align-items:center;justify-content:space-between">
            <span style="color:var(--text-light);font-size:0.85rem">🔒 ${GH.esc(etiquetas[i - 1])}</span>
            <button class="btn-unlock-clue" type="button" ${puedeRevelar ? '' : 'disabled'}>
              Revelar (−${COSTO_PISTA} pts)
            </button>
          </div>
        `;

        if (puedeRevelar) {
          const btn = li.querySelector('button');
          btn.addEventListener('click', () => {
            S.pistasReveladas = i;
            renderPistas();
            actualizarHud();
            ui.input.focus();
          });
        }
      }

      ui.cluesBox.appendChild(li);
    }
  }

  /* ---------- Precarga del Desafío (Sin Empezar) ---------- */
  async function precargarDesafioDiario() {
    try {
      const res = await fetch('/api/desafio-diario', { headers: GH.authHeaders() });
      if (!res.ok) throw new Error('Error al cargar');
      const d = await res.json();
      S.desafio = d;

      const icono = d.categoria.includes('peli') ? '🎬' : d.categoria.includes('mus') ? '🎵' : '⚽';
      ui.previewIcon.textContent = icono;
      ui.previewTitle.textContent = `Desafío de ${d.nombreCategoria || 'Hoy'} listo`;
      ui.previewSub.textContent = `Poné a prueba tu conocimiento en ${d.nombreCategoria || 'esta trivia'}. Usá las pistas con inteligencia y sumá al ranking de hoy.`;

      ui.activeIcon.textContent = icono;
      ui.activeCat.textContent = d.nombreCategoria || 'Desafío';
      ui.qTitle.textContent = d.titulo || '¿Qué es?';
    } catch {
      ui.previewTitle.textContent = '¡Desafío diario disponible!';
    }
  }

  /* ---------- Inicio Manual: El Usuario Elige Cuándo Jugar ---------- */
  function empezarDesafio() {
    if (!S.desafio) {
      precargarDesafioDiario().then(() => empezarDesafio());
      return;
    }

    S.pistasReveladas = 1;
    S.segundos = 0;
    S.terminado = false;
    S.jugando = true;

    // Cambiar de vista
    ui.startView.hidden = true;
    ui.playView.hidden = false;

    renderPistas();
    actualizarHud();
    iniciarTimer();

    setTimeout(() => ui.input.focus(), 150);
  }

  ui.btnEmpezar.addEventListener('click', empezarDesafio);

  /* ---------- Validación y Envío ---------- */
  async function finalizarPartida(respuesta, porTiempo = false) {
    if (S.terminado) return;
    S.terminado = true;
    detenerTimer();
    ui.input.disabled = true;
    ui.btnSubmit.disabled = true;
    cerrarAutocompletado();

    let resultado = { correcto: false, tituloCorrecto: '—' };
    try {
      const res = await fetch('/api/respuesta', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ preguntaId: S.desafio.id, respuesta }),
      });
      if (res.ok) resultado = await res.json();
    } catch { }

    const correcto = !!resultado.correcto && !porTiempo;
    ui.input.className = 'game-input ' + (correcto ? 'correct' : 'wrong');

    let puntosGanados = null;
    let multiplicador = 1.0;

    if (GH.isLogged()) {
      try {
        const resPartida = await fetch('/api/partida', {
          method: 'POST',
          headers: GH.authHeaders({ 'Content-Type': 'application/json' }),
          body: JSON.stringify({
            correcto,
            pistasUsadas: S.pistasReveladas,
            segundosUsados: S.segundos,
            categoria: S.desafio.categoria,
            dificultad: S.desafio.dificultad || 'MEDIO',
            esDesafioDiario: true
          }),
        });

        if (resPartida.ok) {
          const dataPartida = await resPartida.json();
          puntosGanados = dataPartida.puntosGanados;
          multiplicador = dataPartida.multiplicador;

          if (dataPartida.insigniasNuevas && dataPartida.insigniasNuevas.length > 0) {
            GH.toast('🏆 ¡Desbloqueaste insignia: ' + dataPartida.insigniasNuevas.join(', ') + '!', 'success');
          }
        }
      } catch { }
    }

    setTimeout(() => {
      mostrarModalResultado(correcto, porTiempo, resultado.tituloCorrecto, puntosGanados, multiplicador);
      cargarTop3Hoy();
    }, 550);
  }

  function mostrarModalResultado(correcto, porTiempo, respuestaCorrecta, puntos, mult) {
    GH.$('#result-icon').textContent = correcto ? '🎉' : porTiempo ? '⏱️' : '💡';
    GH.$('#result-title').textContent = correcto ? '¡Desafío Completado!' : porTiempo ? '¡Se acabó el tiempo!' : 'Respuesta incorrecta';
    GH.$('#result-answer').textContent = respuestaCorrecta || '—';

    const ptsBox = GH.$('#result-pts');
    const bonusBox = GH.$('#result-bonus');

    if (typeof puntos === 'number') {
      ptsBox.textContent = (puntos > 0 ? '+' : '') + GH.fmt(puntos) + ' pts';
      if (mult > 1.0) {
        bonusBox.textContent = `¡Multiplicador activo ${mult}x por tu racha!`;
        bonusBox.hidden = false;
      } else {
        bonusBox.hidden = true;
      }
    } else {
      ptsBox.textContent = 'Jugaste como invitado';
      bonusBox.textContent = 'Iniciá sesión para guardar tus puntos en el ranking';
      bonusBox.hidden = false;
    }

    GH.openModal('modal-result');
  }

  ui.form.addEventListener('submit', (e) => {
    e.preventDefault();
    const val = ui.input.value.trim();
    if (!val) {
      GH.toast('Escribí una respuesta');
      ui.input.focus();
      return;
    }
    finalizarPartida(val, false);
  });

  /* ---------- Autocompletado ---------- */
  let acTimer = null;
  function cerrarAutocompletado() {
    ui.autocomplete.hidden = true;
    ui.autocomplete.innerHTML = '';
  }

  async function buscarSugerencias(query) {
    if (!S.desafio) return;
    try {
      const modo = S.desafio.categoria || 'pelicula';
      const res = await fetch('/api/buscar?q=' + encodeURIComponent(query) + '&modo=' + encodeURIComponent(modo));
      if (!res.ok) return;
      const items = await res.json();
      if (!Array.isArray(items) || items.length === 0) {
        return cerrarAutocompletado();
      }

      ui.autocomplete.innerHTML = '';
      items.forEach((item) => {
        const li = document.createElement('li');
        li.className = 'autocomplete-item';
        li.textContent = item;
        li.addEventListener('click', () => {
          ui.input.value = item.split(' — ')[0];
          cerrarAutocompletado();
          ui.input.focus();
        });
        ui.autocomplete.appendChild(li);
      });
      ui.autocomplete.hidden = false;
    } catch { }
  }

  ui.input.addEventListener('input', () => {
    clearTimeout(acTimer);
    const q = ui.input.value.trim();
    if (q.length < 2) return cerrarAutocompletado();
    acTimer = setTimeout(() => buscarSugerencias(q), 220);
  });

  document.addEventListener('click', (e) => {
    if (!e.target.closest('#form-daily-game')) cerrarAutocompletado();
  });

  /* ---------- Top 3 de Hoy en Inicio ---------- */
  async function cargarTop3Hoy() {
    try {
      const res = await fetch('/api/ranking?cat=diario');
      if (!res.ok) throw new Error('Error ranking');
      const data = await res.json();

      if (!Array.isArray(data) || data.length === 0) {
        ui.top3List.innerHTML = '<div style="font-size:0.85rem;color:var(--text-light)">Aún no hay puntuaciones hoy. ¡Sé el primero en jugar!</div>';
        return;
      }

      const miNombre = GH.isLogged() ? GH.displayName() : null;
      let html = '';

      data.slice(0, 3).forEach((p) => {
        const esMio = (p.username === miNombre);
        const medalla = p.posicion === 1 ? '🥇' : p.posicion === 2 ? '🥈' : '🥉';

        html += `
          <div style="display:flex;align-items:center;justify-content:space-between;padding:0.35rem 0.6rem;background:${esMio ? 'var(--yellow-bg)' : 'var(--neutral-soft)'};border-radius:var(--radius-sm);font-size:0.88rem;font-weight:700">
            <div style="display:flex;align-items:center;gap:0.4rem">
              <span>${medalla}</span>
              <span style="${esMio ? 'color:var(--yellow-dark)' : ''}">${GH.esc(p.username)}</span>
            </div>
            <span style="color:var(--green-dark);font-weight:900">${GH.fmt(p.puntos)} pts</span>
          </div>
        `;
      });

      ui.top3List.innerHTML = html;
    } catch {
      ui.top3List.innerHTML = '<div style="font-size:0.85rem;color:var(--text-light)">No pudimos cargar el Top 3.</div>';
    }
  }

  async function actualizarRachaUsuario() {
    if (!GH.isLogged()) {
      ui.streakBadge.hidden = true;
      return;
    }
    try {
      const res = await fetch('/api/ranking/mi-posicion', { headers: GH.authHeaders() });
      if (res.ok) {
        const d = await res.json();
        if (d.rachaDias > 0) {
          ui.streakNum.textContent = d.rachaDias;
          ui.streakBadge.hidden = false;
        }
      }
    } catch { }
  }

  // Ejecución inicial
  document.addEventListener('DOMContentLoaded', () => {
    precargarDesafioDiario();
    cargarTop3Hoy();
    actualizarRachaUsuario();
  });

  document.addEventListener('gh:auth', () => {
    cargarTop3Hoy();
    actualizarRachaUsuario();
  });

})();
