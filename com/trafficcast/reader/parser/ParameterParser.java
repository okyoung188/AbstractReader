package com.trafficcast.reader.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.trafficcast.exception.XmlFilePathNotDefinedException;

/**
 * Abstract class to parse parameters in xml file.
 * @author Harry
 *
 */
public abstract class ParameterParser implements XmlParser{

	String defaultXmlPath = "";
	
	Document document;

	public ParameterParser() {
		super();
	}

	@Override
	public String getXmlPath() {
		return defaultXmlPath;
	}

	@Override
	public void setXmlPath(String xmlPath) {
		this.defaultXmlPath = xmlPath;
	}

	@Override
	public Document getDocument() {
		return document;
	}

	@Override
	public void setDocument(Document document) {
		this.document = document;
	}
	
	@Override
	public void parseDocument() throws Exception {
		if(document == null){
			if(defaultXmlPath == null){
				throw new XmlFilePathNotDefinedException();
			}
			InputStream inStream = ClassLoader.getSystemResourceAsStream(defaultXmlPath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(inStream);
		}
		parseParameters(document);
	}

	public abstract void parseParameters(Document document) throws Exception;

}