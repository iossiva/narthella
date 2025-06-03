package com.example.narthella.Service;

import com.example.narthella.DAO.NorthellaRepository;
import com.example.narthella.Model.ConvertedDTO;
import com.example.narthella.Model.RawDTO;
import com.example.narthella.Model.RawOrderLine;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
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

import java.io.*;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

        List<UUID> dataList = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0); // read first sheet

        for (Row row : sheet) {
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        dataList.add(UUID.fromString(cell.getStringCellValue().toString()));
                        break;
                    case NUMERIC:
                        dataList.add(UUID.fromString(String.valueOf(cell.getNumericCellValue())));
                        break;
                    default:
                        dataList.add(UUID.fromString("UNKNOWN"));
                }
            }
        }

        workbook.close();
        System.out.println(dataList);
        List<Map<String, Map<String, Object>>> raw=new ArrayList<>();
        List<Map<String, Map<String, Object>>> converted=new ArrayList<>();
        raw=northellaRepository.getRawJson(dataList);
        converted=northellaRepository.getConvertedJson(dataList);
        String directoryPath = "D:\\Northella";
        Workbook workbookwrte = new XSSFWorkbook();
        String fileName = "northellaOrderLine.xlsx";
         for(int i=0;i<raw.size();i++){
            RawOrderLine combinedJson=new RawOrderLine();
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> rawMap = gson.fromJson(String.valueOf(raw.get(i).get("raw")), type);
            Map<String, Object> convertMap = gson.fromJson(String.valueOf(converted.get(i).get("converted")), type);
            System.out.println(rawMap);
            combinedJson.setRaw(rawMap);
            combinedJson.setConverted(convertMap);
            Sheet sheetWrte = workbookwrte.createSheet("Sheet"+i);
            workbookwrte=  this.northellaJsonCompare(combinedJson,workbookwrte,sheetWrte);
        }
        // Define the full path to save the file
        String filePath = Paths.get(directoryPath, fileName).toString();

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbookwrte.write(fileOut);
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
        return "workbook read sucessfully";
    }

    /*public Map<String, Object> getNorthellaJson() {
        return northellaRepository.getNorthellaJson();
    }*/

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

    public Workbook northellaJsonCompare(RawOrderLine request,Workbook workbook,Sheet sheet) {
        System.out.println("sheet== ");
        Map<String, Object> raw = request.getRaw();
        Map<String, Object> converted = request.getConverted();
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
        Map<String,Object> unmatchedKeys=new HashMap<>();
        unmatchedKeys.put("Colour notes","print:hasColourDetails");
        unmatchedKeys.put("Type of proof","print:hasProofType");
        unmatchedKeys.put("Have we specified FSC paper","print:hasFscPaperBeenSpecified");
        unmatchedKeys.put("Total number of colours","print:hasTotalColours");
        unmatchedKeys.put("Total finished quantity","print:hasFinishedQuantity");
        unmatchedKeys.put("Send to / additional details","print:hasSendToDetails");
        unmatchedKeys.put("Have we offered recycled content?","print:hasRecycledContentBeenOffered");
        unmatchedKeys.put("Colours to face and reverse are the same","print:hasColoursToFaceAndReverseAreSame");
        unmatchedKeys.put("Is artwork double sided different or same?","print:hasArtworkDoubleSidedStatus");
        unmatchedKeys.put("Coatings / sealer","print:hasCoatingOrSealer");
        raw.forEach((key, value) -> {
            AtomicInteger columnindex1 = new AtomicInteger(0);
            //headerCell.setCellValue(i.getKey());
            Row valueRow = sheet.createRow(rowIndex.getAndIncrement());
            Cell valueCell = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell.setCellValue(raw.get("Permutation ID").toString());
            Cell valueCell2 = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell2.setCellValue(key);
            // create a value cell
            String convertedCol = "has" + CaseUtils.toCamelCase(key.replaceAll("\\s+", "_"), true, '_');
            System.out.println("convertedCol=== " + convertedCol);
            List<Map<String, Object>> con = (List<Map<String, Object>>) converted.get("@graph");
            var iteratorcon = con.getFirst().entrySet().stream().iterator();
            String cellValue = "N";
            //log.info("con.getFirst()  {}",con.getFirst());
            while (iteratorcon.hasNext()) {
                var current = iteratorcon.next();
                if (current.getKey().toLowerCase().endsWith(convertedCol.toLowerCase())) {
                    System.out.println("current.getValue().getClass().getSimpleName() " + current.getValue().getClass().getSimpleName());
                    if (current.getValue().getClass().getSimpleName().endsWith("Map")) {
                        Map<String, Object> mapVal = (Map<String, Object>) current.getValue();
                        if (mapVal.get("@value") != null) {
                            System.out.println("obj == " + current.getKey());
                            System.out.println(" raw string " + value.toString());
                            System.out.println(" converted Value" + mapVal.get("@value").toString());
                            if (mapVal.get("@value").toString().equalsIgnoreCase(value.toString())) {
                                cellValue = "Y";
                            } else {
                                cellValue = "N";
                            }
                        }
                    } else {
                        //(!current.getValue().getClass().getSimpleName().equalsIgnoreCase("LinkedHashMap")) {
                        System.out.println("obj == " + current.getKey());
                        System.out.println("String == " + current.getKey());
                        System.out.println(" raw string " + value.toString());
                        System.out.println(" converted string " + current.getValue());
                        if (Objects.equals(current.getValue().toString(), value.toString())) {
                            cellValue = "Y";
                        } else {
                            cellValue = "N";
                        }
                    }
                } else {
                    String unmatchedVal = unmatchedKeys.get(key) != null ? unmatchedKeys.get(key).toString() : "";
                    if (!unmatchedVal.isEmpty() && con.getFirst().get(unmatchedVal) !=null) {
                        if (con.getFirst().get(unmatchedVal).getClass().getSimpleName().endsWith("Map")) {
                            Map<String, Object> mapVal = (Map<String, Object>) con.getFirst().get(unmatchedVal);
                            if (mapVal.get("@value") != null) {
                                System.out.println("obj == " + unmatchedVal);
                                System.out.println(" raw string " + value.toString());
                                System.out.println(" converted Value" + mapVal.get("@value").toString());
                                if (mapVal.get("@value").toString().equalsIgnoreCase(value.toString())) {
                                    cellValue = "Y";
                                } else {
                                    cellValue = "N";
                                }
                            }
                        } else {
                            System.out.println("String == " + unmatchedVal);
                            System.out.println(" raw string " + value.toString());
                            System.out.println(" converted string " + con.getFirst().get(unmatchedVal));
                            if (Objects.equals(con.getFirst().get(unmatchedVal).toString(), value.toString())) {
                                cellValue = "Y";
                            } else {
                                cellValue = "N";
                            }
                        }
                    }
                }

            }
            Cell valueCell3 = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell3.setCellValue(cellValue);
            Cell valueCell4 = valueRow.createCell(columnindex1.getAndIncrement());
            valueCell4.setCellValue(cellValue.equalsIgnoreCase("N") ? "Identified as failed" : "");
        });

        return workbook;
    }


}
