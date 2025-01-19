package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.dto.EntityDetails;
import dev.stiebo.springaisamples.exception.FileErrorException;
import dev.stiebo.springaisamples.model.FileResource;
import dev.stiebo.springaisamples.service.AnalyzeEntityService;
import dev.stiebo.springaisamples.service.ChatClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Service
public class AnalyzeEntityServiceImpl implements AnalyzeEntityService {
    private final ChatClientService chatClientService;

    @Value("classpath:/prompts/analyzeentityprompt.st")
    private Resource analyzeEntityPrompt;

    @Autowired
    public AnalyzeEntityServiceImpl(ChatClientService chatClientService) {
        this.chatClientService = chatClientService;
    }

    @Override
    public EntityDetails analyzeEntity(String entityName) {
        return chatClientService.getResponse(EntityDetails.class, analyzeEntityPrompt, entityName);
    }

    @Override
    public ResponseEntity<byte[]> analyzeEntitiesXls(FileResource fileResource) {
        confirmXlsFileType(fileResource);
        // Step 1: Read the Excel file from the resource input stream
        try (InputStream inputStream = fileResource.resource().getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            // assuming first sheet is the one we are looking for
            Sheet sheet = workbook.getSheetAt(0);

            // Step 2: Always assume "Name" is in the first column (index 0)
            Row headerRow = sheet.getRow(0); // Assuming first row is the header
            if (headerRow == null || headerRow.getCell(0) == null ||
                    !"Name".equalsIgnoreCase(headerRow.getCell(0).getStringCellValue())) {
                throw new IllegalArgumentException("The first column must have the header 'Name'");
            }

            // Add headers for new columns, assuming they are all empty
            headerRow.createCell(1).setCellValue("EntityType");
            headerRow.createCell(2).setCellValue("ShortDescription");
            headerRow.createCell(3).setCellValue("Country");
            headerRow.createCell(4).setCellValue("URL");

            // Step 3: Iterate over rows and process names
            Map<Integer, EntityDetails> results = new HashMap<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell nameCell = row.getCell(0); // Always use the first column
                if (nameCell == null) continue;

                String name = nameCell.getStringCellValue();

                EntityDetails result = chatClientService.getResponse(EntityDetails.class, analyzeEntityPrompt,
                        "Entity name: " + name);

                // store result
                results.put(i, result);
            }

            // Step 4: Update Excel with results
            for (Map.Entry<Integer, EntityDetails> entry : results.entrySet()) {
                Row row = sheet.getRow(entry.getKey());

                EntityDetails dto = entry.getValue();
                row.createCell(1).setCellValue(dto.entityType().toString());
                row.createCell(2).setCellValue(dto.shortDescription());
                row.createCell(3).setCellValue(dto.country());
                row.createCell(4).setCellValue(dto.url());
            }

            // Step 5: Write the updated Excel file to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            byte[] updatedFile = outputStream.toByteArray();

            // Step 6: Return the updated file
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=processed_" + fileResource.fileName())
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(updatedFile);
        }        catch (IOException ioException) {
            throw new FileErrorException("Error accessing file");
        }
    }

    private void confirmXlsFileType(FileResource fileResource) throws FileErrorException {
        if (!(fileResource.contentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                fileResource.fileName().endsWith(".xlsx"))) {
            throw new FileErrorException("Invalid File Type: " + fileResource.contentType());
        }
    }

}