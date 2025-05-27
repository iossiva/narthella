package com.example.narthella.Service;

import com.example.narthella.DAO.NorthellaRepository;
import com.example.narthella.Model.RawOrderLine;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Log4j
public class NarthellaSetvice {
    @Autowired
    private final NorthellaRepository northellaRepository;

    public NarthellaSetvice(NorthellaRepository northellaRepository) {
        this.northellaRepository = northellaRepository;
    }


    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Please upload a valid Excel file.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet

            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t");
                            break;
                        default:
                            System.out.print("UNKNOWN\t");
                    }
                }
                System.out.println();
            }

            workbook.close();
            return "File processed successfully";
        } catch (Exception e) {
            throw new Exception("Failed to process Excel file: " + e.getMessage());
        }
    }

    public Map<String, Object> getNorthellaJson() {
        return northellaRepository.getNorthellaJson();
    }

    public void generateExcelFile(String[] header, String[] values) {
        String fileName = "northellaOrderLine1";
        String directoryPath = "D:\\Northella";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sample Sheet");

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int j = 0; j < header.length; j++) {
            Cell headerCell = headerRow.createCell(j);
            headerCell.setCellValue(header[j]);
        }

        // Create values
        for (int j = 1; j < header.length; j++) {
            Row valueRow = sheet.createRow(j);
            Cell valueCell = valueRow.createCell(j);
            valueCell.setCellValue(header[j]);
        }

        // Define the full path to save the file
        String filePath = Paths.get(directoryPath, fileName).toString();

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            System.out.println("Excel file created successfully at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String northellaJsonCompare(RawOrderLine request) {
        Map<String, Object> raw = request.getRaw();
        Map<String, Object> converted = request.getConverted();
        String directoryPath = "D:\\Northella";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sample Sheet");
        String fileName = "northellaOrderLine.xlsx";
        AtomicInteger columnindex = new AtomicInteger(0);
        AtomicInteger rowIndex = new AtomicInteger(0);
        // Create header row
        Row headerRow = sheet.createRow(rowIndex.getAndIncrement());
        Cell headerCell = headerRow.createCell(columnindex.getAndIncrement());
        headerCell.setCellValue("Permutation ID");
        Cell headerCell1 = headerRow.createCell(columnindex.getAndIncrement());
        headerCell1.setCellValue("RawOrderline Property");
        Cell headerCell2 = headerRow.createCell(columnindex.getAndIncrement());
        headerCell2.setCellValue("Correctly extracted?");
        Cell headerCell3 = headerRow.createCell(columnindex.getAndIncrement());
        headerCell3.setCellValue("Notes");
        raw.entrySet().stream().forEach(i -> {
            AtomicInteger columnindex1 = new AtomicInteger(0);
            //headerCell.setCellValue(i.getKey());
            Row valueRow = sheet.createRow(rowIndex.getAndIncrement());
            Cell valueCell = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell.setCellValue(raw.get("Permutation ID").toString());
            Cell valueCell2 = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell2.setCellValue(i.getKey());
            // create a value cell
            String convertedCol = "has" + CaseUtils.toCamelCase(i.getKey().replaceAll("\\s+", "_"), true, '_');
            System.out.println("convertedCol=== " + convertedCol);
            List<Map<String, Object>> con = (List<Map<String, Object>>) converted.get("@graph");
            var iteratorcon = con.getFirst().entrySet().stream().iterator();
            String cellValue="N";
            //log.info("con.getFirst()  {}",con.getFirst());
            while (iteratorcon.hasNext()) {
                var current = iteratorcon.next();
                if (current.getKey().toLowerCase().endsWith(convertedCol.toLowerCase())) {
                    System.out.println("current.getValue().getClass().getSimpleName() " + current.getValue().getClass().getSimpleName());
                    if (current.getValue().getClass().getSimpleName().equalsIgnoreCase("LinkedHashMap")) {
                        Map<String, Object> mapVal = (Map<String, Object>) current.getValue();
                        if (mapVal.get("@value") != null) {
                            System.out.println("obj == " + current.getKey());
                            System.out.println(" raw string " + i.getValue().toString());
                            System.out.println(" converted Value" + mapVal.get("@value").toString());
                            if (mapVal.get("@value").toString().equalsIgnoreCase(i.getValue().toString())) {
                                cellValue="Y";
                            } else {
                                cellValue="N";
                            }
                        }
                    }
                    else {
                        //(!current.getValue().getClass().getSimpleName().equalsIgnoreCase("LinkedHashMap")) {
                        System.out.println("obj == " + current.getKey());
                        System.out.println("String == " + current.getKey());
                        System.out.println(" raw string " + i.getValue().toString());
                        System.out.println(" converted string " + current.getValue());
                        if (Objects.equals(current.getValue().toString(), i.getValue().toString())) {
                            cellValue="Y";
                        } else {
                            cellValue="N";
                        }
                    }
                }
                if(i.getKey().equalsIgnoreCase("Colour notes") && current.getKey().equalsIgnoreCase("print:hasColourDetails")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Type of proof") && current.getKey().equalsIgnoreCase("print:hasProofType")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Have we specified FSC paper") && current.getKey().equalsIgnoreCase("print:hasFscPaperBeenSpecified")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Total number of colours") && current.getKey().equalsIgnoreCase("print:hasTotalColours")){
                    Map<String, Object> mapVal = (Map<String, Object>) current.getValue();
                    if(i.getValue().toString().equalsIgnoreCase(mapVal.get("@value").toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Total finished quantity") && current.getKey().equalsIgnoreCase("print:hasFinishedQuantity")){
                    Map<String, Object> mapVal = (Map<String, Object>) current.getValue();
                    if(i.getValue().toString().equalsIgnoreCase(mapVal.get("@value").toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Send to / additional details") && current.getKey().equalsIgnoreCase("print:hasSendToDetails")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Have we offered recycled content?") && current.getKey().equalsIgnoreCase("print:hasRecycledContentBeenOffered")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Colours to face and reverse are the same") && current.getKey().equalsIgnoreCase("print:coloursToFaceAndReverseAreSame")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
                if(i.getKey().equalsIgnoreCase("Is artwork double sided different or same?") && current.getKey().equalsIgnoreCase("print:hasArtworkDoubleSidedStatus")){
                    if(i.getValue().toString().equalsIgnoreCase(current.getValue().toString())) cellValue="Y";
                    else cellValue="N";
                }
            }
            Cell valueCell3 = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell3.setCellValue(cellValue);
            Cell valueCell4 = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell4.setCellValue(cellValue.equalsIgnoreCase("N")?"Identified as failed":"");
        });
        // Define the full path to save the file
        String filePath = Paths.get(directoryPath, fileName).toString();

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            System.out.println("Excel file created successfully at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "compared successfully";
    }


}
