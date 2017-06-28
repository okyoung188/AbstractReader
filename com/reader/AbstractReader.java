package com.reader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.trafficcast.base.inccon.IncConRecord;


public class AbstractReader {
	// Current version of this class.
	public static final double VERSION = 1.0;

	// int value 10 represents the con reader
	private static final int READER_TYPE = 10;
	
	// log4j instance
	private final Logger LOGGER = Logger.getLogger(this.getClass());

	/**
	 * Reader ID
	 */
	private final String READER_ID = this.getClass().getName();
	
	/**
	 * Property file location
	 */
	private String PROPERTY_FILE;
	
	/**
	 * Format file location
	 */
	private String FORMAT_FILE;
	
	/**
	 * Pattern file location
	 */
	private String PATTERN_FILE;
	
	/**
	 * StreetAlias file location
	 */
	private String STREET_ALIAS_FILE;
	
	/**
	 * EventType key word file location
	 */
	private String EVENTTYPE_KEYWORD_FILE;
	
	/**
	 * City boundary map file location
	 */
	private String CITY_BOUNDARY_MAPPING_FILE;
	
	/**
	 * Error record file location
	 */
	private String ERROR_RECORDS_FILE;
	
	
	/* **************************
	 * 
	 * 
	 * Property file related parameters
	 * 
	 * 
	 * 
	 *******************************/
	private final String PROP_KEY_CONNECT_TIME_OUT = "CONNECT_TIME_OUT";
	private final String PROP_KEY_SLEEP_TIME_OUT = "SLEEP_TIME_OUT";
	private final String PROP_KEY_RETRY_TIME_OUT = "RETRY_TIME_OUT";
	private final String PROP_KEY_TC_SEPARATE_SIGN = "TC_SEPARATE_SIGN";
	private final String PROP_KEY_REVERSE_GEOCODING_FLAG = "REVERSE_GEOCODING_FLAG";
	private final String PROP_KEY_FILTER_KEYWORD = "FILTER_KEY_WORD";
	
	
	private final String PROP_KEY_DATA_URL_CONSTRUCTION = "DATA_URL_CONSTRUCTION";
	private final String PROP_KEY_DATA_URL_FORSAVE = "DATA_URL_FORSAVE";
	private final String PROP_KEY_CITY = "CITY";

	// sleep time, set default to 5 min, will load from property file
	private int loopSleepTime = 5 * 60 * 1000;

	// Retry wait time, set default to 2 minutes, will load from property file
	private int retryWaitTime = 2 * 60 * 1000;

	// Connection time, set default to 2 minutes, will load from property file
	private int connectOutTime = 2 * 60 * 1000;
	
	// This value is added to the wait time each time an exception is caught in run()
	private final int SEED = 60000;

	// TrafficCast Separate Sign
	private String tcSeparateSign = "~TrafficCastSeparateSign~";
	
	// Reverse geocoding, default true
	boolean REVERSE_GEOCODING_FLAG = true;

	// Reverse geocoding value, default -2
	private final int REVERSE_GEOCODING_VALUE = -2;

	// HashSet to store citys
	private HashSet<String> citySet;
	
	// county city map
	private Map<String,String> countyCityMap;
	
	// List to store filter key word
	private List<String> filterKWordList;
	
	// State code
	private String STATE;

	// City
	private String CITY;	

	// Address to get json
	private String[] dataURLs;
	private String dataUrlForSave;



	
	
	
	

	
	
	


	/* **************************
	 * 
	 * 
	 * Pattern file related parameters
	 * 
	 * 
	 * 
	 *******************************/
	// The Pattern to parse MainSt, CrossFrom, CrossTo
	private final String MAIN_ST_PATTERN = "MAIN_ST_PATTERN";
	private final String FROM_ST_PATTERN = "FROM_ST_PATTERN";
	private final String TO_ST_PATTERN = "TO_ST_PATTERN";
	
