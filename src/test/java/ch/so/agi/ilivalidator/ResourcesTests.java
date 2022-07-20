package ch.so.agi.ilivalidator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class ResourcesTests {
    static Logger logger = LoggerFactory.getLogger(ResourcesTests.class);

    @LocalServerPort
    protected String port;

    private String CONFIG_ENDPOINT = "/config/";

    @Test
    public void isIndexAvailable() throws Exception {
        String serverUrl = "http://localhost:"+port+"/";

        URL logFileUrl = new URL(serverUrl + "index.html");

        String fileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            fileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertTrue(fileContents.contains("ilivalidator web service • Kanton Solothurn"));
    }

    @Test
    public void isTomlAvailable() throws Exception {
        String serverUrl = "http://localhost:"+port+CONFIG_ENDPOINT+"toml/";

        URL logFileUrl = new URL(serverUrl + "so_nutzungsplanung_20171118.toml");

        String fileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            fileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertTrue(fileContents.contains("additionalModels=\"SO_Nutzungsplanung_20171118_Validierung_20211006;SO_FunctionsExt\""));
    }
    
    @Test
    public void isIliAvailable() throws Exception {
        String serverUrl = "http://localhost:"+port+CONFIG_ENDPOINT+"ili/";

        URL logFileUrl = new URL(serverUrl + "VSADSSMINI_2020_LV95_Validierung_IPW_20220624.ili");

        String fileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            fileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertTrue(fileContents.contains("CONTRACTED MODEL VSADSSMINI_2020_LV95_Validierung_IPW_20220624"));
    }

}
