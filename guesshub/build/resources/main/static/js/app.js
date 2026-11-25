/* GuessHub — app.js (Utilidades globales, sesión, modales, feedback y toast) */
(() => {
  'use strict';

  const LS = window.localStorage;
  const GH = (window.GH = {});

  /* ---------- Helpers ---------- */
  GH.$ = (sel, root = document) => root.querySelector(sel);
  GH.$$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  GH.esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  GH.fmt = (n) => Number(n || 0).toLocaleString('es-AR');
  GH.plural = (n, uno, otros) => `${GH.fmt(n)} ${Number(n) === 1 ? uno : otros}`;

  /* ---------- Sesión y JWT ---------- */
  const venceEn = (token) => {
    try {
      const b = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(b)).exp * 1000;
    } catch {
      return Infinity;
    }
  };

  GH.clearSession = () => ['token', 'username', 'displayName'].forEach((k) => LS.removeItem(k));

  GH.token = () => {
    const t = LS.getItem('token');
    if (t && venceEn(t) < Date.now()) {
      GH.clearSession();
      return null;
    }
    return t;
  };

  GH.isLogged = () => !!GH.token();

  GH.displayName = () => {
    const d = (LS.getItem('displayName') || '').trim();
    if (d) return d;
    const u = LS.getItem('username') || '';
    return u.includes('@') ? u.split('@')[0] : u || 'Jugador';
  };

  GH.authHeaders = (extra = {}) => {
    const t = GH.token();
    return t ? { ...extra, Authorization: 'Bearer ' + t } : extra;
  };

  GH.logout = () => {
    GH.clearSession();
    GH.refreshNavbar();
    document.dispatchEvent(new CustomEvent('gh:logout'));
    GH.toast('Sesión cerrada correctamente');
  };

  /* ---------- Toast Notifications ---------- */
  GH.toast = (msg, type = '') => {
    const container = GH.$('#toast-container');
    if (!container) return;
    const t = document.createElement('div');
    t.className = 'toast ' + (type === 'error' ? 'error' : type === 'success' ? 'success' : '');
    t.innerHTML = `<span>${type === 'error' ? '⚠️' : type === 'success' ? '🎉' : '🔔'}</span> <span>${GH.esc(msg)}</span>`;
    container.appendChild(t);
    setTimeout(() => {
      t.style.opacity = '0';
      t.style.transform = 'translateY(10px)';
      t.style.transition = 'all 0.25s ease';
      setTimeout(() => t.remove(), 250);
    }, 3500);
  };

  /* ---------- Control de Modales ---------- */
  GH.openModal = (id) => {
    const m = GH.$('#' + id);
    if (m) m.hidden = false;
  };

  GH.closeModal = (id) => {
    const m = GH.$('#' + id);
    if (m) m.hidden = true;
  };

  GH.openAuth = (modo = 'login') => {
    if (modo === 'registro') {
      GH.closeModal('modal-login');
      GH.openModal('modal-registro');
    } else {
      GH.closeModal('modal-registro');
      GH.openModal('modal-login');
    }
  };

  GH.switchAuth = (modo) => {
    GH.closeModal('modal-recuperar');
    GH.openAuth(modo);
  };

  GH.openRecuperarPassword = () => {
    GH.closeModal('modal-login');
    GH.closeModal('modal-registro');
    GH.resetPasoRecuperar();
    GH.openModal('modal-recuperar');
  };

  GH.resetPasoRecuperar = () => {
    const p1 = GH.$('#form-recuperar-paso1');
    const p2 = GH.$('#form-recuperar-paso2');
    const desc = GH.$('#recup-desc');
    if (p1) p1.hidden = false;
    if (p2) p2.hidden = true;
    if (desc) desc.textContent = 'Ingresá tu email y te enviaremos un código de seguridad de 6 dígitos.';
  };

  GH.openFeedback = () => GH.openModal('modal-feedback');

  /* ---------- Actualización del Navbar ---------- */
  GH.refreshNavbar = async () => {
    const btnAuth = GH.$('#nav-btn-auth');
    const streakPill = GH.$('#nav-streak');
    const streakDays = GH.$('#nav-streak-days');

    if (GH.isLogged()) {
      const name = GH.displayName();
      if (btnAuth) {
        btnAuth.textContent = '👤 ' + name;
        btnAuth.onclick = () => location.href = '/perfil';
      }

      // Mostrar enlace de Admin si tiene rol ADMIN
      const token = GH.token();
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
          const roles = payload.roles || payload.authorities || [];
          const isAdmin = roles.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN');
          if (isAdmin) {
            let adminLink = GH.$('#nav-admin-link');
            if (!adminLink) {
              adminLink = document.createElement('a');
              adminLink.id = 'nav-admin-link';
              adminLink.href = '/admin';
              adminLink.className = 'nav-link-btn';
              adminLink.style.color = '#E65100';
              adminLink.textContent = '🛠️ Admin';
              const desktopNav = GH.$('.desktop-nav-links');
              if (desktopNav) desktopNav.appendChild(adminLink);
            }
          }
        } catch { }
      }

      // Consultar racha
      try {
        const res = await fetch('/api/ranking/mi-posicion', { headers: GH.authHeaders() });
        if (res.ok) {
          const d = await res.json();
          if (d.rachaDias > 0 && streakPill && streakDays) {
            streakDays.textContent = d.rachaDias;
            streakPill.hidden = false;
          }
        }
      } catch { }
    } else {
      if (btnAuth) {
        btnAuth.textContent = 'Ingresar';
        btnAuth.onclick = () => GH.openAuth('login');
      }
      if (streakPill) streakPill.hidden = true;
      // Ocultar link de admin si existe
      const adminLink = GH.$('#nav-admin-link');
      if (adminLink) adminLink.remove();
    }
  };

  /* ---------- Inicialización de Formularios al Cargar ---------- */
  document.addEventListener('DOMContentLoaded', () => {
    GH.refreshNavbar();

    // Botón feedback en header
    const btnFeed = GH.$('#btn-abrir-feedback');
    if (btnFeed) btnFeed.addEventListener('click', GH.openFeedback);

    // Formulario de Login
    const formLogin = GH.$('#form-login');
    if (formLogin) {
      formLogin.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = GH.$('#login-email').value.trim();
        const pass = GH.$('#login-pass').value;
        const btn = GH.$('#btn-submit-login');
        btn.disabled = true;

        try {
          const res = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: email, password: pass }),
          });

          if (!res.ok) {
            throw new Error('Credenciales inválidas');
          }

          const data = await res.json();
          LS.setItem('token', data.token);
          LS.setItem('username', data.username || email);
          if (data.displayName) LS.setItem('displayName', data.displayName);

          GH.closeModal('modal-login');
          GH.refreshNavbar();
          GH.toast('¡Bienvenido a GuessHub!', 'success');
          document.dispatchEvent(new CustomEvent('gh:auth'));
        } catch (err) {
          GH.toast(err.message || 'Error al iniciar sesión', 'error');
        } finally {
          btn.disabled = false;
        }
      });
    }

    // Formulario de Registro
    const formReg = GH.$('#form-registro');
    if (formReg) {
      formReg.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = GH.$('#reg-email').value.trim();
        const displayName = GH.$('#reg-displayname').value.trim();
        const pass = GH.$('#reg-pass').value;
        const pass2 = GH.$('#reg-pass2').value;
        const btn = GH.$('#btn-submit-registro');

        if (pass !== pass2) {
          GH.toast('Las contraseñas no coinciden', 'error');
          return;
        }

        btn.disabled = true;

        try {
          const res = await fetch('/api/usuarios/registrar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              username: email,
              displayName: displayName,
              password: pass,
              verificacionPassword: pass2
            }),
          });

          if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || err.error || 'No se pudo crear la cuenta');
          }

          GH.toast('¡Cuenta creada! Iniciá sesión para continuar.', 'success');
          GH.switchAuth('login');
          if (GH.$('#login-email')) GH.$('#login-email').value = email;
        } catch (err) {
          GH.toast(err.message, 'error');
        } finally {
          btn.disabled = false;
        }
      });
    }

    // Formulario de Feedback Guiado
    const formFeed = GH.$('#form-feedback');
    if (formFeed) {
      formFeed.addEventListener('submit', async (e) => {
        e.preventDefault();
        const tipo = GH.$('#feed-tipo').value;
        const opcion = GH.$('#feed-opcion').value;
        const comentario = GH.$('#feed-comentario').value.trim();
        const btn = GH.$('#btn-submit-feedback');

        btn.disabled = true;

        try {
          const res = await fetch('/api/feedback', {
            method: 'POST',
            headers: GH.authHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({
              tipo: tipo,
              opcionGuiada: opcion,
              comentario: comentario
            }),
          });

          if (res.ok) {
            GH.closeModal('modal-feedback');
            GH.toast('¡Gracias por tu aporte! Lo tendremos muy en cuenta.', 'success');
            formFeed.reset();
          } else {
            throw new Error('Error al enviar');
          }
        } catch {
          GH.toast('No se pudo enviar el feedback en este momento.', 'error');
        } finally {
          btn.disabled = false;
        }
      });
    }

    // Recuperar Contraseña - Paso 1: Solicitar Código
    const formRecup1 = GH.$('#form-recuperar-paso1');
    if (formRecup1) {
      formRecup1.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = GH.$('#recup-email').value.trim();
        const btn = GH.$('#btn-enviar-codigo');
        btn.disabled = true;

        try {
          const res = await fetch('/auth/recuperar-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email }),
          });

          const data = await res.json();

          if (res.ok) {
            GH.toast(data.mensaje || '¡Código enviado! Revisá tu casilla de correo.', 'success');
            formRecup1.hidden = true;
            const p2 = GH.$('#form-recuperar-paso2');
            if (p2) p2.hidden = false;
            const desc = GH.$('#recup-desc');
            if (desc) desc.textContent = 'Ingresá el código de 6 dígitos que te enviamos por correo y tu nueva contraseña.';
          } else {
            GH.toast(data.error || 'No se pudo procesar la solicitud.', 'error');
          }
        } catch {
          GH.toast('No se pudo conectar con el servidor. Intentá más tarde.', 'error');
        } finally {
          btn.disabled = false;
        }
      });
    }

    // Recuperar Contraseña - Paso 2: Cambiar Contraseña
    const formRecup2 = GH.$('#form-recuperar-paso2');
    if (formRecup2) {
      formRecup2.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = GH.$('#recup-codigo').value.trim();
        const nuevaPassword = GH.$('#recup-nueva-pass').value;
        const confirmacionPassword = GH.$('#recup-nueva-pass2').value;
        const btn = GH.$('#btn-cambiar-pass');

        if (nuevaPassword !== confirmacionPassword) {
          GH.toast('Las contraseñas no coinciden', 'error');
          return;
        }

        btn.disabled = true;

        try {
          const res = await fetch('/auth/restablecer-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token, nuevaPassword, confirmacionPassword }),
          });

          const data = await res.json();
          if (res.ok) {
            GH.closeModal('modal-recuperar');
            GH.toast(data.mensaje || '¡Contraseña actualizada con éxito!', 'success');
            GH.openAuth('login');
            formRecup2.reset();
            formRecup1.reset();
            GH.resetPasoRecuperar();
          } else {
            GH.toast(data.error || 'Código inválido o expirado', 'error');
          }
        } catch {
          GH.toast('Error al restablecer la contraseña', 'error');
        } finally {
          btn.disabled = false;
        }
      });
    }
  });

})();

/* ---------- Modo Oscuro Global ---------- */
(function () {
  const savedTheme = localStorage.getItem('gh_theme');
  if (savedTheme === 'dark') {
    document.body.classList.add('dark-mode');
  }
  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('btn-theme-toggle');
    if (!btn) return;
    const syncIcon = () => {
      btn.textContent = document.body.classList.contains('dark-mode') ? '☀️' : '🌙';
    };
    syncIcon();
    btn.addEventListener('click', () => {
      document.body.classList.toggle('dark-mode');
      localStorage.setItem('gh_theme', document.body.classList.contains('dark-mode') ? 'dark' : 'light');
      syncIcon();
    });
  });
})();
