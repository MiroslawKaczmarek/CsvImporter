package com.example.csvimporter.services;

import com.example.csvimporter.controllers.CsvDataController;
import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataRepository;
import com.example.csvimporter.utils.TimestampStringConverter;
import com.example.csvimporter.validators.UploadFileValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CsvDataConverterTest {

    @Inject
    private CsvDataRepository repository;

    @Transactional
    @Before
    public void setUp(){}

    @Transactional
    @After
    public void tearDown() {
        repository.deleteAll();
    }

//    @Test
//    public void convertFileToListOfStringsNullFile() {
//        CsvDataConverter subject = new CsvDataConverter(repository);
//        MultipartFile file = null;
//        List<List<String>>  result = subject.convertFileToListOfStrings(file);
//        Assertions.assertNull(result);
//    }

    @Transactional
    @Test
    public void convertListOfStringToEntitiesAndSave() {
        CsvDataConverter subject = new CsvDataConverter(repository);
        List<List<String>> csvListStructure = buildCsvListStructure();
        long count = subject.convertListOfStringToEntitiesAndSave(csvListStructure);
        Assertions.assertEquals(220, count);
    }

    private List<List<String>> buildCsvListStructure() {
        List<List<String>> list = new ArrayList<>();
        list.add(createHeadder());
        list.add(createRow1());
        for(int i=1; i<=219; i++)
            list.add(createRow2());
        return list;
    }

    private List<String> createHeadder() {
        List<String> l = new ArrayList<>();
        l.add("datasource");
        l.add("campaign");
        l.add("daily"); //test full year
        l.add("clicks"); //test not number exception
        l.add("impressions");
        return l;
    }

    private List<String> createRow1() {
        List<String> l = new ArrayList<>();
        l.add("AAA");
        l.add("BBB");
        l.add("12/31/2019"); //test full year
        l.add("X"); //test not number exception
        l.add("55");
        return l;
    }

    private List<String> createRow2() {
        List<String> l = new ArrayList<>();
        l.add("CCC");
        l.add("DDD");
        l.add(""); //test empty date
        l.add("5");
        l.add("66");
        return l;
    }



}
