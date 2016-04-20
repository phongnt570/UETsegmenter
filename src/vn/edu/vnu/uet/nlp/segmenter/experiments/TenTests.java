package vn.edu.vnu.uet.nlp.segmenter.experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.segmenter.measurement.F1Score;
import vn.edu.vnu.uet.nlp.utils.FileUtils;

public class TenTests {

	public static void main(String[] args) throws IOException {

		if (args.length > 0) {

			BufferedWriter bw = FileUtils.newUTF8BufferedWriterFromNewFile("parameter.txt");
			double R = 0.00;
			for (int k = 0; k < 51; k++) {
				F1Score[] FScores = new F1Score[10];

				for (int i = 0; i < 10; i++) {
					System.out.println("--------------------fold " + i + "--------------------");

					System.out.println("loading model...");
					SegmentationSystem machine = null;
					try {
						machine = new SegmentationSystem("testmodels/models_" + i);
						machine.setR(R);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					System.out.println("testing...");
					List<String> dataLines = FileUtils.readFile("data/test_" + i + ".txt");

					FScores[i] = machine.test(dataLines);
				}

				for (F1Score s : FScores) {
					System.out.println(s);
				}

				bw.write("R = " + R + "\n");
				double averagePre = 0;
				double averageRe = 0;
				double averageF = 0;
				for (int i = 0; i < 10; i++) {
					bw.write(FScores[i].toString());
					bw.newLine();

					averagePre += FScores[i].getPrecision();
					averageRe += FScores[i].getRecall();
					averageF += FScores[i].getF1Score();
				}
				bw.write(averagePre / 10 + "\t" + averageRe / 10 + "\t" + averageF / 10);

				bw.write("\n\n\n");
				bw.flush();

				R += 0.01;
			}
		} else {
			F1Score[] FScores = new F1Score[10];

			for (int i = 0; i < 10; i++) {
				System.out.println("--------------------fold " + i + "--------------------");

				System.out.println("loading model...");
				SegmentationSystem machine = null;
				try {
					machine = new SegmentationSystem("testmodels/models_" + i);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				System.out.println("testing...");
				List<String> dataLines = FileUtils.readFile("data/test_" + i + ".txt");

				FScores[i] = machine.test(dataLines);
			}

			for (F1Score s : FScores) {
				System.out.println(s);
			}
		}
	}
}
