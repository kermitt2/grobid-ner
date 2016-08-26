# GROBID Named Entity Recognition Documentation

## Purpose

Grobid NER is a Named-Entity Recogniser module for [GROBID](https://raw.github.com/kermitt2/grobid), a text mining tool exploiting CRF.
Grobid NER has been developed more specifically for the purpose of supporting disambiguation and resolution of entities against knowledge bases such as Wikipedia.
 
The models supplied with the source have been trained using the following dataset: 
- [CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) Manually annotated training data (20k words, 4 classes)
- Wikipedia semi-automatic generated data (approximately 10k words, 26 classes)

Training data and annotation work will be always welcomed, if you like to contribute, you can contact us via email or by opening an issue in the GitHUB project.

## About

* [License](License.md)

## User manual

* [Install GROBID NER](build-and-install.md)

* [Using GROBID NER](using-grobid-ner.md)

* [Classes and senses](classes-and-senses.md)

* [Training guideline](training-guidelines.md) 

