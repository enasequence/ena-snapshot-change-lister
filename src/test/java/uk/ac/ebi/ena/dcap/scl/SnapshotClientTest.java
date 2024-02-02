package uk.ac.ebi.ena.dcap.scl;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.ena.dcap.scl.service.SnapshotClient;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnapshotClientTest {

    @lombok.SneakyThrows
    @Test
    public void testSort() {
        File output = new File("src/test/resources/tls_set_20240201.tsv");
        SnapshotClient.bigSortFile(new File("src/test/resources/tls_set_20240201.tsv.unsorted"),
                output);
        final List<String> lines = Files.lines(output.toPath()).collect(Collectors.toList());
        assertEquals("TAAT01000000\t2023-12-27", lines.get(lines.size() - 1));
        assertEquals("KAAA01000000\t2019-05-10", lines.get(0));
    }
}
