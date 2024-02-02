package uk.ac.ebi.ena.dcap.scl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class CountClient {
    static final String PORTAL_API_COUNT_URL = "https://www.ebi.ac.uk/ena/portal/api/count?result=%s&format=json";

    @SneakyThrows
    public static long getCountFromResults(String result, String query) {
        // Your JSON array as a string

        // Create ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse JSON array
        JsonNode objNode = objectMapper.readTree(getJson(PORTAL_API_COUNT_URL, result, query));

        // Iterate through objects in the array
        long codingValue = objNode.get("count").asLong();
        return codingValue;
    }

    @SneakyThrows
    private static String getJson(String portalApiResultsUrl, String result, String query) {

        String urlStr = String.format(PORTAL_API_COUNT_URL, result);
        if (StringUtils.isNotBlank(query)) {
            urlStr += "&query=" + query;
        }
        log.info("getting count from:{}", urlStr);
        // Create URL object
        URL url = new URL(urlStr);

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Read the response from the input stream
        StringBuilder response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

        }

        return response.toString();
    }

}
