package com.example.csvimporter.services;

import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataRepository;
import com.example.csvimporter.utils.TimestampStringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CsvDataConverter {

    private final CsvDataRepository csvDataRepository;

    public CsvDataConverter(CsvDataRepository csvDataRepository) {
        this.csvDataRepository = csvDataRepository;
    }

    public List<List<String>> convertFileToListOfStrings(MultipartFile file)  {
        String csvAsString = null;
        try {
            csvAsString = new String(file.getBytes());
        } catch (IOException e) {
            return null;
        }
        return convertFlatStringToStructure(csvAsString);

    }

    public long convertListOfStringToEntitiesAndSave(List<List<String>> csvDataList) {
        log.info("convert data entities and save:");
        List<CsvData> recordsToSave = new ArrayList<>();
        for(int i=1; i<csvDataList.size(); i++) { //skip first line (header)
            CsvData cDataDbObject = new CsvData();
            List<String> csvRow = csvDataList.get(i);
            cDataDbObject.setDatasource(csvRow.get(0));
            cDataDbObject.setCampaign(csvRow.get(1));
            cDataDbObject.setDaily(TimestampStringConverter.convertToTimestamp(csvRow.get(2)));
            cDataDbObject.setClicks(convertToNumber(csvRow.get(3)));
            cDataDbObject.setImpressions(convertToNumber(csvRow.get(4)));
            recordsToSave.add(cDataDbObject);
        }
        //TODO - here can be added validation like mandatory columns
        saveToDatabaseInBatch(recordsToSave);
        return recordsToSave.size();
    }

    private List<List<String>> convertFlatStringToStructure(String fileCsvAsString) {
        //TODO - think about use library opencsv/supercsv/or something
        List<String> linesCsv = Arrays.asList(fileCsvAsString.split("\n"));
        List<List<String>> cellsCsv = new ArrayList<>();
        for(String line: linesCsv) {
            String[] elements = line.split(",");
            List<String> rowElements = Arrays.asList(elements);
            cellsCsv.add(rowElements);
        }
        return cellsCsv;
    }

    private void saveToDatabaseInBatch(List<CsvData> recordsToSave) {
        List<CsvData> batch = new ArrayList<>();
        int counter=0;
        for(CsvData csvData: recordsToSave) {
            batch.add(csvData);
            counter++;
            if(counter==200) {
                csvDataRepository.saveAll(batch);
                counter=0;
                batch = new ArrayList<>();
            }
        }
        if(counter>0)
            csvDataRepository.saveAll(batch);
    }

    private Long convertToNumber(String s) {
        try {
            return Long.parseLong(s);
        }
        catch(NumberFormatException nfe){
            return null;
        }
    }

}
