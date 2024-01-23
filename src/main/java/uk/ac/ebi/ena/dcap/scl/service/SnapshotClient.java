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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;
import uk.ac.ebi.ena.dcap.scl.model.DataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

@Component
@Slf4j
public class SnapshotClient {

    static final String PORTAL_API_URL = "https://www.ebi.ac.uk/ena/portal/api/search?result=%s&fields=accession," +
            "last_updated";

    static final String LIVELST_URL = "https://www.ebi.ac.uk/ena/browser/api/livelist/%s?fields=accession," +
            "last_updated";

    @SneakyThrows
    public File getLatestSnapshot(DataType dataType, File outputFile, String query) {
        String req;
        if (StringUtils.isNotBlank(query)) {
            req = String.format(PORTAL_API_URL, dataType.name().toLowerCase());
            req += "&query=" + query;

        } else {
            req = String.format(LIVELST_URL, dataType.name().toLowerCase());
        }
        URL url = new URL(req);

        log.info("calling:{}", url);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url.toString());
        try (CloseableHttpResponse response1 = client.execute(httpGet)) {
            final HttpEntity entity = response1.getEntity();
            if (entity != null) {
                try (InputStream in = entity.getContent();
                     OutputStream out = new FileOutputStream(outputFile)) {
                    log.info("writing response");
                    IOUtils.copyLarge(in, out);
                }
            }
        }
        log.info("finished new snapshot pull from ENA");
        return outputFile;
    }
}
