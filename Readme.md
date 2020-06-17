# grobid-ner

[![Documentation Status](https://readthedocs.org/projects/grobid-ner/badge/?version=latest)](http://grobid-ner.readthedocs.io/en/latest/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![SWH](https://archive.softwareheritage.org/badge/origin/https://github.com/kermitt2/grobid-ner/)](https://archive.softwareheritage.org/browse/origin/?origin_url=https://github.com/kermitt2/grobid-ner)

## Purpose

__grobid-ner__ is a Named-Entity Recogniser based on [GROBID](https://github.com/kermitt2/grobid), a text mining tool. The installation of GROBID is necessary. 

For a description of the NER, installation, usage and other technical features, see the [documentation](http://grobid-ner.readthedocs.io/en/latest/). 

This project has been developed to explore NER with CRF. It allows in particular reliable and reproducible comparisons with other approaches. For comparisons with Deep Learning architectures used for state-of-the-art sequence labelling, see the project [DeLFT](https://github.com/kermitt2/delft).

__grobid-ner__ is originally more specifically dedicated to the support of disambiguation and resolution of the entities against knowledge bases such as Wikidata. The project comes with a NER model for English with 27 named entity classes and its corresponding dataset, all CC-BY. The high number of classes is motivated to help further entity disambiguation and to bring more entities than the usual datasets.


## Benchmarking 

### 27-class English dataset

```
Summary results: 

Average over 10 folds: 

label                accuracy     precision    recall       f1           support

ANIMAL               79.92        45           35           38.33        12     
ARTIFACT             89.73        45           31.67        34.67        25     
AWARD                79.86        33.33        25           25           11     
BUSINESS             99.35        57.5         37.58        43.83        61     
CONCEPT              99.5         59.5         42.01        46.44        45     
CONCEPTUAL           89.85        61.67        41.67        48.05        19     
CREATION             19.98        0            0            0            2      
EVENT                98.19        56.95        45.73        50.25        175    
IDENTIFIER           69.92        41.67        41.67        40           12     
INSTALLATION         59.9         30           21.67        24.67        10     
INSTITUTION          29.97        0            0            0            3      
LOCATION             97.53        89.37        93.14        91.18        1126   
MEASURE              98.5         89.77        91.18        90.45        650    
MEDIA                89.77        18.33        12.5         13           15     
NATIONAL             98.85        83.16        89.59        86.02        337    
ORGANISATION         98.64        72.46        66.96        69.14        196    
PERIOD               99.17        92.3         89.82        90.92        384    
PERSON               98.51        87.98        89.81        88.8         539    
PERSON_TYPE          99.71        89.13        62.51        71.44        55     
SPORT_TEAM           99.15        77.95        68.46        71.08        133    
TITLE                99.5         66.33        48.25        54.97        62     
UNKNOWN              39.95        0            0            0            3      
WEBSITE              40           40           40           40           4      

all                  99.22        85.55        83.11        84.31 

===== Instance-level results =====

Total expected instances:   109.1
Correct instances:          65.9
Instance-level recall:      60.4


Worst fold

===== Field-level results =====

label                accuracy     precision    recall       f1           support

ANIMAL               99.89        0            0            0            1      
ARTIFACT             99.89        50           100          66.67        1      
AWARD                99.89        100          50           66.67        2      
BUSINESS             99.34        33.33        20           25           5      
CONCEPT              99.34        0            0            0            4      
CONCEPTUAL           99.67        50           33.33        40           3      
CREATION             99.89        0            0            0            1      
EVENT                98.46        40           33.33        36.36        12     
IDENTIFIER           99.78        0            0            0            2      
INSTALLATION         99.78        0            0            0            1      
LOCATION             97.35        90           91.41        90.7         128    
MEASURE              98.68        88.06        93.65        90.77        63     
MEDIA                99.89        0            0            0            1      
NATIONAL             98.46        83.64        90.2         86.79        51     
ORGANISATION         97.46        43.75        33.33        37.84        21     
PERIOD               98.57        85.71        78.95        82.19        38     
PERSON               98.68        89.66        89.66        89.66        58     
PERSON_TYPE          99.78        100          33.33        50           3      
SPORT_TEAM           99.56        81.25        92.86        86.67        14     
TITLE                99.01        33.33        12.5         18.18        8      
UNKNOWN              99.89        0            0            0            1      

all (micro avg.)     99.2         83.08        79.9         81.46        418    
all (macro avg.)     99.2         46.13        40.6         41.31        418    

===== Instance-level results =====

Total expected instances:   109
Correct instances:          60
Instance-level recall:      55.05


Best fold:

===== Field-level results =====

label                accuracy     precision    recall       f1           support

ARTIFACT             99.86        100          50           66.67        2      
AWARD                99.58        0            0            0            2      
BUSINESS             99.86        100          80           88.89        5      
CONCEPT              99.72        66.67        66.67        66.67        3      
CONCEPTUAL           99.86        0            0            0            1      
CREATION             99.86        0            0            0            1      
EVENT                98.33        53.85        53.85        53.85        13     
IDENTIFIER           99.86        50           100          66.67        1      
INSTALLATION         99.86        0            0            0            1      
LOCATION             98.06        93.1         91.01        92.05        89     
MEASURE              98.47        92.11        93.33        92.72        75     
MEDIA                99.58        33.33        50           40           2      
NATIONAL             99.03        80           90.91        85.11        22     
ORGANISATION         99.31        80           85.71        82.76        14     
PERIOD               98.89        96.97        82.05        88.89        39     
PERSON               98.33        84.62        96.49        90.16        57     
PERSON_TYPE          99.86        100          50           66.67        2      
SPORT_TEAM           99.31        100          58.33        73.68        12     
TITLE                99.72        100          66.67        80           6      

all (micro avg.)     99.33        87.65        85.88        86.75        347    
all (macro avg.)     99.33        64.77        58.69        59.72        347    

===== Instance-level results =====

Total expected instances:   109
Correct instances:          77
Instance-level recall:      70.64

```

Runtime __single thread__: 18,599 tokens per second. Tokens are defined by GROBID default tokenizers. Intel(R) Core(TM) i7-4790K CPU @ 4.00GHz, 16GB RAM. 

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


### CoNLL-2003 (English)

We use report results with the official CoNLL eval script:

```
processed 52236 tokens with 5628 phrases; found: 5612 phrases; correct: 4805.
accuracy:  96.86%; precision:  85.62%; recall:  85.38%; FB1:  85.50
         LOCATION: precision:  88.63%; recall:  90.07%; FB1:  89.35  1689
     ORGANISATION: precision:  81.23%; recall:  79.95%; FB1:  80.58  1630
           PERSON: precision:  90.47%; recall:  91.03%; FB1:  90.75  1627
          UNKNOWN: precision:  76.88%; recall:  73.88%; FB1:  75.35  666
```

Runtime __single thread__: 29,781 tokens per second. Tokens are defined by GROBID default tokenizers. Intel(R) Core(TM) i7-4790K CPU @ 4.00GHz, 16GB RAM. 

## License

Grobid and grobid-ner are distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 
 
Author and contact: Patrice Lopez (<patrice.lopez@science-miner.com>) 
