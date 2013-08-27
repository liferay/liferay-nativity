package com.liferay.nativity.control.win;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.nativity.util.OSDetector;

public class WindowsNativityWindowsUtil {

	public static boolean isLoaded() {
		if (!_loaded) {
			_load();
		}

		return _loaded;
	}

	public static native boolean setSystemFolder(String folder);

	public static native boolean updateExplorer(String filePath);

	private static void _load() {
		_loaded = false;

		if (!_load) {
			_logger.trace("Do not load");

			return;
		}

		if (!OSDetector.isMinimumWindowsVersion(OSDetector.WIN_VISTA)) {
			_load = false;
	
			return;
		}
			
		if (_loadLibrary(false, _LIFERAY_NATIVITY_WINDOWS_UTIL_x64)) {
			return;
		}
		else if (_loadLibrary(false, _LIFERAY_NATIVITY_WINDOWS_UTIL_x86)) {
			return;
		}

		_logger.error("Unable to load library");
	}

	private static boolean _loadLibrary(boolean fullPath, String path) {
		try {
			if (fullPath) {
				System.load(path);
			}
			else {
				System.loadLibrary(path);
			}
		
			_loaded = true;
		
			_logger.trace("Loaded library {}", path);
		}
		catch (UnsatisfiedLinkError e) {
		_logger.error("Failed to load {}", path);
		}
		catch (Exception e) {
		_logger.error("Failed to load {}", path);
		}
	
		return _loaded;
	}

	private static final String _LIFERAY_NATIVITY_WINDOWS_UTIL_x86 = 
	"LiferayNativityWindowsUtil_x86";

	private static final String _LIFERAY_NATIVITY_WINDOWS_UTIL_x64 = 
	"LiferayNativityWindowsUtil_x64";

	private static boolean _load = true;
	
	private static boolean _loaded;
	
	private static Logger _logger = LoggerFactory.getLogger(
		WindowsNativityWindowsUtil.class.getName());

}

