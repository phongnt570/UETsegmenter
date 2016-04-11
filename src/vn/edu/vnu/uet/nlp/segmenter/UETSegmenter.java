package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vnu.uet.nlp.tokenizer.Tokenizer;
import vn.edu.vnu.uet.nlp.tokenizer.StringConst;
import vn.edu.vnu.uet.nlp.utils.Logging;

/**
 * The main class provides API for word segmentation
 * 
 * @author tuanphong94
 */
public class UETSegmenter {

	private SegmentationSystem machine = null;

	public static void main(String[] args) {
		UETSegmenter segmenter = new UETSegmenter();

		String test = "  Tất cả   học sinh  tiểu học được nghỉ\t học thứ Bảy và Chủ nhật.\n    Học sinh học sinh học. Tốc độ truyền thông tin ngày càng cao.   ";

		System.out.println(segmenter.segment(test));

		List<String> sents = segmenter.segmentCorpus(test);

		for (String sent : sents) {
			System.out.println(sent);
		}
	}

	private UETSegmenter() {
		new UETSegmenter("models");
	}

	public UETSegmenter(String modelpath) {
		if (machine == null) {
			Logging.info("Loading segmenter's model...");
			try {
				machine = new SegmentationSystem(modelpath);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param str
	 *            A tokenized text
	 * @return Segmented text
	 */
	public String segmentTokenizedSentence(String str) {
		if (machine == null) {
			Logging.error("You must construct the TPSegmenter with model path first!!!");
			System.exit(0);
		}

		return machine.segment(str);
	}

	/**
	 * @param str
	 *            A raw text
	 * @return Segmented text
	 */
	public String segment(String str) {
		if (machine == null) {
			Logging.error("You must construct the TPSegmenter with model path first!!!");
			System.exit(0);
		}

		StringBuffer sb = new StringBuffer();

		List<String> tokens = new ArrayList<String>();
		List<String> sentences = new ArrayList<String>();

		try {
			tokens = Tokenizer.tokenize(str);
			sentences = Tokenizer.joinSentences(tokens);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String sentence : sentences) {
			sb.append((machine.segment(sentence)));
			sb.append(StringConst.SPACE);
		}

		tokens.clear();
		sentences.clear();

		return sb.toString().trim();
	}

	/**
	 * @param corpus
	 *            A raw text
	 * @return List of segmented sentences
	 */
	public List<String> segmentCorpus(String corpus) {
		if (machine == null) {
			Logging.error("You must construct the TPSegmenter with model path first!!!");
			System.exit(0);
		}

		List<String> result = new ArrayList<String>();

		List<String> tokens = new ArrayList<String>();
		List<String> sentences = new ArrayList<String>();

		try {
			tokens = Tokenizer.tokenize(corpus);
			sentences = Tokenizer.joinSentences(tokens);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String sentence : sentences) {
			result.add(machine.segment(sentence));
		}

		tokens.clear();
		sentences.clear();

		return result;
	}

}
