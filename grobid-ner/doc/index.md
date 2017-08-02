<h1>GROBID Named Entity Recognition Documentation</h1>

## Purposes

GROBID NER is a Named-Entity Recogniser module for [GROBID](https://raw.github.com/kermitt2/grobid), a tool based on CRF.
GROBID NER has been developed more specifically for the purpose of further supporting post disambiguation and resolution of entities against knowledge bases such as Wikipedia.

The current models shipped with the source uses 27 Named Entity [classes](class-and-senses.md) and have been trained using the following dataset:

* Manually annotated extract from the Wikipedia article on World War 1 (approximately 10k words, 27 classes),

* Reuters NER [CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) manually annotated training data (10k words, 27 classes). This dataset is not public, so not shipped with the code. It's available by contacting NIST. If you are interested by the 27-classes manual annotations only, we can distribute them but you will need to combine these annotations with the Reuters corpus. 

More training data is currently produced based on public data, Wikipedia and historical texts.

The training has been completed with a very large semi-supervised training based on the Wikipedia Idilia data set.

Annotated data will be always welcomed, if you like to contribute, you can contact us via email or by opening an issue in the GitHub project.

## User manual

* [Install GROBID NER](build-and-install.md)

* [Using GROBID NER](using-grobid-ner.md)

* [Annotation guidelines](annotation-guidelines.md)

* [Classes and senses](class-and-senses.md)

* [Largest entity mention](largest-entity-mention.md)

* [Annotation examples](annotation-examples.md)

* [Training NER models](training-ner-model.md)

* [Generate training data](generate-training-data.md)
