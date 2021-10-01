package com.example.csvimporter.validators;

import com.example.csvimporter.dtos.UploadResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class UploadFileValidator {

    public UploadResultDto validateLevelOne(MultipartFile file) {
        if(file==null || file.getSize() == 0)
            return new UploadResultDto(false, "File parameter cannot be null or empty.");

        if(file.getSize() > 2000000)   //bigger than ~2 MB
            return new UploadResultDto(false, "File is too big. Max size is ~2MB.");

        //TODO - can do other validations like file extension, headder, etc
        return new UploadResultDto(true,"");
    }

    public boolean validateNumberOfColumns(List<List<String>> csvStructure) {
        for(List<String> row: csvStructure)
            if(row.size()!=5)
                return false;
        return true;
    }

//    public UploadResultDto validateLevelTwo(List<List<String>> csvRecords) {
//    }

}
