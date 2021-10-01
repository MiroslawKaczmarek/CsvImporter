package com.example.csvimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
//@Import({CommonsLibComponents.class})
public class CsvImporterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsvImporterApplication.class, args);
    }

}
