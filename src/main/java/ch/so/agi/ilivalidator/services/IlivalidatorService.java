package ch.so.agi.ilivalidator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.itf.ItfReader;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.StartBasketEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.interlis2.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring service class for INTERLIS transfer file validation.
 *
 * @author Stefan Ziegler
 * @since 2017-06-25
 */
@Service
public class IlivalidatorService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * This method validates an INTERLIS transfer file with
     * <a href="https://github.com/claeis/ilivalidator">ilivalidator library</a>.
     * 
     * @param doConfigFile
     *            Use ilivalidator configuration file for tailoring the validation.
     * @param fileName
     *            Name of INTERLIS transfer file.
     * @throws IoxException
     *             if an error occurred when trying to figure out model name.
     * @throws IOException
     *             if config file cannot be read or copied to file system.
     * @return String Returns the log file of the validation.
     */
    public synchronized boolean validate(String allObjectsAccessible, String doConfigFile, String inputFileName, String logFileName)
            throws IoxException, IOException {
        // Set some timeouts for fetching models. Some repos have problems and/or some
        // urls make problems with our waf.
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, Validator.SETTING_DEFAULT_ILIDIRS + "http://interlis-repo.dev.so.ch;");
        settings.setValue(Validator.SETTING_LOGFILE, logFileName);
                
        if (allObjectsAccessible != null) {
            settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, Validator.TRUE);
        }
        
        // Copy the ilivalidator custom functions jar files into temporary directory.
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:libs-ext/*.jar");
            log.info("Found " + String.valueOf(resources.length) + " custom function jar files.");
            for (Resource resource : resources) {
                InputStream is = resource.getInputStream();
                File jarFile = new File(FilenameUtils.getFullPath(inputFileName), resource.getFilename());
                log.info(jarFile.getAbsolutePath());
                Files.copy(is, jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(is);
            }
            EhiLogger.getInstance().setTraceFilter(false);
            settings.setValue(Validator.SETTING_PLUGINFOLDER, new File(FilenameUtils.getFullPath(inputFileName)).getAbsolutePath());
            log.info("Plugin folder: " + settings.getValue(Validator.SETTING_PLUGINFOLDER));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            log.error("Error while copying the ilivalidator custom functions jar files.");
        } 

        // Additional models, e.g. validation models (when they are not available elsewhere).
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:ili/*.ili");
            log.info("Found " + String.valueOf(resources.length) + " local models.");
            for (Resource resource : resources) {
                InputStream is = resource.getInputStream();
                File iliFile = new File(FilenameUtils.getFullPath(inputFileName), resource.getFilename());
                log.info(iliFile.getAbsolutePath());
                Files.copy(is, iliFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(is);
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            log.error("Error while copying the local INTERLIS model files. Continue with validation process.");
        }

        // Copy the configuration file that belongs to the INTERLIS model
        // file into the the transfer file folder.
        if (doConfigFile != null) {
            String modelName = getModelNameFromTransferFile(inputFileName);
            log.info("model name: " + modelName);

            // This is the java pure way to load resources in a jar.
            // InputStream is = getClass().getResourceAsStream("dm01avch24lv95d.toml");

            // Spring offers a more elegant (?) way.
            try {
                Resource resource = resourceLoader.getResource("classpath:toml/" + modelName.toLowerCase() + ".toml");
                InputStream is = resource.getInputStream();

                File configFile = new File(FilenameUtils.getFullPath(inputFileName), modelName.toLowerCase() + ".toml");
                Files.copy(is, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(is);

                settings.setValue(Validator.SETTING_CONFIGFILE, configFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                log.info(e.getMessage());
                log.info("Configuration file " + modelName.toLowerCase()
                        + ".toml not available. Continue validation without configuration file.");
            }
        }

        boolean valid = Validator.runValidation(inputFileName, settings);

        return valid;
    }

    /**
     * Figure out INTERLIS model name from INTERLIS transfer file. Works with ili1
     * and ili2.
     */
    private String getModelNameFromTransferFile(String transferFileName) throws IoxException {
        String model = null;
        String ext = FilenameUtils.getExtension(transferFileName);
        IoxReader ioxReader = null;

        try {
            File transferFile = new File(transferFileName);

            if (ext.equalsIgnoreCase("itf")) {
                ioxReader = new ItfReader(transferFile);
            } else {
                ioxReader = new XtfReader(transferFile);
            }

            IoxEvent event;
            StartBasketEvent be = null;
            do {
                event = ioxReader.read();
                if (event instanceof StartBasketEvent) {
                    be = (StartBasketEvent) event;
                    break;
                }
            } while (!(event instanceof EndTransferEvent));

            ioxReader.close();
            ioxReader = null;

            if (be == null) {
                throw new IllegalArgumentException("no baskets in transfer-file");
            }

            String namev[] = be.getType().split("\\.");
            model = namev[0];

        } catch (IoxException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new IoxException("could not parse file: " + new File(transferFileName).getName());
        } finally {
            if (ioxReader != null) {
                try {
                    ioxReader.close();
                } catch (IoxException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                    throw new IoxException(
                            "could not close interlise transfer file: " + new File(transferFileName).getName());
                }
                ioxReader = null;
            }
        }
        return model;
    }
}
