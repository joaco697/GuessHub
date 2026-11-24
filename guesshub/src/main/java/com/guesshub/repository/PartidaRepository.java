package com.guesshub.repository;

import com.guesshub.entity.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByUsuarioIdOrderByMomentoDesc(Long usuarioId);

    boolean existsByUsuarioIdAndFechaAndEsDesafioDiarioTrue(Long usuarioId, LocalDate fecha);

    /**
     * Top 10 diario basado en los puntos acumulados en la fecha dada.
     * Retorna: [0]=usuario_id, [1]=puntos_dia, [2]=partidas_dia, [3]=correctas_dia, [4]=display_name, [5]=racha_dias
     */
    @Query(value = """
        SELECT p.usuario_id,
               SUM(p.puntos) AS puntos_dia,
               COUNT(*) AS partidas_dia,
               SUM(CASE WHEN p.correcto THEN 1 ELSE 0 END) AS correctas_dia,
               u.display_name,
               COALESCE(pu.racha_dias, 0) AS racha_dias
        FROM partidas p
        JOIN usuarios u ON u.id = p.usuario_id
        LEFT JOIN puntajes pu ON pu.usuario_id = p.usuario_id
        WHERE p.fecha = :fecha
        GROUP BY p.usuario_id, u.display_name, pu.racha_dias
        ORDER BY puntos_dia DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10Diario(@Param("fecha") LocalDate fecha);

    /**
     * Posición del usuario en el ranking diario de la fecha dada.
     */
    @Query(value = """
        WITH totales AS (
            SELECT p.usuario_id, SUM(p.puntos) as total_dia
            FROM partidas p
            WHERE p.fecha = :fecha
            GROUP BY p.usuario_id
        )
        SELECT COUNT(*) + 1
        FROM totales t
        WHERE t.total_dia > (SELECT COALESCE(SUM(p2.puntos), 0) FROM partidas p2 WHERE p2.usuario_id = :usuarioId AND p2.fecha = :fecha)
        """, nativeQuery = true)
    int findPosicionDiaria(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);
}

