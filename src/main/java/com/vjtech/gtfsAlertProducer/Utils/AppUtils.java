package com.vjtech.gtfsAlertProducer.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
	
	public static String getDateStringFromPosixTimeStamp(long timeStamp) {
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ITALIAN);
		String formattedDate = outputFormatter.format(new Timestamp(timeStamp * 1000L).toLocalDateTime());
		return formattedDate;
	}
}
