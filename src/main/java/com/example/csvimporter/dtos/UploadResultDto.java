package com.example.csvimporter.dtos;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadResultDto {

    private boolean success;
    private String additionalInfo;

}
