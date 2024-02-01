package uk.ac.ebi.ena.dcap.scl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.ena.dcap.scl.service.MainService;

@SpringBootTest(args = {"--dataType=TLS_SET", "--previousSnapshot=tls.tsv", "--outputLocation=. "})
public class MainServiceTest {

    @Autowired
    MainService mainRunner;

    @Test
    public void testLivelist() {
        mainRunner.fetchSnapshotAndCompare("TLS_SET", "tls.tsv",
                ".", null, false, "embl", true, false);
    }
}
