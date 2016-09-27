package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.tokenizer.Tokenizer;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * Demo: segmentation for text in file.
 * 
 * @author tuanphong94
 *
 */
public class Segment {

	public static void main(String[] args) throws IOException {
		String filename = "/home/tuanphong94/workspace/TPSegmenter/raw/text.txt";

		List<String> dataLines = FileUtils.readFile(filename);

		OldLogging.info("loading model...");
		SegmentationSystem machine = null;
		try {
			machine = new SegmentationSystem("models");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		OldLogging.info("segmenting...");
		List<String> tokens = new ArrayList<String>();
		List<String> sentences = new ArrayList<String>();

		int numTokens = 0;
		int numSentences = 0;

		String outfile = filename.substring(0, filename.lastIndexOf('.')) + ".seg";
		BufferedWriter bw = FileUtils.newUTF8BufferedWriterFromNewFile(outfile);

		long start_time = System.nanoTime();
		for (String line : dataLines) {
			if (line.isEmpty() || line == null)
				continue;
			tokens = Tokenizer.tokenize(Normalizer.normalize(line, Form.NFC));
			sentences = Tokenizer.joinSentences(tokens);

			for (String sentence : sentences) {
				System.out.println(machine.segment(sentence));
				bw.write(machine.segment(sentence));
				bw.newLine();
				bw.flush();
			}

			numTokens += tokens.size();
			numSentences += sentences.size();

			tokens.clear();
			sentences.clear();
		}
		long end_time = System.nanoTime();

		bw.close();

		double difference = (end_time - start_time) / 1e9;

		OldLogging.info("Tokens: " + numTokens);
		OldLogging.info("Sentences: " + numSentences);
		OldLogging.info("Time: " + difference + " seconds");
		OldLogging.info("Rate: " + numTokens / difference + " tokens/sec");
	}

}
