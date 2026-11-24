/* GuessHub — perfil.js (Gestión de Perfil, Estadísticas, Comodines de Racha e Insignias) */
(() => {
  'use strict';

  const ui = {
    guestState: GH.$('#guest-state'),
    userProfile: GH.$('#user-profile'),
    avatar: GH.$('#profile-avatar'),
    name: GH.$('#profile-name'),
    handle: GH.$('#profile-handle'),
    badgesPills: GH.$('#profile-badges-pills'),
    btnFreeze: GH.$('#btn-congelar-racha'),
    freezeDesc: GH.$('#freeze-desc'),
    statPts: GH.$('#stat-pts'),
    statGames: GH.$('#stat-games'),
    statAcc: GH.$('#stat-acc'),
    statRank: GH.$('#stat-rank'),
    catsBox: GH.$('#cats-breakdown'),
    badgesBox: GH.$('#badges-container'),
    btnLogout: GH.$('#btn-logout'),
  };

  async function cargarPerfil() {
    if (!GH.isLogged()) {
      ui.guestState.hidden = false;
      ui.userProfile.hidden = true;
      return;
    }

    ui.guestState.hidden = true;
    ui.userProfile.hidden = false;

    const nombre = GH.displayName();
    const username = localStorage.getItem('username') || '';
    ui.name.textContent = nombre;
    ui.handle.textContent = '@' + (username.includes('@') ? username.split('@')[0] : nombre.toLowerCase());

    // Cargar perfil completo (avatar guardado)
    try {
      const resUser = await fetch('/api/usuarios/me', { headers: GH.authHeaders() });
      if (resUser.ok) {
        const u = await resUser.json();
        if (u.fotoPerfil) {
          ui.avatar.textContent = u.fotoPerfil;
        } else {
          ui.avatar.textContent = '👤';
        }
      }
    } catch {
      ui.avatar.textContent = '👤';
    }

    try {
      const res = await fetch('/api/ranking/mi-posicion', { headers: GH.authHeaders() });
      if (!res.ok) throw new Error('Error al cargar datos');
      const d = await res.json();

      // Stats
      ui.statPts.textContent = GH.fmt(d.puntos);
      ui.statGames.textContent = GH.fmt(d.partidasJugadas);
      const precision = d.partidasJugadas > 0 ? Math.round((d.partidasCorrectas / d.partidasJugadas) * 100) + '%' : '—';
      ui.statAcc.textContent = precision;
      ui.statRank.textContent = d.partidasJugadas > 0 ? '#' + d.posicion : '—';

      // Pills de racha y estado
      ui.badgesPills.innerHTML = '';
      if (d.insigniaEquipada) {
        ui.badgesPills.innerHTML += `<span class="badge-daily" style="border-color:var(--yellow);color:var(--yellow);background:rgba(250,204,21,0.1)">⭐ ${GH.esc(d.insigniaEquipada)}</span>`;
      }
      if (d.rachaDias > 0) {
        ui.badgesPills.innerHTML += `<span class="nav-streak-pill">🔥 ${d.rachaDias} días de racha</span>`;
      }
      if (d.rachaCongelada) {
        ui.badgesPills.innerHTML += `<span class="badge-daily" style="border-color:#38BDF8;color:#38BDF8;background:rgba(56,189,248,0.1)">❄️ Racha Protegida</span>`;
      }

      // Comodines de racha
      if (d.rachaCongelada) {
        ui.btnFreeze.disabled = true;
        ui.btnFreeze.textContent = '❄️ Ya está protegida';
        ui.freezeDesc.textContent = 'Tu racha no se perderá si hoy o mañana no jugás.';
      } else if (d.comodinesRacha > 0) {
        ui.btnFreeze.disabled = false;
        ui.btnFreeze.textContent = `Usar comodín (${d.comodinesRacha} disponible${d.comodinesRacha > 1 ? 's' : ''})`;
        ui.freezeDesc.textContent = 'Protegé tu racha si mañana no podés jugar.';
      } else {
        ui.btnFreeze.disabled = true;
        ui.btnFreeze.textContent = 'Sin comodines';
        ui.freezeDesc.textContent = 'Jugá partidas y alcanzá logros para obtener más comodines.';
      }

      // Desglose de categorías
      renderCategorias(d);

      // Cargar Insignias
      renderInsignias();

    } catch (err) {
      GH.toast('No pudimos cargar tus estadísticas completas.', 'error');
    }
  }

  function renderCategorias(d) {
    const total = (d.puntosPeliculas || 0) + (d.puntosMusica || 0) + (d.puntosDeportes || 0);
    if (total === 0) {
      ui.catsBox.innerHTML = '<div style="color:var(--text-light);font-size:0.85rem">Aún no sumaste puntos en ninguna categoría.</div>';
      return;
    }

    const items = [
      { nombre: 'Películas', pts: d.puntosPeliculas || 0, icono: '🎬', color: '#38BDF8' },
      { nombre: 'Música', pts: d.puntosMusica || 0, icono: '🎵', color: '#A855F7' },
      { nombre: 'Deportes', pts: d.puntosDeportes || 0, icono: '⚽', color: '#10B981' },
    ];

    ui.catsBox.innerHTML = items.map((cat) => {
      const pct = Math.round((cat.pts / total) * 100);
      return `
        <div>
          <div style="display:flex;justify-content:space-between;font-size:0.85rem;font-weight:700;margin-bottom:0.3rem">
            <span>${cat.icono} ${cat.nombre}</span>
            <span style="color:var(--text-muted)">${GH.fmt(cat.pts)} pts (${pct}%)</span>
          </div>
          <div class="timebar-container" style="height:8px;background:rgba(255,255,255,0.06);margin-bottom:0.3rem">
            <div style="height:100%;width:${pct}%;background:${cat.color};border-radius:var(--radius-full)"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  async function renderInsignias() {
    try {
      const res = await fetch('/api/insignias', { headers: GH.authHeaders() });
      if (!res.ok) throw new Error('Error insignias');
      const insignias = await res.json();

      ui.badgesBox.innerHTML = insignias.map((ins) => {
        const desbloqueada = !!ins.desbloqueada;
        const equipada = !!ins.equipada;

        return `
          <div class="badge-card ${desbloqueada ? 'unlocked' : 'locked'} ${equipada ? 'equipped-badge' : ''}" data-id="${ins.id}">
            <div class="b-icon">${ins.icono || '🏅'}</div>
            <div class="b-name">${GH.esc(ins.nombre)}</div>
            <div class="b-desc">${GH.esc(ins.descripcion)}</div>
            ${equipada ? '<span style="font-size:0.7rem;color:var(--yellow);font-weight:800;margin-top:0.3rem">⭐ Equipada</span>' :
              desbloqueada ? `<button class="btn btn-sm btn-secondary btn-equipar" data-id="${ins.id}" style="margin-top:0.4rem;font-size:0.75rem;padding:0.25rem 0.5rem">Equipar</button>` :
              '<span style="font-size:0.7rem;color:var(--text-light);margin-top:0.3rem">🔒 Bloqueada</span>'}
          </div>
        `;
      }).join('');

      ui.badgesBox.querySelectorAll('.btn-equipar').forEach((btn) => {
        btn.addEventListener('click', async (e) => {
          e.stopPropagation();
          const id = btn.dataset.id;
          try {
            const resp = await fetch('/api/usuario/insignia-equipada/' + id, {
              method: 'PUT',
              headers: GH.authHeaders(),
            });
            if (resp.ok) {
              GH.toast('¡Insignia equipada en tu avatar!', 'success');
              cargarPerfil();
            }
          } catch {
            GH.toast('Error al equipar insignia', 'error');
          }
        });
      });

    } catch {
      ui.badgesBox.innerHTML = '<div style="color:var(--text-light);font-size:0.85rem">No pudimos cargar el catálogo de insignias.</div>';
    }
  }

  // Acción de Congelar Racha
  ui.btnFreeze.addEventListener('click', async () => {
    try {
      const res = await fetch('/api/racha/congelar', {
        method: 'POST',
        headers: GH.authHeaders(),
      });
      if (res.ok) {
        GH.toast('❄️ ¡Racha congelada con éxito! Tu racha está protegida.', 'success');
        cargarPerfil();
      } else {
        const err = await res.json().catch(() => ({}));
        GH.toast(err.error || 'No se pudo congelar la racha', 'error');
      }
    } catch {
      GH.toast('Error al conectar con el servidor', 'error');
    }
  });

  // Selector de Avatar
  document.querySelectorAll('.avatar-opt').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const nuevoAvatar = btn.dataset.avatar;
      try {
        const res = await fetch('/api/usuarios/me/perfil', {
          method: 'PUT',
          headers: GH.authHeaders({ 'Content-Type': 'application/json' }),
          body: JSON.stringify({ fotoPerfil: nuevoAvatar })
        });
        if (res.ok) {
          ui.avatar.textContent = nuevoAvatar;
          GH.toast('Avatar actualizado: ' + nuevoAvatar, 'success');
        } else {
          GH.toast('No se pudo actualizar el avatar', 'error');
        }
      } catch {
        GH.toast('Error de conexión', 'error');
      }
    });
  });

  // Logout
  ui.btnLogout.addEventListener('click', () => {
    GH.logout();
    location.href = '/';
  });

  document.addEventListener('DOMContentLoaded', cargarPerfil);
  document.addEventListener('gh:auth', cargarPerfil);

})();
