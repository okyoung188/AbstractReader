package com.util;

public class StringUtils {

	public static boolean hasText(String content){
		boolean hasText = false;
		if(content != null && !content.trim().equals("")){
			hasText = true;
		}
		return hasText;
	}
	
}
