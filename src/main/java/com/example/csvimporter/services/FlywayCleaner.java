package com.example.csvimporter.services;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;

//TODO - remove me
public class FlywayCleaner {

//    @Bean
//    public FlywayMigrationStrategy cleanMigrateStrategy() {
//        return flyway -> {
//            flyway.repair();
//            flyway.migrate();
//        };
//    }

//    public Flyway flyway(DataSource theDataSource) {
//        Flyway flyway = new Flyway();
//        flyway.setDataSource(theDataSource);
//        flyway.setLocations("classpath:db/migration");
//        flyway.clean();
//        flyway.migrate();
//
//        return flyway;
//    }
}
