package vn.edu.vnu.uet.nlp.segmenter;

/**
 * Consist of the syllable, its type and the label of the white space next to
 * it.
 * 
 * @author tuanphong94
 *
 */
public class SyllabelFeature {
	private String syllabel;
	private SyllableType type;
	private int label;

	public SyllabelFeature(String _syllabel, SyllableType _type, int _label) {
		this.syllabel = _syllabel;
		this.type = _type;
		this.label = _label;
	}

	public int getLabel() {
		return label;
	}

	public String getSyllabel() {
		return syllabel;
	}

	public SyllableType getType() {
		return type;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "(" + this.syllabel + ", " + this.type + ", " + this.label + ")";
	}
}
