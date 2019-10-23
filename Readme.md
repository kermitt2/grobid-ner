# grobid-ner

[![Documentation Status](https://readthedocs.org/projects/grobid-ner/badge/?version=latest)](http://grobid-ner.readthedocs.io/en/latest/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Purpose

GROBID NER is a Named-Entity Recogniser based on the GROBID library ([grobid](https://github.com/kermitt2/grobid)), a text mining tool exploiting CRF. The installation of GROBID is necessary.  

GROBID NER has been developed more specifically for the purpose of supporting the disambiguation and resolution of the entities against knowledge bases such as Wikidata. 

For a description of the NER, installation, usage and other technical features, see the [documentation](http://grobid-ner.readthedocs.io/en/latest/). 

For comparison with Deep Learning architectures used for sequence labelling, see the project [DeLFT](https://github.com/kermitt2/delft).

## Benchmarking 

### CoNLL-2003 (English)

### Corpus LeMonde (FTB, French) 


```
Average over 10 folds: 

===== Field-level results =====

label                accuracy     precision    recall       f1           support

ARTIFACT             99.94        95.56        81.56        86.63        67     
BUSINESS             95.71        84.8         83.27        84           3537   
INSTITUTION          99.72        88.94        76           81.69        212    
LOCATION             97.95        92.55        93.33        92.93        3778   
ORGANISATION         96.77        81.75        73.97        77.62        1979   
PERSON               98.83        93.32        91.54        92.42        2044   

all (micro avg.)     98.15        88.54        86.28        87.39        1161.7  

===== Instance-level results =====

Total expected instances:   1262.5
Correct instances:          1110.6
Instance-level recall:      87.97


Worst fold

===== Field-level results =====

label                accuracy     precision    recall       f1           support

ARTIFACT             99.96        100          83.33        90.91        6      
BUSINESS             94.82        81.42        79.54        80.47        347    
INSTITUTION          99.65        84.21        72.73        78.05        22     
LOCATION             97.88        90.51        94.35        92.39        354    
ORGANISATION         96.79        84.53        73.56        78.66        208    
PERSON               98.61        91.5         90.59        91.04        202    

all (micro avg.)     97.95        86.88        84.9         85.88        1139   
all (macro avg.)     97.95        88.7         82.35        85.25        1139   

===== Instance-level results =====

Total expected instances:   1262
Correct instances:          1107
Instance-level recall:      87.72



Best fold:

===== Field-level results =====

label                accuracy     precision    recall       f1           support

ARTIFACT             99.93        100          80           88.89        10     
BUSINESS             96.03        87.16        84.17        85.64        379    
INSTITUTION          99.7         84.21        76.19        80           21     
LOCATION             98.03        93.56        93.33        93.45        405    
ORGANISATION         97.18        81.4         76.09        78.65        184    
PERSON               99.07        94.47        93.07        93.77        202    

all (micro avg.)     98.32        89.81        87.34        88.56        1201   
all (macro avg.)     98.32        90.13        83.81        86.73        1201   

===== Instance-level results =====

Total expected instances:   1262
Correct instances:          1099
Instance-level recall:      87.08

```

Runtime __single thread__: 88,451 tokens labeled in 1228 ms, 72,028 tokens per second. Tokens are defined by GROBID default tokenizers. This is Wapiti CRF labeling only, without the additional post-processing. Intel(R) Core(TM) i7-4790K CPU @ 4.00GHz, 16GB RAM. 

## License

Grobid and grobid-ner are distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 
 
Author and contact: Patrice Lopez (<patrice.lopez@science-miner.com>) 
