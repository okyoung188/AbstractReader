
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.Proxy.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.trafficcast.base.dbutils.DBConnector;
import com.trafficcast.base.dbutils.DBUtils;
import com.trafficcast.base.enums.EventType;
import com.trafficcast.base.geocoding.MySqlGeocodingEngine;
import com.trafficcast.base.geocoding.MySqlGeocodingInitiater;
import com.trafficcast.base.inccon.IncConDBUtils;
import com.trafficcast.base.inccon.IncConRecord;
import com.trafficcast.base.tctime.TCTime;

/*******************************************************************************
 * AR_idrive_Con.java --------------------- Copyright (C)2017 TrafficCast
 * International Inc.All right reserved
 * <p>
 * This reader get the Incident and Construction info from the AR (Arkansas) State web
 * site; The html
 * address:https://www.idrivearkansas.com/
 * Ticket number: #8079
 * <p>
 * ---------------------------
 * 
 * @author Harry Yang
 * @version 1.0 (04/07/2017)
 * @since 1.6
 * --------------------------------------------------------------------
 * 
 * 
 *******************************************************************************/

public class AR_idrive_Con_Pro {

	// Current version of this class.
	public static final double VERSION = 1.0;

	// log4j instance
	private static final Logger LOGGER = Logger.getLogger(AR_idrive_Con_Pro.class);

	// int value 10 represents the con reader
	private final int READER_TYPE = 10;

	// Reader ID
	private final String READER_ID = AR_idrive_Con_Pro.class.getName();

	// Property file location
	private final String PROPERTY_FILE = "prop/AR_idrive_Con.properties";
	private final String PATTERN_FILE = "prop/AR_idrive_Con_Pattern.txt";
	private final String STREET_ALIAS_FILE = "prop/AR_idrive_Con_StreetAlias.txt";
	private final String FORMAT_FILE = "prop/AR_idrive_Con_Format.txt";
	private final String COUNTY_CITY_FILE = "index/ar_county_city.csv";

	// Property keys in OKC_gov_Con.properties
	private final String PROP_KEY_DATA_URL_CONSTRUCTION = "DATA_URL_CONSTRUCTION";
	private final String PROP_KEY_DATA_URL_BASEPATH = "DATA_URL_BASEPATH";
	private final String PROP_KEY_DATA_URL_FORSAVE = "DATA_URL_FORSAVE";
	private final String PROP_KEY_CONNECT_TIME_OUT = "CONNECT_TIME_OUT";
	private final String PROP_KEY_SLEEP_TIME = "SLEEP_TIME_OUT";
	private final String PROP_KEY_RETRY_WAIT_TIME = "RETRY_TIME_OUT";
	private final String PROP_KEY_TC_SEPARATE_SIGN = "TC_SEPARATE_SIGN";
	private final String PROP_KEY_FILTER_KEYWORD = "FILTER_KEY_WORD";

	// Reverse geocoding flag
	private final String PROP_KEY_REVERSE_GEOCODING_FLAG = "REVERSE_GEOCODING_FLAG";

	// Reverse geocoding
	boolean isReverseGeocoding = true;

	// Reverse geocoding value
	private final int REVERSE_GEOCODING_VALUE = -2;

	// TrafficCast Separate Sign
	private String tcSeparateSign = "~TrafficCastSeparateSign~";

	// sleep time, set default to 5 min, will load from property file
	private int loopSleepTime = 5 * 60 * 1000;

	// Retry wait time, set default to 2 minutes, will load from property file
	private int retryWaitTime = 2 * 60 * 1000;

	// Connection time, set default to 2 minutes, will load from property file
	private int connectOutTime = 2 * 60 * 1000;

	// Address to get json
	private String dataURLConstruction = "https://db9f583wfbsn9.cloudfront.net/construction_point.geojson";
	private String dataURLBasePath = "https://www.idrivearkansas.com//details/construction_projects";
	private String dataUrlForSave = "https://www.idrivearkansas.com/";

	// The Pattern to parse MainSt, CrossFrom, CrossTo
	private final String MAIN_ST_PATTERN = "MAIN_ST_PATTERN";
	private final String FROM_ST_PATTERN = "FROM_ST_PATTERN";
	private final String TO_ST_PATTERN = "TO_ST_PATTERN";

	// Arraylist to store patterns
	private ArrayList<Pattern> mainStPatternArrayList;
	private ArrayList<Pattern> fromStPatternArrayList;
	private ArrayList<Pattern> toStPatternArrayList;

	// Hashmap to store street alias.
	private LinkedHashMap<String, String> streetAliasMap;

	private LinkedHashMap<String, String> stTimeFormatMap;

	private LinkedHashMap<String, String> edTimeFormatMap;

	// Hashmap to store filter key word
	private ArrayList<String> filterKeyWordList;
	
