package com.example.csvimporter.controllers;

import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.repositories.CsvDataRepository;
import com.example.csvimporter.services.CsvData2DtoConverter;
import com.example.csvimporter.services.CsvDataConverter;
import com.example.csvimporter.utils.TimestampStringConverter;
import com.example.csvimporter.validators.UploadFileValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
//TODO setup separate datasource for test (application-test.properties)
public class CsvDataControllerTest {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private CsvDataRepository repository;

    @Inject
    private UploadFileValidator validator;

    @Inject
    private CsvDataConverter csvDataConverter;

    @Inject
    private CsvData2DtoConverter csvData2DtoConverter;


    @Transactional
    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Transactional
    @After
    public void tearDown() {
        repository.deleteAll();
    }

    @Transactional
    @Test
    public void uploadCsvFileOkShowOk() throws IOException {
        String correctFilename = "CsvDataOK.csv";
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(correctFilename)).getFile());
        byte[] payload = Files.readAllBytes(file.toPath());
        MockMultipartFile multiFile = new MockMultipartFile("file", correctFilename, "", payload);

        CsvDataController controller = new CsvDataController(validator, repository, csvDataConverter, csvData2DtoConverter);
        ResponseEntity response = controller.uploadCsvFile(multiFile);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody() == null ? "" : response.getBody().toString();
        Assertions.assertEquals("37 records has been created.", body);
    }

    @Transactional
    @Test
    public void uploadCsvFileBad() throws IOException {
        String correctFilename = "CsvDataBad.csv";
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(correctFilename)).getFile());
        byte[] payload = Files.readAllBytes(file.toPath());
        MockMultipartFile multiFile = new MockMultipartFile("file", correctFilename, "", payload);

        CsvDataController controller = new CsvDataController(validator, repository, csvDataConverter, csvData2DtoConverter);
        ResponseEntity response = controller.uploadCsvFile(multiFile);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String body = response.getBody() == null ? "" : response.getBody().toString();
        Assertions.assertEquals("Each row in CSV file must contains 5 records", body);
    }

    @Transactional
    @Test
    public void getSumOfClicksAllCriteria() throws Exception {
        createData();

        mockMvc.perform(get("/csvdata/sumOfClicks?datasource=AAA&dailyFrom=2020-12-20&dailyTo=2020-12-22")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("33"));
    }

    @Transactional
    @Test
    public void getSumOfClicksNoCriteria() throws Exception {
        createData();

        mockMvc.perform(get("/csvdata/sumOfClicks")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("54"));
    }

    @Transactional
    @Test
    public void newRecord() throws Exception {
        mockMvc.perform(put("/csvdata/new?datasource=AAA2&campaign=BBB2&daily=2020-12-25&clicks=10&impressions=100")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Assertions.assertEquals(1, repository.findAll().size());
    }

    @Transactional
    @Test
    public void getClicksThroughRateByDatasourceAndCampaign() throws Exception {
        createData();
        mockMvc.perform(get("/csvdata/clicksThroughRateByDatasourceAndCampaign?datasource=AAA&campaign=BBB")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("6.50%"));
    }

    @Transactional
    @Test
    public void getByDay() throws Exception {
        createData();
        mockMvc.perform(get("/csvdata/byDay?daily=2020-12-20")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clicks").value("5"))
                .andExpect(jsonPath("$.[1].clicks").value("5"))
                .andExpect(jsonPath("$.[2].clicks").value("8"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Transactional
    @Test
    public void getCsvDataList1() throws Exception {
        createData();
        mockMvc.perform(get("/csvdata/csvdataList?datasource=AAA")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clicks").value("5"))
                .andExpect(jsonPath("$.[1].clicks").value("6"))
                .andExpect(jsonPath("$", hasSize(8)));
    }

    @Transactional
    @Test
    public void getCsvDataList2() throws Exception {
        createData();
        mockMvc.perform(get("/csvdata/csvdataList?campaign=CCC&daily=2020-12-20")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clicks").value("5"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Transactional
    @Test
    public void deleteAll() throws Exception {
        createData();
        mockMvc.perform(delete("/csvdata/deleteAll")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Assertions.assertEquals(0, repository.findAll().size());
    }


    private void createData() {
        repository.save(new CsvData("AAA", "BBB", TimestampStringConverter.stringToTimestamp("2020-12-20"), 5L, 100L));
        repository.save(new CsvData("AAA", "BBB", TimestampStringConverter.stringToTimestamp("2020-12-21"), 6L, 100L));
        repository.save(new CsvData("AAA", "BBB", TimestampStringConverter.stringToTimestamp("2020-12-22"), 7L, 100L));
        repository.save(new CsvData("AAA", "BBB", TimestampStringConverter.stringToTimestamp("2020-12-23"), 8L, 100L));
        repository.save(new CsvData("AAA", "CCC", TimestampStringConverter.stringToTimestamp("2020-12-20"), 5L, 100L));
        repository.save(new CsvData("AAA", "CCC", TimestampStringConverter.stringToTimestamp("2020-12-21"), 5L, 100L));
        repository.save(new CsvData("AAA", "CCC", TimestampStringConverter.stringToTimestamp("2020-12-22"), 5L, 100L));
        repository.save(new CsvData("AAA", "CCC", TimestampStringConverter.stringToTimestamp("2020-12-23"), 5L, 100L));
        repository.save(new CsvData("QQQ", "CCC", TimestampStringConverter.stringToTimestamp("2020-12-20"), 8L, 100L));
    }

}
