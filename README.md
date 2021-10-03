## What is CsvImporter ?
CsvImporter - it is test project - Simple Data Warehouse (extract, transform, load, query). Access via API

## How to run
* Before you start you need to have on your machine - maven, java (at least 8)
* Checkout the project 
* On your machine open command line
* Go to directory with project (the directory where is pom.xml)
* Call :
  **mvn spring-boot:run**
* For call jUnits you can call command:
  **mvn test** 

## Optional run
Project is deployed on heroku server here:
https://fierce-shelf-64688.herokuapp.com/swagger-ui.html
Important - this heroku server(account) is free. In result all applications after 30 minutes of inactivity are stoped.
So, for use given heroku server - please let me know (+48 664-959-442) - then I will wake up him :)  

## Tech stack
* java 8
* database H2 (memory)
* spring boot

##Operating on API 
On initial when you start server open http://localhost:8080/swagger-ui.html (or if you want use existing server: https://fierce-shelf-64688.herokuapp.com/swagger-ui.html). Open group **csv-data-controller**. Because as a database is used memory solution (H2), first API operation to do should be **/csvdata/uploadCsv**.
Should be used CSV file in format:
>headder: datasource,campaign,daily,clicks,impressions<br>
>rows:    (string),(string),(date as string in format MM/dd/yy),(long),(long)<br>

Example file is here: **./src/test/resources/CsvDataOK.csv**

After load some data, can be used other API operations - just check on swagger

##Additional thoughts about upload algorithm
Current solution works fine, however if the assumption of the API would be much bigger CSV files, I would consider alternative solution. I was thinking about split upload process to 2 parts.
* First would be just upload file (API will respond quickly).
* Second part (as separate process on server) would be parsing data and add to the database.
    
However in this solution in case any problems (like validation) client will not know about them. So, in this case will be also needed add some way to check status validation/parsing by customer.   
Conclussion - this alternative solution is longer to implement, but in case very big CSV files would be worth to consider.

