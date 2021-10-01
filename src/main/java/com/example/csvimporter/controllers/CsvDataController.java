package com.example.csvimporter.controllers;

import com.example.csvimporter.dtos.CsvDataDto;
import com.example.csvimporter.dtos.UploadResultDto;
import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataCriteriaBuilder;
import com.example.csvimporter.repositories.CsvDataRepository;
import com.example.csvimporter.services.CsvData2DtoConverter;
import com.example.csvimporter.services.CsvDataConverter;
import com.example.csvimporter.utils.TimestampStringConverter;
import com.example.csvimporter.validators.UploadFileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class CsvDataController {

    private final UploadFileValidator uploadFileValidator;
    private final CsvDataRepository csvDataRepository;
    private final CsvDataConverter csvDataConverter;
    private final CsvData2DtoConverter csvData2DtoConverter;

    public CsvDataController(UploadFileValidator uploadFileValidator, CsvDataRepository csvDataRepository,
                             CsvDataConverter csvDataConverter, CsvData2DtoConverter csvData2DtoConverter) {
        this.uploadFileValidator = uploadFileValidator;
        this.csvDataRepository = csvDataRepository;
        this.csvDataConverter = csvDataConverter;
        this.csvData2DtoConverter = csvData2DtoConverter;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = {"/csvdata/uploadCsv"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@Operation(summary = "Upload a CSV file", description = "Upload a CSV file")
    public ResponseEntity<?> uploadCsvFile(@RequestParam(value = "file", required = true) MultipartFile file) {
        log.info("uploadCsvFile: " + file.getOriginalFilename());
        UploadResultDto resultOfFirstValidation = uploadFileValidator.validateLevelOne(file);
        if(!resultOfFirstValidation.isSuccess())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultOfFirstValidation.getAdditionalInfo());
        List<List<String>> csvStructure = csvDataConverter.convertFileToListOfStrings(file);
        if(csvStructure == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Problem with reading CSV file.");
        if(!uploadFileValidator.validateNumberOfColumns(csvStructure))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Each row in CSV file must contains 5 records");

        long numberOfImportedRecords = csvDataConverter.convertListOfStringToEntitiesAndSave(csvStructure);
        log.info(numberOfImportedRecords + " records has been created.");
        return ResponseEntity.ok("" + numberOfImportedRecords + " records has been created.");
    }

    //TODO - not needed -  can be removed
    @GetMapping({"/csvdata/all"})
    public List<CsvDataDto> getAll() {
        return csvData2DtoConverter.convertList(csvDataRepository.findAll());
    }

    @GetMapping({"/csvdata/clicksByDatasource"})
    public ResponseEntity<?> getClicksByDatasource(@RequestParam String datasource) {
        log.info("getClicksByDatasource: " + datasource);
        Long clicks = csvDataRepository.countClicksByDatasource(datasource);
        if(clicks==null)
            clicks=Long.valueOf(0);
        return ResponseEntity.ok(""+clicks);
    }

    @GetMapping({"/csvdata/clicksByDatasourceAndDailyRange"})
    public ResponseEntity<?> getClicksByDatasourceAndDailyRange(@RequestParam String datasource,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyFrom,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyTo) {
        log.info("getClicksByDatasourceAndDailyRange: " + datasource + " / " + dailyFrom.toString() + " / " + dailyTo.toString());
        Long clicks = csvDataRepository.countClicksByDatasourceAndDaily(datasource, new Timestamp(dailyFrom.getTime()),
                new Timestamp(dailyTo.getTime()));
        if(clicks==null)
            clicks=Long.valueOf(0);
        return ResponseEntity.ok(""+clicks);
    }

    @PutMapping({"/csvdata/new"})
    public ResponseEntity<?> createNew(
            @RequestParam String datasource,
            @RequestParam String campaign,
            @RequestParam String daily,
            @RequestParam Long clicks,
            @RequestParam Long impressions) {
        log.info("createNew: " + datasource);
        CsvData csvData = new CsvData(datasource, campaign, TimestampStringConverter.stringToTimestamp(daily), clicks, impressions);

        csvData = csvDataRepository.save(csvData);
        return ResponseEntity.ok(csvData);
    }

    @GetMapping({"/csvdata/clicksThroughRateByDatasourceAndCampaign"})
    public ResponseEntity<?> getClicksThroughRateByDatasourceAndCampaign(@RequestParam String datasource, @RequestParam String campaign) {
        log.info("getClicksThroughRateByDatasourceAndCampaign: " + datasource + " / " + campaign);
        Double ctr = csvDataRepository.countClicksThroughRateByDatasourceAndCampaign(datasource, campaign);
        if(ctr==null)
            ctr=Double.valueOf(0);

        DecimalFormat formatter = new DecimalFormat("#0.00");
        String ctrAsString = formatter.format(100*ctr);
        return ResponseEntity.ok(ctrAsString + "%");
    }

    @GetMapping({"/csvdata/byDay"})
    public ResponseEntity<?> getByDay(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily) {
        log.info("getByDay: " + daily.toString());
        List<CsvData> csvData = csvDataRepository.findCsvDataByDaily(new Timestamp(daily.getTime()));
        return ResponseEntity.ok(csvData2DtoConverter.convertList(csvData));
    }

    @GetMapping({"/csvdata/byOptionalParameters"})
    public List<CsvDataDto> getAllByOptionalParameters(@RequestParam(required=false) String datasource,
                                   @RequestParam(required=false) String campaign,
                                   @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily) {
        log.info("getAllByOptionalParameters: " + datasource + " / " + campaign + " / " + daily.toString());
        return csvData2DtoConverter.convertList(csvDataRepository.findAll(
                CsvDataCriteriaBuilder.buildCriteriaByDatasourceCampaignDaily(datasource, campaign, daily)));
    }

    @DeleteMapping({"/csvdata/deleteAll"})
    public ResponseEntity<?> deleteAll() {
        log.info("deleteAll");
        csvDataRepository.deleteAll();
        return ResponseEntity.ok().body("");
    }

}
