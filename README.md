# UETsegmenter

UETsegmenter is a toolkit for Vietnamese word segmentation. It uses a hybrid approach that is based on longest matching with logistic regression.

UETsegmenter is written in Java and developed in Esclipse IDE.

## Overview

+ ```src``` : folder of java source code

+ ```uetsegmenter.jar``` : an executable jar file (see [How to use](#how-to-use))

+ ```models``` : a pre-trained model for Vietnamese word segmentation

+ ```dictionary``` : necessary dictionaries for word segmentation

## How to use

The following command is used to run this toolkit, your PC needs JDK 1.8 or newer:

```
java -jar uetsegmenter.jar -r <what_to_execute> {additional arguments}

	-r	:	the method you want to execute (required: seg|train|test)
```

Additional arguments for each method:

+ ```-r seg``` : Method for word segmentation. Needed arguments:

```
-m <models_path> -i <input_path> [-ie <input_extension>] -o <output_path> [-oe <output_extension>]

	-m	:	path to the folder of segmenter model (required)
	-i	:	path to the input text (file/folder) (required)
	-ie	:	input extension, only use when input_path is a folder (default: *)
	-o	:	path to the output text (file/folder) (required)
	-oe	:	output extension, only use when output_path is a folder (default: seg)
```

+ ```-r train``` : Method for training a new model. Needed arguments:

```
-i <training_data> [-e <file_extension>] -m <models_path>

	-i	:	path to the training data (file/folder) (required)
	-e	:	file extension, only use when training_data is a folder (default: *)
	-m	:	path to the folder you want to save model after training (required)
```

After training, the ```models_path``` folder will contain 2 files: ```model``` and ```features```.

+ ```-r test``` : Method for testing a model. Needed arguments:

```
-m <models_path> -t <test_file>

	-m	:	path to the folder of segmenter model (required)
	-t	:	path to the test file (required)
```

## APIs

3 APIs for Vietnames word segmentation are provided:

+ Segment a raw text:

```java
	String modelsPath = "models"; // path to the model folder. This folder must contain two files: model, features
	UETSegmenter segmenter = new UETSegmenter(modelsPath); // construct the segmenter
	String raw_text_1 = "Tốc độ truyền thông tin ngày càng cao.";
	String raw_text_2 = "Tôi yêu Việt Nam!";

	String seg_text_1 = segmenter.segment(raw_text_1); // Tốc_độ truyền thông_tin ngày_càng cao .
	String seg_text_2 = segmenter.segment(raw_text_2); // Tôi yêu Việt_Nam !

	// ... You only need to construct the segmenter one time, then you can segment any number of texts.
```

+ Segment a tokenized text:

```java
	// ...
	// ... construct the segmenter

	String tokenized = "Tôi , bạn tôi yêu Việt Nam !";
	String segmented = segmenter.segmentTokenizedText(raw_text_2); // Tôi , bạn tôi yêu Việt_Nam !
```

+ Segment a raw text and return list of segmented sentences:

```java
	// ...
	// ... construct the segmenter

	String text = "Tốc độ truyền thông tin ngày càng cao. Tôi, bạn tôi yêu Việt Nam!";
	List<String> segmented_sents = segmenter.segmentSentences(text); // [0] : Tốc_độ truyền thông_tin ngày_càng cao .
																	// [1] : Tôi , bạn tôi yêu Việt_Nam !
```

## Author

Phong Tuan Nguyen

University of Engineering and Technology, Vietnam National University - Hanoi

Email: phongnt.uet@gmail.com