	private HashMap<String, String> countyCityMap;

	// State code
	private final String STATE = "AR";

	// City
	private final String CITY = "BSV";

	// General OKC Time Zone
	private TimeZone arTimeZone = null;

	// Default disable function of saveError
	private final boolean enableSaveError = false;

	// SimpleDateFormat to parse time
	private static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(
			"MM-dd-yyyy", Locale.US);

	// ArrayList to store Con
	private ArrayList<IncConRecord> ar_con_list = null;

	// This value is added to the wait time each time an exception is caught in
	// run()
	private final int SEED = 60000;


	public AR_idrive_Con_Pro() {
		super();
	}

	/**
	 * Main will create a new AR_idrive_Con, call run function
	 * @param args
	 * @return None
	 * @exception
	 * @see
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configureAndWatch("log4j_Pro.properties", 60000);
		try {
			AR_idrive_Con_Pro ar_idrive_Con_Pro = new AR_idrive_Con_Pro();
			ar_idrive_Con_Pro.run();
		} catch (Exception ex) {
			LOGGER.fatal("Unexpected problem, program will terminate now ("
					+ ex.getMessage() + ")");
		}
	}

	/**
	 * Read from website, parse json, analyze record, save to database,
	 * Geocoding, sleep and then start another loop.
	 * 
	 * @param None
	 * @return None
	 * @exception Exception
	 * @see
	 **/
	private void run() throws Exception {
		long startTime, sleepTime, waitTime = 0;
		loadCountyCityMap();
		if (loadProperties() && loadStreatAlias() && loadFormat()) {
			LOGGER.info("Load properties and initialize completed, next will enter while()");
		} else {
			LOGGER.fatal("Load properties failed ! Program will terminate.");
			throw new RuntimeException(); // main() will catch this exception.
		}
		
		initVariables(); 
		
		while (true) {
			try {
				startTime = System.currentTimeMillis();
				LOGGER.info("Starting to parse arkansas website for construction information.");
				
  				// Read data source
				readDataSource();
				
				// Update DB
			    LOGGER.info("Start to update db.");
				IncConDBUtils.updateDB(ar_con_list, STATE, EventType.CONSTRUCTION);
				// Update "last run" field in MySql table containing reader
				// program IDs.
				DBUtils.updateReaderLastRun(loopSleepTime, READER_TYPE);

				// Geocoding
				LOGGER.info("Starting GEOCoding process.");
				MySqlGeocodingEngine geo = null;
				geo = new MySqlGeocodingInitiater(CITY, READER_ID);
				geo.initiateGeocoding();
				sleepTime = loopSleepTime
						- (System.currentTimeMillis() - startTime);
				if (sleepTime < 0) {
					sleepTime = 1000;
				}
				// Clear the ArrayList
				ar_con_list.clear();
				System.gc();
				LOGGER.info("Last built on 04/07/2017; Ticket Number: #8079");
				LOGGER.info("Sleeping for " + (sleepTime / 1000) + " seconds.");
				System.out.println();
				DBConnector.getInstance().disconnect();
				Thread.sleep(sleepTime);
				waitTime = 0;
			} catch (NoRouteToHostException ex) {
				LOGGER.warn("This machine's internet connection is unavailable, retrying in  "
						+ retryWaitTime / 60000 + "  mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (ConnectException ex) {
				LOGGER.warn("Connection to the Arkansas website"
						+ " feed was refused, retyring in " + retryWaitTime
						/ 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (SocketException ex) {
				LOGGER.warn("Connection to the Arkansas website"
						+ " feed was refused, retyring in " + retryWaitTime
						/ 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (UnknownHostException ex) {
				LOGGER.warn("Unkown host. Could not establish contact with the Arkansas website, retrying in "
						+ retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (FileNotFoundException ex) {
				LOGGER.warn("Could not retrieve Inc data, retrying in "
						+ retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (IOException ex) {
				LOGGER.warn(ex.getMessage() + ", retrying in " + retryWaitTime
						/ 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (Exception ex) {
				waitTime += waitTime == 0 ? SEED : waitTime;
				LOGGER.log(Level.FATAL, "Unexpected exception (" + ex + "). "
						+ "Restarting parsing process in " + waitTime / 60000
						+ " minute(s).", ex);
				System.out.println();
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread interrupted!");
				}
			} finally {
				ar_con_list.clear();
			}
		}
	}
	
	/**
	 * Read data source from html
	 * @throws Exception
	 */
	private void readDataSource() throws Exception {
		URL url = null;
		HttpsURLConnection conn = null;
		BufferedReader buffReader = null;
		InputStreamReader inReader = null;
		InputStream inStream = null;
		JSONObject object = null;
		JSONArray features = null;
		JSONObject feature = null;
		String lineRead = null;
		Iterator<?> featureItor = null;
//https://www.idrivearkansas.com//details/construction_projects?job_number=090342&begin_log=2.45&route=14&end_log=2.69&latitude=36.054553140626304&longitude=-92.63397175390628&zoom=9
		try {	
			url = new URL(dataURLConstruction);
			// Use proxy server
//			Properties prop = System.getProperties();      
//			prop.setProperty("http.proxyHost", "52.10.174.225");        
//			prop.setProperty("http.proxyPort", "2107");
			InetSocketAddress socket = new InetSocketAddress("52.10.174.225", 2107);
			Proxy proxy = new Proxy(Type.HTTP, socket);
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setConnectTimeout(connectOutTime);
			conn.setReadTimeout(connectOutTime);
//			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
//			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//			conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
//			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
//			conn.setRequestProperty("Cache-Control", "max-age=0");
//			conn.setRequestProperty("Connection", "keep-alive");
//			conn.setRequestProperty("Host", "db9f583wfbsn9.cloudfront.net");
//			conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
//			conn.setRequestProperty("If-Modified-Since", "Fri, 07 Apr 2017 03:06:06 GMT");
			
			inStream = conn.getInputStream();
			inReader = new InputStreamReader(inStream);
			buffReader = new BufferedReader(inReader);
			while ((lineRead = buffReader.readLine()) != null) {
				lineRead = lineRead.trim();
				object = JSONObject.fromObject(lineRead);
				if (object != null && !object.isNullObject()) {
					features = object.getJSONArray("features");
					if (features != null && features.size() > 0) {
						// Requester to request the time and county
						IncidentRequester requester = new IncidentRequester();
						featureItor = features.iterator();
						while (featureItor.hasNext()) {
							try {								
								feature = (JSONObject) featureItor.next();
								if (feature != null && !feature.isNullObject()) {
									JSONObject properties = null;
									JSONObject geometry = null;
									JSONArray coordinates = null;
									String latitude = null;
									String longitude = null;
									String description = null;

									properties = feature.getJSONObject("properties");
									geometry = feature.getJSONObject("geometry");
									coordinates = geometry.getJSONArray("coordinates");
									latitude = coordinates.getString(1);
									longitude = coordinates.getString(0);
									if (properties != null
											&& !properties.isNullObject()) {
										String jobNumber = properties.getString("job_number");
										String beginLog = properties.getString("begin_log_");
										String endLogMi = properties.getString("end_log_mi");
										description = properties.getString("project_de");
										String route = properties.getString("route");
										String routeType = properties.getString("route_type");
										if (beginLog != null && beginLog.matches("\\.\\d+")) {
											beginLog = "0" + beginLog;
										}
										if (endLogMi != null && endLogMi.matches("\\.\\d+")) {
											endLogMi = "0" + endLogMi;
										}
										if (beginLog == null || !beginLog.matches("\\d+(\\.\\d+)?")) {
											LOGGER.debug("BeginLog is invalid: " + properties.toString());
											continue;
										}
										if (endLogMi == null || !endLogMi.matches("\\d+(\\.\\d+)?")) {
											LOGGER.debug("endLogMi is invalid: "+ properties.toString());
											continue;
										}
										if (route == null || !route.matches("\\d+")) {
											LOGGER.debug("route is invalid: "+ properties.toString());
											continue;
										}
										if (jobNumber == null|| !jobNumber.matches("\\w+")) {
											LOGGER.debug("jobNumber is invalid: "+ properties.toString());
											continue;
										}
										if (latitude == null || !latitude.matches("\\d+(\\.\\d+)?")) {
											LOGGER.debug("latitude is invalid: " + properties.toString());
											continue;
										}
										if (longitude == null || !longitude.matches("-\\d+(\\.\\d+)?")) {
											LOGGER.debug("longitude is invalid: " + properties.toString());
											continue;
										}
										requester
										.addRequest(new String[]{jobNumber,route, routeType, beginLog,  endLogMi, latitude, longitude, description});
									}
								}
								// parseJSON(feature);
							} catch (Exception e) {
								LOGGER.debug("Parse record error, parse next: "	+ object.toString());
							}
						}
						
						requester.requestIncident();
						
					}
				} else {
					LOGGER.debug("JSONObject is null.");
				}
			}
		} finally {
			if (buffReader != null) {
				try {
					buffReader.close();
				} catch (Exception e) {
					LOGGER.debug("BufferedReader is not closed.");
				}
				buffReader = null;
			}
			if (inReader != null) {
				try {
					inReader.close();
				} catch (Exception e) {
					LOGGER.debug("InReader is not closed.");
				}
				inReader = null;
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
					LOGGER.debug("InStream is not closed.");
				}
				inStream = null;
			}
			conn = null;
			url = null;
		}

		
		
	}

	/**
	 * Rerequest the time info by combine the request URL
	 * @param jobNumber
	 * @param beginLog
	 * @param route
	 * @param endLogMi
	 * @param latitude
	 * @param longitude
	 * @return infoArray 0-startTimeString, 1-endTimeString, 3-CountyString
	 * @throws MalformedURLException 
	 */
	private String[] requestTimeCounty(String jobNumber, String beginLog, String route,
			String endLogMi, String latitude, String longitude) {
		String[] infoArr = new String[3];
		URL url = null;
		URLConnection conn = null;
		BufferedReader buffReader = null;
		InputStreamReader inReader = null;
		InputStream inStream = null;
		String lineRead = null;
		String startTime = null;
		String endTime = null;
		String county = null;
		if (dataURLBasePath == null || dataURLBasePath.trim().equals("")) {
			LOGGER.debug("DataURLBasePath is null.");
			return null;
		}
		String basePath = dataURLBasePath;
		//?job_number=090342&begin_log=2.45&route=14&end_log=2.69&latitude=36.054553140626304&longitude=-92.63397175390628&zoom=9
		basePath = basePath + "?job_number=" + jobNumber +"&begin_log=" + beginLog +"&route=" + route +"&end_log=" + endLogMi +"&latitude=" + latitude +"&longitude=" + longitude;
		try {
			url = new URL(basePath);
			conn = url.openConnection();
			conn.setConnectTimeout(connectOutTime);
			conn.setReadTimeout(connectOutTime);
			inStream = conn.getInputStream();
			inReader = new InputStreamReader(inStream);
			buffReader = new BufferedReader(inReader);
			while ((lineRead = buffReader.readLine()) != null) {
				lineRead = lineRead.trim().toUpperCase();
				if (lineRead.matches("<DT>WORK BEGAN:.*")) {
					startTime = lineRead.replaceAll("^<DT>WORK BEGAN:.*<DD>(.+)</DD>$", "$1").trim();
					startTime = formatStTime(startTime);
				}
				if (lineRead.matches("<DT>EST\\. COMPLETION:.*")) {
					endTime = lineRead.replaceAll("^<DT>EST. COMPLETION:.*<DD>(.+)</DD>$", "$1").trim();
					endTime = formatEdTime(endTime);
				}
				if (lineRead.matches("<DT>COUNTY:.*")) {
					county = lineRead.replaceAll("^<DT>COUNTY:.*<DD>(.+)</DD>$", "$1").trim();
					county = county.replaceAll("(.+?)/.+", "$1");
				}				
			}
			if (startTime == null || endTime == null || county == null) {
				LOGGER.debug("Get timeCounty info error. JobNume :" + jobNumber);
				infoArr = null;
			} else {
				infoArr[0] = startTime;
				infoArr[1] = endTime;
				infoArr[2] = county;
			}
			
		} catch (Exception e) {
			LOGGER.debug("Exception occur when request time and county info: " + e.getMessage());
			infoArr = null;
		}
		return infoArr;
	}

	/**
	 * Parse json for expecting info
	 * @param feature
	 */
	private void parseJSON(JSONObject feature) {
		
		if (feature == null|| feature.isNullObject()) {
			LOGGER.debug("Feature is null, parse next.");
			return;
		}

		JSONObject properties = null;
		JSONObject geometry = null;
		JSONArray coordinates = null;
		String latitude = null;
		String longitude = null;
		String mainSt= null;
		String fromSt = null;
		String toSt = null;
		String description = null;
		try {
			properties = feature.getJSONObject("properties");
			geometry = feature.getJSONObject("geometry");
			coordinates = geometry.getJSONArray("coordinates");
			latitude = coordinates.getString(1);
			longitude = coordinates.getString(0);
			if (properties != null && !properties.isNullObject()) {
				String jobNumber = properties.getString("job_number");
				String beginLog = properties.getString("begin_log_");
				String endLogMi = properties.getString("end_log_mi");
				description = properties.getString("project_de");
				String route = properties.getString("route");
				String routeType = properties.getString("route_type");
				if (beginLog == null || !beginLog.matches("\\d+(\\.\\d+)?")) {
					LOGGER.debug("BeginLog is invalid: " + properties.toString());
					return;
				}
				if (endLogMi == null || !endLogMi.matches("\\d+(\\.\\d+)?")) {
					LOGGER.debug("endLogMi is invalid: " + properties.toString());
					return;
				}
				if (route == null || !route.matches("\\d+")) {
					LOGGER.debug("route is invalid: " + properties.toString());
					return;
				}
				if (jobNumber == null || !jobNumber.matches("\\w+")){
					LOGGER.debug("jobNumber is invalid: " + properties.toString());
					return;
				}
				if (latitude == null || !latitude.matches("\\d+(\\.\\d+)?")) {
					LOGGER.debug("latitude is invalid: " + properties.toString());
					return;
				}
                if (longitude == null || !longitude.matches("-\\d+(\\.\\d+)?")) {
                	LOGGER.debug("longitude is invalid: " + properties.toString());
					return;
				}
				String[] timeCounty = requestTimeCounty(jobNumber, beginLog, route, endLogMi, latitude, longitude);
				if (timeCounty != null) {
					// Set default values
					IncConRecord incConRecord = new IncConRecord();
					incConRecord.setMapUrl(new URL(dataUrlForSave));
					incConRecord.setState(STATE);
					incConRecord.setType(EventType.CONSTRUCTION);// set default
					
					// Get the mainSt
					if (routeType != null && !"".equals(routeType.trim())) {
						mainSt = routeType + "-" + route;
					}			
					if (mainSt == null) {
						LOGGER.debug("MainSt is null. routeType:" + routeType + "; route:" + route);
						return;
					}
					incConRecord.setMain_st(mainSt);
					fromSt = String.valueOf(Math.round(Double.parseDouble(beginLog)));
					toSt = String.valueOf(Math.round(Double.parseDouble(endLogMi)));
					incConRecord.setFrom_st(fromSt);
					incConRecord.setTo_st(toSt);
					if (timeCounty[0] != null && !timeCounty[0].equals("")) {
						TCTime startTCTime = new TCTime(DEFAULT_TIME_FORMAT, timeCounty[0], arTimeZone);
						incConRecord.setStartTime(startTCTime);
					}
					if (timeCounty[1] != null && !timeCounty[1].equals("")) {
						TCTime endTCTime = new TCTime(DEFAULT_TIME_FORMAT, timeCounty[1], arTimeZone);
						incConRecord.setEndTime(endTCTime);
					}
					// Set county and city
					if (timeCounty[2] != null && !timeCounty[2].equals("")) {
						timeCounty[2] = timeCounty[2].trim().toUpperCase();
						if (countyCityMap != null) {
							String city = countyCityMap.get(timeCounty[2]);
							if (city != null && !"".equals(city.trim())) {
								incConRecord.setCity(city);
							} else {
								LOGGER.debug("County is invalid: " + timeCounty[2]);
							}
						} else {
							LOGGER.debug("County city map is null.");
						}
					}
					// Set the lat/lon
					incConRecord.setS_lat(Double.parseDouble(latitude));
					incConRecord.setS_long(Double.parseDouble(longitude));
					incConRecord.setChecked(REVERSE_GEOCODING_VALUE);
					
				    if (description != null && !"".equals(description.trim())) {
				        description = description.trim().toUpperCase();
				        if (!description.endsWith(".")) {
				        	description = description + ".";
				        }
				        incConRecord.setDescription(description);
				    }
					
				    if (incConRecord.getType().equals(EventType.CONSTRUCTION)) {
                        ar_con_list.add(incConRecord);
				    }
				}
				
			}
		} catch (Exception e) {
			LOGGER.debug("Parse feature error: " + feature.toString());
		}	
	}

	/**
	 * Format the street name.
	 * 
	 * @param street
	 * @return Formated street name
	 */
	private String formatSt(String street) {
		String key = "", value = "";
		String initName = "";
		if (street == null) {
			LOGGER.debug("Street is null.");
			return null;
		} else if (street.equals("")) {
			LOGGER.debug("Street name is a no-letter line!");
			return "";
		}
		initName = street;
		LOGGER.debug("Begin of formatStreet:" + street);

		street = street.trim().toUpperCase();

		Iterator<String> iterator = streetAliasMap.keySet().iterator();
		while (iterator.hasNext()) {
			key = iterator.next();
			value = streetAliasMap.get(key);
			street = street.replaceAll(key, value).trim();
		}
		LOGGER.debug("End of formatStreet:" + street);
		if (street.equals("")) {
			LOGGER.debug("Street is formatted to a no-letter line:" + initName);
		}
		return street;
	}
	
	/**
	 * Format startTime from French into English
	 * @param stTime
	 * @return
	 */
	private String formatStTime(String stTime) {
		Iterator<?> iterator = null;
		String key = null;
		if (stTime == null) {
			return null;
		}
		LOGGER.debug("Start of stTime: " + stTime);
		stTime = stTime.toUpperCase().trim();
		try {
			if (stTimeFormatMap != null) {
				iterator = stTimeFormatMap.keySet().iterator();
				while(iterator.hasNext()) {
					key = (String) iterator.next();
					stTime = stTime.replaceAll(key, stTimeFormatMap.get(key)).trim();
				}
			}
			// if the year is not leap year
			if (stTime.matches("29 February \\d+{4}")) {
				String year = stTime.replaceAll("29 February (\\d+{4})", "$1");
				if (Integer.parseInt(year) % 4 != 0) {
					stTime = stTime
							.replaceAll("29 (February \\d+{4})", "28 $1");
				}
			}
		} catch (Exception e) {
			LOGGER.debug("Format stTime error: " + e.getMessage() + ", key[" + key + "]" );
		}
		LOGGER.debug("End of stTime: " + stTime);
		if (stTime.equals("")) {
			LOGGER.debug("StTime is invalid: " + stTime);
		}
		return stTime;
	}

	/**
	 * Format endTime from French into English
	 * @param edTime
	 * @return
	 */
	private String formatEdTime(String edTime) {
		Iterator<?> iterator = null;
		String key = null;
		if (edTime == null) {
			return null;
		}
		LOGGER.debug("Start of edTime: " + edTime);
		edTime = edTime.toUpperCase().trim();
		try {
			if (edTimeFormatMap != null) {
				iterator = edTimeFormatMap.keySet().iterator();
				while(iterator.hasNext()) {
					key = (String) iterator.next();
					edTime = edTime.replaceAll(key, edTimeFormatMap.get(key)).trim();
				}
			}
			// if the year is not leap year
			if (edTime.matches("29 February \\d+{4}")) {
				String year = edTime.replaceAll("29 February (\\d+{4})", "$1");
			    if (Integer.parseInt(year) % 4 != 0) {
			    	edTime = edTime.replaceAll("29 (February \\d+{4})", "28 $1");
			    }
			}
		} catch (Exception e) {
			LOGGER.debug("Format edTime error: " + e.getMessage() + ", key[" + key + "]" );
		} 		
		LOGGER.debug("End of edTime: " + edTime);
		if (edTime.equals("")) {
			LOGGER.debug("EdTime is invalid: " + edTime);
		}
		return edTime;
	}
	
	/**
	 * Initialize instance level variables
	 * 
	 * @param None
	 * @return None
	 */
	private void initVariables() throws Exception {
		ar_con_list = new ArrayList<IncConRecord>();
		DBConnector.getInstance().setReaderID(READER_ID);
		arTimeZone = DBUtils.getTimeZone(CITY, STATE);
		LOGGER.info("InitVariable successfully!");
	}

	/**
	 * Load the properties file
	 * 
	 * @return true if successfully, otherwise false
	 */
	private boolean loadProperties() {
		FileInputStream is = null;
		String propValue = null;
		Properties prop = new Properties();
		LOGGER.info("Start to load properties");
		try {
			is = new FileInputStream(PROPERTY_FILE);
			prop.load(is);

			// Get the loop sleep time
			propValue = prop.getProperty(PROP_KEY_SLEEP_TIME);
			if (propValue != null && propValue.trim().length() > 0) {
				loopSleepTime = Integer.parseInt(propValue.trim());
				LOGGER.info("Get the loop_sleep_time is :  " + loopSleepTime);
			} else {
				LOGGER.info("Get the loop_sleep_time failed!");
				return false;
			}

			// Get the retry wait time
			propValue = prop.getProperty(PROP_KEY_RETRY_WAIT_TIME);
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

			// Get the URL of Construction
			propValue = prop.getProperty(PROP_KEY_DATA_URL_CONSTRUCTION);
			if (propValue != null && propValue.trim().length() > 0) {
				dataURLConstruction = propValue.trim();
				LOGGER.info("Get the URL of Construction is:  "
						+ dataURLConstruction);
			} else {
				LOGGER.info("Get the URL of Construction failed!");
				return false;
			}

			// Get the URL of Closure
			propValue = prop.getProperty(PROP_KEY_DATA_URL_BASEPATH);
			if (propValue != null && propValue.trim().length() > 0) {
				dataURLBasePath = propValue.trim();
				LOGGER.info("Get the URL of base path is:  " + dataURLBasePath);
			} else {
				LOGGER.info("Get the URL of base path failed!");
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
				isReverseGeocoding = Boolean.parseBoolean(propValue.trim());
				LOGGER.info("ReverseGeocoding flag: " + isReverseGeocoding);
			} else {
				LOGGER.info("Get reverseGeocoding flag failed!");
				return false;
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
	 * Store the county city mapping info
	 */
	private void loadCountyCityMap() {
		BufferedReader reader = null;
		String line = "";
		String[] tokens = null;
		countyCityMap = new HashMap<String, String>();
		try {
			reader = new BufferedReader(new FileReader(COUNTY_CITY_FILE));
			line = reader.readLine(); // Skip the first line.

			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					LOGGER.debug("Comment line: " + line);
					continue;
				}
				tokens = line.split(",");
				if (tokens.length > 1) {
					countyCityMap.put(tokens[0].trim(), tokens[1].trim());
				}
			}
		} catch (Exception ex) {
			LOGGER.log(Level.FATAL,"An unexpected problem occured while attempting to generate " + "CountyCityHashtable", ex);
			System.exit(1);
		} finally {
			LOGGER.debug("Filled the city region hashtable with "
					+ countyCityMap.size() + " pairs.");
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception ex) {
					LOGGER.warn("Could not close BufferedReader.");
				}
			}
		}// end finally
	}
	
	/**
	 * Load street alias
	 * 
	 * @return true if successfully, otherwise false
	 */
	private boolean loadStreatAlias() {
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
	 *  Load time format
	 * @return
	 */
	private boolean loadFormat() {
		BufferedReader reader = null;
		stTimeFormatMap = new LinkedHashMap<String, String>();
		edTimeFormatMap = new LinkedHashMap<String, String>();
		String lineRead = null;
		String[] keyValue = null;
		LOGGER.info("Start to load time format.");
		try {
			reader = new BufferedReader(new FileReader(FORMAT_FILE));
			while ((lineRead = reader.readLine()) != null) {
				if (lineRead.length() < tcSeparateSign.length() + 2
						|| lineRead.startsWith("#")) {
					LOGGER.debug("Empty or comment line, skipped:" + lineRead);
					continue;
				}
				keyValue = lineRead.split(tcSeparateSign);
				if (keyValue != null && keyValue.length > 1) {
					if (keyValue[0].startsWith("START_TIME_FORMAT")) {
						stTimeFormatMap.put(keyValue[0].replaceAll("START_TIME_FORMAT", ""), keyValue[1]);
						LOGGER.debug("START_TIME_FORMAT: " + keyValue[0].replaceAll("START_TIME_FORMAT", "") + " = "
								+ keyValue[1]);
					} else if (keyValue[0].startsWith("END_TIME_FORMAT")) {
						edTimeFormatMap.put(keyValue[0].replaceAll("END_TIME_FORMAT", ""), keyValue[1]);
						LOGGER.debug("END_TIME_FORMAT: " + keyValue[0].replaceAll("END_TIME_FORMAT", "") + " = "
								+ keyValue[1]);
					}
			    }
			}
			LOGGER.info("Load time format successfully!");
			return true;
		} catch (FileNotFoundException ex) {
			LOGGER.fatal("Time format file:" + FORMAT_FILE
					+ " does not exist, program will terminate now ("
					+ ex.getMessage() + ")");
		} catch (Exception ex) {
			LOGGER.fatal("Load time format error, program will terminate now ("
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
	 * Get the time and county info from another thread
	 * @author admin
	 *
	 */
	class IncidentRequester {
		
		private int requestNum = 0;
		private int requestSize = 0;
		private int requestedIndex = -1;
		private Thread[] threads = null;
		private HashMap<String, String[]> timeCountyMap = null;
		private List<String[]> requestList = null;
//		private ArrayList<IncConRecord> constructionList = new ArrayList<IncConRecord>();

		public IncidentRequester() throws Exception {
			super();
			threads = new Thread[5];
			timeCountyMap = new HashMap<String, String[]>();
			this.requestList = new ArrayList<String[]>();
		}

		/**
		 * Add request
		 */
		public void addRequest(String[] requestParams) {
			synchronized(requestList) {
				if (requestList != null) {
					requestSize++;
					requestList.add(requestParams);
					LOGGER.info("Current thread: " + Thread.currentThread().getName() + ", requestSize: " + requestSize);
				}
			}
		}
				
		private void parseConstruction(String jobNumber, String route, String routeType, String beginLog, String endLogMi,  String latitude, String longitude, String description)
				throws MalformedURLException, NumberFormatException {
//			LOGGER.debug("Thread " + Thread.currentThread().getName() + " parseConstruction.");
			String mainSt = null;
			String fromSt = null;
			String toSt = null;
			
			String[] timeCounty = AR_idrive_Con_Pro.this.requestTimeCounty(jobNumber, beginLog, route, endLogMi, latitude, longitude);
			if (timeCounty != null) {
				// Set default values
				IncConRecord incConRecord = new IncConRecord();
				incConRecord.setMapUrl(new URL(dataUrlForSave));
				incConRecord.setState(STATE);
				incConRecord.setTimeZone(arTimeZone);
				incConRecord.setType(EventType.CONSTRUCTION);// set default
				
				// Get the mainSt
				if (routeType != null && !"".equals(routeType.trim())) {
					mainSt = routeType + "-" + route;
					mainSt = formatSt(mainSt);
				}
				if (mainSt == null) {
					LOGGER.debug("MainSt is null. routeType:" + routeType + "; route:" + route);
					return;
				}
				incConRecord.setMain_st(mainSt);
				fromSt = "MM " + String.valueOf(Math.round(Double.parseDouble(beginLog)));
				toSt = "MM " + String.valueOf(Math.round(Double.parseDouble(endLogMi)));
				incConRecord.setFrom_st(fromSt);
				incConRecord.setTo_st(toSt);
				if (timeCounty[0] != null && !timeCounty[0].equals("")) {
					TCTime startTCTime = new TCTime(DEFAULT_TIME_FORMAT, timeCounty[0], arTimeZone);
					incConRecord.setStartTime(startTCTime);
				}
				if (timeCounty[1] != null && !timeCounty[1].equals("")) {
					TCTime endTCTime = new TCTime(DEFAULT_TIME_FORMAT, timeCounty[1], arTimeZone);
					incConRecord.setEndTime(endTCTime);
				}
				// Set county and city
				if (timeCounty[2] != null && !timeCounty[2].equals("")) {
					timeCounty[2] = timeCounty[2].trim().toUpperCase();
					if (countyCityMap != null) {
						String city = countyCityMap.get(timeCounty[2]);
						if (city != null && !"".equals(city.trim())) {
							incConRecord.setCity(city);
						} else {
							LOGGER.debug("County is invalid: " + timeCounty[2]);
						}
					} else {
						LOGGER.debug("County city map is null.");
					}
				}
				// Set the lat/lon
				incConRecord.setS_lat(Double.parseDouble(latitude));
				incConRecord.setS_long(Double.parseDouble(longitude));
				incConRecord.setChecked(REVERSE_GEOCODING_VALUE);
				
			    if (description != null && !"".equals(description.trim())) {
			        description = description.trim().toUpperCase();
			        if (!description.endsWith(".")) {
			        	description = description + ".";
			        }
			        incConRecord.setDescription(description);
			    }
				
			    if (incConRecord.getType().equals(EventType.CONSTRUCTION)) {
			       synchronized(ar_con_list) {
			    	   ar_con_list.add(incConRecord);
			       }
			    }
			}
		}
				
		/**
		 * Next request params
		 * @return
		 */
		private String[] nextRequest() {
			String[] nextRequest = null;
			synchronized(requestList) {
				requestNum++;
				requestedIndex++;
				if (requestList != null && requestedIndex < requestSize) {
					nextRequest = requestList.get(requestedIndex);
					LOGGER.info("Current thread: " + Thread.currentThread().getName() + ", RequestedIndex: " + requestedIndex);
				}
			}
			return nextRequest;
		}
		
		/**
		 * Request the incident and store it in incidentList.
		 * @throws InterruptedException 
		 */
		private void requestIncident() throws InterruptedException {
			for(int i=0; i < threads.length; i++) {
				Thread thread = new Thread() {
					@Override
					public void run () {
						String[] params = null;
						while ((params = nextRequest()) != null) {
							try {
								parseConstruction(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
					}
				};
				threads[i] = thread;
				thread.start();
			}
			
			for (Thread thread : threads) {
//				if (thread.isAlive()) {
				
					thread.join();
//				}
				LOGGER.info("Main thread wait thread " + thread.getName() + " dead.");
			}
			
			LOGGER.info("Main thread continues.");
			
//			// Determine whether all thread are dead.
//			while(true) {
//				boolean arrAlive = false;
////				int iThread = -1;
//				for (Thread thread : threads) {
////					iThread++;
//					if (thread.isAlive()) {
//						LOGGER.info("Thread[ " + thread.getName() + "] is alive.");
//						arrAlive = true;
////						iThread = -1;
//						break;
//					} else {
//						LOGGER.info("Thread[ " + thread.getName() + "] is dead.");
//					}
//				}
//				if (arrAlive) {
//					LOGGER.info("Main thread will sleep for 3 seconds.");
//					Thread.currentThread().sleep(3 * 1000);
//				} else {
//					LOGGER.info("Main thread will continue.");
//					break;
//				}
//			}
			
		}
		
		
		/**
		 * Get the timeCounty array, if 
		 * @param jobNumber
		 * @return
		 */
		public String[] getTimeCounty(String jobNumber) {
			
			//flag of status whether have requested all the construction
			boolean requestDone = false;
			
			String[] timeCounty = null;
			if (jobNumber == null || !jobNumber.matches("\\w+")) {
				LOGGER.debug("Get timeCounty with invalid jobNumber: " + jobNumber);
				return null;
			}
			synchronized(timeCountyMap) {
				timeCounty = timeCountyMap.get(jobNumber);
				if (!requestDone) {
					try {
						timeCountyMap.wait();
					} catch (InterruptedException e) {
	                    LOGGER.debug("Exception when getTimeCounty: " + e.getMessage());
					}
				}
			}
			return timeCounty;
		}
		public void putTimeCounty(String jobNumber, String[] timeCounty) {
            synchronized(timeCountyMap) {
				timeCountyMap.put(jobNumber, timeCounty);
			}
		}
		
	}
}
