package org.apache.phoenix.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Basic class to load Maven generated properties file into the application's memory. 
 * Provides simple read/write functionality as well.
 * @author genesismedia1
 */
public class ConfigReader {

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class.getName());
	
	private static final String PHOENIX_PROPERTIES_FILE = "gm.phoenix.properties";

	private static Properties properties = new Properties();

	/**
	 * Default constructor.
	 * 
	 * Creates ConfigReader instance and call function to load properties into application memory.
	 */
	public ConfigReader() {
		LOGGER.info("Constructor: Load GM Phoenix properties file...");
		this.loadPropertyFile(PHOENIX_PROPERTIES_FILE);
	}

	@PostConstruct
	/**
	 * Initialize loading of properties file.
	 */
	public void initialize() {
		LOGGER.info("Initialize...Load Phoenix properties file...");
		this.loadPropertyFile(PHOENIX_PROPERTIES_FILE);
	}

	/**
	 * Takes properties file reference and uses <code>ClassLoader</code> to obtain properties file as resource stream.
	 * @param clazz
	 * @param propertiesFile
	 */
	private void loadPropertyFile(Class clazz, String propertiesFile) {
		try {
			if (new File(propertiesFile).exists()) {
				properties.load(new FileInputStream(propertiesFile));
			} else if (new File("src/main/resources/" + propertiesFile).exists()) {
				properties.load(new FileInputStream("src/main/resources/"+ propertiesFile));
			}else {
				ClassLoader classLoader = ConfigReader.class.getClassLoader();
				properties.load(classLoader.getResourceAsStream(propertiesFile));
				LOGGER.info("Load Properties from classpath... " + propertiesFile);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		}
		LOGGER.debug(properties.toString());
	}
	
	/**
	 * Get value based on key.
	 * 
	 * @param key
	 * @return String
	 */
	public String getValue(String key) {
		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		}
		return null;
	}
	
	/**
	 * Add new key-value pair.
	 * 
	 * @param key
	 * @return String
	 */
	public void put(String key, String value) {
		properties.put(key, value);
	}

	/**
	 * Get key-value entries from  properties object.
	 * @return Set<Entry<Object, Object>>
	 */
	public Set<Entry<Object, Object>> getEntries() {
		return properties.entrySet();
	}

	/**
	 * Load properties.
	 * @param propertiesFile
	 */
	public void loadPropertyFile(String propertiesFile) {
		loadPropertyFile(this.getClass(), propertiesFile);
	}
}