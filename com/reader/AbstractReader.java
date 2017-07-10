package com.reader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.extracter.Extracter;
import com.formatter.Formatter;
import com.requester.Requester;
import com.trafficcast.base.dbutils.DBConnector;
import com.trafficcast.base.dbutils.DBUtils;
import com.trafficcast.base.enums.EventType;
import com.trafficcast.base.geocoding.MySqlGeocodingEngine;
import com.trafficcast.base.geocoding.MySqlGeocodingInitiater;
import com.trafficcast.base.inccon.IncConDBUtils;
import com.trafficcast.base.inccon.IncConRecord;


public abstract class AbstractReader {
	// Current version of this class.
	public static final double VERSION = 1.0;

	// int value 10 represents the con reader
	private static final int READER_TYPE = 10;
	
	// log4j instance
	public static final Logger LOGGER = Logger.getLogger(AbstractReader.class);
	
	private final String READER_ID = this.getClass().getName();

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
	boolean reverseGeocodingFlag = true;

	// Reverse geocoding value, default -2
	private final int reverseGeocodingValue = -2;
	
	// State code
	private String State;

	// City
	private String Ctiy;	
	
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
		return reverseGeocodingFlag;
	}

	public void setReverseGeocoding(boolean isReverseGeocoding) {
		this.reverseGeocodingFlag = isReverseGeocoding;
	}
	
	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public String getCtiy() {
		return Ctiy;
	}

	public void setCtiy(String ctiy) {
		Ctiy = ctiy;
	}




	// HashSet to store citys
	private HashSet<String> citySet;
	
	// county city map
	private Map<String,String> countyCityMap;
	
	// List to store filter key word
	private List<String> filterKWordList;

	// Address to get json
	private String[] dataURLs;
	private String dataUrlForSave;
	
    private int urlSize;
    private int finishedSize;
	
    public boolean isFinished(){
    	return urlSize == finishedSize;
    }
	
	/**
	 * County city boundary map
	 */
	private LinkedHashMap<String, String[]> cityBoundaryMap;
	
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
	
	Requester requester;
	Formatter formatter;
	Extracter extracter;
	Map<String,Processor> processorMap;
	ReaderParamParser parser;

	public void setParser(ReaderParamParser parser) throws Exception{
		if (parser == null){
			throw new Exception("No paramParser defined.");
		}
		this.parser = parser;
	}	
	
	/**
	 * Initialize instance level variables
	 * 
	 * @param None
	 * @return None
	 */
	private void initVariables() throws Exception {
		inc_list = new ArrayList<IncConRecord>();
		con_list = new ArrayList<IncConRecord>();
		tta_list = new ArrayList<IncConRecord>();
		DBConnector.getInstance().setReaderID(READER_ID);
		defaultTimeZone = DBUtils.getTimeZone(Ctiy, State);
		LOGGER.info("InitVariable successfully!");
	}

	/**
	 * Read from website, parse html, analyze record, save to database,
	 * Geocoding, sleep and then start another loop.
	 * 
	 * @param None
	 * @return None
	 * @exception Exception
	 * @see
	 **/
	public void run() throws Exception {
		long startTime, sleepTime, waitTime = 0;
        if (parser == null){
            this.parser = new ReaderParamParser();
        }
        parser.parseParam(this);
        processorMap = parser.getProcessors();
        
        Collection<Processor> processors = processorMap.values();
        for (Processor processor:processors){
        	boolean result = processor.load();
        	if (!result){
        		LOGGER.fatal("Load properties failed ! Program will terminate.");
        		throw new RuntimeException(); // main() will catch this exception.
        	}
        }
        LOGGER.info("Load properties and initialize completed, next will enter while()");

		initVariables();

		while (true) {
			try {
				startTime = System.currentTimeMillis();
				LOGGER.info("Starting to parse sea website for Incident/Construction information.");

				// Read the data source
				readDataSource();

				// util all finished
				while(!isFinished()){
				}
				
				// Update DB
				IncConDBUtils.updateDB(inc_list, State, EventType.INCIDENT);
				IncConDBUtils.updateDB(con_list, State, EventType.CONSTRUCTION);
				IncConDBUtils.updateDB(tta_list, State, EventType.TTA);

				// Update "last run" field in MySql table containing reader
				// program IDs.
				DBUtils.updateReaderLastRun(loopSleepTime, READER_TYPE);

				// Geocoding
				LOGGER.info("Starting GEOCoding process.");
				MySqlGeocodingEngine geo = null;
				geo = new MySqlGeocodingInitiater(Ctiy, READER_ID);
				geo.initiateGeocoding();
				sleepTime = loopSleepTime - (System.currentTimeMillis() - startTime);
				if (sleepTime < 0) {
					sleepTime = 1000;
				}

				// Clear the ArrayList
				inc_list.clear();
				con_list.clear();
				tta_list.clear();
				System.gc();
				LOGGER.info("Last built on 06/22/2017; Ticket Number: #8497");
				LOGGER.info("Sleeping for " + (sleepTime / 1000) + " seconds.");
				System.out.println();
				DBConnector.getInstance().disconnect();
				Thread.sleep(sleepTime);
				waitTime = 0;
			} catch (NoRouteToHostException ex) {
				LOGGER.warn("This machine's internet connection is unavailable, retrying in  " + retryWaitTime / 60000 + "  mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (ConnectException ex) {
				LOGGER.warn("Connection to the sea website" + " feed was refused, retyring in " + retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (SocketException ex) {
				LOGGER.warn("Connection to the sea website" + " feed was refused, retyring in " + retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (UnknownHostException ex) {
				LOGGER.warn("Unkown host. Could not establish contact with the sea website, retrying in " + retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (FileNotFoundException ex) {
				LOGGER.warn("Could not retrieve Inc data, retrying in " + retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (IOException ex) {
				LOGGER.warn(ex.getMessage() + ", retrying in " + retryWaitTime / 60000 + " mins...");
				try {
					Thread.sleep(retryWaitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (Exception ex) {
				waitTime += waitTime == 0 ? SEED : waitTime;
				LOGGER.log(Level.FATAL, "Unexpected exception (" + ex + "). " + "Restarting parsing process in " + waitTime / 60000 + " minute(s).", ex);
				System.out.println();
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread interrupted!");
				}
			} finally {
				inc_list.clear();
				con_list.clear();
				tta_list.clear();
			}
		}// end of while

	}

	private void readDataSource() {
		Requester requester = (Requester) getProcessor("Requester");
		Executor executor = Executors.newCachedThreadPool();
		executor.execute(new Runnable(){
			@Override
			public void run() {
				parseDataSource();
			}
		});		
	}
	
	private Processor getProcessor(String name) {
		if(name != null && !name.trim().equals("")){
			if(processorMap != null){
				Processor processor = processorMap.get(name);
				if(processor != null){
					LOGGER.info("Find the processor named " + name);
					return processor;
				} else {
					 LOGGER.info("Cannot find the processor named " + name);
				}
			}
		} else {
		    LOGGER.info("Name is empty or null.");
		}
		return null;
	}


	public abstract void parseDataSource();

	
	
}