	private final String STREET_PATTERN ="" ;
	private final String SPLIT_PATTERN ="" ;
	private final String DATE_PATTERN ="" ;
	private final String WEEKDAY_AND_TIME_PATTERN ="" ;
	private final String TIME_PATTERN ="" ;
	
	// Arraylist to store patterns
	private ArrayList<Pattern> mainStPatternArrayList;
	private ArrayList<Pattern> fromStPatternArrayList;
	private ArrayList<Pattern> toStPatternArrayList;
	
	private LinkedHashMap<Pattern, String> streetPatternMap;
	private ArrayList<Pattern> datePatternArrayList;
	private ArrayList<Pattern> weekdayTimePatternArrayList;
	private ArrayList<Pattern> timePatternArrayList;
	private ArrayList<Pattern> splitPatternArrayList;
	
	
	
	
	/* ****************************************
	 * 
	 * Street alias file related parameters * 
	 * 
	 * 
	 *****************************************/
	/**
	 * Street alias map
	 */
	private LinkedHashMap<String, String> streetAliasMap; 

	
	
	
	/* ****************************************
	 * 
	 * Event type key word file related parameters * 
	 * 
	 * 
	 *****************************************/
	/**
	 * Event key word map
	 */
	private LinkedHashMap<String, String> eventKWordMap;
	
	
	
	

	/* ****************************************
	 * 
	 * City boundary file related parameters * 
	 * 
	 * 
	 *****************************************/
	/**
	 * County city boundary map
	 */
	private LinkedHashMap<String, String[]> cityBoundaryMap;
	
	
	
	
	
	
	
	
	
	
	
	
	
		
	/* ****************************************
	 * 
	 * Reader related parameters * 
	 * 
	 * 
	 *****************************************/
	// ArrayList to store record
	private ArrayList<IncConRecord> con_list = null;
	private ArrayList<IncConRecord> inc_list = null;
	private ArrayList<IncConRecord> tta_list = null;	
	
	// Default Time Zone
	private TimeZone defaultTimeZone = null;

	// SimpleDateFormat to parse time
	private static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy", Locale.US);
	
	// HashMap to store error info
	private HashMap<String, String> unknownErrorMap;
	
	//init when the reader constructed
	{
		setDefaultFilePath();//init its default file path
		
	}



	/**
	 * Set the file path by default
	 * @return
	 */
	public boolean setDefaultFilePath() {
		String className = this.getClass().getName();
		String propDir = "prop/";		
		this.PROPERTY_FILE = propDir + className + ".properties";
		this.FORMAT_FILE = propDir + className + "_Format.txt";
		this.PATTERN_FILE = propDir + className + "_Pattern.txt";
		this.STREET_ALIAS_FILE = propDir + className + "_StreetAlias.txt";
		this.EVENTTYPE_KEYWORD_FILE = propDir + className + "_ETypeKWord.txt";	
		this.ERROR_RECORDS_FILE = propDir + className + "_Error.txt";
		return true;
	}
	
	public boolean setFilePath(String fileType){
		String className = this.getClass().getName();
		String indexDir = "index/";
		this.CITY_BOUNDARY_MAPPING_FILE = indexDir + className + "_county_city.csv";
		return true;
	}

	/**
	 * Get the loop sleep time
	 * @return loop sleep time
	 */
	public int getLoopSleepTime() {
		return loopSleepTime;
	}

	/**
	 * Set the loop sleep time
	 * @param loopSleepTime
	 */
	public void setLoopSleepTime(int loopSleepTime) {
		this.loopSleepTime = loopSleepTime;
	}

	/**
	 * Get the retry wait time
	 * @return the retry wait time
	 */
	public int getRetryWaitTime() {
		return retryWaitTime;
	}

	/**
	 * Set the retry wait time
	 * @param retryWaitTime
	 */
	public void setRetryWaitTime(int retryWaitTime) {
		this.retryWaitTime = retryWaitTime;
	}

	/**
	 * Get the connect timeout
	 * @return the connect timeout
	 */
	public int getConnectOutTime() {
		return connectOutTime;
	}

