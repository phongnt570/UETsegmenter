package vn.edu.vnu.uet.nlp.segmenter.bin;

import java.io.IOException;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.utils.FileUtils;

/**
 * @author tuanphong94
 *
 */
public class Test {

	/**
	 * @param modelsPath
	 * @param testSet
	 */
	private static void test(String modelsPath, String testSet) {
		System.out.println("Models path:\t" + modelsPath);
		SegmentationSystem machine = null;

		try {
			System.out.println("Load model...\n");
			machine = new SegmentationSystem(modelsPath);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Test file:\t" + testSet + "\n");
		List<String> dataLines = null;

		try {
			dataLines = FileUtils.readFile(testSet);
			machine.test(dataLines);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			showHelp();
			return;
		}

		if (!(args[0].equals("-m") && args[2].equals("-t"))) {
			showHelp();
			return;
		}

		test(args[1], args[3]);
	}

	protected static void showHelp() {
		System.out.println("Method for testing a model. Needed arguments:\n");

		System.out.println("-m <models_path> -t <test_file>" + "\n");

		System.out.println("\t" + "-m" + "\t" + ":" + "\t" + "path to the folder of segmenter models (required)");
		System.out.println("\t" + "-t" + "\t" + ":" + "\t" + "path to the test file (required)");

		System.out.println();
	}

}
