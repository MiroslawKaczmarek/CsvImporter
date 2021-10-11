package com.example.csvimporter.services;

import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataCriteriaBuilder;
import com.example.csvimporter.repositories.CsvDataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class CsvDataServiceTest {

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

    @Test
    public void noHeader()  throws IOException {
        MultipartFile mf = generateMultipartFile("CsvDataBad_NoHeader.csv");
        CsvDataService subject = new CsvDataService(repository);
        CsvDataService.CsvDataImportResult result = subject.saveData(mf);
        Assertions.assertEquals(0, result.getImportedRecords());
        Assertions.assertEquals("First line is empty. Expected header.", result.getErrorReason());
    }

    @Test
    public void tooManyColumnsInHeader()  throws IOException {
        MultipartFile mf = generateMultipartFile("CsvDataBad_TooManyColumnsInHeader.csv");
        CsvDataService subject = new CsvDataService(repository);
        CsvDataService.CsvDataImportResult result = subject.saveData(mf);
        Assertions.assertEquals(0, result.getImportedRecords());
        Assertions.assertEquals("First line (header) should have 5 lines. Have 6", result.getErrorReason());
    }

    @Test
    public void tooShortRow()  throws IOException {
        MultipartFile mf = generateMultipartFile("CsvDataBad_TooShortRow.csv");
        CsvDataService subject = new CsvDataService(repository);
        CsvDataService.CsvDataImportResult result = subject.saveData(mf);
        Assertions.assertEquals(0, result.getImportedRecords());
        Assertions.assertEquals("Row 2 not contain correct size of records. Expected 5 (was  3).", result.getErrorReason());
    }

    @Test
    public void okManyRecords()  throws IOException {
        MultipartFile mf = generateMultipartFile("CsvDataOK_Big.csv");
        CsvDataService subject = new CsvDataService(repository);
        CsvDataService.CsvDataImportResult result = subject.saveData(mf);
        Assertions.assertEquals(222, result.getImportedRecords());
        Assertions.assertNull(result.getErrorReason());

        List<CsvData> r = repository.findAll(
                CsvDataCriteriaBuilder.buildCriteriaByDatasourceCampaignDaily("AAA", null, null));
        Assertions.assertEquals(36, r.size());
    }

    @Test
    public void manyRecordsComitPartAndError()  throws IOException {
        MultipartFile mf = generateMultipartFile("CsvDataBad_Big.csv");
        CsvDataService subject = new CsvDataService(repository);
        CsvDataService.CsvDataImportResult result = subject.saveData(mf);
        Assertions.assertEquals(0, result.getImportedRecords());
        Assertions.assertNotNull(result.getErrorReason());

        List<CsvData> r = repository.findAll(
                CsvDataCriteriaBuilder.buildCriteriaByDatasourceCampaignDaily(null, null, null));
        Assertions.assertEquals(0, r.size());

    }

    private MultipartFile generateMultipartFile(String fileName)  throws IOException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile());
        byte[] payload = Files.readAllBytes(file.toPath());
        return new MockMultipartFile("file", fileName, "", payload);
    }

}
