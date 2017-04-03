# Training corpus 

## Datasets
  
### English

grobid-ner project includes the following dataset:

- manually annotated extract of the *Wikipedia article on World War 1* (approximately 10k words, 26 classes). This data, as the whole Wikipedia content, is available under the licence [Creative Commons Attribution-ShareAlike License](https://creativecommons.org/licenses/by-sa/3.0/). 

- manually annotated extract of the *Holocaust data* from the [EHRI](https://portal.ehri-project.eu) research portal, openly available ([data policy](https://portal.ehri-project.eu/data-policy)) (26 classes).

In addition to these datasets, the CRF models shipped with Grobid NER has been also trained with these different datasets :
 
 - a small extract of the *Reuters dataset* corresponding to the *[CONLL 2003](http://www.cnts.ua.ac.be/conll2003/ner/) subset*, around 10k words, which has been manually re-annotated with 26 classes. The Reuters corpus is available by contacting [NIST](http://trec.nist.gov/data/reuters/reuters.html). If you are interested by the 26-classes manual annotations only, we can distribute them but you will need to combine these annotations with the Reuters corpus.  

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

- reuters.ner26.train which is a 26 classes reuters based data


To start the training: 

```
mvn generate-resources -Ptrain_ner
```

Due to the semi-supervised training, the process is pretty heavy and it will require several days, depending on the hardware available.  

The French NER can be trained as follow:

> mvn generate-resources -Ptrain_nerfr

### Train and evaluation of the NER model 

The following commands will split automatically and randomly the available annotated data into a training set and an evaluation set, train a model based on the first set and launch an evaluation based on the second set. 

For the English NER model:
> mvn compile exec:exec -Ptrain_eval_ner

For the French NER model:
> mvn compile exec:exec -Ptrain_eval_nerfr

In this mode, by default, 80% of the available data is used for training and the remaining for evaluation. This ratio can be changed by editing the corresponding exec profile in the pom.xml file. 

### Train the Sense model 

To start the training: 


> mvn generate-resources -Ptrain_nersense


## Generate training data

### Semi-supervised training data from IDILIA download page

The training data generated from this corpus is quite handy because some annotation are already used. 

1. Download the data from [IDILIA download page](http://download.idilia.com/datasets/wikipedia/index.html) and unzip it on the hard drive
2. Configure the path to this resource in the `grobid-nerd.properties`, in particular the entry `grobid.ner.reuters.idilia_path`. 
3. Edit the file `resources/dataset/ner/corpus/wikipedia.txt` and add the wikipedia pages to be processed
4. Build the project:

> mvn clean package

5. Run the process: 

> java -jar target/grobid-ner-<version>.one-jar.jar -dOut /my/output/directory -exe createTrainingIDILLIA

6. The generated files will be located in `/my/output/directory/{inputFileName}.out'. Review the output and eventually correct missing / wrong labels.

### Generate training data from text files

The training data generation from text file is annotating the files and output them in CONLL format. The process requires an input and output directories.

To generate the data: 

> java -jar target/grobid-ner-<version>.one-jar.jar -dOut /my/output/directory -dIn /my/input/directory  -exe createTrainingNER
