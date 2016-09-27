package vn.edu.vnu.uet.nlp.segmenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.FeatureNode;
import vn.edu.vnu.uet.liblinear.Linear;
import vn.edu.vnu.uet.liblinear.Model;
import vn.edu.vnu.uet.liblinear.Parameter;
import vn.edu.vnu.uet.liblinear.Problem;
import vn.edu.vnu.uet.liblinear.SolverType;
import vn.edu.vnu.uet.nlp.segmenter.measurement.F1Score;
import vn.edu.vnu.uet.nlp.tokenizer.StringConst;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

/**
 * The segmentation system contains three components: Longest matching, Logistic
 * regression, Post-processing.
 * 
 * @author tuanphong94
 *
 */
public class SegmentationSystem {
	private static double r = 0.33;
	private Problem problem;
	private Parameter parameter;
	private Model model;
	private FeatureExtractor fe;
	private String pathToSave = "models";
	private int n; // number of unique features, used as parameter of LIBLINEAR
	private List<SyllabelFeature> segmentList;
	private int N1 = 0, N2 = 0, N3 = 0; // use for evaluation
	private double[] confidences;

	// Constructor for TRAINING
	public SegmentationSystem(FeatureExtractor _fe, String pathToSave) {
		this.problem = new Problem();
		this.parameter = new Parameter(SolverType.L2R_LR, 1.0, 0.01);
		this.model = new Model();
		this.fe = _fe;
		this.pathToSave = pathToSave;
		// Create folder if not exist
		File file = new File(pathToSave);
		if (!file.exists()) {
			file.mkdirs();
		}

		this.n = fe.getFeatureMap().getSize();
	}

	// Constructor for TESTING and SEGMENTING
	public SegmentationSystem(String folderpath) throws ClassNotFoundException, IOException {
		problem = new Problem();
		parameter = new Parameter(SolverType.L2R_LR, 1.0, 0.01);
		load(folderpath);
	}

	// Convert to problem's format of LIBLINEAR
	private void setProblem() {
		int numSamples = fe.getNumSamples();
		FeatureNode[][] x = new FeatureNode[numSamples][];
		double[] y = new double[numSamples];

		SortedSet<Integer> featSet;

		int sampleNo = 0;

		for (int s = 0; s < fe.getNumSents(); s++) {
			for (int i = 0; i < fe.getSegmentList().get(s).size(); i++) {
				y[sampleNo] = fe.getSegmentList().get(s).get(i).getLabel();

				featSet = fe.getSegmentList().get(s).get(i).getFeatset();

				x[sampleNo] = new FeatureNode[featSet.size()];

				int cnt = 0;
				for (Integer t : featSet) {
					x[sampleNo][cnt] = new FeatureNode(
							t + 1 /* Feature index must be greater than 0 */, 1.0);
					cnt++;
				}

				// free the memory
				featSet.clear();
				sampleNo++;
			}
		}

		problem.l = numSamples;
		problem.n = n;
		problem.y = y;
		problem.x = x;
		problem.bias = -1;
	}

	/**
	 * Training the logistic regression classifier.
	 */
	public void train() {
		OldLogging.info("saving feature map");
		saveMap();

		OldLogging.info("clear the map to free memory");
		fe.clearMap();

		OldLogging.info("setting up the problem");
		setProblem();

		OldLogging.info("start training");
		model = Linear.train(problem, parameter);
		OldLogging.info("finish training");

		OldLogging.info("saving model");
		saveModel();

		OldLogging.info("finish.");
	}

	/**
	 * @param sentences
	 *            A list of word-segmented sentences
	 * @return F1Score
	 * @throws IOException
	 */
	public F1Score test(List<String> sentences) throws IOException {
		int sentCnt = 0;
		N1 = 0; // number of words recognized by the system
		N2 = 0; // number of words in the manually segmented text
		N3 = 0; // number of right segmented words.

		// create log file
		try {
			File fol = new File("log");
			fol.mkdir();
		} catch (Exception e) {
			// do nothing
		}
		String logName = "log/log_test_" + new Date() + ".txt";
		BufferedWriter bw = FileUtils.newUTF8BufferedWriterFromNewFile(logName);
		// finish creating log file

		int sentID = 1;
		for (String sentence : sentences) {
			sentence = Normalizer.normalize(sentence, Form.NFC);
			if (testSentence(sentence)) {
				sentCnt++;
			}

			else {
				bw.write("\n-----Sent " + (sentID) + "-----\n" + sentence + "\n" + segment(sentence) + "\n\n");
				bw.flush();
			}
			sentID++;
		}

		bw.close();

		F1Score result = new F1Score(N1, N2, N3);

		double pre = result.getPrecision();
		double rec = result.getRecall();
		double f_measure = result.getF1Score();

		OldLogging.info("\n" + "Number of words recognized by the system:\t\t\t\tN1 = " + N1
				+ "\nNumber of words in reality appearing in the corpus:\t\tN2 = " + N2
				+ "\nNumber of words that are correctly recognized by the system:\tN3 = " + N3 + "\n");
		OldLogging.info("Precision\t\tP = N3/N1\t\t=\t" + pre + "%");
		OldLogging.info("Recall\t\tR = N3/N2\t\t=\t" + rec + "%");
		OldLogging.info("\nF-Measure\t\tF = (2*P*R)/(P+R)\t=\t" + f_measure + "%\n");

		OldLogging.info("\nNumber of sentences:\t" + sentences.size());
		OldLogging.info("Sentences right:\t\t" + sentCnt);
		OldLogging.info("\nSentences right accuracy:\t" + (double) sentCnt / (double) sentences.size() * 100.0 + "%");

		OldLogging.info("\nLogged wrong predictions to " + logName);

		return result;
	}

