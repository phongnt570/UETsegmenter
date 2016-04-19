# UETsegmenter

UETsegmenter is a toolkit for Vietnamese word segmentation. It uses a hybrid approach that combines longest matching with logistic regression.

## How to run

The following command is used to run this toolkit:

```
vn.edu.vnu.uet.nlp.segmenter.bin.Execute -r <what_to_execute> {additional arguments}

	-r	:	the method you want to execute (required: seg|train|test)
```

* Additional arguments for each method:

+ ```-r seg``` : Method for word segmentation. Needed arguments:

```
-m <models_path> -i <input_path> [-ie <input_extension>] -o <output_path> [-oe <output_extension>]

	-m	:	path to the folder of segmenter models (required)
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
	-m	:	path to the folder you want to save models after training (required)
```

+ ```-r test``` : Method for testing a model. Needed arguments:

```
-m <models_path> -t <test_file>

	-m	:	path to the folder of segmenter models (required)
	-t	:	path to the test file (required)
```