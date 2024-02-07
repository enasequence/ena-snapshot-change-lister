package uk.ac.ebi.ena.dcap.scl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.ena.dcap.scl.service.MainService;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainServiceTest {


    @SneakyThrows
    @Test
    public void testLivelist() {
        File output = new MainService().fetchSnapshotAndCompare("TLS_SET", "tls.tsv",
                ".", null, false);
        System.out.println(output.getAbsolutePath());
        List<String> lines = Files.lines(output.toPath()).collect(Collectors.toList());
        assertEquals("TAAT01000000\t2023-12-27", lines.get(lines.size() - 1));
        assertEquals("KAAA01000000\t2019-05-10", lines.get(1));
        assertEquals("accession\tlast_updated", lines.get(0));

        File nou = new File(output.getAbsolutePath().replace(".tsv", "_new-or-updated.tsv"));
        System.out.println(nou.getAbsolutePath());
        lines = Files.lines(nou.toPath()).collect(Collectors.toList());
        assertEquals("TAAT01000000", lines.get(lines.size() - 1));
        assertEquals("KAAA01000000", lines.get(0));
    }

    @SneakyThrows
    @Test
    public void testLivelistCoding() {
        File output = new MainService().fetchSnapshotAndCompare("CODING", "cds.tsv",
                ".", null, false);

    }
}
