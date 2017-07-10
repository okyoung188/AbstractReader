package com.reader;


import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ReaderParamParser {
	
	private AbstractReader abstractReader;
	
	private Logger logger = Logger.getLogger(this.getClass());

    String defaultXmlPath = "prop/reader.xml";

	Document document;

	Map<String,Processor> processors;

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

	public void setProcessors(Map<String,Processor> processors) {
		this.processors = processors;
	}

	public Map<String,Processor> getProcessors() {
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
	
	public AbstractReader getReader(){
		return this.abstractReader;
	}
	
	/**
	 * Parse reader xml configuration file
	 * @throws Exception
	 */
	public void parseParam(AbstractReader abstractReader) throws Exception {
		if(abstractReader == null){
			throw new Exception("Reader is null, cannot parse params.");
		}
		this.abstractReader = abstractReader;
		InputStream inStream = ClassLoader.getSystemResourceAsStream(defaultXmlPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(inStream);
		Element rootElement = document.getDocumentElement();
        NodeList nodeList = rootElement.getChildNodes();
        for (int i=0;i<nodeList.getLength();i++){
        	Node node = nodeList.item(i);
        	Element element = (Element) node;
        	short nodeType = element.getNodeType(); 
        	String nodeName = element.getNodeName();
     		String value = element.getNodeValue();
        	if(nodeType == Node.ELEMENT_NODE){
        		String defaultParamSetter = "set" + nodeName;
        		Method defaultMethod = getMethod(defaultParamSetter,String.class);
        		if(nodeName.equals(XmlFileProperty.CITY)){
        			String name = element.getAttribute(XmlFileProperty.ATTRIBUTE_NAME);
        			defaultMethod = getMethod(defaultParamSetter,String.class);	
        			defaultMethod.invoke(abstractReader, name);
        		} else if(nodeName.equals(XmlFileProperty.CITY_SET)){
        			defaultMethod = getMethod(defaultParamSetter,Map.class);
        			Map<String,List<?>> cityCountyMap = parseCitySet(element);
        			defaultMethod.invoke(abstractReader, cityCountyMap);
        		} else if(nodeName.equals(XmlFileProperty.STATES)){
        			defaultMethod = getMethod(defaultParamSetter,Map.class);
        			Map<String,List<?>> stateCityMap = parseStates(element);
        			defaultMethod.invoke(abstractReader, stateCityMap);
        		} else if(nodeName.equals(XmlFileProperty.CONNECT_TIME_OUT)){
        			defaultMethod = getMethod(defaultParamSetter,Long.class);
        			defaultMethod.invoke(abstractReader, Long.valueOf(value));
        		} else if(nodeName.equals(XmlFileProperty.PROCESSOR)){
        			defaultMethod = getMethod(defaultParamSetter,Map.class);//todo
        			Map<String,List<?>> cityCountyMap = parseCitySet(element);
        			defaultMethod.invoke(abstractReader, cityCountyMap);
        		} else if(nodeName.equals(XmlFileProperty.RETRY_TIME_OUT)){
        			defaultMethod = getMethod(defaultParamSetter,Long.class);
        			defaultMethod.invoke(abstractReader, Long.valueOf(value));
        		} else if(nodeName.equals(XmlFileProperty.REVERSE_GEOCODING_FLAG)){
        			defaultMethod = getMethod(defaultParamSetter,Boolean.class);
        			defaultMethod.invoke(abstractReader, Boolean.valueOf(value));
        		} else if(nodeName.equals(XmlFileProperty.SLEEP_TIME_OUT)){
        			defaultMethod = getMethod(defaultParamSetter,Long.class);
        			defaultMethod.invoke(abstractReader, Long.valueOf(value));
        		} else if(nodeName.equals(XmlFileProperty.STATE)){
        			String name = element.getAttribute(XmlFileProperty.ATTRIBUTE_NAME);
        			defaultMethod = getMethod(defaultParamSetter,String.class);
        			defaultMethod.invoke(abstractReader, name);
        		} else if(nodeName.equals(XmlFileProperty.TC_SEPARATE_SIGN)){
        			defaultMethod = getMethod(defaultParamSetter,String.class);
        			defaultMethod.invoke(abstractReader, value);
        		}         		
//        		else if(nodeName.equals(XmlFileProperty.DATA_URL)){
//        			
//        		} else if(nodeName.equals(XmlFileProperty.DATA_URL_LIST)){
//        			
//        		} else if(nodeName.equals(XmlFileProperty.EXTRACTER)){
//        			
//        		} else if(nodeName.equals(XmlFileProperty.FORMATTER)){
//        			
//        		} else if(nodeName.equals(XmlFileProperty.MAP_URL)){
//        			
//        		}  else if(nodeName.equals(XmlFileProperty.REFINER)){
//        			
//        		} else if(nodeName.equals(XmlFileProperty.REQUESTER)){
//        			
//        		} 
   
        		Field field = AbstractReader.class.getField(nodeName);
        		if(field != null){
        			field.set(abstractReader, value);// if error, determine the field type
        		}
        		
        	} else{
        		logger.info("Encounter not element: nodetype[" + nodeType + ", nodename[" + nodeName + "]");
        	}
        }
	}

	public static void main(String[] args) throws Exception {
		ReaderParamParser parser = new ReaderParamParser();
		parser.parseParam(new AbstractReader(){

			@Override
			public void parseDataSource() {
				
				
			}});
	}
	
	/**
	 * Parse states
	 * @param parent
	 * @return
	 */
    public static Map<String,List<?>>	parseStates(Element parent){
    	Map<String,List<?>> stateCityMap = null;
    	List<String> cityList =null;
		if(parent != null){
		    NodeList stateList = parent.getChildNodes();
		    stateCityMap= new HashMap<String,List<?>>();
		    for (int index = 0; index < stateList.getLength();index++){
		    	Element stateElmt = (Element) stateList.item(index);
		    	String nodeName = stateElmt.getNodeName();
		    	if(nodeName.equals(XmlFileProperty.STATE)){
		    		String nameAttribute = stateElmt.getAttribute(XmlFileProperty.ATTRIBUTE_NAME);
		    		NodeList cityNode = stateElmt.getElementsByTagName(XmlFileProperty.CITY);
		    		if(cityNode != null){
		    			Element cityElmt = (Element) cityNode.item(0);
		    			String cityStr = cityElmt.getNodeValue();
		    			cityList = parseList(cityStr);
		    			stateCityMap.put(nameAttribute, cityList);
		    		}
		    	}		    	
		    }
		}
		return stateCityMap;
	}
    
    /**
     * Parse citySet
     * @param parent
     * @return
     */
    public static Map<String,List<?>>	parseCitySet(Element parent){
    	Map<String,List<?>> cityCountyMap = null;
    	List<String> countyList =null;
		if(parent != null){
		    NodeList cityList = parent.getChildNodes();
		    cityCountyMap= new HashMap<String,List<?>>();
		    for (int index = 0; index < cityList.getLength();index++){
		    	Element cityElmt = (Element) cityList.item(index);
		    	String nodeName = cityElmt.getNodeName();
		    	if(nodeName.equals(XmlFileProperty.CITY)){
		    		String nameAttribute = cityElmt.getAttribute(XmlFileProperty.ATTRIBUTE_NAME);
		    		NodeList countyNode = cityElmt.getElementsByTagName(XmlFileProperty.COUNTY);
		    		if(countyNode != null){
		    			Element countyElmt = (Element) countyNode.item(0);
		    			String countyStr = countyElmt.getNodeValue();
		    			countyList = parseList(countyStr);
		    			cityCountyMap.put(nameAttribute, countyList);
		    		}
		    	}		    	
		    }
		}
		return cityCountyMap;
	}
	
	/**
	 * Parse the list element
	 * @param listStr
	 * @return
	 */
	public static List<String> parseList(String listStr){
		List<String> itemList = null;
		if(listStr != null){
			listStr = listStr.trim();
			String[] list = listStr.split(XmlFileProperty.LIST_SEPERATE);
			if(list != null){
				itemList = new ArrayList<String>();
				for (String item:list){
					if(item != null && !item.trim().equals("")){
						itemList.add(item.trim().toUpperCase());
					}
				}
			}
		}
		return itemList;
	}
	

	public Method getMethod(String methodName,Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException{
		if (getReader() != null){
			Method method = getReader().getClass().getMethod(methodName, parameterTypes);	
			return method;
		} else {
			logger.info("Reader is null, so cannot get method.");
		}	
		return null;
	}
}
