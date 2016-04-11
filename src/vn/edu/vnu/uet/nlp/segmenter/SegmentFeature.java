package vn.edu.vnu.uet.nlp.segmenter;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Consist of the label of white space and its feature vector.
 * 
 * @author tuanphong94
 *
 */
public class SegmentFeature {
	private int label;
	private SortedSet<Integer> featset;

	public SegmentFeature(int l, SortedSet<Integer> set) {
		setLabel(l);
		setFeatset(set);
	}

	public SortedSet<Integer> getFeatset() {
		return featset;
	}

	public int getLabel() {
		return label;
	}

	public void setFeatset(SortedSet<Integer> featset) {
		this.featset = new TreeSet<>();
		this.featset.addAll(featset);
	}

	public void setLabel(int label) {
		this.label = label;
	}

}
