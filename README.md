"# csvUploadFile" 
"# csvUploadFile"
This project provides a Spring application for uploading , processing and getting  CSV data from the Excel file
# Features
.Upload Excel file
. Validate file format(check for .xls or xlsx
extension)

. Convert Excel data into list of entries
. Saves CSV file to the database

# Dependencies
. Springboot framework
. Apache POI (for reading excel files)
. Junit and Mokito for unit testing

# Api Documentation


   

1. post/upload: uploads an Excel file

. Request body Multipart named file

. Response Body: on success 202 Accepted
and OnError : 400 BAD_REQUEST: invalid file format or empty file
500 INTERNAL_SERVER_ERROR: internal server error during processing


2. Get//download/id : Download The excel file

. PathVariable we have taken the Id

. FileProcessing : if file is processing then give upload is still been processing

.Response Body :404 NOT_FOUND : file is not found
 500 INTERNAL_SERVER_ERROR : internal server error

# Running the Application
. Cole or Download the project Repository

. Ensure you have Java and Maven installed

. Navigate to the project directory into your terminal

. Run mvn clean install to build project

.Run mvn spring-boot:run to start the project
 
