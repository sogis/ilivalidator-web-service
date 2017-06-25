package ch.so.agi.ilivalidator.services;

import org.springframework.stereotype.Service;

import ch.ehi.basics.settings.Settings;

import org.apache.commons.io.FilenameUtils;
import org.interlis2.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class IlivalidatorService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public String validate(String fileName) {	
		String baseFileName = FilenameUtils.getFullPath(fileName) 
				+ FilenameUtils.getBaseName(fileName);

		Settings settings = new Settings();
		settings.setValue(Validator.SETTING_ILIDIRS, Validator.SETTING_DEFAULT_ILIDIRS);
		settings.setValue(Validator.SETTING_LOGFILE, baseFileName + ".log");

		Validator.runValidation(fileName, settings);

		return baseFileName + ".log";
	}
}
