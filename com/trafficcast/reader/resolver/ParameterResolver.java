package com.trafficcast.reader.resolver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.trafficcast.reader.AbstractReader;
import com.util.StringUtils;

/**
 * Resolve the specific node to get the parameter in node and set parameter by {@link AbstractReader};
 * @author Harry
 *
 */
public abstract class ParameterResolver {
	
	static final Logger logger = Logger.getLogger(ParameterResolver.class);
	
	AbstractReader reader;
	
	Set<String> resolveNodeList = new HashSet<String>();
	
	Map<String,Method> readerMethodMap;
	
	private boolean methodExtracted = false;
	
	public void setReader(AbstractReader reader){
		this.reader = reader;
	}
	
	public AbstractReader getReader(){
	    return this.reader;	
	}
	
	public void addResolveNode(String... resolveNodeName){
		int addSize = 0;
		if(resolveNodeName != null){
			for (String nodeName:resolveNodeName){
				if(nodeName != null){
					if(!resolveNodeList.contains(nodeName)){
						resolveNodeList.add(nodeName);
						addSize++;
					}					
				}
			}
			logger.debug("Adding size of nodes to be resolved : " + addSize);
		} else {
			logger.debug("Resovle Node Name to be added is null.");
		}
	}
	
	public boolean canResolve(String nodeName){
		if(nodeName != null){
			for(String canResovleName:resolveNodeList){
				if(canResovleName.equals(nodeName)){
					return true;
				}
			}
			logger.debug("Resolvable node list doesn't contain this node: " + nodeName);
		} else {
			logger.debug("Node name is null, please check.");
		}
		return false;
	}	
	
	public Map<String, Method> getReaderMethodMap() {
		return readerMethodMap;
	}

	public void setReaderMethodMap(Map<String, Method> readerMethodMap) {
		this.readerMethodMap = readerMethodMap;
	}

	public Map<String, Method> extractReaderMethodMap(){
		if(!methodExtracted){
			Map<String,Method> extractedMap = null;
			if(reader == null){
				logger.warn("Reader is not defined!");
				return null;
			}
			Method[] methods = reader.getClass().getMethods();
			if(methods != null){
				for (Method method: methods){
					if(method != null){
						String methodName = method.getName();
						if(extractedMap == null){
							extractedMap = new HashMap<String,Method>();
						}
						extractedMap.put(methodName, method);
					}
				}
			}
			readerMethodMap = extractedMap;
			methodExtracted = true;
			return extractedMap;
		} else {
			return readerMethodMap;
		}
	}
	
	public void setByMethod(String nodeName,Object value,String methodName) throws Exception {
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
		Map<String,Method> methodMap = getReaderMethodMap();
		if(methodMap != null && methodMap.size() > 0){
			method = methodMap.get(paramSetter);
		}
        if(method != null){
        	method.invoke(getReader(),value);
        } else{
        	logger.warn("Cannot find method named as " + paramSetter + "!");
        }
	}

	public abstract Object resolve(Node node);
	
		
}
