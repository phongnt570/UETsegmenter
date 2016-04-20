package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.edu.vnu.uet.nlp.tokenizer.StringConst;
import vn.edu.vnu.uet.nlp.tokenizer.Tokenizer;

/**
 * The main class provides APIs for word segmentation
 * 
 * @author tuanphong94
 */
public class UETSegmenter {

	private SegmentationSystem machine = null;

	public UETSegmenter() {
		this("models");
	}

	public UETSegmenter(String modelpath) {
		if (machine == null) {
			try {
				machine = new SegmentationSystem(modelpath);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param str
	 *            A tokenized text
	 * @return Segmented text
	 */
	public String segmentTokenizedText(String str) {
		StringBuffer sb = new StringBuffer();

		List<String> tokens = new ArrayList<String>();
		List<String> sentences = new ArrayList<String>();

		tokens.addAll(Arrays.asList(str.split("\\s+")));
		sentences = Tokenizer.joinSentences(tokens);

		for (String sentence : sentences) {
			sb.append((machine.segment(sentence)));
			sb.append(StringConst.SPACE);
		}

		tokens.clear();
		sentences.clear();

		return sb.toString().trim();
	}

	/**
	 * @param str
	 *            A raw text
	 * @return Segmented text
	 */
	public String segment(String str) {
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
	public List<String> segmentSentences(String corpus) {
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
	
	/**
	 * Change threshold r for post-processing
	 * 
	 * @param r
	 *            threshold r
	 */
	public void setR(double r) {
		this.machine.setR(r);
	}

}
