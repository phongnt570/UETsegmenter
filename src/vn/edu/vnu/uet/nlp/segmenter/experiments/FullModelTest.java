package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * Used for testing the model trained on full corpus of VTB
 * 
 * @author tuanphong94
 *
 */
public class FullModelTest {

	public static void main(String[] args) throws IOException {
		OldLogging.info("loading model...");
		SegmentationSystem machine = null;
		try {
			machine = new SegmentationSystem("original_models");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		OldLogging.info("testing...");
		List<String> dataLines = FileUtils.readFile("raw/independent-test-100sentences.seg");
		List<String> lines = new ArrayList<String>();

		for (String line : dataLines) {
			lines.add(Normalizer.normalize(line, Form.NFC));
		}

		try {
			machine.test(lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
