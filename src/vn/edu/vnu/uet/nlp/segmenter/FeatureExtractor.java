package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import vn.edu.vnu.uet.nlp.tokenizer.Regex;
import vn.edu.vnu.uet.nlp.tokenizer.StringConst;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * The class for extracting features using in logistic regression.
 * 
 * @author tuanphong94
 *
 */
public class FeatureExtractor {
	private FeatureMap featureMap;
	private List<List<SegmentFeature>> listOfSegmentFeatureLists;

	private static Map<String, String> normalizationMap;
	private static Set<String> normalizationSet;

	static {
		normalizationMap = new HashMap<String, String>();
		normalizationMap.put("òa", "oà");
		normalizationMap.put("óa", "oá");
		normalizationMap.put("ỏa", "oả");
		normalizationMap.put("õa", "oã");
		normalizationMap.put("ọa", "oạ");
		normalizationMap.put("òe", "oè");
		normalizationMap.put("óe", "oé");
		normalizationMap.put("ỏe", "oẻ");
		normalizationMap.put("õe", "oẽ");
		normalizationMap.put("ọe", "oẹ");
		normalizationMap.put("ùy", "uỳ");
		normalizationMap.put("úy", "uý");
		normalizationMap.put("ủy", "uỷ");
		normalizationMap.put("ũy", "uỹ");
		normalizationMap.put("ụy", "uỵ");
		normalizationMap.put("Ủy", "Uỷ");

		normalizationSet = normalizationMap.keySet();
	}

	public FeatureExtractor() {
		listOfSegmentFeatureLists = new ArrayList<List<SegmentFeature>>();
		featureMap = new FeatureMap();
	}

	public FeatureExtractor(String featMapPath) throws ClassNotFoundException, IOException {
		listOfSegmentFeatureLists = new ArrayList<List<SegmentFeature>>();
		featureMap = new FeatureMap();
		loadMap(featMapPath);
	}

	public void extract(List<String> sentences, int mode) {
		for (int i = 0; i < sentences.size(); i++) {
			extract(sentences.get(i), mode);
			if (i % 1000 == 999 || i == sentences.size() - 1) {
				OldLogging.info((i + 1) + " sentences extracted to features");
			}

		}
	}

	public List<SyllabelFeature> extract(String sentence, int mode) {
		List<SyllabelFeature> sylList = convertToFeatureOfSyllabel(sentence, mode);
		int length = sylList.size();

		if (length == 0) {
			return null;
		}

		SortedSet<Integer> indexSet = new TreeSet<>();
		String featureName;

		List<SegmentFeature> segfeats = new ArrayList<SegmentFeature>();

		for (int i = Configure.WINDOW_LENGTH; i < length - Configure.WINDOW_LENGTH - 1; i++) {
			// ------- start feature selection -------

			// 1-gram
			for (int j = i - Configure.WINDOW_LENGTH; j <= i + Configure.WINDOW_LENGTH; j++) {
				// 1-gram for syllable
				featureName = (j - i) + "|" + sylList.get(j).getSyllabel().toLowerCase();
				indexSet.add(featureMap.getIndex(featureName, mode));

				// 1-gram for type
				if (sylList.get(j).getType() != SyllableType.LOWER) {
					featureName = (j - i) + ":" + sylList.get(j).getType();
					indexSet.add(featureMap.getIndex(featureName, mode));
				}

			}

			// 2-gram
			for (int j = i - Configure.WINDOW_LENGTH; j < i + Configure.WINDOW_LENGTH; j++) {
				// 2-gram for syllable
				featureName = (j - i) + "||" + sylList.get(j).getSyllabel().toLowerCase() + " "
						+ sylList.get(j + 1).getSyllabel().toLowerCase();
				indexSet.add(featureMap.getIndex(featureName, mode));

				// 2-gram for type
				if (sylList.get(j).getType() != SyllableType.LOWER) {
					featureName = (j - i) + "::" + sylList.get(j).getType() + " " + sylList.get(j + 1).getType();
					indexSet.add(featureMap.getIndex(featureName, mode));
				}
			}

			// 3-gram
			for (int j = i - Configure.WINDOW_LENGTH; j < i + Configure.WINDOW_LENGTH - 1; j++) {
				// 3-gram for type
				if (sylList.get(j).getType() != SyllableType.LOWER) {
					featureName = (j - i) + ":::" + sylList.get(j).getType() + " " + sylList.get(j + 1).getType() + " "
							+ sylList.get(j + 2).getType();
					indexSet.add(featureMap.getIndex(featureName, mode));
				}
			}

			String thisSyl = sylList.get(i).getSyllabel().toLowerCase();
			String nextSyl = sylList.get(i + 1).getSyllabel().toLowerCase();

			// current syllable and next syllable are UPPER
			if (sylList.get(i).getType() == SyllableType.UPPER && sylList.get(i + 1).getType() == SyllableType.UPPER) {

				// only one of them is a valid Vietnamese syllable
				if ((SyllableList.isVNsyl(thisSyl) && !SyllableList.isVNsyl(nextSyl))
						|| (SyllableList.isVNsyl(nextSyl) && !SyllableList.isVNsyl(thisSyl))) {
					featureName = "(0:vi&&1:en)||(0:en&&1:vi)";
					indexSet.add(featureMap.getIndex(featureName, mode));
				}

				// current syllable is Vietnamese family name
				if (FamilyName.isVNFamilyName(thisSyl)) {
					featureName = "0.isVNFamilyName";
					indexSet.add(featureMap.getIndex(featureName, mode));
				}
			}

			// reduplicative words
			if (sylList.get(i).getType() == SyllableType.LOWER && sylList.get(i + 1).getType() == SyllableType.LOWER) {
				if (thisSyl.equalsIgnoreCase(nextSyl)) {
					featureName = "0&&1.reduplicativeword";
					indexSet.add(featureMap.getIndex(featureName, mode));
				}
			}
			// ------- end of feature selection -------

			// add label and feature vector to the list
			if (indexSet.size() > 0) {
				segfeats.add(new SegmentFeature(sylList.get(i).getLabel(), indexSet));
			}

			// free the memory
			featureName = "";
			indexSet.clear();
		}

		listOfSegmentFeatureLists.add(segfeats);

		return sylList;
	}

