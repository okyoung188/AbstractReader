package com.reader;


import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.util.StringUtils;


public class ReaderParamParser {

	private Logger logger = Logger.getLogger(this.getClass());
	
	String defaultXmlPath = "prop/reader.xml";
	
	private AbstractReader abstractReader;
	
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
        	short nodeType = node.getNodeType(); 
        	if(nodeType == Node.ELEMENT_NODE){
//            		setByMethod(node);
        			Element element = (Element) node;
        			String nodeName = node.getNodeName();
        			Object value = null;
        			String methodName = null;
        			Class<?> parameterTypes = null;
        			if(nodeName.equals(XmlFileProperty.CITY)){
        				value = element.getAttribute(XmlFileProperty.ATTRIBUTE_NAME);
        				parameterTypes = String.class;
        			} else if(nodeName.equals(XmlFileProperty.CITY_SET)){
        				value = parseCitySet(element);
        				parameterTypes = Map.class;
        			} else if(nodeName.equals(XmlFileProperty.STATE)){
        				value = element.getAttribute(XmlFileProperty.ATTRIBUTE_NAME);
        				parameterTypes = String.class;
        			} else if(nodeName.equals(XmlFileProperty.STATES)){
        				value = parseStates(element);
        				parameterTypes = Map.class;
        			} else if(nodeName.equals(XmlFileProperty.CONNECT_TIME_OUT)){
        			    value = new Long(getValueInChildTxNode(element));
        			    parameterTypes = long.class;
        			} else if(nodeName.equals(XmlFileProperty.SLEEP_TIME_OUT)){
        				value = new Long(getValueInChildTxNode(element));
        				parameterTypes = long.class;
        			} else if(nodeName.equals(XmlFileProperty.RETRY_TIME_OUT)){
        				value = new Long(getValueInChildTxNode(element));
        				parameterTypes = long.class;
        			} else if(nodeName.equals(XmlFileProperty.REVERSE_GEOCODING_FLAG)){
        				value = getValueInChildTxNode(element);
        				parameterTypes = String.class;
        			} else if(nodeName.equals(XmlFileProperty.TC_SEPARATE_SIGN)){
        				value = getValueInChildTxNode(element);
        				parameterTypes = String.class;
        			} else if(nodeName.equals(XmlFileProperty.PROCESSOR)){
        		        
        			}         		
//        	            		else if(nodeName.equals(XmlFileProperty.DATA_URL)){
//        	            			
//        	            		} else if(nodeName.equals(XmlFileProperty.DATA_URL_LIST)){
//        	            			
//        	            		} else if(nodeName.equals(XmlFileProperty.EXTRACTER)){
//        	            			
//        	            		} else if(nodeName.equals(XmlFileProperty.FORMATTER)){
//        	            			
//        	            		} else if(nodeName.equals(XmlFileProperty.MAP_URL)){
//        	            			
//        	            		}  else if(nodeName.equals(XmlFileProperty.REFINER)){
//        	            			
//        	            		} else if(nodeName.equals(XmlFileProperty.REQUESTER)){
//        	            			
//        	            		} 
        			
        			  setByMethod(nodeName, value, methodName,parameterTypes);
        	} else{
        		logger.info("Encounter not element: nodetype[" + nodeType + ", nodename[" + node.getNodeName() + "]");
        	}
        }
	}
	
	/**
	 * Get the value placed at the child text node
	 * @param ele Element instance
	 */
	public String getValueInChildTxNode(Element ele){
		String value = null;
		if(ele != null){
			Node node = ele.getFirstChild();
			if(isNodeType(node, Node.TEXT_NODE)){
				value = node.getNodeValue();
			} else {
				logger.info("First child node of " + ele.getNodeName() + " isn't a text node.");
			}
		}
		return value;
	}
	
	/**
	 * Determine whether the node is in the node type.
	 * @param node
	 * @param type
	 * @return
	 */
	public boolean isNodeType(Node node, Short type){
		boolean isEqual = false;
		if(node != null && type != null){
			if(node.getNodeType() == type){
				isEqual = true;
			}
		}
		return isEqual;
	}

	/**
	 * Set field by reflect field
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public void setByField(String fieldName,Object value) throws Exception{
        if(fieldName != null){
        	Field field = getReader().getClass().getDeclaredField(fieldName);
    		if(field != null){
    			boolean accessible = field.isAccessible();
        		field.setAccessible(true);
    			field.set(abstractReader, value);
    			// Return to initial state
        		if(!accessible){
        			field.setAccessible(false);
        		} else {
        			field.setAccessible(true);
        		}
    		}
    		
        } else {
        	logger.info("FieldName is null.");
        }		
	}
	
	public void setByMethod(String nodeName,Object value,String methodName,Class... parameterTypes) throws Exception {
		if(getReader() == null){
			throw new Exception("Reader is not specified.");
		}
		Class<?> readerClass = getReader().getClass();
		if(!StringUtils.hasText(nodeName)){
			throw new Exception("Field to be set not specified. Please check.");
		}
		String defaultParamSetter = "set" + nodeName;
		Method method = null;
		if(StringUtils.hasText(methodName)){
			defaultParamSetter = methodName.trim();
		}
		method = readerClass.getMethod(defaultParamSetter, parameterTypes);
        if(method != null){
        	method.invoke(getReader(),value);
        } else{
        	logger.warn("Cannot find method named as " + defaultParamSetter + "!");
        }
	}

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch("AbstractReader/prop/log4j.properties", 60000);
		ReaderParamParser parser = new ReaderParamParser();
	    parser.parseParam((AbstractReader)Class.forName("ConcreteReader").newInstance());
		System.out.println("Done.");
	}
	
	/**
	 * Parse states
	 * @param parent
	 * @return
	 */
    public static Map<String,List<?>> parseStates(Element parent){
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
		try {
			if (getReader() != null){
				Method method = getReader().getClass().getMethod(methodName, parameterTypes);	
				return method;
			} else {
				logger.info("Reader is null, so cannot get method.");
			}
		} catch(Exception e){
		    logger.info(e.getMessage());	
		}			
		return null;
	}
}
