<h1>Generate training data</h1>


## Generate training data from text files for creating supervised training data

For producing supervised training data, the tool offers the possibility to generate pre-annotated training data from a text, where the annotations are realized by the currently available model. Curators can then correct the wrong annotations and regenerate a new model with this additional training data.

The training data generation from text file is annotating the files and output them in XML format (CoNNL format output is deprecated). The process requires an input and output directories.

To generate the data:

```bash
> java -jar build/libs/grobid-ner-<version>.one-jar.jar -dOut /my/output/directory -dIn /my/input/directory -l en -exe createTrainingNER
```

The parameter _-l_ is used to indicate the language of the training data to be generated.

Example:

```bash
> java -jar build/libs/grobid-ner-0.7.3.one-jar.jar -dOut resources/dataset/ner/corpus/xml/generated/ -dIn resources/dataset/ner/corpus/raw/wikipedia/ -l en -exe createTrainingNER
```

## Semi-supervised training data from the IDILIA dataset(s)

**Note: deprecated, this dataset is not available anymore**

The training data generated from this corpus is quite handy because a larger set of named entity classes is already used. However it has been automatically generated so it is involved in the training of grobid-ner only as an additional semi-supervised step for increasing recall.

1. Download the data from [IDILIA download page](http://download.idilia.com/datasets/wikipedia/index.html) and unzip it.
2. Configure the path to this resource in the `grobid-nerd.properties`, in particular the entry `grobid.ner.reuters.idilia_path`.
3. Edit the file `resources/dataset/ner/corpus/wikipedia.txt` and add the wikipedia pages to be processed.
4. Build the project.
5. Run the process:

```bash
> java -jar build/libs/grobid-ner-<version>.one-jar.jar -dOut /my/output/directory -exe createTrainingIDILLIA
```

6. The generated files will be located in `/my/output/directory/{inputFileName}.out'. Review the output and eventually correct missing / wrong labels.
