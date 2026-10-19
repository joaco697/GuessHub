# **Notas de Relevamiento**

**Participantes: Santiago Bigliardi, Santino Caputo y Joaquin De Candido**

## **Flujo principal del sistema**

* Al ingresar a la página, el usuario **comienza a jugar automáticamente** sin necesidad de hacer clic.  
* El primer elemento visible es el **minijuego diario (principal)**.  
* El objetivo es **reducir la cantidad de clics** y facilitar el acceso inmediato al juego.  
* Luego de completar el juego o hacer scroll, el usuario puede ver el resto de los minijuegos.

## **Juego principal**

* Es el **elemento central de la plataforma**.  
* Está disponible para todos los usuarios.  
* Es el mismo para todos los jugadores.  
* Funciona como incentivo para volver diariamente.

Características:

* No tiene selección de dificultad  
* Se inicia automáticamente  
* Tiene pistas progresivas 

## **Mecánica de juego**

* El puntaje depende de:  
  * Tiempo de respuesta  
* Sistema de puntaje:  
  * Máximo: 1000 puntos  
  * Disminuye con el tiempo  
  * Respuesta correcta siempre otorga puntos

## 

## **Sistema de ranking** 

* Ranking por minijuego  
* Posibilidad de ranking global  
* Ranking diario y ranking general

Características:

* Se muestra un Top (ej: Top 10\)  
* El usuario puede ver su posición aunque esté fuera del top  
* En caso de empate:  
  * Se comparte posición  
  * Se salta la siguiente posición (ej: dos primeros → siguiente es tercero)

## **👤 Usuario**

* Nombre de usuario:  
  * Mínimo: 5 caracteres  
  * Máximo: 15 caracteres

## **Sistema de rachas** 

* Racha global por días consecutivos  
* A partir de:  
  * 3 días → insignia inicial  
  * 7 días → nueva insignia  
  * 1 mes → insignia  
  * etc.  
* La racha otorga:  
  * **Multiplicador de puntaje progresivo**  
  * Ejemplo inicial: \+0.05

## **Multiplicadores y dificultad**

* Multiplicador por dificultad:  
  * Fácil → base  
  * Medio → x1.2  
  * Difícil → mayor  
* Multiplicador por racha:  
  * Aumenta con días consecutivos  
* Multiplicador por desempeño en partida:  
  * Se activa a partir de cierta cantidad de respuestas correctas consecutivas  
* Multiplicador máximo: x2

## **Tipos de racha**

1. **Racha global** → días consecutivos jugando  
2. **Racha por partida** → respuestas correctas seguidas

## **Diseño**

* Colores principales:  
  * Verde  
  * Amarillo  
* Evitar:  
  * Rojo (asociado a error/peligro)  
* Estilo:  
  * Colores vibrantes  
  * Enfocado a público joven

## **Publicidad**

* Formato:  
  * Banners laterales  
  * Banners inferiores  
* Restricciones:  
  * No deben interrumpir el juego  
  * No usar anuncios invasivos (ej: tipo YouTube)

## **Notificaciones**

Se enviarán notificaciones cuando:

* Se agreguen nuevos juegos  
* El usuario esté por perder la racha  
* El usuario esté inactivo  
* El usuario sea desplazado del ranking (Top 10 / Top 3 / Top 1\)

## **MVP** 

* No es necesario tener los 10 minijuegos iniciales  
* MVP puede incluir:  
  * 1 o 2 minijuegos  
  * 3 categorías (películas, música, deportes)

## 

## **Feedback de usuarios**

* Se implementará mediante formularios guiados  
* Preguntas cerradas (opciones)  
* Posibilidad de agregar comentario opcional

Objetivo:

* Obtener información útil y estructurada  
* Evitar respuestas poco claras

## **Decisiones clave del sistema**

* Prioridad en:  
  * rapidez de acceso  
  * engagement del usuario  
* Sistema enfocado en:  
  * repetición diaria  
  * competencia (ranking)  
  * progresión (rachas y logros)

