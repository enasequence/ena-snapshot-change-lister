/*******************************************************************************
 * Copyright 2021 EMBL-EBI, Hinxton outstation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.ena.dcap.scl.service;

import com.github.davidmoten.bigsorter.Sorter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.ena.dcap.scl.model.DataType;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

@Component
@Slf4j
public class SnapshotClient {

    static final String PORTAL_API_URL = "https://www.ebi.ac.uk/ena/portal/api/search?result=%s&fields=%s";


    static final String LIVELIST_URL = "https://www.ebi.ac.uk/ena/browser/api/livelist/%s?fields=%s";
    static final String PARENT_ACCESSION = "parent_accession";
    static final String ACCESSION = "accession";
    static final String LAST_UPDATED = "last_updated";

    @Autowired
    CountClient countClient;

    private static String getFields(String resultId, boolean includeParentAccession) {
        String defaultFields;
        if (includeParentAccession && resultId.equalsIgnoreCase("coding") ||
                resultId.equalsIgnoreCase("noncoding")) {
            defaultFields = ACCESSION + "," + PARENT_ACCESSION + "," + LAST_UPDATED;
        } else {
            defaultFields = ACCESSION + "," + LAST_UPDATED;
        }

        return defaultFields;
    }

    public static void bigSortFile(File infile, File outfile) {

        Sorter
                // set both serializer and natural comparator
                .linesUtf8()
                .input(infile)
                .output(outfile)
                .sort();
    }

    @SneakyThrows
    public File getLatestSnapshot(DataType dataType, File outputFile, String query, boolean includeParentAccession) {
        String req;
        if (StringUtils.isNotBlank(query)) {
            req = String.format(PORTAL_API_URL, dataType.name().toLowerCase(), getFields(dataType.name(),
                    includeParentAccession));
            req += "&query=" + query;

        } else {
            req = String.format(LIVELIST_URL, dataType.name().toLowerCase(), getFields(dataType.name(),
                    includeParentAccession));
        }
        URL url = new URL(req);

        log.info("calling:{}", url);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url.toString());
        try (CloseableHttpResponse response1 = client.execute(httpGet)) {
            final HttpEntity entity = response1.getEntity();
            if (entity != null) {
                File unsortedFile = new File(outputFile.getAbsolutePath() + ".unsorted");
                String header;
                long count = 0;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
                     BufferedWriter out = new BufferedWriter(new FileWriter(unsortedFile))) {
                    header = in.readLine();
                    log.info("writing response");
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        count++;
                        out.write(line + "\n");
                    }
                }
                log.info("records fetched:{}", count);
                final long countFromResults = countClient.getCountFromResults(dataType.name().toLowerCase(), query);
                if (count < countFromResults) {
                    throw new Exception("Fetched record count " + count + "is lower than index count " + countFromResults);
                }
                log.info("sorting to:{}", outputFile.getAbsolutePath());
                bigSortFile(unsortedFile, outputFile);
                addHeader(header, outputFile);
                log.info("deleting unsorted file:{}", unsortedFile.getAbsolutePath());
                Files.delete(unsortedFile.toPath());
            }
        }
        log.info("finished new {} snapshot IDs pull from ENA", dataType);
        return outputFile;
    }

    @SneakyThrows
    private void addHeader(String header, File outputFile) {
        File tempFile = new File(outputFile.getAbsolutePath() + ".temp");
        try (BufferedReader in = new BufferedReader(new FileReader(outputFile));
             BufferedWriter out = new BufferedWriter(new FileWriter(tempFile))) {
            out.write(header + "\n");
            String line = null;
            while ((line = in.readLine()) != null) {
                out.write(line + "\n");
            }
        }
        Files.delete(outputFile.toPath());
        Files.move(tempFile.toPath(), outputFile.toPath());
    }
}
