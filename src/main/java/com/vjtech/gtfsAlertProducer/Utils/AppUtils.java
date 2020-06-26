package com.vjtech.gtfsAlertProducer.Utils;

import java.io.File;
import java.io.IOException;

public class AppUtils {
	public static boolean isValidFilePath(String path) {
		File f = new File(path);
		try {
			f.getCanonicalPath();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
