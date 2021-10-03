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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
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
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
public class CsvDataController {

    private final static String DATE_IN_FORMAT = "Date in format yyyy-MM-dd";

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
    @ApiOperation(value = "method:uploadCsvFile", notes="Upload csv file and save content to database.")
    public ResponseEntity<?> uploadCsvFile(@ApiParam(value="CSV file (required)")  @RequestParam MultipartFile file) {
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

//    @GetMapping({"/csvdata/all"})
//    public List<CsvDataDto> getAll() {
//        return csvData2DtoConverter.convertList(csvDataRepository.findAll());
//    }

    @GetMapping({"/csvdata/clicksByDatasource"})
    @ApiOperation(value = "method:getClicksByDatasource", notes="Get sum of clicks for csv_data records specified by datasource")
    public ResponseEntity<?> getClicksByDatasource(@ApiParam(value="String (required)") @RequestParam String datasource) {
        log.info("getClicksByDatasource: " + datasource);
        Long clicks = csvDataRepository.countClicksByDatasource(datasource);
        if(clicks==null)
            clicks=0L;
        return ResponseEntity.ok(""+clicks);
    }

    @GetMapping({"/csvdata/clicksByDatasourceAndDailyRange"})
    @ApiOperation(value = "method:getClicksByDatasourceAndDailyRange", notes="Get sum of clicks for csv_data records specified by datasource and date range parameters")
    public ResponseEntity<?> getClicksByDatasourceAndDailyRange(
                   @ApiParam(value="String (required)") @RequestParam String datasource,
                   @ApiParam(value=DATE_IN_FORMAT+" (required)") @RequestParam(defaultValue="2019-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyFrom,
                   @ApiParam(value=DATE_IN_FORMAT+" (required)") @RequestParam(defaultValue="2019-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyTo) {
        log.info("getClicksByDatasourceAndDailyRange: " + datasource + " / " + TimestampStringConverter.dateAsString(dailyFrom) + " / " +
                TimestampStringConverter.dateAsString(dailyTo));
        Long clicks = csvDataRepository.countClicksByDatasourceAndDaily(datasource, new Timestamp(dailyFrom.getTime()),
                new Timestamp(dailyTo.getTime()));
        if(clicks==null)
            clicks=0L;
        return ResponseEntity.ok(""+clicks);
    }

    @PutMapping({"/csvdata/new"})
    @ApiOperation(value = "method:createNew", notes="Creates new record in csv_data")
    public ResponseEntity<?> createNew(
            @ApiParam(value="String (required)") @RequestParam String datasource,
            @ApiParam(value="String (required)") @RequestParam String campaign,
            @ApiParam(value=DATE_IN_FORMAT + " (required)") @RequestParam(defaultValue="2019-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily,
            @ApiParam(value="Number (required)") @RequestParam(defaultValue="1") @NumberFormat(style = NumberFormat.Style.NUMBER, pattern="1") Long clicks,
            @ApiParam(value="Number (required)") @RequestParam(defaultValue="1") @NumberFormat(style = NumberFormat.Style.NUMBER, pattern="1") Long impressions) {
        log.info("createNew: " + datasource);
        CsvData csvData = new CsvData(datasource, campaign, new Timestamp(daily.getTime()), clicks, impressions);

        csvData = csvDataRepository.save(csvData);
        return ResponseEntity.ok(csvData);
    }

    @GetMapping({"/csvdata/clicksThroughRateByDatasourceAndCampaign"})
    @ApiOperation(value = "method:getClicksThroughRateByDatasourceAndCampaign", notes="Get CTR (sum of clicks divded by sum of impressions) for csv_data records specified by datasource and campaign parameters.")
    public ResponseEntity<?> getClicksThroughRateByDatasourceAndCampaign(@ApiParam(value="String (required)") @RequestParam String datasource,
                                                                         @ApiParam(value="String (required)") @RequestParam String campaign) {
        log.info("getClicksThroughRateByDatasourceAndCampaign: " + datasource + " / " + campaign);
        Double ctr = csvDataRepository.countClicksThroughRateByDatasourceAndCampaign(datasource, campaign);
        if(ctr==null)
            ctr=0D;

        DecimalFormatSymbols decimalFormatSeparator = new DecimalFormatSymbols(Locale.ENGLISH);
        decimalFormatSeparator.setDecimalSeparator('.');
        decimalFormatSeparator.setGroupingSeparator(' ');

        DecimalFormat formatter = new DecimalFormat("#0.00", decimalFormatSeparator);
        String ctrAsString = formatter.format(100*ctr);
        return ResponseEntity.ok(ctrAsString + "%");
    }

    @GetMapping({"/csvdata/byDay"})
    @ApiOperation(value = "method:getByDay", notes="Get csv_data records for specified date")
    public ResponseEntity<?> getByDay(@ApiParam(value=DATE_IN_FORMAT+" (required)") @RequestParam(defaultValue="2019-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily) {
        log.info("getByDay: " + TimestampStringConverter.dateAsString(daily));
        List<CsvData> csvData = csvDataRepository.findCsvDataByDaily(new Timestamp(daily.getTime()));
        return ResponseEntity.ok(csvData2DtoConverter.convertList(csvData));
    }


    @GetMapping({"/csvdata/byOptionalParameters"})
    @ApiOperation(value = "method:getAllByOptionalParameters", notes="Get csv_data records. Possible to define search criteria like: datasource, campaingn, daily (date in format yyyy-MM-dd)")
    public List<CsvDataDto> getAllByOptionalParameters(
           @ApiParam(value="String(optional)")             @RequestParam(required=false) String datasource,
           @ApiParam(value="String(optional)")             @RequestParam(required=false) String campaign,
           @ApiParam(value=DATE_IN_FORMAT+" (optional)" )  @RequestParam(required=false, defaultValue="2019-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily) {
        log.info("getAllByOptionalParameters: " + datasource + " / " + campaign + " / " + TimestampStringConverter.dateAsString(daily));
        return csvData2DtoConverter.convertList(csvDataRepository.findAll(
                CsvDataCriteriaBuilder.buildCriteriaByDatasourceCampaignDaily(datasource, campaign, daily)));
    }

    @DeleteMapping({"/csvdata/deleteAll"})
    @ApiOperation(value = "method:deleteAll", notes="Deletes all csv_data records.")
    public ResponseEntity<?> deleteAll() {
        log.info("deleteAll");
        csvDataRepository.deleteAll();
        return ResponseEntity.ok().body("");
    }

//    @GetMapping({"/csvdata/clicksByOptionalParameters"})
//    public List<Long> getClicksByOptionalParameters(@RequestParam(required=false) String datasource,
//                                                    @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyFrom,
//                                                    @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyTo) {
//        log.info("getClicksByOptionalParameters: " + datasource + " / " + TimestampStringConverter.dateAsString(dailyFrom) +
//                " / " + TimestampStringConverter.dateAsString(dailyTo));
//        return csvDataRepository.clicks(
//                CsvDataCriteriaBuilder.buildCriteriaForCountSum(datasource, dailyFrom, dailyTo));
//    }

}
