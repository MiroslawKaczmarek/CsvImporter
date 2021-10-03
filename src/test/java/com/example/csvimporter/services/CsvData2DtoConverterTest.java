package com.example.csvimporter.services;

import com.example.csvimporter.dtos.CsvDataDto;
import com.example.csvimporter.models.CsvData;
import com.example.csvimporter.utils.TimestampStringConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class CsvData2DtoConverterTest {

    @Inject
    private CsvData2DtoConverter subject;

    @Test
    public void convertList() {
        List<CsvData> listCsvData = new ArrayList<>();
        listCsvData.add(new CsvData("AAA", "BBB", TimestampStringConverter.stringToTimestamp("2020-12-20"), 5L, 100L));
        listCsvData.add(new CsvData("AAA", "BBB", TimestampStringConverter.stringToTimestamp("2020-12-21"), 5L, 100L));
        List<CsvDataDto> listDto = subject.convertList(listCsvData);
        Assertions.assertEquals(2, listDto.size());
        Assertions.assertEquals("AAA", listDto.get(0).getDatasource());
    }

}
