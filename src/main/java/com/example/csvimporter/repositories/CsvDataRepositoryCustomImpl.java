package com.example.csvimporter.repositories;

import com.example.csvimporter.models.CsvData;
import org.springframework.data.jpa.domain.Specification;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class CsvDataRepositoryCustomImpl implements CsvDataRepositoryCustom{

    @Inject
    private EntityManager entityManager;

    @Override
    public <S extends Number> S sumOfClicks(Specification<CsvData> spec, Class<S> resultType, String fieldName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<S> query = builder.createQuery(resultType);
        Root<CsvData> root = applySpecificationToCriteria(spec, query);
        query.select(builder.sum(root.get(fieldName).as(resultType)));
        TypedQuery<S> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    protected <S> Root<CsvData> applySpecificationToCriteria(Specification<CsvData> spec, CriteriaQuery<S> query) {
        Root<CsvData> root = query.from(CsvData.class);
        if (spec == null) {
            return root;
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);
        if (predicate != null) {
            query.where(predicate);
        }
        return root;
    }
}
