package com.example.narthella.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonComparator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> findDifferences(JsonNode node1, JsonNode node2, String path) {
        List<String> diffs = new ArrayList<>();

        // Handle object nodes
        Iterator<String> fieldNames = node1.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            String currentPath = path.isEmpty() ? field : path + "." + field;

            JsonNode value1 = node1.get(field);
            JsonNode value2 = node2.get(field);

            if (value2 == null) {
                diffs.add("Missing in second: " + currentPath);
            } else if (value1.isObject() && value2.isObject()) {
                diffs.addAll(findDifferences(value1, value2, currentPath));
            } else if (!value1.equals(value2)) {
                diffs.add("Mismatch at " + currentPath + ": " + value1 + " vs " + value2);
            }
        }

        // Check fields present in node2 but missing in node1
        Iterator<String> fieldNames2 = node2.fieldNames();
        while (fieldNames2.hasNext()) {
            String field = fieldNames2.next();
            if (!node1.has(field)) {
                String currentPath = path.isEmpty() ? field : path + "." + field;
                diffs.add("Missing in first: " + currentPath);
            }
        }

        return diffs;
    }

    public static List<String> compareJson(String json1, String json2) throws Exception {
        JsonNode node1 = objectMapper.readTree(json1);
        JsonNode node2 = objectMapper.readTree(json2);
        return findDifferences(node1, node2, "");
    }
}


