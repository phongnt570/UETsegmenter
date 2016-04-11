package vn.edu.vnu.uet.nlp.segmenter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * List of Vietnamese syllables.
 * 
 * @author tuanphong94
 *
 */
@SuppressWarnings("unchecked")
public class SyllableList {
	private static Set<String> sylList;
	private static String path = "dictionary/VNsylObject";

	private static void getInstance() {
		sylList = new HashSet<String>();
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fin);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			sylList = (Set<String>) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		try {
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isVNsyl(String syl) {
		if (sylList == null) {
			getInstance();
		}
		return sylList.contains(syl.trim().toLowerCase());
	}

	public static void setPath(String _path) {
		path = _path;
	}
}
