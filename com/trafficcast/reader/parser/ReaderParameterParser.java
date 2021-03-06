package com.trafficcast.reader.parser;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.trafficcast.reader.AbstractReader;
import com.trafficcast.reader.processor.Processor;
import com.trafficcast.reader.resolver.ParameterResolver;
import com.util.StringUtils;


public class ReaderParameterParser extends ParameterParser implements XmlParser {

	Logger logger = Logger.getLogger(this.getClass());
	
	private AbstractReader reader;
	
	private List<ParameterResolver> resolvers = new ArrayList<ParameterResolver>();	
	
	private Map<String,Method> readerMethodMap;
	
	public ReaderParameterParser(AbstractReader reader) {
		this.reader = reader;
		extractReaderMethodMap();
	}
	
	public List<ParameterResolver> getResolvers() {
		return resolvers;
	}

	public void setResolvers(List<ParameterResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	public void addResovler(ParameterResolver resolver){
		if(resolver !=null){
			resolver.setReader(this.reader);
			resolver.setReaderMethodMap(readerMethodMap);
			resolvers.add(resolver);
		}
	}
	
	public AbstractReader getReader(){
		return this.reader;
	}
	
	public void setReader(AbstractReader Reader) throws Exception{
		this.reader = Reader;
	}
	
	public Map<String, Method> getReaderMethodMap() {
		return readerMethodMap;
	}

	public void setReaderMethodMap(Map<String, Method> readerMethodMap) {
		this.readerMethodMap = readerMethodMap;
	}
	
	public Map<String, Method> extractReaderMethodMap(){
		Map<String, Method> extractedMap = null;
		if (reader == null) {
			logger.warn("Reader is not defined!");
			return null;
		}
		Method[] methods = reader.getClass().getMethods();
		if (methods != null) {
			for (Method method : methods) {
				if (method != null) {
					String methodName = method.getName();
					if (extractedMap == null) {
						extractedMap = new HashMap<String, Method>();
					}
					extractedMap.put(methodName, method);
				}
			}
		}
		readerMethodMap = extractedMap;
		return extractedMap;
	}
	
	@Override
	public void parseParameters(Document document) throws Exception {
		Element rootElement = document.getDocumentElement();
	    NodeList nodeList = rootElement.getChildNodes();
	    for (int i=0;i<nodeList.getLength();i++){
	    	Node node = nodeList.item(i);
	    	short nodeType = node.getNodeType(); 
	    	if(nodeType == Node.ELEMENT_NODE){
				Element element = (Element) node;
				String nodeName = node.getNodeName();
				Object value = null;
				String methodName = null;
				if (nodeName.equals(ReaderXmlFileProperty.CITY)) {
					value = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_NAME);
				} else if (nodeName.equals(ReaderXmlFileProperty.CITYMAP)) {
					value = parseCityMap(element);
				} else if (nodeName.equals(ReaderXmlFileProperty.STATE)) {
					value = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_NAME);
				} else if (nodeName.equals(ReaderXmlFileProperty.STATEMAP)) {
					value = parseStateMap(element);
				} else if (nodeName.equals(ReaderXmlFileProperty.CONNECT_TIME_OUT)) {
					value = new Long(parseValueInChildTextNode(element));
				} else if (nodeName.equals(ReaderXmlFileProperty.SLEEP_TIME_OUT)) {
					value = new Long(parseValueInChildTextNode(element));
				} else if (nodeName.equals(ReaderXmlFileProperty.RETRY_TIME_OUT)) {
					value = new Long(parseValueInChildTextNode(element));
				} else if (nodeName.equals(ReaderXmlFileProperty.REVERSE_GEOCODING_FLAG)) {
					value = new Boolean(parseValueInChildTextNode(element));
				} else if (nodeName.equals(ReaderXmlFileProperty.TC_SEPARATE_SIGN)) {
					value = parseValueInChildTextNode(element);
				} else if (nodeName.equals(ReaderXmlFileProperty.DEFAULT_TIME_ZONE)){
					String state = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_STATE);
					String city = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_CITY);
					if(state !=null && city != null){
						List<String> timeZoneList = new ArrayList<String>();
						timeZoneList.add(parseTimeZone(element));
						value = timeZoneList;
						methodName = ReaderXmlFileProperty.SETMETHOD_TIMEZONES;
					} else {
						throw new Exception("Invalid parameter of defaultTimeZone!");
					}
				} else if(nodeName.equals(ReaderXmlFileProperty.TIME_ZONES)){
					value = parseTimeZones(element);
				} else if (nodeName.equals(ReaderXmlFileProperty.PROCESSORS)) {
	                value = parseProcessors(element);
				} else {
					if(this.resolvers != null && this.resolvers.size() > 0){
						for (ParameterResolver resolver:resolvers){
							if(resolver.canResolve(nodeName)){
								value = resolver.resolve(element);
							} else {
								continue;
							}
						}
					}
				}
				if(value == null){
					throw new Exception("The node " + nodeName + " cannot be resolved, please check the configuration file and ParameterResolver object.");
				}
				setByMethod(nodeName, value, methodName, null);
	    	} else{
	    		logger.info("Encounter not element: nodetype[" + nodeType + ", nodename[" + node.getNodeName() + "]");
	    	}
	    }
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
    			field.set(reader, value);
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
	
	public void setByMethod(String nodeName,Object value,String methodName,Class<?>... parameterTypes) throws Exception {
		if(getReader() == null){
			throw new Exception("Reader is not specified.");
		}
		
		if(!StringUtils.hasText(nodeName)){
			throw new Exception("Field to be set not specified. Please check.");
		}
		String paramSetter = "set" + nodeName;
		Method method = null;
		if(StringUtils.hasText(methodName)){
			paramSetter = methodName.trim();
		}
		if(readerMethodMap != null && readerMethodMap.size() > 0){
		    method = readerMethodMap.get(paramSetter);	
		}
		if(method == null){
			Class<?> readerClass = getReader().getClass();
			method = readerClass.getMethod(paramSetter, parameterTypes);
		}
        if(method != null){
        	method.invoke(getReader(),value);
        } else{
        	logger.warn("Cannot find method named as " + paramSetter + "!");
        }
	}

