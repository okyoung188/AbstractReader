package com.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;


public class ReaderParamParser {
	Document document;
	
	List<String> processors;

	public ReaderParamParser() {
		super();
	}

	public void parseParam() throws Exception {
		
		
		InputStream inStream = ClassLoader.getSystemResourceAsStream("prop/reader.xml");
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream));
		String line = null;
		while((line = buffReader.readLine()) != null){
			System.out.println(line);
		}
		buffReader.close();
		inStream.close();
		
		inStream = ClassLoader.getSystemResourceAsStream("prop/reader.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		System.out.println(factory instanceof javax.xml.parsers.DocumentBuilderFactory);
		System.out.println(factory instanceof com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl);
		
		document = builder.parse(inStream);
		System.out.println(document.getNamespaceURI());
		System.out.println(document.getChildNodes());
		System.out.println(document.getDocumentElement());
		System.out.println(document.getDocumentElement().getChildNodes().getLength());
		
		inStream = ClassLoader.getSystemResourceAsStream("prop/reader.xml");
		SAXReader reader = new SAXReader();
		org.dom4j.Document doc = reader.read(inStream);
		System.out.println(doc);
		
		
	}
	
    public List<String> getProcessors(){
    	return processors;
    }
    
    public static void main(String[] args) throws Exception {
		ReaderParamParser parser = new ReaderParamParser();
		parser.parseParam();
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
    
    

	
	
}
