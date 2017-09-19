package com.trafficcast.exception;

@SuppressWarnings("serial")
public class XmlFilePathNotDefinedException extends Exception {	
	
	public XmlFilePathNotDefinedException() {
		super("xml file path not given, please check your code.");
	}

	public XmlFilePathNotDefinedException(String message) {
		super(message);
	}

}
