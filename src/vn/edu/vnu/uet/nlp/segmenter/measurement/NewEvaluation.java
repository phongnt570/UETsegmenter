package vn.edu.vnu.uet.nlp.segmenter.measurement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.UETSegmenter;

public class NewEvaluation {
	private static final String TEXT_DIR = "test";
	private static final String SEG_MODEL_DIR = TEXT_DIR + "/seg_model";
	private static final String SEG_HUMAN_DIR = TEXT_DIR + "/seg_human";

	private static int totalHumanCount, totalModelCount, totalMatchCount;
	private static int totalLines;

	private static int humanCount, modelCount, matchCount;

	// private static TPSegmenter segmenter = new TPSegmenter("models");
	private static UETSegmenter segmenter;

	public static void main(String args[]) {
		long initTime = init();
		System.out.println("Initialize time: " + initTime);

		System.out.println("--------------------\n");
		System.out.println("File\tLines\tHumanCount\tSystemCount\tMatchCount\t" + "Pre\tRecall\tF1");

		File textDir = new File(TEXT_DIR);
		for (String s : textDir.list()) {
			String fileName;
			try {
				fileName = s.substring(0, s.lastIndexOf('.'));
			} catch (Exception e) {
				continue;
			}
			process(fileName);

		}

		double precision = (double) totalMatchCount / totalModelCount;
		double recall = (double) totalMatchCount / totalHumanCount;
		double f1 = 2 * precision * recall / (precision + recall);
		System.out.println(
				"Total" + "\t" + totalLines + "\t" + totalHumanCount + "\t" + totalModelCount + "\t" + totalMatchCount +

		"\t" + precision + "\t" + recall + "\t" + f1);
	}

	private static long init() {
		long start = System.currentTimeMillis();

		totalHumanCount = totalModelCount = totalMatchCount = 0;
		totalLines = 0;

		return (System.currentTimeMillis() - start);
	}

	private static void process(String fileName) {
		try {
			String fold = fileName.substring(fileName.length() - 1);
			segmenter = new UETSegmenter("testmodels/models_" + fold);

			String textPath = TEXT_DIR + "/" + fileName + ".txt";
			BufferedReader reader = new BufferedReader(new FileReader(textPath));
			String line;

			int lineCount = 0;
			List<List<String>> segmentRes = new ArrayList<>();

			while ((line = reader.readLine()) != null) {
				segmentRes.add(Arrays.asList(segmenter.segmentTokenizedText(line).split("\\s+")));
				lineCount++;
			}

			reader.close();

			saveSegment(segmentRes, fileName);
			evaluate(segmentRes, fileName);

			double precision = (double) matchCount / modelCount;
			double recall = (double) matchCount / humanCount;
			double f1 = 2 * precision * recall / (precision + recall);
			System.out.println(fileName + "\t" + lineCount + "\t" + humanCount + "\t" + modelCount + "\t" + matchCount +

			"\t" + precision + "\t" + recall + "\t" + f1);

			totalLines += lineCount;
			totalHumanCount += humanCount;
			totalModelCount += modelCount;
			totalMatchCount += matchCount;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveSegment(List<List<String>> seg, String fileName) {
		try {
			StringBuilder res = new StringBuilder();
			for (List<String> line : seg) {
				StringBuilder sb = new StringBuilder();
				for (String word : line)
					sb.append(word + " ");
				res.append(sb.toString().trim() + "\n");
			}

			String outFilePath = SEG_MODEL_DIR + "/" + fileName + ".seg";
			FileWriter writer = new FileWriter(outFilePath);
			writer.write(res.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void evaluate(List<List<String>> modelSeg, String fileName) {
		try {
			humanCount = modelCount = matchCount = 0;

			String humanSeg = SEG_HUMAN_DIR + "/" + fileName + ".seg";
			BufferedReader reader = new BufferedReader(new FileReader(humanSeg));

			for (List<String> modelWords : modelSeg) {
				String line = reader.readLine();
				if (line == null)
					break;
				String[] humanWords = line.split("\\s+");

				humanCount += humanWords.length;
				modelCount += modelWords.size();
				matchCount += getMatchCount(humanWords, modelWords);
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int[] calculateIndex(String[] words) {
		int[] indexArr = new int[words.length];
		int index = 0;
		for (int i = 0; i < words.length; i++) {
			indexArr[i] = index;
			index += words[i].replace("_", "").length();
		}
		return indexArr;
	}

	public static int[] calculateIndex(List<String> words) {
		int[] indexArr = new int[words.size()];
		int index = 0;
		for (int i = 0; i < words.size(); i++) {
			indexArr[i] = index;
			index += words.get(i).replace("_", "").length();
		}
		return indexArr;
	}

	private static int getMatchCount(String[] humanWords, List<String> modelWords) {
		int[] humanIndex = calculateIndex(humanWords);
		int[] modelIndex = calculateIndex(modelWords);

		int matchCount = 0;
		for (int i = 0, j = 0; i < humanWords.length; i++) {
			while (j < modelIndex.length - 1 && humanIndex[i] > modelIndex[j])
				j++;
			if (humanIndex[i] == modelIndex[j])
				if (humanWords[i].equals(modelWords.get(j)))
					matchCount++;
		}

		return matchCount;
	}
}
