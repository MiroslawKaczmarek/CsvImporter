package com.example.csvimporter.services;

import com.example.csvimporter.dtos.CsvDataDto;
import com.example.csvimporter.models.CsvData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CsvData2DtoConverter {

    public List<CsvDataDto> convertList(List<CsvData> csvData) {
        List<CsvDataDto> converted = new ArrayList<>();
        for(CsvData cData: csvData)
            converted.add(convertElement(cData));
        return converted;
    }

    public CsvDataDto convertElement(CsvData csvData) {
        CsvDataDto cdDto = new CsvDataDto(csvData.getId(), csvData.getDatasource(),
                csvData.getCampaign(), convertDate(csvData.getDaily()), csvData.getClicks(), csvData.getImpressions());
        return cdDto;
    }

    private String convertDate(Timestamp daily) {
        if(daily==null)
            return "";
        Date dt = new Date(daily.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(dt);
    }
}
