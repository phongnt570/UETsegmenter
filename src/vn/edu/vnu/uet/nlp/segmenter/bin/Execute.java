package vn.edu.vnu.uet.nlp.segmenter.bin;

public class Execute {

	public static void main(String[] args) {
		int length = args.length;

		if (length < 2) {
			showHelp();
		} else if (length == 2) {
			if (args[0].equals("-r")) {
				if (args[1].equals("seg")) {
					Segment.showHelp();
				} else if (args[1].equals("train")) {
					Train.showHelp();
				} else if (args[1].equals("test")) {
					Test.showHelp();
				} else {
					showHelp();
				}
			} else {
				showHelp();
			}
		} else {
			String[] newArgs = new String[length - 2];
			for (int i = 2; i < length; i++) {
				newArgs[i - 2] = args[i];
			}

			if (args[0].equals("-r")) {
				if (args[1].equals("seg")) {
					Segment.main(newArgs);
				} else if (args[1].equals("train")) {
					Train.main(newArgs);
				} else if (args[1].equals("test")) {
					Test.main(newArgs);
				} else {
					showHelp();
				}
			} else {
				showHelp();
			}
		}
	}

	private static void showHelp() {
		System.out.println("\n* Welcome to UETSegmenter! You need the following arguments to execute:\n");

		System.out.println(Execute.class.getName() + " -r <what_to_execute> {additional arguments}" + "\n");

		System.out.println(
				"\t" + "-r" + "\t" + ":" + "\t" + "the method you want to execute (required: seg|train|test)" + "\n");

		System.out.println("* Additional arguments for each method:" + "\n");

		System.out.print("+ '-r seg' : ");
		Segment.showHelp();

		System.out.print("+ '-r train' : ");
		Train.showHelp();

		System.out.print("+ '-r test' : ");
		Test.showHelp();
	}

}
