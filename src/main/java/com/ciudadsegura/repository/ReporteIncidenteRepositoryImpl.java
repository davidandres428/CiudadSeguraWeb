package com.ciudadsegura.repository;

import com.ciudadsegura.model.ReporteIncidente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteIncidenteRepositoryImpl implements ReporteIncidenteRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> contarPorEstado(LocalDateTime desde, LocalDateTime hasta) {
        return contarPor("estado", desde, hasta);
    }

    @Override
    public List<Object[]> contarPorPrioridad(LocalDateTime desde, LocalDateTime hasta) {
        return contarPor("prioridad", desde, hasta);
    }

    @Override
    public List<Object[]> contarPorTipo(LocalDateTime desde, LocalDateTime hasta) {
        return contarPor("tipo", desde, hasta);
    }

    private List<Object[]> contarPor(String atributo, LocalDateTime desde, LocalDateTime hasta) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
        Root<ReporteIncidente> reporte = query.from(ReporteIncidente.class);
        Path<Object> campoAgrupacion = reporte.get(atributo);
        List<Predicate> filtros = new ArrayList<>();

        if (desde != null) {
            filtros.add(builder.greaterThanOrEqualTo(reporte.get("fechaReporte"), desde));
        }
        if (hasta != null) {
            filtros.add(builder.lessThan(reporte.get("fechaReporte"), hasta));
        }

        query.multiselect(campoAgrupacion, builder.count(reporte))
                .where(filtros.toArray(Predicate[]::new))
                .groupBy(campoAgrupacion);
        return entityManager.createQuery(query).getResultList();
    }
}