	/**
	 * @param sentence
	 *            A word-segmented sentence
	 * @return Whether the system can produce a right segmented sentence or not.
	 */
	private boolean testSentence(String sentence) {
		boolean sentCheck = true;

		fe.clearList();
		segmentList = new ArrayList<SyllabelFeature>();
		List<SyllabelFeature> sylList = fe.extract(sentence, Configure.TEST);

		// No feature set returned
		if (fe.getSegmentList().isEmpty()) {
			return true;
		}

		if (fe.getSegmentList().get(0).isEmpty()) {

			if (sylList.size() == 2 * Configure.WINDOW_LENGTH + 1) { // Sentences
																		// has
																		// only
																		// one
																		// token
				N1++;
				N2++;
				N3++;

				return true;

			} else // Sentence is empty
				return true;
		}

		for (int i = Configure.WINDOW_LENGTH; i < sylList.size() - Configure.WINDOW_LENGTH; i++) {
			segmentList.add(sylList.get(i));
		}

		sylList.clear();

		int size = segmentList.size() - 1;

		double[] reality = new double[size];

		double[] predictions = new double[size];

		confidences = new double[size];

		for (int i = 0; i < size; i++) {
			reality[i] = segmentList.get(i).getLabel();
			confidences[i] = Double.MIN_VALUE;
		}

		// Convert features to FeatureNode structure of LIBLINEAR
		setProblem();

		// Processing the prediction
		process(predictions, size, Configure.TEST);

		// Get the comparision
		boolean previousSpaceMatch = true;

		for (int i = 0; i < size; i++) {
			if (reality[i] == Configure.SPACE) {
				N2++;
			}

			if (predictions[i] == Configure.SPACE) {
				N1++;
				if (reality[i] == Configure.SPACE) {
					if (previousSpaceMatch) {
						N3++;
					}
					previousSpaceMatch = true;
				}
			}

			if (predictions[i] != reality[i]) {
				sentCheck = false;
				previousSpaceMatch = false;
			}
		}
		// The last word of sentence
		N1++;
		N2++;
		if (previousSpaceMatch) {
			N3++;
		}

		return sentCheck;

	}

	/**
	 * @param sentence
	 *            A tokenized sentence
	 * @return The word-segmented sentence
	 */
	public String segment(String sentence) {
		fe.clearList();
		segmentList = new ArrayList<SyllabelFeature>();
		List<SyllabelFeature> sylList = fe.extract(sentence, Configure.TEST);

		// No feature set returned
		if (fe.getSegmentList().isEmpty()) {
			return "";
		}

		if (fe.getSegmentList().get(0).isEmpty()) {

			if (sylList.size() == 2 * Configure.WINDOW_LENGTH + 1) { // Sentences
																		// has
																		// only
																		// one
																		// token
				return sylList.get(Configure.WINDOW_LENGTH).getSyllabel();

			} else // Sentence is empty
				return "";
		}

		for (int i = Configure.WINDOW_LENGTH; i < sylList.size() - Configure.WINDOW_LENGTH; i++) {
			segmentList.add(sylList.get(i));
		}

		sylList.clear();

		int size = segmentList.size() - 1;

		double[] predictions = new double[size];

		confidences = new double[size];

		for (int i = 0; i < size; i++) {
			confidences[i] = Double.MIN_VALUE;
		}

		// Convert features to FeatureNode structure of LIBLINEAR
		setProblem();

		// Processing the prediction
		process(predictions, size, Configure.PREDICT);

		// Get the result
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < size; i++) {
			sb.append(segmentList.get(i).getSyllabel());

			if (predictions[i] == Configure.SPACE)
				sb.append(StringConst.SPACE);
			else
				sb.append(StringConst.UNDERSCORE);
		}
		sb.append(segmentList.get(size).getSyllabel());

