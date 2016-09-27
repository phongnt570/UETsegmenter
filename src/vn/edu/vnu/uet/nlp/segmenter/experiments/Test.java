package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.IOException;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * Used for testing the system in an individual fold
 * 
 * @author tuanphong94
 */
public class Test {

	public static void main(String[] args) throws IOException {
		OldLogging.info("loading model...");
		String fold = "0";
		SegmentationSystem machine = null;
		try {
			machine = new SegmentationSystem("testmodels/models_" + fold);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		OldLogging.info("testing...");
		List<String> dataLines = FileUtils.readFile("data/test_" + fold + ".txt");

		try {
			machine.test(dataLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
