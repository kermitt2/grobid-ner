
<h1>Training corpus</h1>

## Datasets

### English

grobid-ner project includes the following dataset:

- manually annotated extract of the *Wikipedia article on World War 1* (approximately 10k words, 27 classes). This data, as the whole Wikipedia content, is available under the licence [Creative Commons Attribution-ShareAlike License](https://creativecommons.org/licenses/by-sa/3.0/).

- manually annotated extract of the *Holocaust data* from the [EHRI](https://portal.ehri-project.eu) research portal, openly available ([data policy](https://portal.ehri-project.eu/data-policy)) (27 classes).

In addition to these datasets, the CRF models shipped with Grobid NER has been also trained with these different datasets :

 - a small extract of the *Reuters dataset* corresponding to the *[CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) subset*, around 10k words, which has been manually re-annotated with 27 classes. The Reuters corpus is available by contacting [NIST](http://trec.nist.gov/data/reuters/reuters.html). If you are interested by the 27-classes manual annotations only, we can distribute them but you will need to combine these annotations with the Reuters corpus.  

 - A large set of Wikipedia articles automatically annotated by Idilia. This data can be downloaded from [IDILIA download page](http://download.idilia.com/datasets/wikipedia/index.html). We use a part of this dataset in a semi-supervised training step as a complement to the supervised training based on the manually annotated corpus.

### French

It is possible to train grobid-ner with _Le Monde corpus_, a specific XML parser is included.

## Training NER Models

### Training data
Since the training data are not freely available, it is necessarily to assembly them beforehand.


### Train the NER model
The assumption is that all the required datasets have been downloaded and the property file `grobid-ner.properties` updated accordingly.
The required files are:

- wikipedia.ner26.train shipped with the project

- reuters.ner26.train which is a 27 classes reuters based data


To start the training:

```bash
> mvn generate-resources -Ptrain_ner
```

Due to the semi-supervised training, the process is pretty heavy and it will require several days, depending on the hardware available.  

The French NER can be trained as follow:

```bash
> mvn generate-resources -Ptrain_nerfr
```

### Train and evaluation of the NER model

The following commands will split automatically and randomly the available annotated data into a training set and an evaluation set, train a model based on the first set and launch an evaluation based on the second set.

For the English NER model:
```bash
> mvn compile exec:exec -Ptrain_eval_ner
```

For the French NER model:
```bash
> mvn compile exec:exec -Ptrain_eval_nerfr
```

In this mode, by default, 90% of the available data is used for training and the remaining for evaluation. This ratio can be changed by editing the corresponding exec profile in the `pom.xml` file.

### n-fold evaluation

For 10-fold cross evaluation, use the following commands:

For the English NER model:
```bash
> mvn compile exec:exec -Ptrain_eval_nfold_ner
```

For the French NER model:
```bash
> mvn compile exec:exec -Ptrain_eval_nfold_nerfr
```

You can adjust the number of folds (default is 10 as indicated above) by editing the corresponding exec profile in the `pom.xml` file.


### Train the Sense model

To start the training:

```bash
> mvn generate-resources -Ptrain_nersense
```
