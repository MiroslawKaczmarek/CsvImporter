package com.example.csvimporter.controllers;

import com.example.csvimporter.dtos.CsvDataDto;
import com.example.csvimporter.dtos.UploadResultDto;
import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataCriteriaBuilder;
import com.example.csvimporter.repositories.CsvDataRepository;
import com.example.csvimporter.services.CsvData2DtoConverter;
import com.example.csvimporter.services.CsvDataService;
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
    private final CsvData2DtoConverter csvData2DtoConverter;
    private final CsvDataService csvDataService;

    public CsvDataController(UploadFileValidator uploadFileValidator, CsvDataRepository csvDataRepository,
                             CsvData2DtoConverter csvData2DtoConverter, CsvDataService csvDataService) {
        this.uploadFileValidator = uploadFileValidator;
        this.csvDataRepository = csvDataRepository;
        this.csvData2DtoConverter = csvData2DtoConverter;
        this.csvDataService = csvDataService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = {"/csvdata/uploadCsv"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "method:uploadCsvFile", notes="Upload csv file and save content to database.")
    public ResponseEntity<String> uploadCsvFile(@ApiParam(value="CSV file (required)")  @RequestParam MultipartFile file) {
        log.info("uploadCsvFile: " + file.getOriginalFilename());
        UploadResultDto resultOfFirstValidation = uploadFileValidator.validateLevelOne(file);
        if(!resultOfFirstValidation.isSuccess())
            return new ResponseEntity<String>(resultOfFirstValidation.getAdditionalInfo(), HttpStatus.BAD_REQUEST);

        CsvDataService.CsvDataImportResult importResult = csvDataService.saveData(file);
        if(importResult.getImportedRecords()==0)
            return new ResponseEntity<String>(importResult.getErrorReason(), HttpStatus.BAD_REQUEST);
        log.info(importResult.getImportedRecords() + " records has been created.");
        return new ResponseEntity<String>("" + importResult.getImportedRecords() + " records has been created.", HttpStatus.OK);
    }

    @PutMapping({"/csvdata/new"})
    @ApiOperation(value = "method:createNew", notes="Creates new record in csv_data")
    public ResponseEntity<CsvData> createNew(
            @ApiParam(value="String (required)") @RequestParam String datasource,
            @ApiParam(value="String (required)") @RequestParam String campaign,
            @ApiParam(value=DATE_IN_FORMAT + " (required)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily,
            @ApiParam(value="Number (required)") @RequestParam @NumberFormat(style = NumberFormat.Style.NUMBER, pattern="1") Long clicks,
            @ApiParam(value="Number (required)") @RequestParam @NumberFormat(style = NumberFormat.Style.NUMBER, pattern="1") Long impressions) {
        log.info("createNew: " + datasource);
        CsvData csvData = new CsvData(datasource, campaign, new Timestamp(daily.getTime()), clicks, impressions);

        csvData = csvDataRepository.save(csvData);
        return new ResponseEntity<CsvData>(csvData, HttpStatus.OK);
    }

    @GetMapping({"/csvdata/clicksThroughRateByDatasourceAndCampaign"})
    @ApiOperation(value = "method:getClicksThroughRateByDatasourceAndCampaign", notes="Get CTR (sum of clicks divded by sum of impressions (%)) for csv_data records specified by datasource and campaign parameters.")
    public ResponseEntity<String> getClicksThroughRateByDatasourceAndCampaign(@ApiParam(value="String (required)") @RequestParam String datasource,
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
        return new ResponseEntity<String>(ctrAsString + "%", HttpStatus.OK);
    }

    @GetMapping({"/csvdata/byDay"})
    @ApiOperation(value = "method:getByDay", notes="Get csv_data records for specified date")
    public ResponseEntity<List<CsvDataDto>> getByDay(@ApiParam(value=DATE_IN_FORMAT+" (required)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily) {
        log.info("getByDay: " + TimestampStringConverter.dateAsString(daily));
        List<CsvData> csvData = csvDataRepository.findCsvDataByDaily(new Timestamp(daily.getTime()));
        return new ResponseEntity<List<CsvDataDto>>(csvData2DtoConverter.convertList(csvData), HttpStatus.OK);
    }


    @GetMapping({"/csvdata/csvdataList"})
    @ApiOperation(value = "method:getCsvDataList", notes="Get csv_data records for given criteria (result can be filtered through datasource, campaign and daily). All filter parameters are optional (not mandatory).")
    public ResponseEntity<List<CsvDataDto>> getCsvDataList(
           @ApiParam(value="String(optional)")             @RequestParam(required=false) String datasource,
           @ApiParam(value="String(optional)")             @RequestParam(required=false) String campaign,
           @ApiParam(value=DATE_IN_FORMAT+" (optional)" )  @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date daily) {
        log.info("getCsvDataList: " + datasource + " / " + campaign + " / " + TimestampStringConverter.dateAsString(daily));
        List<CsvDataDto> result =  csvData2DtoConverter.convertList(csvDataRepository.findAll(
                CsvDataCriteriaBuilder.buildCriteriaByDatasourceCampaignDaily(datasource, campaign, daily)));
        return new ResponseEntity<List<CsvDataDto>>(result, HttpStatus.OK);
    }

    @DeleteMapping({"/csvdata/deleteAll"})
    @ApiOperation(value = "method:deleteAll", notes="Deletes all csv_data records.")
    public ResponseEntity<String> deleteAll() {
        log.info("deleteAll");
        csvDataRepository.deleteAll();
        return new ResponseEntity<String>("", HttpStatus.OK);
    }

    @GetMapping({"/csvdata/sumOfClicks"})
    @ApiOperation(value = "method:getSumOfClicks", notes="Count sum of click for given criteria (result can be filtered through datasource and dates range). All filter parameters are optional (not mandatory).")
    public ResponseEntity<String> getSumOfClicks(
            @ApiParam(value="String (optional)") @RequestParam(required=false) String datasource,
            @ApiParam(value=DATE_IN_FORMAT+" (optional)") @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyFrom,
            @ApiParam(value=DATE_IN_FORMAT+" (optional)") @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dailyTo) {
        log.info("getSumOfClicks: " + datasource + " / " + TimestampStringConverter.dateAsString(dailyFrom) +
                " / " + TimestampStringConverter.dateAsString(dailyTo));

        Long clicks = csvDataRepository.sumOfClicks(
                CsvDataCriteriaBuilder.buildCriteriaForCountSum(datasource, dailyFrom, dailyTo),
                Long.class, "clicks");
        if(clicks==null)
            clicks=0L;
        return new ResponseEntity<String>(""+clicks, HttpStatus.OK);
    }

}
