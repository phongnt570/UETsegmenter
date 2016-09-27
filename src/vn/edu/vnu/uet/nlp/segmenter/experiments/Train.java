package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import vn.edu.vnu.uet.nlp.segmenter.Configure;
import vn.edu.vnu.uet.nlp.segmenter.FeatureExtractor;
import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * Used for training an individual segmentation system for a fold
 * 
 * @author tuanphong94
 * 
 */
public class Train {

	public static void main(String[] args) throws IOException {
		String fold = "0";
		Path path = Paths.get("data/train_" + fold + ".txt");
		BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);

		FeatureExtractor fe = new FeatureExtractor();

		OldLogging.info("extracting features...");
		String line = null;
		int cnt = 0;
		while ((line = br.readLine()) != null) {
			if (line.isEmpty())
				continue;
			fe.extract(Normalizer.normalize(line, Form.NFC), Configure.TRAIN);
			if (cnt % 1000 == 0 && cnt > 0) {
				System.out.println(cnt + " sentences extracted to features");
			}
			cnt++;
		}
		System.out.println(cnt + " sentences extracted to features");
		System.out.println("\t\t\t\t\t\tTotal number of unique features: " + fe.getFeatureMapSize());
		br.close();

		SegmentationSystem machine = new SegmentationSystem(fe, "testmodels/models_" + fold);

		machine.train();

	}

}
