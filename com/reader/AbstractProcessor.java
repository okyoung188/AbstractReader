package com.reader;
import org.apache.log4j.Logger;


public abstract class AbstractProcessor {
	
	/**
	 * Logger for the processor
	 */
	public Logger LOGGER = Logger.getLogger(this.getClass());
	
	/**
	 * The processor's file path
	 */
	private String filePath;
	
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

	public abstract boolean load() throws Exception;

}
