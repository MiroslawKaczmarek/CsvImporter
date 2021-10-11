DROP TABLE IF EXISTS csv_data;

CREATE TABLE csv_data
   (
      id                            SERIAL PRIMARY KEY,
      datasource                    VARCHAR(255),
      campaign                      VARCHAR(255),
      daily                         TIMESTAMP,
      clicks                        BIGINT,
      impressions                   BIGINT,
      active                        boolean
   );
