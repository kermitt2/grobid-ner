# Generate training corpus 

### Datasets
  
Grobid NER has been trained on several different datasets :
 
 - *Reuters dataset*: This dataset was made available from Reuters Ltd. It is not public and it is not provided within this project. 
To obtain it, contact [NIST](http://trec.nist.gov/data/reuters/reuters.html).
 
 - *[CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) NER manually annotated dataset*: made available for CONLL 2003 conference.
This dataset contains the annotations used for the CONLL conference. It is public and it ships only annotations. 
It requires the Reuters dataset (see above) [to be built](http://www.cnts.ua.ac.be/conll2003/ner/000README). 

 - *Manually annotated extract from the Wikipedia articles* on World War 1 (approximately 10k words, 26 classes). 
This data can be downloaded from [IDILIA download page](http://download.idilia.com/datasets/wikipedia/index.html). 
This data, as the whole wikipedia, is freely available under the licence [Creative Commons Attribution-ShareAlike License](https://creativecommons.org/licenses/by-sa/3.0/). 
 
 - *Holocaust data* from the [EHRI](https://portal.ehri-project.eu) research portal, openly available ([data policy](https://portal.ehri-project.eu/data-policy)).