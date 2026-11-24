/* GuessHub — ranking.js (Tabla de Posiciones: Hoy vs General y Categorías) */
(() => {
  'use strict';

  let periodo = 'diario'; // 'diario' | 'general'
  let categoria = 'global'; // 'global' | 'peliculas' | 'musica' | 'deportes'

  const ui = {
    tabDiario: GH.$('#tab-periodo-diario'),
    tabGeneral: GH.$('#tab-periodo-general'),
    chipsCat: GH.$$('.chip-cat'),
    rankTitle: GH.$('#rank-title'),
    rankList: GH.$('#rank-list'),
    meBox: GH.$('#me'),
  };

  function actualizarTitulos() {
    const textoPeriodo = periodo === 'diario' ? 'de Hoy' : 'Histórico General';
    const textoCat = categoria === 'peliculas' ? 'en Películas' :
                     categoria === 'musica' ? 'en Música' :
                     categoria === 'deportes' ? 'en Deportes' : 'Global';
    ui.rankTitle.textContent = `Top 10 ${textoCat} (${textoPeriodo})`;
  }

  /* ---------- Cargar Ranking ---------- */
  async function cargarRanking() {
    ui.rankList.innerHTML = '<div style="text-align:center;padding:2rem;color:var(--text-muted)">Cargando posiciones...</div>';

    let url = '/api/ranking';
    if (periodo === 'diario') {
      url += '?cat=diario';
    } else {
      url += '?cat=' + encodeURIComponent(categoria);
    }

    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error('Error al obtener ranking');
      const data = await res.json();

      if (!Array.isArray(data) || data.length === 0) {
        ui.rankList.innerHTML = '<div style="text-align:center;padding:2.5rem;color:var(--text-muted)">Aún no hay puntuaciones registradas en esta vista. ¡Sé el primero!</div>';
        return;
      }

      const miNombre = GH.isLogged() ? GH.displayName() : null;
      let html = '';

      data.forEach((p) => {
        const esMio = (p.username === miNombre);
        const medalla = p.posicion === 1 ? '🥇' : p.posicion === 2 ? '🥈' : p.posicion === 3 ? '🥉' : `#${p.posicion}`;
        const clasePos = p.posicion === 1 ? 'gold' : p.posicion === 2 ? 'silver' : p.posicion === 3 ? 'bronze' : '';

        html += `
          <div class="rank-row ${esMio ? 'is-me' : ''}">
            <div class="rank-left">
              <span class="rank-pos ${clasePos}">${medalla}</span>
              <div>
                <span class="rank-user-name">${GH.esc(p.username)}</span>
                ${p.rachaDias > 0 ? `<span class="rank-user-badge">🔥 ${p.rachaDias}d</span>` : ''}
              </div>
            </div>
            <div style="text-align:right">
              <span class="rank-pts">${GH.fmt(p.puntos)} pts</span>
              <div style="font-size:0.75rem;color:var(--text-light)">${p.partidasJugadas} partidas</div>
            </div>
          </div>
        `;
      });

      ui.rankList.innerHTML = html;
    } catch {
      ui.rankList.innerHTML = '<div style="text-align:center;padding:2rem;color:var(--text-muted)">No pudimos cargar la tabla de posiciones.</div>';
    }
  }

  /* ---------- Mi Posición (Tarjeta del Usuario) ---------- */
  async function cargarMiPosicion() {
    if (!GH.isLogged()) {
      ui.meBox.innerHTML = `
        <div class="card" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:1rem;background:rgba(250,204,21,0.06);border-color:rgba(250,204,21,0.3)">
          <div>
            <div style="font-weight:800;color:var(--yellow)">¿Querés figurar en la tabla?</div>
            <div style="font-size:0.85rem;color:var(--text-muted)">Iniciá sesión para guardar tus partidas y subir en el ranking.</div>
          </div>
          <button class="btn btn-sm btn-yellow" type="button" onclick="GH.openAuth('login')">Ingresar ahora</button>
        </div>
      `;
      return;
    }

    try {
      const res = await fetch('/api/ranking/mi-posicion', { headers: GH.authHeaders() });
      if (!res.ok) return;
      const d = await res.json();

      if (d.partidasJugadas > 0) {
        ui.meBox.innerHTML = `
          <div class="card" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:1rem;background:rgba(16,185,129,0.08);border-color:var(--green)">
            <div style="display:flex;align-items:center;gap:1rem">
              <div style="font-size:1.8rem;font-weight:900;color:var(--green);font-family:var(--font-display)">#${d.posicion}</div>
              <div>
                <div style="font-weight:800;font-size:1.1rem">${GH.esc(GH.displayName())}</div>
                <div style="font-size:0.8rem;color:var(--text-muted)">
                  ${d.partidasJugadas} partidas jugadas ${d.rachaDias > 0 ? `· 🔥 ${d.rachaDias} días de racha` : ''}
                </div>
              </div>
            </div>
            <div style="text-align:right">
              <div style="font-size:1.3rem;font-weight:900;color:var(--green);font-family:var(--font-display)">${GH.fmt(d.puntos)} pts</div>
              <div style="font-size:0.75rem;color:var(--text-light)">Puntaje acumulado</div>
            </div>
          </div>
        `;
      } else {
        ui.meBox.innerHTML = `
          <div class="card" style="text-align:center;padding:1.2rem">
            <div style="font-weight:700">Todavía no jugaste ninguna partida</div>
            <a class="btn btn-sm btn-green" href="/game" style="margin-top:0.6rem">¡Jugar mi primera partida!</a>
          </div>
        `;
      }
    } catch { }
  }

  /* ---------- Eventos de Filtro ---------- */
  document.addEventListener('DOMContentLoaded', () => {
    // Pestañas período
    ui.tabDiario.addEventListener('click', () => {
      periodo = 'diario';
      ui.tabDiario.classList.add('active');
      ui.tabGeneral.classList.remove('active');
      actualizarTitulos();
      cargarRanking();
    });

    ui.tabGeneral.addEventListener('click', () => {
      periodo = 'general';
      ui.tabGeneral.classList.add('active');
      ui.tabDiario.classList.remove('active');
      actualizarTitulos();
      cargarRanking();
    });

    // Chips categorías
    ui.chipsCat.forEach((chip) => {
      chip.addEventListener('click', () => {
        ui.chipsCat.forEach((c) => c.classList.remove('active'));
        chip.classList.add('active');
        categoria = chip.dataset.cat;

        // Si selecciona una categoría específica, pasar a vista general para mostrar el acumulado de esa categoría
        if (categoria !== 'global' && periodo === 'diario') {
          periodo = 'general';
          ui.tabGeneral.classList.add('active');
          ui.tabDiario.classList.remove('active');
        }

        actualizarTitulos();
        cargarRanking();
      });
    });

    actualizarTitulos();
    cargarMiPosicion();
    cargarRanking();
  });

  document.addEventListener('gh:auth', () => {
    cargarMiPosicion();
    cargarRanking();
  });

})();
