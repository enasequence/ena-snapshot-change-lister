package uk.ac.ebi.ena.dcap.scl;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.ena.dcap.scl.service.SnapshotClient;

import java.io.File;

public class SnapshotClientTest {

    @Test
    public void testSort() {
        SnapshotClient.bigSortFile(new File("C:\\projects\\ena-snapshot-tool\\build\\libs\\tls_set_20240201.tsv.unsorted"),
                new File("C:\\projects\\ena-snapshot-tool\\build\\libs\\tls_set_20240201.tsv"));
    }
}
