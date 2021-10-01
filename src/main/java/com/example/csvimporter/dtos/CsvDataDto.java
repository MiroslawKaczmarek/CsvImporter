package com.example.csvimporter.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor

public class CsvDataDto {
    private Long id;

    private String datasource;

    private String campaign;

    private String daily;

    private Long clicks;

    private Long impressions;
}
