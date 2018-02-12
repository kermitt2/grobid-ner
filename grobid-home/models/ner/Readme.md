NER models
 - model.wapiti: current used model 

NER models:  

1. sentence based training data (1 training element => 1 sentence)
	- model.wapiti.500 -> NER model trained using the idillia corpus, and 500 documents
	- model.wapiti.2000 -> NER model trained using the idillia corpus, and 2000 documents
	- model.wapiti.2000bis -> NER model trained using the idillia corpus, and 2000 documents, period are expanded using large mentiones
	- model.wapiti.10000bis -> NER model trained using the idillia corpus, and 10000 documents, period are expanded using large mentiones


2. paragraph based training data (1 training element => 1 paragraph)
	- model.wapiti.10000bis-paragraphLevel -> NER model trained using the idillia corpus, and 10000 documents, period are expanded using large mentiones


3. Comparison models 
	- model.wapiti.conll4classes -> NER model trained using CONLL with 4 classes
	- model.wapiti.connl 

4. Unknown/old models 
	- model.wapiti.inria.paragraphLevelSegmentation
	- model.wapiti.original -> Model used in grobid NER at the moment