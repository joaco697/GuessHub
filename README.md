# 🏆 GuessHub — Plataforma de Minijuegos y Desafíos Diarios

Plataforma web de minijuegos rápidos tipo trivia y adivinanza, orientada a entretener al usuario, fomentar el juego diario y la competencia sana a través de rankings, rachas, insignias y multiplicadores de puntuación.

Proyecto desarrollado para la materia de desarrollo de software por el equipo:
- **Santiago Bigliardi**
- **Santino Caputo**
- **Joaquín De Cándido**

---

## 🚀 Tecnologías Principales

- **Backend:** Java 21, Spring Boot 3.5.x (Spring Web, Spring Security, Spring Data JPA, Spring Validation, Spring Mail, Thymeleaf).
- **Seguridad:** Autenticación por JWT (JSON Web Tokens), BCrypt Password Encoder, control de acceso por roles (`ADMIN`, `CLIENTE`).
- **Base de Datos:**
  - **Producción / Nube:** PostgreSQL alojado en Supabase (Connection Pooler).
  - **Desarrollo Local / Offline:** H2 embebido persistente en archivo con compatibilidad PostgreSQL.
- **APIs Externas en Vivo (Live Data):**
  - 🎬 **Cine / Películas:** The Movie Database (TMDB) API + Open Movie Database (OMDb) API.
  - 🎵 **Música:** Last.fm API + Deezer API.
  - ⚽ **Deportes:** API-Sports (Football) API con fallback estructurado.
- **Frontend:** HTML5 semántico con Thymeleaf, CSS3 moderno (paleta primaria Verde `#4CAF50` y Amarillo `#FFA726`, tipografías Nunito y Bricolage Grotesque, mobile-first responsivo y banners publicitarios no invasivos), Vanilla JavaScript modular.
- **Servidor de Correo (SMTP):** Envío real de emails mediante Gmail SMTP (`smtp.gmail.com:587`) para notificaciones automáticas y recuperación de contraseña.

---

## 📋 Requisitos Previos

- **Java JDK 21** o superior instalado (`java -version`).
- Conexión a Internet (para APIs de películas/música en vivo y SMTP).
- Puerto `8080` disponible.

---

## ⚙️ Cómo Ejecutar el Proyecto

El repositorio incluye el wrapper de Gradle (`gradlew`), por lo que no es necesario instalar Gradle previamente.

### Opción A: Ejecución Estándar (conecta a Supabase en la nube)
```bash
./guesshub/gradlew -p guesshub bootRun
```
*Si la base de datos de Supabase está activa, se conectará directamente a ella.*

### Opción B: Ejecución Local / Offline (con H2 persistente)
Si te encontrás sin conexión o el proyecto gratuito de Supabase se encuentra pausado por inactividad, podés ejecutarlo con el perfil local:
```bash
./guesshub/gradlew -p guesshub bootRun --args='--spring.profiles.active=local'
```

Una vez levantado, ingresá desde cualquier navegador a:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 👤 Cuentas y Credenciales de Prueba

Al iniciar, el sistema inicializa automáticamente los dos perfiles principales:

| Rol | Email / Usuario | Contraseña | Nombre Visible | Acceso / Propósito |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | `admin@gmail.com` | `admin123` | `admin` | Acceso completo al Panel de Administración (`/admin`), métricas y feedbacks |
| **Jugador / Cliente** | `joacodeca07@gmail.com` | `joaco123` | `joaco` | Jugador con racha activa y posicionado en la cima del Ranking |

*También podés registrar nuevos usuarios en cualquier momento desde el botón **"Ingresar" → "Registrate"**.*

---

## 🎮 Mecánicas y Funcionalidades del Sistema

### 1. Desafío Diario Principal
- Accesible en la portada (`/`).
- **Inicio a elección del usuario:** No comienza de forma automática invasiva; el usuario presiona **"JUGAR AHORA"** cuando está listo.
- Mismo desafío para todos los usuarios durante el día con pistas progresivas.
- Multiplicador de puntos por racha acumulada.

### 2. Minijuegos por Categoría & Modo Aleatorio
- **Películas (`/game?cat=peliculas`):** Pistas de género, director y año.
- **Música (`/game?cat=musica`):** Pistas de artista, álbum y año.
- **Deportes (`/game?cat=deportes`):** Pistas de país, fundación y estadio.
- **Modo Aleatorio / Endless (`/game?cat=random`):** Mezcla continua de categorías que rota en cada ronda sin recargar la página.

### 3. Puntuación y Multiplicadores
- **Puntuación base:** 1.000 puntos.
- **Penalización no punitiva:** Las pistas adicionales y el tiempo transcurrido descuentan puntos progresivamente, pero acertar **siempre suma un mínimo garantizado**.
- **Multiplicadores combinados (Tope 2.0x):**
  - Por racha global de días consecutivos.
  - Por racha en partida (respuestas correctas consecutivas).
  - Por dificultad del desafío.

### 4. Sistema de Ranking Competitivo
- Ranking Global y Ranking Diario en `/ranking`.
- **Desempate olímpico:** Si dos jugadores tienen igual puntuación comparten puesto y se salta la siguiente posición (ej. 1°, 1°, 3°).
- **"Mi Posición":** El usuario puede ver su puesto exacto y distancia de puntos aunque se encuentre fuera del Top 10.

### 5. Rachas, Insignias y Comodines
- Racha de días consecutivos visible en el perfil y navbar.
- **Comodines de congelamiento:** Permite congelar la racha para no perderla si un día no se juega.
- **Insignias desbloqueables:** Otorgadas automáticamente por hitos (rachas, categorías, top 10) y equipables en el perfil del jugador.

### 6. Recuperación de Contraseña Segura
- Flujo en 2 pasos: ingreso de email registrado → código de 6 dígitos con expiración a 15 minutos enviado vía Gmail SMTP → cambio seguro de contraseña.

### 7. Formulario de Feedback Guiado & Panel Admin
- Los usuarios pueden enviar sugerencias estructuradas desde el botón **"💡 Sugerir"**.
- Los administradores pueden visualizar todos los reportes, métricas y probar la conectividad de las APIs en tiempo real en `/admin`.

---

## 🧪 Ejecución de Tests Automatizados

La suite de pruebas incluye tests unitarios y de integración para servicios de puntuación, insignias, feedback, scheduler de notificaciones y recuperación de contraseñas:

```bash
./guesshub/gradlew -p guesshub test
```
Los reportes HTML generados se encuentran en `guesshub/build/reports/tests/test/index.html`.
