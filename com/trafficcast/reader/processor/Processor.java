package com.trafficcast.reader.processor;
import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;


public abstract class Processor {
	
	/**
	 * Logger for the processor
	 */
	Logger LOGGER = Logger.getLogger(this.getClass());
	
	/**
	 * The processor's file path
	 */
	private String filePath;
	
	/**
	 * Last modified time of the processor file
	 */
	private Date lastModifiedTime;
	
	/**
	 * tcSeperateSign to split the pattern type and pattern string.
	 */
	public String tcSeparateSign;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String file_path) {
		this.filePath = file_path;
	}
	
	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}

	private void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	/**
	 * Get the tcSeperateSign
	 * @return
	 */
	public String getTcSeparateSign() {
		return tcSeparateSign;
	}
	
	/**
	 * Set the tcSeparateSign
	 * @param tcSeparateSign
	 */
	public void setTcSeparateSign(String tcSeparateSign) {
		this.tcSeparateSign = tcSeparateSign;
	}
	
	/**
	 * Validate the current file is valid or not, used by warm restart
	 * @return
	 */
	private boolean validateModifiedTime(){
		if(this.filePath != null){
			File file = new File(this.filePath);
			return false;
		}
		return true;
	}

	public abstract boolean load() throws Exception;

}
