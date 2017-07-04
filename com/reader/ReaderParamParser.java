package com.reader;


import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ReaderParamParser {

    String defaultXmlPath = "prop/reader.xml";

	Document document;

	List<String> processors;

	/**
	 * Get the reader xml default path
	 * @return default reader xml path
	 */
	public String getDefaultXmlPath() {
		return defaultXmlPath;
	}

	/**
	 * Set the reader xml path
	 * @param defaultXmlPath default reader xml path
	 */
	public void setDefaultXmlPath(String defaultXmlPath) {
		this.defaultXmlPath = defaultXmlPath;
	}

	public void setProcessors(List<String> processors) {
		this.processors = processors;
	}

	public List<String> getProcessors() {
		return processors;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public ReaderParamParser() {
		super();
	}
	
	/**
	 * Parse reader xml configuration file
	 * @throws Exception
	 */
	public void parseParam() throws Exception {
		InputStream inStream = ClassLoader.getSystemResourceAsStream(defaultXmlPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(inStream);
		Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        for (int i=0;i<nodeList.getLength();i++){
        	Node node = nodeList.item(i);
        	short nodeType = node.getNodeType(); 
        	if(nodeType == Node.ELEMENT_NODE){
        		String nodeName = node.getNodeName();
        		if(nodeName.equals(XmlFileProperty.CITY)){
        			
        		} else if(nodeName.equals(XmlFileProperty.CITY_SET)){
        			
        		} else if(nodeName.equals(XmlFileProperty.CONNECT_TIME_OUT)){
        			
        		} else if(nodeName.equals(XmlFileProperty.DATA_URL)){
        			
        		} else if(nodeName.equals(XmlFileProperty.DATA_URL_LIST)){
        			
        		} else if(nodeName.equals(XmlFileProperty.EXTRACTER)){
        			
        		} else if(nodeName.equals(XmlFileProperty.FORMATTER)){
        			
        		} else if(nodeName.equals(XmlFileProperty.MAP_URL)){
        			
        		} else if(nodeName.equals(XmlFileProperty.PROCESSOR)){
        			
        		} else if(nodeName.equals(XmlFileProperty.READER_PARAM)){
        			
        		} else if(nodeName.equals(XmlFileProperty.REFINER)){
        			
        		} else if(nodeName.equals(XmlFileProperty.REQUESTER)){
        			
        		} else if(nodeName.equals(XmlFileProperty.RETRY_TIME_OUT)){
        			
        		} else if(nodeName.equals(XmlFileProperty.REVERSE_GEOCODING_FLAG)){
        			
        		} else if(nodeName.equals(XmlFileProperty.SLEEP_TIME_OUT)){
        			
        		} else if(nodeName.equals(XmlFileProperty.STATE)){
        			
        		} else if(nodeName.equals(XmlFileProperty.TC_SEPARATE_SIGN)){
        			
        		} else {// other config
        			
        		}
        	}
        }
	}

	public static void main(String[] args) throws Exception {
		ReaderParamParser parser = new ReaderParamParser();
		parser.parseParam();
	}
}
