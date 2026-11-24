# 🎯 Checklist y Guión de Demo para la Presentación (3 a 5 min)

Este documento te sirve como **hoja de ruta rápida** durante la defensa o presentación del proyecto ante los profesores.

---

## ⏱️ Estructura del Guión de Presentación

### Paso 1: Introducción y Portada (1 minuto)
1. **Abrir [http://localhost:8080](http://localhost:8080)**.
2. **Explicar el concepto y diseño:**
   - *"GuessHub es una plataforma de minijuegos rápidos diarios y progresivos pensada para alta retención y engagement juvenil."*
   - *"El diseño respeta una paleta estricta en verde (#4CAF50) y amarillo (#FFA726) sin colores rojos punitivos para las fallas, con layout responsivo mobile-first y banners de anuncios no intrusivos."*
3. **Mostrar el Desafío Diario:**
   - Mostrar que la portada presenta el desafío del día con un botón destacado **"JUGAR AHORA"** (el usuario elige cuándo empezar, no es intrusivo).
   - Mostrar el Top 3 de Hoy debajo de la tarjeta.

---

### Paso 2: Partida en Vivo y Mecánicas (1.5 minutos)
1. **Hacer clic en "JUGAR AHORA":**
   - Mostrar cómo la primera pista ya está abierta (gratis).
   - Mostrar cómo el reloj decrece y el contador de puntos baja progresivamente (pero nunca llega a cero si acertás).
2. **Revelar una pista adicional:**
   - Hacer clic en "🔒 Revelar Pista 2" → se descuentan 200 puntos pero da información clave.
3. **Escribir y Autocompletar:**
   - Empezar a escribir en el campo de texto y mostrar cómo el autocompletado sugiere títulos válidos sin revelar la respuesta en el frontend.
4. **Acertar y ver el Modal de Resultado:**
   - Mostrar la puntuación final ganada, el multiplicador aplicado y el feedback positivo.
   - Si se desbloqueó una insignia, mostrar la notificación toast instantánea.

---

### Paso 3: Minijuegos por Categoría y Modo Aleatorio (1 minuto)
1. **Bajar al selector de categorías:**
   - Mostrar que existen las 3 categorías del relevamiento: **Películas, Música y Deportes**.
2. **Abrir el Modo Aleatorio (`/game?cat=random`):**
   - Explicar que combina dinámicamente preguntas de cine, canciones de Last.fm y clubes de fútbol en una misma partida continua.
   - Mostrar la racha en partida acumulativa (`🔥 Racha en partida: N aciertos`).

---

### Paso 4: Rankings, Perfil y Rachas (1 minuto)
1. **Ir a la sección de Ranking (`/ranking`):**
   - Explicar el filtro entre **Ranking Global** y **Ranking Diario**.
   - Destacar la regla de **desempate olímpico** (si dos jugadores empatan, comparten puesto y se salta el siguiente).
   - Mostrar la tarjeta destacada **"Mi Posición"** que ubica al jugador incluso si está fuera del Top 10.
2. **Ir al Perfil (`/perfil`):**
   - Mostrar el avatar personalizable.
   - Mostrar el contador de racha de días y los **comodines para congelar racha** sin perderla.
   - Mostrar el catálogo de **Insignias desbloqueadas** y la opción de equipar la favorita para lucirla en los rankings.

---

### Paso 5: Seguridad, Recuperación de Clave y Panel Admin (1 minuto)
1. **Recuperación de Contraseña:**
   - En el modal de login, hacer clic en *"¿Olvidaste tu contraseña?"*.
   - Explicar que el sistema genera un código numérico seguro de 6 dígitos que se despacha de forma real al correo del usuario mediante **Gmail SMTP** con expiración a 15 minutos.
2. **Panel de Administración (`/admin`):**
   - Iniciar sesión con `admin@gmail.com` / `admin123`.
   - Mostrar el link `🛠️ Admin` en la barra de navegación.
   - Mostrar las métricas de feedback de los usuarios (clasificados por sugerencia, dificultad, bugs, experiencia).
   - Presionar *"Probar conectividad ahora"* para validar en tiempo real el enlace con las APIs de TMDB y Last.fm.

---

## 📋 Checklist Rápido Previas a la Entrega

- [ ] **Java 21 instalado:** verificar con `java -version`.
- [ ] **Base de datos lista:** el inicializador carga roles, insignias y los 2 perfiles base automáticamente.
- [ ] **Credenciales a mano:**
  - Admin: `admin@gmail.com` / `admin123` (displayName: `admin`)
  - Jugador: `joacodeca07@gmail.com` / `joaco123` (displayName: `joaco`)
- [ ] **Tests verdes:** ejecutar `./guesshub/gradlew -p guesshub test` (todos pasan en 100%).
- [ ] **Pestañas del navegador listas:** tener abiertas la pestaña de la app (`http://localhost:8080`) y la de la casilla de correo (`guesshubsupport@gmail.com`).
