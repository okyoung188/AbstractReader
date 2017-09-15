package com.trafficcast.reader.processor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




public class Formatter extends Processor {
	private final String LOCATION_FORMAT = "LOCATION_FORMAT";
	private final String TIME_FORMAT = "TIME_FORMAT";
	
	private Map<String, String> locationFormatMap;
	private Map<String, String> timeFormatMap;
	private Map<String,Map<String,String>> extraFormatMap;
	private List<String> extraFormatType;
	
	private String typeDelimiter = ":";


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
		timeFormatMap = new LinkedHashMap<String,String>();
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
				if (keyValue[0] != null && keyValue[1] != null&& keyValue[0].startsWith(LOCATION_FORMAT)) {
					locationFormatMap.put(keyValue[0].replaceAll(LOCATION_FORMAT, ""), keyValue[1]);
					LOGGER.info("Load LOCATION FORMAT: " + keyValue[0].replaceAll(LOCATION_FORMAT, "") + " = "
							+ keyValue[1]);
				} else if(keyValue[0] != null && keyValue[1] != null && keyValue[0].startsWith(TIME_FORMAT)){
					timeFormatMap.put(keyValue[0].replaceAll(TIME_FORMAT, ""), keyValue[1]);
					LOGGER.info("Load time format: " + keyValue[0].replaceAll(TIME_FORMAT, "") + " = "
							+ keyValue[1]);
				} else if(keyValue[0] != null && keyValue[1] != null){
					String[] typeAndPattern = keyValue[0].split(typeDelimiter);
					if(typeAndPattern != null && typeAndPattern.length == 2){
						String type = typeAndPattern[0];
						String pattern = typeAndPattern[1];
						if(type != null && !type.trim().equals("") && pattern != null && !pattern.equals("")){
							if(extraFormatType == null){
								extraFormatType = new ArrayList<String>();
							}
							if(!extraFormatType.contains(type)){
								extraFormatType.add(type);
							}
							Map<String,String> patternMap = extraFormatMap.get(type);
							if(patternMap == null){
								patternMap = new LinkedHashMap<String,String>();
								extraFormatMap.put(type, patternMap);
							}
							patternMap.put(pattern, keyValue[1]);
							LOGGER.info("Load "+type+" format: " + pattern + " = "
									+ keyValue[1]);			
						}	
					}				
				}
			}
			LOGGER.info("Load formatter successfully!");
			return true;
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("Load formatter error, program will terminate now ("
					+ ex.getMessage() + ")");
		} catch (Exception ex) {
			LOGGER.fatal("Load formatter error, program will terminate now ("
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
	
	public String formatLocation(String location){
		return null;
	}
	
	public String formatTime(String timeInfo){
		return null;
	}

}
