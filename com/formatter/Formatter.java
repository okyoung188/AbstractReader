package com.formatter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.reader.AbstractProcessor;


public class Formatter extends AbstractProcessor {

	private Map<String, String> locationFormatMap;
	private Map<String,Map<String,String>> extraFormatMap;
	private List<String> extraFormatType;
	
	private final String LOCATION_FORMAT = "LOCATION_FORMAT";

	/**
	 * Constructor, initialize with the base reader's parameter tcSeparateSign
	 * @param tcSeparateSign
	 */
	public Formatter(String filePath,String tcSeparateSign) {
		setFilePath(filePath);
		this.tcSeparateSign = tcSeparateSign;
	}
	public Formatter() {
		super();
	}


	@Override
	public boolean load() throws Exception {
		BufferedReader reader = null;
		locationFormatMap = new LinkedHashMap<String,String>();
		String lineRead = null;
		String[] keyValue = null;
		
		try {
			reader = new BufferedReader(new FileReader(getFilePath()));
			while ((lineRead = reader.readLine()) != null) {
				if (lineRead.length() < tcSeparateSign.length() + 2
						|| lineRead.startsWith("#")) {
					LOGGER.info("Empty or comment line, skipped:" + lineRead);
					continue;
				}
				keyValue = lineRead.split(tcSeparateSign);
				if (keyValue.length < 2) {
					LOGGER.info("Invalid line: " + lineRead);
					continue;
				}
				if (keyValue != null && keyValue.length > 1
						&& keyValue[0].startsWith(LOCATION_FORMAT)) {
					locationFormatMap.put(keyValue[0].replaceAll(LOCATION_FORMAT, ""), keyValue[1]);
					LOGGER.info("Load LOCATION FORMAT: " + keyValue[0].replaceAll(LOCATION_FORMAT, "") + " = "
							+ keyValue[1]);
				}
			}
			LOGGER.info("Load location format successfully!");
			return true;
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("Load locationFormat error, program will terminate now ("
					+ ex.getMessage() + ")");
		} catch (Exception ex) {
			LOGGER.fatal("Load locationFormat error, program will terminate now ("
					+ ex.getMessage() + ")");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				reader = null;
			}
		}
		return false;
	}

}
