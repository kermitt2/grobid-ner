# GROBID Named Entity Recognition Documentation

## Purposes

GROBID NER is a Named-Entity Recogniser module for [GROBID](https://raw.github.com/kermitt2/grobid), a tool based on CRF.
GROBID NER has been developed more specifically for the purpose of further supporting post disambiguation and resolution of entities against knowledge bases such as Wikipedia.
 
The current models shipped with the source uses 26 Named Entity classes and have been trained using the following dataset: 
- Reuters NER [CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) partially manually annotated training data (10k words)
- Manually annotated extract from the Wikipedia article on World War 1 (approximately 10k words)

The training has been completed with a very large semi-supervised training based on the Wikipedia Idilia data set. 

Annotated data will be always welcomed, if you like to contribute, you can contact us via email or by opening an issue in the GitHub project.

## About

* [License](License.md)

## User manual

* [Install GROBID NER](build-and-install.md)

* [Using GROBID NER](using-grobid-ner.md)

* [Classes and senses](classes-and-senses.md)

* [Training guideline](training-guidelines.md) 

