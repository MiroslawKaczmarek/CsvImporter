package com.example.csvimporter.repositories;

import com.example.csvimporter.models.CsvData;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
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
}