//	public static void main(String[] args) throws Exception {
//		PropertyConfigurator.configureAndWatch("AbstractReader/prop/log4j.properties", 60000);
//		XmlParser parser = new ReaderParamParser();
//	    parser.parseDocument();
//		System.out.println("Done.");
//	}
	
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
	 * Determine whether the node's name equals the given name 
	 * @param node
	 * @param nodeName
	 * @return
	 */
	public boolean isNodeName(Node node,String nodeName){
		boolean isEqual = false;
		if(node != null && nodeName != null){
			String name = node.getNodeName();
			if(name != null && name.equals(nodeName)){
				isEqual = true;
			}
		}
		return isEqual;
	}
	
	public Method getMethod(String methodName,Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException{
		try {
			if (getReader() != null){
				Method method = getReader().getClass().getMethod(methodName, parameterTypes);	
				return method;
			} else {
				logger.debug("The method [" + methodName + "] with method parameterTypes [" + Arrays.toString(parameterTypes) + "] doesn't exist.");
			}
		} catch(Exception e){
		    logger.debug(e.getMessage());	
		}			
		return null;
	}
	
	List<String> parseTimeZones(Element element) throws Exception {
		List<String> timeZoneKeyList = null;
		int defaultPosition = -1;
		if(element != null){
			NodeList nodeList = element.getChildNodes();
			for (int i = 0;i< nodeList.getLength();i++){
				Node node = nodeList.item(i);
				if(isNodeType(node, Node.ELEMENT_NODE)){
					if(isNodeName(node, ReaderXmlFileProperty.TIME_ZONE)){
						String timeZoneKey = parseTimeZone((Element)node);
						if(timeZoneKey != null){
							if(timeZoneKeyList == null){
								timeZoneKeyList = new ArrayList<String>();								
							}
							if(timeZoneKey.endsWith("default") && defaultPosition == -1){
								defaultPosition = timeZoneKeyList.size();
    						}
							timeZoneKeyList.add(timeZoneKey);
						}
					}
				}
			}
		}
		if(timeZoneKeyList != null){
			// exchange default item and first item
			if(defaultPosition != -1){
				String timeZone0 = timeZoneKeyList.get(0);
				String timeZoneDefault = timeZoneKeyList.get(defaultPosition);
				timeZoneKeyList.set(0, timeZoneDefault);
				timeZoneKeyList.set(defaultPosition, timeZone0);
			}
		}
		return timeZoneKeyList;
	}
	
	/**
	 * Parse timeZone element to string key
	 * @param element
	 * @return
	 */
	String parseTimeZone(Element element) throws Exception{
		if(element != null){
			String state = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_STATE);
			String city = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_CITY);
			String defaultStr = element.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_DEFAULT);
			if(state != null && city != null){
				StringBuffer buffer = new StringBuffer();
				buffer.append(state);
				buffer.append("_");
				buffer.append(city);
				if(defaultStr != null && defaultStr.equals("true")){
					buffer.append("_");
					buffer.append("default");
				}
				return buffer.toString();
			}
		}
		return null;
	}

	/**
	 * Get the value placed at the child text node
	 * @param ele Element instance
	 */
	String parseValueInChildTextNode(Element ele){
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
	 * Parse attributes in the element with specific class
	 * @param parent
	 * @param clazz
	 * @return
	 * @throws Exception 
	 */
	private <T> T parseAttributesToObject(Element parent, Class<T> clazz,String...attrs) throws Exception{
		if(clazz != null){
		    Map<String,String> attrMap = parseValueInAttributes(parent, attrs);
		    if(attrMap !=null){
		    	T t = clazz.newInstance();
		        Set<String> attrNames = attrMap.keySet();
		        for(String name: attrNames){
		        	Field field = null;
		        	if((field = clazz.getDeclaredField(name)) !=null){
		        		Class<?> fieldType = field.getType();
		        		if(fieldType.equals(int.class)){
		        			field.setInt(t, Integer.parseInt(attrMap.get(name)));
		        		} else if(fieldType.equals(double.class)){
		        			field.setDouble(t, Double.parseDouble(attrMap.get(name)));
		        		} else if(fieldType.equals(float.class)){
		        			field.setFloat(t, Float.parseFloat(attrMap.get(name)));
		        		} else if(fieldType.equals(short.class)){
		        			field.setShort(t, Short.parseShort(attrMap.get(name)));
		        		} else if(fieldType.equals(byte.class)){
		        			field.setByte(t, Byte.parseByte(attrMap.get(name)));
		        		} else if(fieldType.equals(long.class)){
		        			field.setLong(t, Long.parseLong(attrMap.get(name)));
		        		} else if(fieldType.equals(char.class)){
		        			field.setChar(t, attrMap.get(name).charAt(0));
		        		} else if(fieldType.equals(boolean.class)){
		        			field.setBoolean(t, Boolean.parseBoolean(attrMap.get(name)));
		        		} else if(fieldType.equals(String.class)){
		        			field.set(t, attrMap.get(name));
		        		} else {
		        			throw new Exception("FieldType and attribute are not matched exception.");
		        		}
		        	}
		        }	    
		    }		    
		}
		return null;
	}
		
	private Map<String,String> parseValueInAttributes(Element ele,String... attrs){
		Map<String,String> attrMap = null;		
		if(ele != null){
			String eleName = ele.getNodeName();
			 if(logger.isDebugEnabled()){
				logger.debug("ParseValueInAttributes executing for element[ " + eleName+ "]");
			 }
		     NamedNodeMap nnm = ele.getAttributes();
		     int parsedSize = 0;
             for(int i = 0; i< nnm.getLength();i++){
            	 Node node = nnm.item(i);
            	 String nName = node.getNodeName();
            	 String nValue = node.getNodeValue();
            	 if(StringUtils.hasText(nName) && StringUtils.hasText(nValue)){
            		 if(validateAttr(nName, attrs)){
                		 if(attrMap == null){
                			 attrMap = new HashMap<String,String>();
                		 }
            			 attrMap.put(nName, nValue);
            			 parsedSize++;
            		 }
            	 }
             }
             if(logger.isDebugEnabled()){
 				logger.debug("ParseValueInAttributes for element[" + eleName+ "], size: " + parsedSize);
 			 }
             return attrMap;
		}
		return null;
	}
	
	/**
	 * Determine whether the attribute is valid
	 * @param attrName   Name of the attribute
	 * @param validNames if null, all attrName are valid
	 * @return
	 */
	private boolean validateAttr(String attrName, String... validNames){
		boolean isValid = false;
		if(StringUtils.hasText(attrName)){
			if(validNames != null){
				for(String name:validNames){
					if(StringUtils.hasText(name) && attrName.equals(name)){
						isValid = true;
					}
				}
			} else {
				isValid = true;
			}
		}
		return isValid;
	}
	
	/**
	 * Parse sub element, maybe need recurse
	 * @param parent
	 * @param clazz
	 * @return
	 */
	private <T> T parseSubElement(Element parent, Class<T> clazz){
		return null;
	}
	
	/**
	 * Parse states
	 * @param parent
	 * @return
	 */
    Map<String,List<?>> parseStateMap(Element parent){
    	Map<String,List<?>> stateCityMap = null;
    	List<String> cityList =null;
		if(parent != null){
		    NodeList stateList = parent.getChildNodes();
		    stateCityMap= new HashMap<String,List<?>>();
		    for (int index = 0; index < stateList.getLength();index++){
		    	Node node = stateList.item(index);
		    	if(isNodeType(node,Node.ELEMENT_NODE)){
		    		Element stateElmt = (Element) stateList.item(index);
		    		String nodeName = stateElmt.getNodeName();
		    		if(nodeName.equals(ReaderXmlFileProperty.STATE)){
		    			String nameAttribute = stateElmt.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_NAME);
		    			NodeList cityNode = stateElmt.getElementsByTagName(ReaderXmlFileProperty.CITY);
		    			if(cityNode != null){
		    				Element cityElmt = (Element) cityNode.item(0);
		    				String cityStr = parseValueInChildTextNode(cityElmt);
		    				cityList = parseList(cityStr);
		    				stateCityMap.put(nameAttribute, cityList);
		    			}
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
    Map<String,List<?>>	parseCityMap(Element parent){
    	Map<String,List<?>> cityCountyMap = null;
    	List<String> countyList =null;
		if(parent != null){
		    NodeList cityList = parent.getChildNodes();
		    cityCountyMap= new HashMap<String,List<?>>();
		    for (int index = 0; index < cityList.getLength();index++){
                Node node = cityList.item(index);
                if(isNodeType(node,Node.ELEMENT_NODE)){
                	Element cityElmt = (Element) cityList.item(index);
                	String nodeName = cityElmt.getNodeName();
                	if(nodeName.equals(ReaderXmlFileProperty.CITY)){
                		String nameAttribute = cityElmt.getAttribute(ReaderXmlFileProperty.ATTRIBUTE_NAME);
                		NodeList countyNode = cityElmt.getElementsByTagName(ReaderXmlFileProperty.COUNTY);
                		if(countyNode != null){
                			Element countyElmt = (Element) countyNode.item(0);
                			String countyStr = parseValueInChildTextNode(countyElmt);
                			countyList = parseList(countyStr);
                			cityCountyMap.put(nameAttribute, countyList);
                		}
                	}
                }
		    }
		}
		return cityCountyMap;
	}
    
    /**
     * Parse the processors
     * @param element
     * @return
     * @throws Exception 
     */
    private Map<String,Processor> parseProcessors(Element element) throws Exception{
    	Map<String,Processor> processorMap = null;
    	if(element != null){
    		NodeList nodes = element.getChildNodes();
    		for (int i = 0; i< nodes.getLength();i++){
    			Node node = nodes.item(i);
    			if(isNodeType(node, Node.ELEMENT_NODE)){
    				if(isNodeName(node, ReaderXmlFileProperty.REQUESTER)){
    					ProcessorDefinition processorDef = parseProcessorDefinition(element);
    					if(processorDef == null){
    					   throw new Exception("Parse request definition error! Please check xml configuration.");
    					}
    					if(processorMap == null){
    						processorMap = new HashMap<String,Processor>();
    					}
    				    Processor requester= getProcessor(processorDef);
    				    if(requester == null){
    				    	throw new Exception("Requester is not properly configured, please check xml configuration.");
    				    }
    				    processorMap.put(processorDef.getName(), requester);
    				} else if(isNodeName(node, ReaderXmlFileProperty.EXTRACTER) 
    						|| isNodeName(node, ReaderXmlFileProperty.FORMATTER)
    						|| isNodeName(node, ReaderXmlFileProperty.REFINER) 
    						|| isNodeName(node, ReaderXmlFileProperty.PROCESSOR)) {
    					ProcessorDefinition processorDef = parseProcessorDefinition(element);
    					if(processorDef == null){
    					   throw new Exception("Parse request definition error! Please check xml configuration.");
    					}
    					if(processorMap == null){
    						processorMap = new HashMap<String,Processor>();
    					}
    					Processor processor= getProcessor(processorDef);
     				    if(processor == null){
     				    	throw new Exception("Requester is not properly configured, please check xml configuration.");
     				    }
     				    processorMap.put(processorDef.getName(), processor);
    				}
    			}
    		}
    	}    	
    	return null;
    }
    
    /**
     * Parse the processor
     * @param element
     */
    private ProcessorDefinition parseProcessorDefinition(Element element){
    	return null;
    }
    
    /**
     * Parse the requester
     * @param element
     */
    private Processor parseRequester(Element element){
    	if(isNodeName(element, ReaderXmlFileProperty.REQUESTER)){
    		
    	} else {
    		
    	}
    	return null;
    }
    
    private Processor getProcessor(ProcessorDefinition processorDefinition){
    	return null;
    }
	
	/**
	 * Parse the list element
	 * @param listStr
	 * @return
	 */
	private List<String> parseList(String listStr){
		List<String> itemList = null;
		if(listStr != null){
			listStr = listStr.trim();
			String[] list = listStr.split(ReaderXmlFileProperty.LIST_SEPERATE);
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
	
}
