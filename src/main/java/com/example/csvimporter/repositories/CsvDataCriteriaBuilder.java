package com.example.csvimporter.repositories;

import com.example.csvimporter.models.CsvData;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsvDataCriteriaBuilder {

    public static Specification<CsvData> buildCriteriaByDatasourceCampaignDaily(String datasource, String campaign, Date daily) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(datasource != null)
                predicates.add(criteriaBuilder.equal(root.get("datasource"), datasource));
            if(campaign != null)
                predicates.add(criteriaBuilder.equal(root.get("campaign"), campaign));
            //predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("daily"), new Timestamp(daily.getTime())));
            if(daily != null)
                predicates.add(criteriaBuilder.equal(root.get("daily"), new Timestamp(daily.getTime())));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CsvData> buildCriteriaForCountSum(String datasource, Date dailyFrom, Date dailyTo) {
        return new Specification<CsvData>() {
            @Override
            public Predicate toPredicate(Root<CsvData> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (datasource != null)
                    predicates.add(criteriaBuilder.equal(root.get("datasource"), datasource));
                if (dailyFrom != null)
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("daily"), new Timestamp(dailyFrom.getTime())));
                if (dailyTo != null)
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("daily"), new Timestamp(dailyTo.getTime())));
                Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
                return criteriaBuilder.and(predicateArray);
            }
        };
    }
}
