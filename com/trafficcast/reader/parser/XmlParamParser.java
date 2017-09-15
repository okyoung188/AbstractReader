package com.trafficcast.reader.parser;

import org.w3c.dom.Document;

import com.trafficcast.reader.AbstractReader;

public interface XmlParamParser {

	/**
	 * Get the reader xml default path
	 * @return default reader xml path
	 */
	public abstract String getDefaultXmlPath();

	/**
	 * Set the reader xml path
	 * @param defaultXmlPath default reader xml path
	 */
	public abstract void setDefaultXmlPath(String defaultXmlPath);

	public abstract Document getDocument();

	public abstract void setDocument(Document document);

	/**
	 * Parse reader xml configuration file
	 * @throws Exception
	 */
	public abstract void parseParam(AbstractReader abstractReader) throws Exception;

}