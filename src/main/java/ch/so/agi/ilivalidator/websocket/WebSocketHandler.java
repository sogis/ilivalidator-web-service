package ch.so.agi.ilivalidator.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import ch.so.agi.ilivalidator.service.IlivalidatorService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketHandler extends AbstractWebSocketHandler {    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    private static String FOLDER_PREFIX = "ilivalidator_";
    private static String LOG_ENDPOINT = "log";
    private static String HEX_COLOR_SUCCESS = "#58D68D";
    private static String HEX_COLOR_FAIL = "#EC7063";
    
    @Value("${server.port}")
    protected String serverPort;
    
    @Value("${app.s3Bucket}")
    private String s3Bucket;

    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.folderPrefix}")
    private String folderPrefix;

    @Autowired
    IlivalidatorService ilivalidator;
    
    // Dient dem Verwalten der Websocket-Session und
    // dem Zugriff der zu prüfenden Datein über verschiedene Methoden
    // hinweg. Die Kombination Session-Id und Datei ist eindeutig,
    // während der Prüfung keine weitere Datei hochgeladen werden.
    HashMap<String, File> sessionFileMap = new HashMap<String, File>();
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {        
        File file = sessionFileMap.get(session.getId());
        String filename = message.getPayload();
        
        // "file" hat nicht mehr der Original-Dateinamen, weil der Payload der
        // Binary-Message, diesen nicht mitschickt.
        Path namedFile = Paths.get(file.getParent(), filename);
        Files.copy(file.toPath(), namedFile, StandardCopyOption.REPLACE_EXISTING);
        
        session.sendMessage(new TextMessage("Received: " + filename));
        
        String logFilename = namedFile.toFile().getAbsolutePath() + ".log";
        log.info(logFilename);
        
        // Zurzeit wird diese Option im GUI nicht exponiert.
        String configFile = "on";

        // Zurzeit wird diese Option im GUI nicht exponiert.
        String allObjectsAccessible = "true";

        boolean valid;
        try {
            // Run the validation.
            session.sendMessage(new TextMessage("Validating..."));
            valid = ilivalidator.validate(allObjectsAccessible, configFile, namedFile.toFile().getAbsolutePath(), logFilename);
                        
            String logKey = new File(new File(logFilename).getParent()).getName() + "/" + new File(logFilename).getName();
            String xtfLogKey = logKey + ".xtf";
            
            String resultText = "<span style='background-color:"+HEX_COLOR_SUCCESS+";'>...validation done:</span>";
            if (!valid) {
                resultText = "<span style='background-color:"+HEX_COLOR_FAIL+"'>...validation failed:</span>";
            }
            
            String scheme = session.getUri().getScheme().length() == 3 ? "https" : "http";
            String host = session.getUri().getHost();
            String port = session.getUri().getPort() != 80 ? ":"+String.valueOf(session.getUri().getPort()) : "";
            String path = session.getUri().getPath().replaceFirst("/socket", "");
            String baseUrl = scheme+"://"+host+port+"/"+path+"/logs/";
            
            TextMessage resultMessage = new TextMessage(resultText 
                    + " <a href='"+fixUrl(baseUrl+logKey)+"' target='_blank'>Download log file</a> / "
                    + " <a href='"+fixUrl(baseUrl+xtfLogKey)+"' target='_blank'>Download XTF log file.</a><br/><br/>");
            session.sendMessage(resultMessage);
            
        } catch (Exception e) {
            e.printStackTrace();            
            log.error(e.getMessage());
            
            TextMessage errorMessage = new TextMessage("An error occured while validating the data:<br>" + e.getMessage());
            session.sendMessage(errorMessage);
            
            return;
        } finally {
            // Die Websocket-Session und damit das dazugehörige Transferfile aus
            // der Websocket-Map löschen. 
            sessionFileMap.remove(session.getId());
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        Path tmpDirectory = Files.createTempDirectory(Paths.get(workDirectory), folderPrefix);
        
        // ilivalidator muss wissen, ob es sich um eine ili1- oder ili2-Datei handelt.
        // Der Namen muss jedoch separat mitgeschickt werden. Gespeichert wird die Datei mit einem
        // generischen Namen und anschliessend umbenannt.
        Path uploadFilePath = Paths.get(tmpDirectory.toString(), "data.file"); 
        log.info(uploadFilePath.toAbsolutePath().toString());
                
        FileChannel fc = new FileOutputStream(uploadFilePath.toFile().getAbsoluteFile(), false).getChannel();
        fc.write(message.getPayload());
        fc.close();

        // Die Datei wird in eine Map "kopiert", damit man via
        // Websocket-Session-Id Zugriff hat und in einer anderen Methode (wenn man alle 
        // benötigten Infos hat) die Prüfung durchführen kann.
        sessionFileMap.put(session.getId(), uploadFilePath.toFile());
    }
        
    /*
     * - It finds multiple slashes in url preserving ones after protocol regardless of it.
     * - ... 
     */
    private static String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
  }
}