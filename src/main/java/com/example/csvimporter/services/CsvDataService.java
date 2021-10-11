package com.example.csvimporter.services;

import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataRepository;
import com.example.csvimporter.utils.TimestampStringConverter;
import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvDataService {

    private final CsvDataRepository csvDataRepository;

    public CsvDataService(CsvDataRepository csvDataRepository) {
        this.csvDataRepository = csvDataRepository;
    }

    public CsvDataImportResult saveData(MultipartFile file) {
        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVReader csvReader = new CSVReader(reader);
            checkHeader(csvReader.readNext()); //read header line and check
            long numberOfImportedLines = saveLines(csvReader);
            return new CsvDataImportResult(numberOfImportedLines, null);
        } catch (Exception e) {
            //rollback all created records because of error and return object with error
            csvDataRepository.deleteNotActive();
            log.error(e.getMessage());
            return new CsvDataImportResult(0, e.getMessage());
        }
    }

    private void checkHeader(String[] line) throws Exception{
        if(line==null)
            throw new Exception("First line is empty. Expected header.");
        if(line.length!=5)
            throw new Exception("First line (header) should have 5 lines. Have " + line.length);
        //check line header
    }

    private long saveLines(CSVReader csvReader) throws Exception {
        long numberOfImportedLines = 0;
        int commitPack = 0;
        String[] line;
        List<CsvData> recordsToSave = new ArrayList<>();
        while ((line = csvReader.readNext()) != null) {
            numberOfImportedLines++;
            CsvData cd = convertStringsArrayToCsvData(line, numberOfImportedLines);
            recordsToSave.add(cd);
            commitPack++;
            if(commitPack==200) {
                csvDataRepository.saveAll(recordsToSave);
                commitPack=0;
                recordsToSave.clear();
            }
        }
        if(commitPack>0)
            csvDataRepository.saveAll(recordsToSave);
        csvDataRepository.activateNewCreatedRecords();
        return numberOfImportedLines;
    }

    private CsvData convertStringsArrayToCsvData(String[] line, long lineNumber) throws Exception {
        if(line.length!=5)
            throw new Exception("Row " + lineNumber + " not contain correct size of records. Expected 5 (was  " + line.length + ").");

        CsvData cDataDbObject = new CsvData();
        cDataDbObject.setDatasource(line[0]);
        cDataDbObject.setCampaign(line[1]);
        cDataDbObject.setDaily(TimestampStringConverter.convertToTimestamp(line[2]));
        cDataDbObject.setClicks(convertToNumber(line[3]));
        cDataDbObject.setImpressions(convertToNumber(line[4]));
        cDataDbObject.setActive(false);
        return cDataDbObject;
    }

    private Long convertToNumber(String s) {
        try {
            return Long.parseLong(s);
        }
        catch(NumberFormatException nfe){
            return null;
        }
    }

    @Getter
    public class CsvDataImportResult {
        private long importedRecords;
        private String errorReason;
        public CsvDataImportResult(long importedRecords, String errorReason) {
            this.importedRecords = importedRecords;
            this.errorReason = errorReason;
        }

    }

}
