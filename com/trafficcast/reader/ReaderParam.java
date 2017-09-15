package com.trafficcast.reader;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.trafficcast.reader.parser.XmlParamParser;
import com.trafficcast.reader.processor.Extracter;
import com.trafficcast.reader.processor.Formatter;
import com.trafficcast.reader.processor.Processor;
import com.trafficcast.reader.processor.Refiner;
import com.trafficcast.reader.processor.Requester;

/**
 * The intermittent class to receive parameters and dispatch parameters which
 * acts between parser and abstract reader
 * 
 * @author harry
 */
public class ReaderParam {

	// log4j instance
	public static final Logger LOGGER = Logger.getLogger(ReaderParam.class);

	protected XmlParamParser parser;

	// sleep time, set default to 5 min, will load from property file
	protected long sleepTimeOut = 5 * 60 * 1000;

	// Retry wait time, set default to 2 minutes, will load from property file
	protected long retryTimeOut = 2 * 60 * 1000;

	// Connection time, set default to 2 minutes, will load from property file
	protected long connectTimeOut = 2 * 60 * 1000;

	// This value is added to the wait time each time an exception is caught in
	// run()
	protected final int SEED = 60000;

	// TrafficCast Separate Sign
	protected String tcSeparateSign = "~TrafficCastSeparateSign~";

	// Reverse geocoding, default true
	protected boolean reverseGeocodingFlag = true;

	// Reverse geocoding value, default -2
	protected final int reverseGeocodingValue = -2;

	// State code
	protected String State;

	// City
	protected String City;

	// HashSet to store citys
	protected Map<String, List<String>> cityMap;

	protected Map<String, List<String>> stateMap;

	protected TimeZone defaultTimeZone;

	protected Map<String, TimeZone> timeZoneMap;

	protected List<String> timeZones;

	public void setParser(XmlParamParser parser) throws Exception {
		if (parser == null) {
			throw new Exception("No paramParser defined.");
		}
		this.parser = parser;
	}

	public long getSleepTimeOut() {
		return sleepTimeOut;
	}

	public void setSleepTimeOut(long sleepTimeOut) {
		this.sleepTimeOut = sleepTimeOut;
	}

	public long getRetryTimeOut() {
		return retryTimeOut;
	}

	public void setRetryTimeOut(long retryTimeOut) {
		this.retryTimeOut = retryTimeOut;
	}

	public long getConnectTimeOut() {
		return connectTimeOut;
	}

	public void setConnectTimeOut(long connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}

	public boolean isReverseGeocodingFlag() {
		return reverseGeocodingFlag;
	}

	public void setReverseGeocodingFlag(boolean reverseGeocodingFlag) {
		this.reverseGeocodingFlag = reverseGeocodingFlag;
	}

	public String getTcSeparateSign() {
		return tcSeparateSign;
	}

	public void setTcSeparateSign(String tcSeparateSign) {
		this.tcSeparateSign = tcSeparateSign;
	}

	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public String getCity() {
		return City;
	}

	public void setCity(String city) {
		City = city;
	}

	public Map<String, List<String>> getCityMap() {
		return cityMap;
	}

	public void setCityMap(Map<String, List<String>> cityMap) {
		this.cityMap = cityMap;
	}

	public Map<String, List<String>> getStateMap() {
		return stateMap;
	}

	public void setStateMap(Map<String, List<String>> stateMap) {
		this.stateMap = stateMap;
	}

	public List<String> getTimeZones() {
		return timeZones;
	}

	public void setTimeZones(List<String> timeZones) {
		this.timeZones = timeZones;
	}

	/* ****************************************
	 * *****************Processor***************
	 * ****************************************
	 */
	Requester requester;
	// Address to get json
	private String[] dataURLs;
	private String dataUrlForSave;

	private int urlSize;
	private int finishedSize;

	public boolean isFinished() {
		return urlSize == finishedSize;
	}

	
	
	Formatter formatter;
	Extracter extracter;
	Refiner refiner;
	Map<String, Processor> processorMap;

	public Requester getRequester() {
		return requester;
	}

	public void setRequester(Requester requester) {
		this.requester = requester;
	}

	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}

	public Extracter getExtracter() {
		return extracter;
	}

	public void setExtracter(Extracter extracter) {
		this.extracter = extracter;
	}

	public Refiner getRefiner() {
		return refiner;
	}

	public void setRefiner(Refiner refiner) {
		this.refiner = refiner;
	}

	public Map<String, Processor> getProcessorMap() {
		return processorMap;
	}

	public void setProcessorMap(Map<String, Processor> processorMap) {
		this.processorMap = processorMap;
	}

	protected Processor getProcessor(String name) {
		if (name != null && !name.trim().equals("")) {
			if (processorMap != null) {
				Processor processor = processorMap.get(name);
				if (processor != null) {
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

}
