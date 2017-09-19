package com.trafficcast.reader.parser;

import org.w3c.dom.Document;

import com.trafficcast.reader.AbstractReader;

public interface XmlParser {

	/**
	 * Get the reader xml default path
	 * @return default reader xml path
	 */
	public abstract String getXmlPath();

	/**
	 * Set the reader xml path
	 * @param xmlPath default reader xml path
	 */
	public abstract void setXmlPath(String xmlPath);

	public abstract Document getDocument();

	public abstract void setDocument(Document document);

	/**
	 * Parse reader xml configuration file
	 * @throws Exception
	 */
	public abstract void parseDocument() throws Exception;

}