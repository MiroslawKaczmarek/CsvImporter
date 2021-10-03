package com.example.csvimporter.repositories;

import com.example.csvimporter.models.CsvData;
import org.springframework.data.jpa.domain.Specification;

public interface CsvDataRepositoryCustom {
    <S extends Number> S sumOfClicks(Specification<CsvData> spec, Class<S> resultType, String fieldName);
}
