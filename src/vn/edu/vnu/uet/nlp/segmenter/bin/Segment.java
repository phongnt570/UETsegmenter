package vn.edu.vnu.uet.nlp.segmenter.bin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.UETSegmenter;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * @author tuanphong94
 *
 */
public class Segment {
	private static UETSegmenter segmenter = null;
	private static String modelsPath;

	/**
	 * @param input
	 * @param output
	 */
	private static void segmentFile(String input, String output) {
		if (segmenter == null) {
			System.out.println("Load model...");
			segmenter = new UETSegmenter(modelsPath);
		}

		System.out.println("Segment file:\t" + input);
		List<String> dataLines = null;
		BufferedWriter bw = null;
		try {
			dataLines = FileUtils.readFile(input);
			bw = FileUtils.newUTF8BufferedWriterFromNewFile(output);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (String line : dataLines) {
			try {
				bw.write(segmenter.segment(line));
				bw.newLine();
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param inputFolder
	 * @param inExt
	 * @param outputFolder
	 * @param outExt
	 */
	private static void segmentFolder(String inputFolder, String inExt, String outputFolder, String outExt) {
		File inputFol = new File(inputFolder);
		File outputFol = new File(outputFolder);

		if (!inputFol.isDirectory()) {
			OldLogging.error(inputFolder + " is not a valid directory.");
			return;
		}

		if (!outputFol.isDirectory()) {
			OldLogging.error(outputFol + " is not a valid directory.");
			return;
		}

		File[] inFiles = inputFol.listFiles();

		for (File inFile : inFiles) {
			String filePath = inFile.getPath();
			if (inFile.isFile()) {
				if (inExt.equals("*") || filePath.endsWith(inExt)) {
					String fileName = inFile.getName();
					String outFilePath = outputFol.getPath() + File.separator + fileName + "." + outExt;
					segmentFile(filePath, outFilePath);
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int length = args.length;

		if (length != 6 && length != 8 && length != 10) {
			showHelp();
			return;
		}

		if (!(args[0].equals("-m") && args[2].equals("-i"))) {
			showHelp();
			return;
		}

		modelsPath = args[1];

		if (length == 10) {
			if (!(args[4].equals("-ie") && args[6].equals("-o") && args[8].equals("-oe"))) {
				showHelp();
				return;
			}
			segmentFolder(args[3], args[5], args[7], args[9]);
		}

		if (length == 6) {
			if (!args[4].equals("-o")) {
				showHelp();
				return;
			}

			File inFile = new File(args[3]);
			File outFile = new File(args[5]);

			if (inFile.isFile()) {
				segmentFile(args[3], args[5]);
			} else if (inFile.isDirectory() && outFile.isDirectory()) {
				segmentFolder(args[3], "*", args[5], "seg");
			} else {
				showHelp();
				return;
			}
		}

		if (length == 8) {
			if (!(args[6].equals("-o") || args[6].equals("-oe"))) {
				showHelp();
				return;
			}

			if (args[6].equals("-o")) {
				if (!args[4].equals("-ie")) {
					showHelp();
					return;
				}

				File inFile = new File(args[3]);
				File outFile = new File(args[7]);

				if (inFile.isDirectory() && outFile.isDirectory()) {
					segmentFolder(args[3], args[5], args[7], "seg");
				} else {
					showHelp();
					return;
				}
			}

			if (args[6].equals("-oe")) {
				if (!args[4].equals("-o")) {
					showHelp();
					return;
				}

				File inFile = new File(args[3]);
				File outFile = new File(args[5]);

				if (inFile.isFile() && outFile.isDirectory()) {
					segmentFile(args[3], outFile.getPath() + File.separator + inFile.getName() + "." + args[7]);
				} else if (inFile.isDirectory() && outFile.isDirectory()) {
					segmentFolder(args[3], "*", args[5], args[7]);
				} else {
					showHelp();
					return;
				}
			}
		}

	}

	protected static void showHelp() {
		System.out.println("Method for word segmentation. Needed arguments:\n");

		System.out.println(
				"-m <models_path> -i <input_path> [-ie <input_extension>] -o <output_path> [-oe <output_extension>]"
						+ "\n");

		System.out.println("\t" + "-m" + "\t" + ":" + "\t" + "path to the folder of segmenter models (required)");
		System.out.println("\t" + "-i" + "\t" + ":" + "\t" + "path to the input text (file/folder) (required)");
		System.out.println("\t" + "-ie" + "\t" + ":" + "\t"
				+ "input extension, only use when input_path is a folder (default: *)");
		System.out.println("\t" + "-o" + "\t" + ":" + "\t" + "path to the output text (file/folder) (required)");
		System.out.println("\t" + "-oe" + "\t" + ":" + "\t"
				+ "output extension, only use when output_path is a folder (default: seg)");

		System.out.println();
	}

}