	public static List<SyllabelFeature> convertToFeatureOfSyllabel(String sentence, int mode) {
		String sent = sentence.trim();

		if (sent.equals(StringConst.SPACE) || sent.isEmpty()) {
			return new ArrayList<SyllabelFeature>();
		}

		for (int i = 0; i < Configure.WINDOW_LENGTH; i++)
			sent = StringConst.BOS + StringConst.SPACE + sent + StringConst.SPACE + StringConst.EOS;

		return token(sent, mode);
	}

	public static List<SyllabelFeature> token(String sent, int mode) {
		List<SyllabelFeature> list = new ArrayList<SyllabelFeature>();
		String[] tokens = sent.split("\\s+");

		if (mode == Configure.TRAIN || mode == Configure.TEST) {
			for (String token : tokens) {
				if (token.contains(StringConst.UNDERSCORE)) {
					String[] tmp = token.split(StringConst.UNDERSCORE);
					for (int i = 0; i < tmp.length - 1; i++) {
						String tmp_i = normalize(tmp[i]);
						list.add(new SyllabelFeature(tmp_i, typeOf(tmp_i), Configure.UNDERSCORE));
					}
					try {
						String tmp_last = normalize(tmp[tmp.length - 1]);
						list.add(new SyllabelFeature(tmp_last, typeOf(tmp_last), Configure.SPACE));
					} catch (Exception e) {
						// System.out.println(tmp[tmp.length - 1]);
					}
				} else {
					String tmp = normalize(token);
					list.add(new SyllabelFeature(tmp, typeOf(tmp), Configure.SPACE));
				}
			}
		} else {
			for (String token : tokens) {
				String tmp = normalize(token);
				list.add(new SyllabelFeature(tmp, typeOf(tmp), Configure.SPACE));
			}
		}

		return list;
	}

	public static String normalize(String token) {
		if (SyllableList.isVNsyl(token)) {
			return token;
		}
		for (String wrongTyping : normalizationSet) {
			if (token.contains(wrongTyping)) {
				token = token.replace(wrongTyping, normalizationMap.get(wrongTyping));
				break;
			}
		}
		return token;
	}

	public static SyllableType typeOf(String syllabel) {
		if (syllabel.equals(StringConst.BOS)) {
			return SyllableType.BOS;
		}

		if (syllabel.equals(StringConst.EOS)) {
			return SyllableType.EOS;
		}

		boolean upper = false;
		boolean lower = false;
		boolean num = false;
		boolean other = false;

		if (syllabel.matches("\\p{Upper}\\p{L}*\\.") || syllabel.matches("\\p{Upper}\\p{L}*-\\w+")) {
			return SyllableType.UPPER;
		}

		for (int i = 0; i < syllabel.length(); i++) {
			char character = syllabel.charAt(i);
			if (!Character.isLetterOrDigit(character)) {
				if (character == '.' || character == ',') {
					other = true;
					continue;
				}
				return SyllableType.OTHER;
			} else {
				if (Character.isDigit(character)) {
					num = true;
				} else {
					if (Character.isLowerCase(character)) {
						lower = true;
					} else {
						upper = true;
					}
				}
			}
		}

		if (num) {
			if (syllabel.matches(Regex.NUMBER))
				return SyllableType.NUMBER;
			else {
				return SyllableType.OTHER;
			}
		}
		if (other)
			return SyllableType.OTHER;

		if (lower) {
			if (upper) {
				return SyllableType.UPPER;
			} else {
				return SyllableType.LOWER;
			}
		}
		if (upper) {
			return SyllableType.ALLUPPER;
		}

		return SyllableType.OTHER;
	}

	protected FeatureMap getFeatureMap() {
		return this.featureMap;
	}

	protected void saveMap(String path) {
		try {
			featureMap.save(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void loadMap(String path) throws ClassNotFoundException, IOException {
		featureMap.load(path);
	}

	protected void clearList() {
		listOfSegmentFeatureLists.clear();
	}

	protected void clearMap() {
		featureMap.clear();
	}

	protected int getNumSents() {
		return this.listOfSegmentFeatureLists.size();
	}

	protected int getNumSamples() {
		int cnt = 0;
		for (int i = 0; i < listOfSegmentFeatureLists.size(); i++) {
			cnt += listOfSegmentFeatureLists.get(i).size();
		}
		return cnt;
	}

	protected List<List<SegmentFeature>> getSegmentList() {
		return this.listOfSegmentFeatureLists;
	}

	public int getFeatureMapSize() {
		return featureMap.getSize();
	}
}
