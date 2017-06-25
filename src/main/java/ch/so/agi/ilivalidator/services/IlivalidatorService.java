package ch.so.agi.ilivalidator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.itf.ItfReader;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.StartBasketEvent;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.interlis2.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class IlivalidatorService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ResourceLoader resourceLoader;

	// JAVADOC
	public String validate(String doConfigFile, String fileName) throws Exception {	
		String baseFileName = FilenameUtils.getFullPath(fileName) 
				+ FilenameUtils.getBaseName(fileName);
		
		if (doConfigFile != null) {
			String modelName = getModelNameFromTransferFile(fileName);
			log.info("model name: " + modelName);
			
			// This is the java pure way to load resources in a jar.
			//InputStream is = getClass().getResourceAsStream("dm01avch24lv95d.toml");

			// Spring offers a more elegant (?) way.
			Resource resource = resourceLoader.getResource("classpath:toml/"+modelName.toLowerCase()+".toml");
			InputStream is = resource.getInputStream();
						
			File configFile = new File(FilenameUtils.getFullPath(fileName), modelName.toLowerCase()+".toml");
			Files.copy(is, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(is);

			//settings.setValue(Validator.SETTING_CONFIGFILE, args[argi]);

		}
		
		Settings settings = new Settings();
		settings.setValue(Validator.SETTING_ILIDIRS, Validator.SETTING_DEFAULT_ILIDIRS);
		settings.setValue(Validator.SETTING_LOGFILE, baseFileName + ".log");

		boolean res = Validator.runValidation(fileName, settings);
		
		return baseFileName + ".log";
	}
	
	// JAVADOC
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
					throw new IoxException("could not close interlise transfer file: " + new File(transferFileName).getName());
				}
				ioxReader =  null;
			}
		}
		return model;
	}
}
