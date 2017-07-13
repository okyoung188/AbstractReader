package com.processor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class Extracter extends Processor {
	
	/* *
	 * Default pattern type
	 */
	private String STREET_PATTERN = "STREET_PATTERN";
	private String MAIN_ST_PATTERN = "MAIN_ST_PATTERN";
	private String FROM_ST_PATTERN = "FROM_ST_PATTERN";
	private String TO_ST_PATTERN = "TO_ST_PATTERN";
	private String DATE_PATTERN = "DATE_PATTERN";
	private String WEEKDAY_AND_TIME_PATTERN = "WEEKDAY_AND_TIME_PATTERN";
	private String TIME_PATTERN = "TIME_PATTERN";
	
	
	/* ************************
	 * Street patterns
	 **************************/
	private Map<Pattern, String> streetPatternMap;
	private ArrayList<Pattern> mainStPatternArrayList;
	private ArrayList<Pattern> fromStPatternArrayList;
	private ArrayList<Pattern> toStPatternArrayList;
	
	
	/* ************************
	 * Time patterns
	 **************************/
	private ArrayList<Pattern> timePatternArrayList;
	private ArrayList<Pattern> datePatternArrayList;
	private ArrayList<Pattern> weekdayTimePatternArrayList;
	
	/**
	 * Extra pattern map for not pre-defined patterns
	 */
	private Map<String,List<String>> extraPatternMap;
	
	/**
	 * Extra pattern types list
	 */
	private List<String> extraPatternType;
	
	/**
	 * Constructor, initialize with the base reader's parameter tcSeparateSign
	 * @param tcSeparateSign
	 */
	public Extracter(String filePath,String tcSeparateSign) {
		setFilePath(filePath);
		this.tcSeparateSign = tcSeparateSign;
	}
	public Extracter() {
	    super();
	}


	
	
	/* **************************************************
	 * Getters and Setters for pattern string
	 ****************************************************/
	public String getSTREET_PATTERN() {
		return STREET_PATTERN;
	}
	public void setSTREET_PATTERN(String sTREET_PATTERN) {
		STREET_PATTERN = sTREET_PATTERN;
	}
	public String getMAIN_ST_PATTERN() {
		return MAIN_ST_PATTERN;
	}
	public void setMAIN_ST_PATTERN(String mAIN_ST_PATTERN) {
		MAIN_ST_PATTERN = mAIN_ST_PATTERN;
	}
	public String getFROM_ST_PATTERN() {
		return FROM_ST_PATTERN;
	}
	public void setFROM_ST_PATTERN(String fROM_ST_PATTERN) {
		FROM_ST_PATTERN = fROM_ST_PATTERN;
	}
	public String getTO_ST_PATTERN() {
		return TO_ST_PATTERN;
	}
	public void setTO_ST_PATTERN(String tO_ST_PATTERN) {
		TO_ST_PATTERN = tO_ST_PATTERN;
	}
	public String getDATE_PATTERN() {
		return DATE_PATTERN;
	}
	public void setDATE_PATTERN(String dATE_PATTERN) {
		DATE_PATTERN = dATE_PATTERN;
	}
	public String getWEEKDAY_AND_TIME_PATTERN() {
		return WEEKDAY_AND_TIME_PATTERN;
	}
	public void setWEEKDAY_AND_TIME_PATTERN(String wEEKDAY_AND_TIME_PATTERN) {
		WEEKDAY_AND_TIME_PATTERN = wEEKDAY_AND_TIME_PATTERN;
	}
	public String getTIME_PATTERN() {
		return TIME_PATTERN;
	}
	public void setTIME_PATTERN(String tIME_PATTERN) {
		TIME_PATTERN = tIME_PATTERN;
	}
	
	
	/* *
	 * Getters for pattern maps
	 */
	public Map<Pattern, String> getStreetPatternMap() {
		return streetPatternMap;
	}
	public ArrayList<Pattern> getMainStPatternArrayList() {
		return mainStPatternArrayList;
	}
	public ArrayList<Pattern> getFromStPatternArrayList() {
		return fromStPatternArrayList;
	}
	public ArrayList<Pattern> getToStPatternArrayList() {
		return toStPatternArrayList;
	}
	public ArrayList<Pattern> getTimePatternArrayList() {
		return timePatternArrayList;
	}
	public ArrayList<Pattern> getDatePatternArrayList() {
		return datePatternArrayList;
	}
	public ArrayList<Pattern> getWeekdayTimePatternArrayList() {
		return weekdayTimePatternArrayList;
	}
	public Map<String, List<String>> getExtraPatternMap() {
		return extraPatternMap;
	}
	public List<String> getExtraPatternType() {
		return extraPatternType;
	}
	
	/**
	 * Get pattern according to the type string
	 * @param type pattern type string
	 * @return pattern list
	 * @throws Exception
	 */
    public List<String> getPatternList(String type) throws Exception{
    	if (extraPatternType != null){
    		if (extraPatternType.contains(type)){
    			List<String> patternList = extraPatternMap.get(type);
    			return patternList;
    		} else {
    			throw new Exception("This pattern doesn't exist. Please check parameter and the pattern file.");
    		}
    	} else {
    		throw new Exception("No extra pattern. Please check parameter and the pattern file.");
    	}
    }
	
	
	/**
	 * Load patterns from the pattern file
	 */
	@Override
	public boolean load() throws Exception {
		if (getFilePath() != null){
			BufferedReader reader = null;
			String lineRead = null;
			String[] keyValue = null;
			Pattern pattern = null;
			streetPatternMap = new LinkedHashMap<Pattern, String>();
			mainStPatternArrayList = new ArrayList<Pattern>();
			fromStPatternArrayList = new ArrayList<Pattern>();
			toStPatternArrayList = new ArrayList<Pattern>();
			datePatternArrayList = new ArrayList<Pattern>();
			weekdayTimePatternArrayList = new ArrayList<Pattern>();
			timePatternArrayList = new ArrayList<Pattern>();
			LOGGER.info("Start to load patterns.");

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
							&& keyValue[0].contains(STREET_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						String groupNum = keyValue[0]
								.replaceAll(STREET_PATTERN, "");
						if (groupNum.equals("2") || groupNum.equals("3") || groupNum.equals("1")) {
							streetPatternMap.put(pattern, groupNum);
						}
						LOGGER.info("Add pattern to streetPatternMap:"
								+ pattern.toString());
					} else if (keyValue != null && keyValue.length > 1
							&& keyValue[0].contains(MAIN_ST_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						mainStPatternArrayList.add(pattern);
						LOGGER.info("Add pattern to mainStPatternArrayList:"
								+ pattern.toString());
					} else if (keyValue != null && keyValue.length > 1
							&& keyValue[0].contains(FROM_ST_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						fromStPatternArrayList.add(pattern);
						LOGGER.info("Add pattern to fromStPatternArrayList:"
								+ pattern.toString());
					} else if (keyValue != null && keyValue.length > 1
							&& keyValue[0].contains(TO_ST_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						toStPatternArrayList.add(pattern);
						LOGGER.info("Add pattern to toStPatternArrayList: "
								+ pattern.toString());
					} else if (keyValue != null && keyValue.length > 1
							&& keyValue[0].contains(DATE_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						datePatternArrayList.add(pattern);
						LOGGER.info("Add pattern to datePatternArrayList: "
								+ pattern.toString());
					} else if (keyValue != null && keyValue.length > 1
							&& keyValue[0].contains(WEEKDAY_AND_TIME_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						weekdayTimePatternArrayList.add(pattern);
						LOGGER.info("Add pattern to weekdayTimePatternArrayList: "
								+ pattern.toString());
					} else if (keyValue != null && keyValue.length > 1
							&& keyValue[0].contains(TIME_PATTERN)) {
						pattern = Pattern.compile(keyValue[1]);
						timePatternArrayList.add(pattern);
						LOGGER.info("Add pattern to timePatternArrayList: "
								+ pattern.toString());
					} else if(keyValue != null && keyValue.length > 1){
						pattern = Pattern.compile(keyValue[1]);
						if(extraPatternType == null){
							extraPatternType = new ArrayList<String>();
						}
						if(extraPatternMap == null){
							extraPatternMap = new LinkedHashMap<String,List<String>>();
      					}
						if(!extraPatternType.contains(keyValue[0])){
							extraPatternType.add(keyValue[0]);
							extraPatternMap.put(keyValue[0], new ArrayList<String>());
						}
						List<String> extra = extraPatternMap.get(keyValue[0]);
						extra.add(keyValue[1]);
						LOGGER.info("Add pattern to extraPatternArrayList: key[" + keyValue[0] + "], pattern[" + keyValue[1] + "]");
					}
				}
				LOGGER.info("Load patterns successfully!");
				return true;
			} catch (FileNotFoundException ex) {
				LOGGER.fatal("Patterns file:" + getFilePath()
						+ " does not exist, program will terminate now ("
						+ ex.getMessage() + ")");
			} catch (Exception ex) {
				LOGGER.fatal("Parse patterns error, program will terminate now ("
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
		} else {
			throw new Exception("Extracter's file path doesn't exist.");
		}
	}

}
