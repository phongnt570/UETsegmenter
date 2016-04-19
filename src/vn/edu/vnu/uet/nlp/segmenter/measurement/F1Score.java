package vn.edu.vnu.uet.nlp.segmenter.measurement;

public class F1Score {
	private double P;
	private double R;
	private double F;

	public F1Score() {
		P = 0;
		R = 0;
		F = 0;
	}

	public F1Score(int N1, int N2, int N3) {
		if (N1 == 0 || N2 == 0) {
			new F1Score();
		} else {
			P = ((double) N3 / (double) N1) * 100.0;
			R = ((double) N3 / (double) N2) * 100.0;
			F = (2 * P * R) / (P + R);
		}
	}

	public double getPrecision() {
		return this.P;
	}

	public double getRecall() {
		return this.R;
	}

	public double getF1Score() {
		return this.F;
	}

	public String toString() {
		return P + "%" + "\t" + R + "%" + "\t" + F + "%";
	}
}
