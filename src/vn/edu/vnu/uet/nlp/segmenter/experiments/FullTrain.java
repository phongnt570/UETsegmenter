package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.Configure;
import vn.edu.vnu.uet.nlp.segmenter.FeatureExtractor;
import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * Train the segmetation system with data placed in a directory
 * 
 * @author tuanphong94
 *
 */
public class FullTrain {

	static int cnt = 0;

	public static void main(String[] args) throws IOException {

		String directory = "original_train/";

		FeatureExtractor fe = new FeatureExtractor();

		OldLogging.info("extracting features...");

		Files.walk(Paths.get(directory)).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				List<String> dataLines = null;
				try {
					dataLines = FileUtils.readFile(filePath.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (String line : dataLines) {
					if (line.isEmpty()) {
						continue;
					}
					fe.extract(Normalizer.normalize(line, Form.NFC), Configure.TRAIN);
					if (cnt % 1000 == 0 && cnt > 0) {
						System.out.println(cnt + " sentences extracted to features");
					}
				}
				cnt += dataLines.size();
			}
		});

		System.out.println(cnt + " sentences extracted to features");
		System.out.println("\t\t\t\t\t\tTotal number of unique features: " + fe.getFeatureMapSize());

		SegmentationSystem machine = new SegmentationSystem(fe, "original_models");

		machine.train();
	}

}
