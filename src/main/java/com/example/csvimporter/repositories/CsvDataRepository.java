package com.example.csvimporter.repositories;

import com.example.csvimporter.models.CsvData;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface CsvDataRepository extends JpaRepository<CsvData, Long> {
//    Optional<CsvData> findById(Long id);

    List<CsvData> findAll();
    List<CsvData> findAll(Specification<CsvData> specification);
    List<CsvData> findCsvDataByDaily(Timestamp daily);

    @Query("SELECT SUM(cd.clicks) FROM CsvData cd where cd.datasource=?1")
    Long countClicksByDatasource(String datasource);

    @Query("SELECT SUM(cd.clicks) FROM CsvData cd where cd.datasource=?1 and cd.daily between ?2 and ?3")
    Long countClicksByDatasourceAndDaily(String datasource, Timestamp dateRangeFrom, Timestamp dateRangeTo);

    @Query(nativeQuery = true, value = "SELECT CASE WHEN SUM(cd.clicks) IS NULL THEN 0 ELSE (SUM(cd.clicks) / SUM(cd.impressions)) END FROM csv_data cd where cd.datasource=?1 and cd.campaign=?2")
    Double countClicksThroughRateByDatasourceAndCampaign(String datasource, String campaing);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE from csv_data")
    void deleteAll();
}
