package vn.edu.vnu.uet.nlp.helper;

import java.io.BufferedReader;
import java.io.IOException;

import vn.edu.vnu.uet.nlp.utils.FileUtils;

public class DictionaryAnalysis {
	public static void main(String[] args) throws IOException {
		BufferedReader br = FileUtils
				.newUTF8BufferedReaderFromFile("/home/tuanphong94/workspace/dictionary/original_word_list.txt");
		int c1 = 0;
		int c2 = 0;
		int c3 = 0;
		int c4 = 0;
		int c5 = 0;

		for (String line; (line = br.readLine()) != null;) {
			if (line.isEmpty())
				continue;

			String[] tokens = line.split("\\s+");

			switch (tokens.length) {
			case 1:
				c1++;
				break;
			case 2:
				c2++;
				break;
			case 3:
				c3++;
				break;
			case 4:
				c4++;
				break;

			default:
				c5++;
				break;
			}
		}

		System.out.println(c1 + "\n" + c2 + "\n" + c3 + "\n" + c4 + "\n" + c5);
		System.out.println(c1 + c2 + c3 + c4 + c5);
	}
}