		// Clear the list of FeatureExtractor
		// and clear the problem
		// to prepare for the next segmentation
		delProblem();

		return sb.toString().trim();
	}

	private void process(double[] predictions, int size, int mode) {
		// Longest matching for over2-syllable words
		longestMatching(predictions, size, mode);

		// Logistic regression
		logisticRegression(predictions, size, mode);

		// Other post processing
		postProcessing(predictions, size, mode);
	}

	// Longest matching for over-2-syllable words
	private void longestMatching(double[] predictions, int size, int mode) {
		for (int i = 0; i < size; i++) {
			if (segmentList.get(i).getType() == SyllableType.OTHER)
				continue;
			for (int n = 6; n >= 2; n--) {
				StringBuilder sb = new StringBuilder();
				boolean hasUpper = false;
				boolean hasLower = false;
				int j = i;
				for (j = i; j < i + n; j++) {

					if (j >= segmentList.size() || segmentList.get(j).getType() == SyllableType.OTHER) {
						break;
					}

					if (mode != Configure.TEST) {
						if (segmentList.get(j).getType() == SyllableType.UPPER
								|| segmentList.get(j).getType() == SyllableType.ALLUPPER) {
							hasUpper = true;
						}
					}

					if (segmentList.get(j).getType() == SyllableType.LOWER) {
						hasLower = true;
					}

					sb.append(" " + segmentList.get(j).getSyllabel());
				}

				if (j == i + n) {
					String word = sb.toString();

					if (n > 2 && hasLower && Dictionary.inVNDict(word)) {

						for (int k = i; k < j - 1; k++) {
							predictions[k] = Configure.UNDERSCORE;
						}

						i = j - 1;
						break;
					}

					if (mode != Configure.TEST) {
						if (hasUpper && RareNames.isRareName(word)) {

							for (int k = i; k < j - 1; k++) {
								predictions[k] = Configure.UNDERSCORE;
							}

							i = j - 1;
							break;
						}
					}
				}
			}
		}

		// ruleForProperName(predictions, size, mode);
	}

	@SuppressWarnings("unused")
	private void ruleForProperName(double[] predictions, int size, int mode) {
		double[] temp = new double[size];
		for (int i = 0; i < size; i++) {
			temp[i] = predictions[i];
		}
		for (int i = 0; i < size; i++) {
			if (temp[i] != Configure.UNDERSCORE) {
				if ((i == 0 || temp[i - 1] != Configure.UNDERSCORE)
						&& (i == size - 1 || temp[i + 1] != Configure.UNDERSCORE)) {

					// Dictionary
					if (segmentList.get(i).getType() == SyllableType.UPPER
							&& segmentList.get(i + 1).getType() == SyllableType.UPPER) {
						predictions[i] = Configure.UNDERSCORE;
						continue;
					}
				}
			}
		}
	}

	private void logisticRegression(double[] predictions, int size, int mode) {
		double[] temp = new double[size];
		for (int i = 0; i < size; i++) {
			temp[i] = predictions[i];
		}
		for (int i = 0; i < size; i++) {
			if (temp[i] != Configure.UNDERSCORE) {
				if ((i == 0 || temp[i - 1] != Configure.UNDERSCORE)
						&& (i == size - 1 || temp[i + 1] != Configure.UNDERSCORE)) {
					// Machine Learning
					predictions[i] = predict(problem.x[i], i, mode);
				}
			}
		}
	}

	// Logistic regression classifier
	private double predict(Feature[] featSet, int sampleNo, int mode) {

		double[] dec_values = new double[model.getNrClass()];
		double result = Linear.predict(model, featSet, dec_values);

		confidences[sampleNo] = dec_values[0];

		return result;
	}

	// Post-processing
	private void postProcessing(double[] predictions, int size, int mode) {
		if (size < 2) {
			return;
		}

		for (int i = 0; i < size - 1; i++) {
			double sigm = sigmoid(confidences[i]);

			SyllabelFeature preSyl = (i > 0) ? segmentList.get(i - 1) : null;
			SyllabelFeature thisSyl = segmentList.get(i);
			SyllabelFeature nextSyl = segmentList.get(i + 1);

			// Xu ly tu co 2 am tiet confidence thap
			if ((i == 0 || predictions[i - 1] == Configure.SPACE) && predictions[i + 1] == Configure.SPACE) {
				// confidence thap
				if (Math.abs(sigm - 0.5) < r) {
					String thisOne = thisSyl.getSyllabel();
					String nextOne = nextSyl.getSyllabel();

					// LOWER cases
					if ((preSyl == null && nextSyl.getType() == SyllableType.LOWER)
							|| ((thisSyl.getType() == SyllableType.LOWER || thisSyl.getType() == SyllableType.UPPER)
									&& (nextSyl.getType() == SyllableType.LOWER))) {

						String word1 = thisOne + " " + nextOne;

						if (Dictionary.inVNDict(word1)) {
							predictions[i] = Configure.UNDERSCORE;
						}

						if (!(Dictionary.inVNDict(word1))) {
							predictions[i] = Configure.SPACE;
						}
					}
				}
				continue;
			}

			// xu ly nhap nhang tu co 3 am tiet ko co trong tu dien
			if (predictions[i] == Configure.UNDERSCORE) {
				if ((i == 0 || predictions[i - 1] == Configure.SPACE) && predictions[i + 1] == Configure.UNDERSCORE
						&& (i == size - 2 || predictions[i + 2] == Configure.SPACE)) {

					// check dieu kien ton tai it nhat 1 lower
					int j = i;
					boolean flag = false;

					for (j = i; j < i + 3; j++) {

						if (j == 0 && (segmentList.get(j).getType() == SyllableType.LOWER
								|| segmentList.get(j).getType() == SyllableType.UPPER)) {
							continue;
						}

						if (segmentList.get(j).getType() == SyllableType.LOWER) {
							flag = true;
						}

						if (!(segmentList.get(j).getType() == SyllableType.LOWER
								|| segmentList.get(j).getType() == SyllableType.UPPER)) {
							break;
						}
					}

					// thoa man dieu kien ton tai it nhat 1 lower syllable
					if (j == i + 3 && flag) {
						String word = segmentList.get(i).getSyllabel() + " " + segmentList.get(i + 1).getSyllabel()
								+ " " + segmentList.get(i + 2).getSyllabel();
						// khong co trong tu dien
						if (!Dictionary.inVNDict(word) && !RareNames.isRareName(word)) {
							String leftWord = segmentList.get(i).getSyllabel() + " "
									+ segmentList.get(i + 1).getSyllabel();
							String rightWord = segmentList.get(i + 1).getSyllabel() + " "
									+ segmentList.get(i + 2).getSyllabel();

							// s1s2 in dict, s2s3 not in dict
							if (Dictionary.inVNDict(leftWord) && !Dictionary.inVNDict(rightWord)) {
								predictions[i + 1] = Configure.SPACE;
							}

							// s1s2 not in dict, s2s3 in dict
							if (Dictionary.inVNDict(rightWord) && !Dictionary.inVNDict(leftWord)) {
								predictions[i] = Configure.SPACE;
							}

							// both in dict
							if (Dictionary.inVNDict(rightWord) && Dictionary.inVNDict(leftWord)) {
								if (confidences[i] * confidences[i + 1] > 0) {
									if (Math.abs(confidences[i]) < Math.abs(confidences[i + 1])) {
										predictions[i] = Configure.SPACE;
									} else {
										predictions[i + 1] = Configure.SPACE;
									}
								}
							}

							// none of them in dict
							if (!Dictionary.inVNDict(rightWord) && !Dictionary.inVNDict(leftWord)) {

								if (segmentList.get(i).getType() == SyllableType.LOWER
										|| segmentList.get(i + 1).getType() == SyllableType.LOWER) {
									predictions[i] = Configure.SPACE;
								}

								if (segmentList.get(i + 2).getType() == SyllableType.LOWER
										|| segmentList.get(i + 1).getType() == SyllableType.LOWER) {
									predictions[i + 1] = Configure.SPACE;
								}
							}

						}

						i = i + 2;
					}
				}
			}
		}
	}

	private void delProblem() {
		problem = new Problem();
		fe.clearList();
		if (segmentList != null) {
			segmentList.clear();
		}
	}

	private void saveMap() {
		String mapFile = pathToSave + File.separator + "features";
		fe.saveMap(mapFile);
	}

	private void saveModel() {
		File modelFile = new File(pathToSave + File.separator + "model");

		try {
			model.save(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void load(String path) throws ClassNotFoundException, IOException {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		String modelPath = path + File.separator + "model";
		String featMapPath = path + File.separator + "features";

		File modelFile = new File(modelPath);

		model = Model.load(modelFile);
		fe = new FeatureExtractor(featMapPath);
		pathToSave = path;
		n = fe.getFeatureMap().getSize();
	}

	public FeatureExtractor getFeatureExactor() {
		return this.fe;
	}

	private double sigmoid(double d) {
		return 1 / (1 + Math.exp(-d));
	}

	public void setR(double r) {
		if (r < 0 || r > 0.5) {
			return;
		}
		SegmentationSystem.r = r;
	}
}
