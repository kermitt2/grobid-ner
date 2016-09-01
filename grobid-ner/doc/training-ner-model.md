# Generate training corpus 

## Datasets
  
grobid-ner project includes the following dataset:

- manually annotated extract of the *Wikipedia article on World War 1* (approximately 10k words, 26 classes). This data, as the whole Wikipedia content, is available under the licence [Creative Commons Attribution-ShareAlike License](https://creativecommons.org/licenses/by-sa/3.0/). 

- manually annotated extract of the *Holocaust data* from the [EHRI](https://portal.ehri-project.eu) research portal, openly available ([data policy](https://portal.ehri-project.eu/data-policy)) (26 classes).

In addition to these datasets, the CRF models shipped with Grobid NER has been also trained with these different datasets :
 
 - a small extract of the *Reuters dataset* corresponding to the *[CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) subset*, around 10k words, which has been manually re-annotated with 26 classes. The Reuters corpus is available by contacting [NIST](http://trec.nist.gov/data/reuters/reuters.html). If you are interested by the 26-classes manual annotations only, we can distribute them but you will need to combine these annotations with the Reuters corpus.  

 - A large set of Wikipedia articles automatically annotated by Idilia. This data can be downloaded from [IDILIA download page](http://download.idilia.com/datasets/wikipedia/index.html). We use a part of this dataset in a semi-supervised training step as a complement to the supervised training based on the manually annotated corpus.


## Training NER Model

### Training data
Since the training data are not freely available, it is necessarily to assembly them beforehand.

TBD


### Train the NER model 
The assumption is that all the required datasets have been downloaded and the property file `grobid-ner.properties` updated accordingly.
The required files are: 

- wikipedia.ner26.train shipped with the project

- reuters.ner26.train which is a 26 classes reuters based data


To start the training: 

```
mvn generate-resources -Ptrain_ner
```

The process is pretty heavy and it will require several days, depending on the hardware available.  


### Train the Sense model 

TBD