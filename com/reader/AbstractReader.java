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







import com.processor.Extracter;
import com.processor.Formatter;
import com.processor.Processor;
import com.processor.Refiner;
import com.processor.Requester;
import com.trafficcast.base.dbutils.DBConnector;
import com.trafficcast.base.dbutils.DBUtils;
import com.trafficcast.base.enums.EventType;
import com.trafficcast.base.geocoding.MySqlGeocodingEngine;
import com.trafficcast.base.geocoding.MySqlGeocodingInitiater;
import com.trafficcast.base.inccon.IncConDBUtils;
import com.trafficcast.base.inccon.IncConRecord;

/**
 * 
 * @author harry
 *
 */
public abstract class AbstractReader extends ReaderParam{
	// Current version of this class.
	public static final double VERSION = 1.0;

	// int value 10 represents the con reader
	private static final int READER_TYPE = 10;
	
	// log4j instance
	public static final Logger LOGGER = Logger.getLogger(AbstractReader.class);
	
	private final String READER_ID = this.getClass().getName();

	// county city map
	private Map<String,String> countyCityMap;
	
	// List to store filter key word
	private List<String> filterKWordList;
		
	// County city boundary map
	private LinkedHashMap<String, String[]> cityBoundaryMap;
	
	// ArrayList to store record
	private ArrayList<IncConRecord> con_list = null;
	private ArrayList<IncConRecord> inc_list = null;
	private ArrayList<IncConRecord> tta_list = null;

	// SimpleDateFormat to parse time
	private static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy", Locale.US);
	
	private String lastTicketNumber = "#0000";
	
	private String lastBuiltDate = "01/01/1970";

	public String getTicketNumber() {
		return lastTicketNumber;
	}

	public void setTicketNumber(String ticketNumber) {
		this.lastTicketNumber = ticketNumber;
	}

	public String getLastBuilt() {
		return lastBuiltDate;
	}

	public void setLastBuilt(String lastBuilt) {
		this.lastBuiltDate = lastBuilt;
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
				DBUtils.updateReaderLastRun(sleepTimeOut, READER_TYPE);

				// Geocoding
				LOGGER.info("Starting GEOCoding process.");
				MySqlGeocodingEngine geo = null;
				geo = new MySqlGeocodingInitiater(City, READER_ID);
				geo.initiateGeocoding();
				sleepTime = sleepTimeOut - (System.currentTimeMillis() - startTime);
				if (sleepTime < 0) {
					sleepTime = 1000;
				}

				// Clear the ArrayList
				inc_list.clear();
				con_list.clear();
				tta_list.clear();
				System.gc();
				LOGGER.info("Last built on " + lastBuiltDate + "; Ticket Number:"+ lastTicketNumber);
				LOGGER.info("Sleeping for " + (sleepTime / 1000) + " seconds.");
				System.out.println();
				DBConnector.getInstance().disconnect();
				Thread.sleep(sleepTime);
				waitTime = 0;
			} catch (NoRouteToHostException ex) {
				LOGGER.warn("This machine's internet connection is unavailable, retrying in  " + retryTimeOut / 60000 + "  mins...");
				try {
					Thread.sleep(retryTimeOut);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (ConnectException ex) {
				LOGGER.warn("Connection to the sea website" + " feed was refused, retyring in " + retryTimeOut / 60000 + " mins...");
				try {
					Thread.sleep(retryTimeOut);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (SocketException ex) {
				LOGGER.warn("Connection to the sea website" + " feed was refused, retyring in " + retryTimeOut / 60000 + " mins...");
				try {
					Thread.sleep(retryTimeOut);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (UnknownHostException ex) {
				LOGGER.warn("Unkown host. Could not establish contact with the sea website, retrying in " + retryTimeOut / 60000 + " mins...");
				try {
					Thread.sleep(retryTimeOut);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (FileNotFoundException ex) {
				LOGGER.warn("Could not retrieve Inc data, retrying in " + retryTimeOut / 60000 + " mins...");
				try {
					Thread.sleep(retryTimeOut);
				} catch (InterruptedException ex1) {
					LOGGER.fatal("Thread was interrupted.");
				}
			} catch (IOException ex) {
				LOGGER.warn(ex.getMessage() + ", retrying in " + retryTimeOut / 60000 + " mins...");
				try {
					Thread.sleep(retryTimeOut);
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
	
	/**
	 * Initialize instance level variables
	 * 
	 * @param None
	 * @return None
	 */
	private void initVariables() throws Exception {
		Collection<Processor> processors = processorMap.values();
		for (Processor processor : processors) {
			boolean result = processor.load();
			if (!result) {
				LOGGER.fatal("Load properties failed ! Program will terminate.");
				throw new RuntimeException(); // main() will catch this
												// exception.
			}
		}
		LOGGER.info("Load properties and initialize completed, next will enter while()");
		inc_list = new ArrayList<IncConRecord>();
		con_list = new ArrayList<IncConRecord>();
		tta_list = new ArrayList<IncConRecord>();
		DBConnector.getInstance().setReaderID(READER_ID);
		defaultTimeZone = DBUtils.getTimeZone(City, State);
		LOGGER.info("InitVariable successfully!");
	}

	private void readDataSource() {
		Requester requester = getRequester();
		Executor executor = Executors.newCachedThreadPool();
		executor.execute(new Runnable(){
			@Override
			public void run() {
				parseDataSource();
			}
		});		
	}

	public abstract void parseDataSource();
	

	
	
}
