package com.liferay.nativity.util.win;

import java.lang.reflect.Method;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryUtil {
	
	public static boolean writeRegistry(String key, String name, int value){
		return writeRegistry(key, name, String.valueOf(value));
	}

	public static boolean writeRegistry(String key, String name, String value){
		try {
			_init();
			
			boolean result = _regCreateKeyEx(key);
			
			if(!result) {
				return false;
			}
			
			int handle = _regOpenKeyToWrite(key);
			
			if(handle == 0) {
				return false;
			}
			
			boolean success = _regSetStringValueEx(handle, name, value);
			
		    if(!_regCloseKey(handle)) {
		    	return false;
		    }
		    
		    return success;
		}
		catch(Exception e) {
			_logger.error(e.getMessage(), e);
		}
		
		return false;
	}

	private static void _init() {
		if(userRoot == null) {
			userRoot = Preferences.userRoot();
		}
		
		if(clazz == null) {
			clazz = userRoot.getClass();
		}
	}
	
	private static boolean _regCloseKey(int handle) throws Exception{
		Method regCloseKey = clazz.getDeclaredMethod(
		   	WINDOWS_REG_CLOSE_KEY, new Class[] { int.class });
		     
		regCloseKey.setAccessible(true);
		     
		regCloseKey.invoke(userRoot, new Object[] {handle});
		
		return true;
	}

	private static boolean _regCreateKeyEx(String key) throws Exception{
		Method regCreateKeyEx = clazz.getDeclaredMethod(  
			WINDOWS_REG_CREATE_KEY_EX, 
			new Class[] { int.class, byte[].class });
	        
		regCreateKeyEx.setAccessible(true);
			
		Object returnValue = regCreateKeyEx.invoke(
			userRoot, 
			new Object[] { HKEY_CURRENT_USER, _stringToByteArray(key) });
			
		if(returnValue == null) {
			return false;
		}
			
		if(returnValue instanceof int[]) {
			int[] handle = (int[])returnValue;
			
			if(handle.length == 0) {
				return false;
			}
				
			return _regCloseKey(handle[0]);
		}
	
		return false;
	}

	private static int _regOpenKeyToWrite(String key) throws Exception {
		Method regOpenKey = clazz.getDeclaredMethod(
			WINDOWS_REG_OPEN_KEY, 
			new Class[] { int.class, byte[].class, int.class });
		      
		regOpenKey.setAccessible(true);
		      
		Object result = regOpenKey.invoke(
			userRoot, new Object[] {HKEY_CURRENT_USER, _stringToByteArray(key), 
			KEY_WRITE });
		
		if(result == null) {
			return 0;
		}
		
		if(result instanceof int[]) {
			int[] hResult = (int[])result;
			
			if(hResult.length == 0) {
				return 0;
			}
			
			return hResult[0];
		}
		
		return 0;
	}

	private static boolean _regSetStringValueEx(
		int handle, String name, String value) 
		throws Exception {
		
		Method regSetValueEx = clazz.getDeclaredMethod(  
		  	WINDOWS_REG_SET_VALUE_EX, 
		   	new Class[] { int.class, byte[].class, byte[].class });
		     
		regSetValueEx.setAccessible(true);
		          
		Object hResult = regSetValueEx.invoke(
			userRoot,  
		    new Object[] { 
		    	handle, _stringToByteArray(name), 
		    	_stringToByteArray(value) });
		     
		if(hResult instanceof Integer) {
			int result = ((Integer)hResult).intValue();
		    	 
		    if(result  == 0) {
		    	return true;
		    }
		    else {
		    	_logger.error(
		    		"Unable to set registry value {} {}", name, result);
		   }
		}
		
		return false;
	}

	private static byte[] _stringToByteArray(String str) {
	    byte[] result = new byte[str.length() + 1];
	
	    for (int i = 0; i < str.length(); i++) {
	      result[i] = (byte) str.charAt(i);
	    }
	
	    result[str.length()] = 0;
		  
	    return result;
	}

	private static final int HKEY_CURRENT_USER = 0x80000001;
	
	private static final int KEY_WRITE = 0x20006;
	
	private static Preferences userRoot = null;
	
	private static Class<? extends Preferences> clazz = null;
	
	private static final String WINDOWS_REG_CREATE_KEY_EX = 
		"WindowsRegCreateKeyEx";
	
	private static final String WINDOWS_REG_SET_VALUE_EX = 
		"WindowsRegSetValueEx";
	
	private static final String WINDOWS_REG_CLOSE_KEY =
		"WindowsRegCloseKey";
	
	private static final String WINDOWS_REG_OPEN_KEY =
		"WindowsRegOpenKey";
			
	private static Logger _logger = LoggerFactory.getLogger(
		RegistryUtil.class.getName());
}
