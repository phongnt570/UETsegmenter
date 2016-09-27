package vn.edu.vnu.uet.nlp.utils;

import java.util.Date;

/**
 * @author tuanphong94
 *
 */
public class OldLogging {

	private static void log(String mes, String type) {
		String lines[] = mes.split("\\r?\\n");
		for (String line : lines) {
			if (!line.isEmpty())
				if (type.equals("error")) {
					System.err.println(new Date() + " : " + type.toUpperCase() + " : " + line);
				} else {
					System.out.println(new Date() + " : " + type.toUpperCase() + " : " + line);
				}
		}
	}

	/**
	 * Print to the screen the message with time and tag "INFO".
	 * 
	 * @param mes
	 */
	public static void info(String mes) {
		log(mes, "info");
	}

	/**
	 * Print to the screen the message with time and tag "ERROR".
	 * 
	 * @param mes
	 */
	public static void error(String mes) {
		log(mes, "error");
	}

}