	/**
	 * Set the connect timeout
	 * @param connectOutTime
	 */
	public void setConnectOutTime(int connectOutTime) {
		this.connectOutTime = connectOutTime;
	}

	public boolean isReverseGeocoding() {
		return REVERSE_GEOCODING_FLAG;
	}

	public void setReverseGeocoding(boolean isReverseGeocoding) {
		this.REVERSE_GEOCODING_FLAG = isReverseGeocoding;
	}
	
	/**
	 * Load the properties file
	 * 
	 * @return true if successfully, otherwise false
	 */
	private boolean loadProperties() {
		if (PROPERTY_FILE == null || "".equals(PROPERTY_FILE.trim())) {
			LOGGER.debug("Property file isn't properly configured: " + PROPERTY_FILE);
			return false;
		}
		FileInputStream is = null;
		String propValue = null;
		Properties prop = new Properties();
		LOGGER.info("Start to load properties");
		try {
			is = new FileInputStream(PROPERTY_FILE);
//			prop.load(is);
			prop.loadFromXML(is);
			Set<String> keySet = prop.stringPropertyNames();

			// Get the loop sleep time
			propValue = prop.getProperty(PROP_KEY_SLEEP_TIME_OUT);
			if (propValue != null && propValue.trim().length() > 0) {
				loopSleepTime = Integer.parseInt(propValue.trim());
				LOGGER.info("Get the loop_sleep_time is :  " + loopSleepTime);
			} else {
				LOGGER.info("Get the loop_sleep_time failed!");
				return false;
			}

			// Get the retry wait time
			propValue = prop.getProperty(PROP_KEY_RETRY_TIME_OUT);
			if (propValue != null && propValue.trim().length() > 0) {
				retryWaitTime = Integer.parseInt(propValue.trim());
				LOGGER.info("Get the retry_wait_time is : " + retryWaitTime);
			} else {
				LOGGER.info("Get the retry_wait_time failed!");
				return false;
			}

			// Get connect out time
			propValue = prop.getProperty(PROP_KEY_CONNECT_TIME_OUT);
			if (propValue != null && propValue.trim().length() > 0) {
				connectOutTime = Integer.parseInt(propValue.trim());
				LOGGER.info("Get the connect out time is : " + connectOutTime);
			} else {
				LOGGER.info("Get the connect out time failed!");
				return false;
			}

			// Get the URL of location
			propValue = prop.getProperty(PROP_KEY_DATA_URL_CONSTRUCTION);
			if (propValue != null && propValue.trim().length() > 0) {
				dataURLs = propValue.trim().split(tcSeparateSign);
				LOGGER.info("Get the URL of location is:  " + propValue);
			} else {
				LOGGER.info("Get the URL of location failed!");
				return false;
			}

			// Get the URL for save
			propValue = prop.getProperty(PROP_KEY_DATA_URL_FORSAVE);
			if (propValue != null && propValue.trim().length() > 0) {
				dataUrlForSave = propValue.trim();
				LOGGER.info("Get the URL for save is:  " + dataUrlForSave);
			} else {
				LOGGER.info("Get the URL for save failed!");
				return false;
			}

			// Get the separate sign
			propValue = prop.getProperty(PROP_KEY_TC_SEPARATE_SIGN);
			if (propValue != null && propValue.trim().length() >= 5) {
				tcSeparateSign = propValue.trim();
				LOGGER.info("tcSeparateSign: " + tcSeparateSign);
			} else {
				LOGGER.info("Get tcSeparateSign failed!");
				return false;
			}

			// Get the reverse geocoding flag
			propValue = prop.getProperty(PROP_KEY_REVERSE_GEOCODING_FLAG);
			if (propValue != null && propValue.trim().length() > 0) {
				REVERSE_GEOCODING_FLAG = Boolean.parseBoolean(propValue.trim());
				LOGGER.info("ReverseGeocoding flag: " + REVERSE_GEOCODING_FLAG);
			} else {
				LOGGER.info("Get reverseGeocoding flag failed!");
				return false;
			}
			
			// Get city and county mapping
			propValue = prop.getProperty(PROP_KEY_CITY);
			if(propValue != null && !propValue.trim().equals("")){
				String[] cityCodes= null;
				propValue = propValue.trim().toUpperCase(); 
				cityCodes = propValue.split(",");
				String city = null;
				String[] strArray = null;
				String county = null;
				int cityCount = cityCodes.length;
				if(cityCount < 1) {
					LOGGER.fatal("There is no city code in property file,program can not start");
					return false;
				}
				countyCityMap = new LinkedHashMap<String, String>();
				for(int i = 0;i < cityCount; i++){
					city = cityCodes[i].trim();
					propValue = prop.getProperty(city + "_COUNTY");
					if(propValue != null && !"".equals(propValue.trim())){
						propValue = propValue.trim().toUpperCase();
						strArray = propValue.split(",");
						LOGGER.debug(city + ", Counties: " + propValue);
						//Add county to ArrayList
						for(int j = 0;j < strArray.length; j ++) {
							county = strArray[j].trim().toUpperCase();
							if (!countyCityMap.containsKey(county)) {
								countyCityMap.put(county, city);
							}
						}
					}
				}
			}
			
			LOGGER.info("Load properties and init successfully!");
			return true;
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			LOGGER.fatal("Properties file does not exist, program will terminate now ("
					+ ex.getMessage() + ")");
		} catch (Exception ex) {
			LOGGER.fatal("Load/Parse properties error, program will terminate now ("
					+ ex.getMessage() + ")");
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				LOGGER.warn("Error while closing FileInputStream  "
						+ e.getMessage());
			}
			is = null;
		}
		return false;
	}
	
	/**
	 * Load the patterns file
	 * 
	 * @return true if successfully, otherwise false
	 */
	private boolean loadPatterns() {
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
		splitPatternArrayList = new ArrayList<Pattern>();
		LOGGER.info("Start to load patterns.");

		try {
			reader = new BufferedReader(new FileReader(PATTERN_FILE));
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
						&& keyValue[0].contains(SPLIT_PATTERN)) {
					pattern = Pattern.compile(keyValue[1]);
					splitPatternArrayList.add(pattern);
					LOGGER.info("Add pattern to splitPatternArrayList: "
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
				} else {
					LOGGER.info("Unknown pattern: " + keyValue[1]);
				}
			}

			LOGGER.info("Load patterns successfully!");
			return true;
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("Patterns file:" + PATTERN_FILE
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
	}
	
	/**
	 * Load street alias
	 * 
	 * @return true if successfully, otherwise false
	 */
	private boolean loadStreatAlias() {
		if (STREET_ALIAS_FILE == null || "".equals(STREET_ALIAS_FILE.trim())) {
			LOGGER.debug("StreetAlias file isn't properly configured: " + STREET_ALIAS_FILE);
			return false;
		}
		BufferedReader reader = null;
		streetAliasMap = new LinkedHashMap<String, String>();
		String lineRead = null;
		String[] keyValue = null;
		LOGGER.info("Start to load street alias");
		try {
			reader = new BufferedReader(new FileReader(STREET_ALIAS_FILE));
			while ((lineRead = reader.readLine()) != null) {
				if (lineRead.length() < tcSeparateSign.length() + 2
						|| lineRead.startsWith("#")) {
					LOGGER.info("Empty or comment line, skipped:" + lineRead);
					continue;
				}
				keyValue = lineRead.split(tcSeparateSign);
				if (keyValue != null && keyValue.length > 1) {
					streetAliasMap.put(keyValue[0], keyValue[1]);
					LOGGER.info("STREET ALIAS: " + keyValue[0] + " = "
							+ keyValue[1]);
				}
			}
			LOGGER.info("Load street alias successfully!");
			return true;
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("StreetAlias file:" + STREET_ALIAS_FILE
					+ " does not exist, program will terminate now ("
					+ ex.getMessage() + ")");
		} catch (Exception ex) {
			LOGGER.fatal("Load street alias error, program will terminate now ("
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
	
	/**
	 * Load eventType keywords from specified file.
	 * 
	 * @return true if load successfully, otherwise false.
	 */
	private boolean loadEventTypeKeywords() {
		BufferedReader buffReader = null;
		String lineRead = null;
		String key = null;
		String value = null;
		eventKWordMap = new LinkedHashMap<String, String>();
		LOGGER.info("Start to load event type keywords!");
		try {
			buffReader = new BufferedReader(new FileReader(
					EVENTTYPE_KEYWORD_FILE));
			while ((lineRead = buffReader.readLine()) != null) {
				lineRead = lineRead.toUpperCase();// dont't trim, some have
													// white spaces
				if (lineRead.startsWith("#") || lineRead.equals("")
						|| !lineRead.matches("(.+),(3|2|1)")) {
					LOGGER.info("Empty or comment line, skipped:" + lineRead);
					continue;
				}
				key = lineRead.replaceAll("(.+),(3|2|1)", "$1");
				value = lineRead.replaceAll("(.+),(3|2|1)", "$2");
				if (key != null && !key.equals("") && value != null
						&& value.matches("[321]")) {
					LOGGER.info("EVENT KEY WORD: " + key + " = " + value);
					eventKWordMap.put(key, value);
					key = null;
					value = null;
				}
			} // End of while

			LOGGER.info("Load event key word successfully");
			return true;
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("event key word file:" + EVENTTYPE_KEYWORD_FILE
					+ " does not exist, program will terminate now ("
					+ ex.getMessage() + ")");
			return false;
		} catch (Exception ex) {
			LOGGER.fatal("Parse event key word error, program will terminate now ("
					+ ex.getMessage() + ")");
			return false;
		} finally {
			try {
				if (buffReader != null) {
					buffReader.close();
				}// end if
			} catch (Exception e) {
				LOGGER.warn("Error while closing FileInputStream  "
						+ e.getMessage());
			}
			buffReader = null;
		}
	}

	/**
	 * Load city Lat-Lon infomation
	 * 
	 * @return
	 */
	private boolean loadCityBoundary() {
		BufferedReader reader = null;
		String lineData = null;
		String[] tokens = null;
		String[] latlons = null;
		cityBoundaryMap = new LinkedHashMap<String, String[]>();
		int latlonNum = 0;
		citySet = new HashSet<String>();
		try {
			reader = new BufferedReader(new FileReader(
					CITY_BOUNDARY_MAPPING_FILE));
			// skip first line
			// reader.readLine();
			while ((lineData = reader.readLine()) != null) {
				tokens = lineData.split(",");
				latlonNum = tokens.length - 1;
				latlons = new String[latlonNum];
				if (tokens != null && tokens.length > 1) {
					for (int i = 1; i < tokens.length; i++) {
						latlons[i - 1] = tokens[i];
					}
					cityBoundaryMap.put(tokens[0].toUpperCase(),
							calculateBoundary(latlons));
					if (!citySet.contains(tokens[0])) {
						citySet.add(tokens[0]);
					}
				}
			}
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("Mapping file:" + CITY_BOUNDARY_MAPPING_FILE
					+ " does not exist, program will terminate now ("
					+ ex.getMessage() + ")");
			return false;
		} catch (IOException ex) {
			LOGGER.fatal("Read city-county mapping file error, program will terminate now ("
					+ ex.getMessage() + ")");
			return false;
		} catch (Exception e) {
			LOGGER.fatal("Error when getTimeZone" + e.getMessage() + ")");
			return false;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.fatal("Can't close fileReader; " + e.getMessage());
				}
			}
		}
		LOGGER.info("Load County-City mapping file succeed.");
		return true;
	}

	/**
	 * <pre>
	 *       Check if the point inside the polygon or not;
	 * </pre>
	 * 
	 * @param polygon
	 * @param px
	 * @param py
	 * @return boolean
	 */
	private boolean insidePolygon(String[] polygon, double px, double py) {
		int i, j = 0;
		boolean flag = false;
		String[] latlong1 = null, latlong2 = null;
		double x1, x2, y1, y2;
		int length = polygon.length;
		for (i = 0, j = length - 1; i < length; j = i++) {
			latlong1 = polygon[i].split("\\s");
			latlong2 = polygon[j].split("\\s");
			x1 = Double.valueOf(latlong1[0]);
			y1 = Double.valueOf(latlong1[1]);
			x2 = Double.valueOf(latlong2[0]);
			y2 = Double.valueOf(latlong2[1]);

			if ((((y1 <= py) && (py <= y2)) || ((y2 <= py) && (py <= y1)))
					&& (px < (x2 - x1) * (py - y1) / (y2 - y1) + x1))
				flag = !flag;
		}
		return flag;
	}

	/**
	 * <pre>
	 *              Calculate all the boundary data by the two points;
	 * </pre>
	 * 
	 * @param tokens
	 * @return String[]
	 */
	private String[] calculateBoundary(String[] tokens) {
		String[] boundaryArray = null;
		String lonLeft, latLeft, lonRight, latRight = null;
		if (tokens.length == 4) {
			lonLeft = tokens[0].trim();
			latLeft = tokens[1].trim();
			lonRight = tokens[2].trim();
			latRight = tokens[3].trim();
			boundaryArray = new String[5];
			boundaryArray[0] = lonLeft + " " + latLeft;
			boundaryArray[1] = lonRight + " " + latLeft;
			boundaryArray[2] = lonRight + " " + latRight;
			boundaryArray[3] = lonLeft + " " + latRight;
			boundaryArray[4] = lonLeft + " " + latLeft;
		} else if (tokens.length > 4) {
			boundaryArray = new String[tokens.length / 2];
			for (int i = 0, j = 0; i < tokens.length; i = i + 2, j++) {
				boundaryArray[j] = tokens[i].trim() + " "
						+ tokens[i + 1].trim();
			}
		}
		return boundaryArray;
	}

	/**
	 * Locate citycode by lat/lon, set record checked flag to -2, so backend
	 * process will do reverse geocoding. If there is no city can be located by
	 * this lat/lon, save it to output file.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return String cityCode
	 * @throws Exception
	 * @exception
	 * @see
	 */
	private String processLatLon(String latitude, String longitude)
			throws Exception {
		double lat, lon;
		String cityCode = "";
		if (latitude == null || longitude == null) {
			return cityCode;
		}
		Set<String> key = cityBoundaryMap.keySet();
		lat = Double.parseDouble(latitude);
		lon = Double.parseDouble(longitude);

		// Go through each city, locate lat/lon.
		for (Iterator<?> it = key.iterator(); it.hasNext();) {
			cityCode = (String) it.next();
			if (insidePolygon(cityBoundaryMap.get(cityCode), lon, lat)) {
				return cityCode;
			}
		}

		String error = "lat:" + lat + " lon:" + lon;
		if (!unknownErrorMap.containsKey(error)) {
			unknownErrorMap.put(error, error);
			error = "Can not locate city for: " + error;
			LOGGER.info(error);
			saveErrors(error + "\n");
		}
		return cityCode;
	}

	/**
	 * Save error message to a output file
	 * 
	 * @param errorMessage
	 * @return None
	 * @exception
	 * @see
	 */
	public void saveErrors(String errorMessage) {
		FileWriter fw = null;
		if (ERROR_RECORDS_FILE != null && !"".equals(ERROR_RECORDS_FILE.trim())) {
			try {
				fw = new FileWriter(ERROR_RECORDS_FILE, true);
				fw.write(errorMessage);
			} catch (IOException ex) {
				LOGGER.info("I/O exception: " + ex.getMessage());
			} catch (Exception ex) {
				LOGGER.info("Can not wrte output file: " + ex.getMessage());
			} finally {
				try {
					if (fw != null) {
						fw.close();
					} else {
						fw = null;
					}
				} catch (IOException e) {
					fw = null;
				}
			}
		} else {
			LOGGER.debug("Error file is not properly configured.");
		}
	}
	
	
}
