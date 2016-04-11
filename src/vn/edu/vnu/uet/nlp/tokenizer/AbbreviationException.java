package vn.edu.vnu.uet.nlp.tokenizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import vn.edu.vnu.uet.nlp.utils.FileUtils;

public class AbbreviationException {
	private static HashSet<String> abbreviation = null;
	private static HashSet<String> exception = null;
	private static String abbPath = "dictionary/abbreviation.dic";
	private static String excPath = "dictionary/exception.dic";

	public static HashSet<String> getAbbreviation() throws IOException {
		if (abbreviation == null) {
			abbreviation = new HashSet<String>();
			List<String> abbreviationList;
			abbreviationList = FileUtils.readFile(abbPath);

			for (String s : abbreviationList) {
				abbreviation.add(s);
			}
		}

		return abbreviation;
	}

	public static HashSet<String> getException() throws IOException {
		if (exception == null) {
			exception = new HashSet<String>();
			List<String> exceptionList;
			exceptionList = FileUtils.readFile(excPath);

			for (String s : exceptionList) {
				exception.add(s);
			}
		}
		return exception;
	}

	public static void setPath(String path) {
		if (!path.endsWith("/")) {
			path += "/";
		}

		abbPath = path + "abbreviation.dic";
		excPath = path + "exception.dic";
	}
}
