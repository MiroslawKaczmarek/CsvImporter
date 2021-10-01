package com.example.csvimporter.models;

//import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "csv_data")
@NoArgsConstructor
@AllArgsConstructor
//@ApiModel(description = "Uploaded entry")
public class CsvData implements Serializable {

    private static final long serialVersionUID = -3442876734863637805L;

    public CsvData(String datasource, String campaign, Timestamp daily, Long clicks, Long impressions){
        this.datasource = datasource;
        this.campaign = campaign;
        this.daily = daily;
        this.clicks = clicks;
        this.impressions = impressions;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, length = 40)
    //@ApiModelProperty("Unique ID")
    private Long id;

    @Column(name = "datasource", nullable = true)
    private String datasource;

    @Column(name = "campaign", nullable = true)
    private String campaign;

    @Column(name = "daily", nullable = true)
    private Timestamp daily;

    @Column(name = "clicks", nullable = true)
    private Long clicks;

    @Column(name = "impressions", nullable = true)
    private Long impressions;

}
