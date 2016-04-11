package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import vn.edu.vnu.uet.nlp.segmenter.UETSegmenter;

/**
 * Speed measurement.
 * 
 * @author tuanphong94
 *
 */
public class SpeedTest {
	final static Charset cs = StandardCharsets.UTF_8;

	public static void main(String[] args) throws IOException {
		UETSegmenter segmenter = new UETSegmenter("models");
		long time = 0;
		int fileCnt = 0;
		int tokenCnt = 0;

		File folder = new File("/home/tuanphong94/workspace/TPCoRef/raw");
		File[] listOfSubFolders = folder.listFiles();

		for (File subFolder : listOfSubFolders) {
			if (subFolder.isDirectory()) {
				System.out.println("Directory " + subFolder.getPath());

				File[] listOfFiles = subFolder.listFiles();

				for (File file : listOfFiles) {
					if (file.isFile()) {
						System.out.println("File " + file.getPath());

						BufferedReader br = Files.newBufferedReader(Paths.get(file.getPath()), cs);
						String text = br.readLine();
						br.close();

						long startTime = System.currentTimeMillis();
						String segmentedText = segmenter.segment(text);
						long endTime = System.currentTimeMillis();

						time += endTime - startTime;
						fileCnt++;
						tokenCnt += segmentedText.split("\\s|_").length;
					}
				}
			}
		}

		System.out.println("Number of files:\t" + fileCnt);
		System.out.println("Number of tokens:\t" + tokenCnt);
		System.out.println("Total time:\t\t\t" + time);
	}

}
