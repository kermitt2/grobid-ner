# Guidelines for annotation of Named Entities Recognition

The creation of annotated corpus for Named Entities is the process of find the correct class of named entities for words based on the context.

Grobid-NER can automatically generate training data from text files ( [Link to Page] ), recognising the best named entities with the model currently used.

The goal of the annotator is to correct the generated entities by: (1) changing them, (2) extending them to the proximity tokens or (3) removing them.

### Format 

The format the training data is managed is the [CONLL 2003 format](http://www.cnts.ua.ac.be/conll2003/ner/), which is a 2 column tab separated file.
The first column is the token, the second column is the class:
```
token           B-CLASS
token           CLASS
```

The `B-` prefix is used to indicate the beginning of the class. This is important when the same class is repeated for two adiacent entities, **normally this is a very rare event**.

During training it's mandatory not to modify the token for any reason. Only the column of the class can be changed. 

### Classes
The list of classes with the set of examples are defined in the [classes page](class-and-senses.md) of this manual. 
    
### Largest entity mention

Entities with more than one token, can be recognized in different way (for example given two tokens could be interpreted as one entity of two tokens or two entities of one token). 
The approach choosen with GROBID-NER is to try to match the largest entity mentions. Here some examples: 

  1. the token _british_: 
    
    _british_ is recognised as class NATIONAL
    
    but 
    
    _british referendum_ is recognised as class EVENT
    
    _british government_ is recognised as class INSTITUTION

  2. composed token like European Union should be considered as a whole (please note that the fact that _European Union_ it's an INSTITUTION could vary based on the context): 

    ```
    President       B-INSTITUTION
    Union           INSTITUTION 
    ```
    
    instead the following case, which is not correct because it consider the two tokens separately: 

    ```
    European        B-NATIONAL
    Union           O
    ```
    
    3. more realworld case is when the entity precede an object that make the object specific: 

    ```
    European        B-EVENT
    Union           EVENT
    membership      EVENT
    referendum      EVENT
    ```
    
    instead of the following, wrong approach: 
    
    ```
    European         B-INSTITUTION
    Union            INSTITUTION
    membership       B-CONCEPT
    referendum       B-CONCEPT    
    ```
    
    and so on and so forth. 
    


### Practical example

For example the phrase: World War I (WWI) was a global war centred in Europe that began on 28 July 1914 and lasted until 11 November 1918. 

The generated training data happears like below.  

```
World       B-EVENT
War         B-EVENT
I           B-EVENT
(           O
WWI         O
)           O
was         O
a           O
global      O
war         O
centred     O
in          O
Europe      B-LOCATION
that        O
began       O
on          O
28          B-PERIOD
July        B-PERIOD
1914        PERIOD
and         O
lasted      O
until       O
11          B-MEASURE
November    B-PERIOD
1918        PERIOD
.           O
```    
    
Annotation process: 

1. The first token World War I it's correctly recognised but as a three separated tokens (note the B- at the beginning of each class), it shoudl be corrected as 
        
  ```
  World        B-EVENT
  War          EVENT
  I            EVENT
  ```

2. WWI is not recognised, it should be tagged as ACRONYM

  ```
  WWI        B-ACRONYM
  ```

3. Europe is intended as the european continent, therefore the class LOCATION is correct. 

4. The token 28 July 1914 it's a single PERIOD and not two:

  ```
  28        B-PERIOD
  July      PERIOD
  1914      PERIOD
  ```

5. lastly the 11 Novembre 1918 has been wrongly split, although the tokens are correct if 11 and November 1918 would be isolated, they are not correct in this context: 
  
  ```
  11          B-PERIOD
  November    PERIOD
  1918        PERIOD
  ```

The result is as following: 
```
World       B-EVENT
War         EVENT
I           EVENT
(           O
WWI         B-ACRONYM
)           O
was         O
a           O
global      O
war         O
centred     O
in          O
Europe      B-LOCATION
that        O
began       O
on          O
28          B-PERIOD
July        PERIOD
1914        PERIOD
and         O
lasted      O
until       O
11          B-PERIOD
November    PERIOD
1918        PERIOD
.           O
```    
    

#### More examples
 
*European Union* it's a country? it's an institution? 

The answer will be always _it depends_. If you take the wikipedia page on BREXIT, it's mostly referring to the European Union as an institution, but in different context could be something else, like a LOCATION.

```
[...]    
to          O
gauge       O
support     O
for         O
the         O
country        O
'           O
s           O
continued   O
membership  O
in          O
the         O
European    B-INSTITUTION
Union       INSTITUTION
.           O
```

another example: 

``` 
The         O
country     O
joined      O
the         O
European    B-INSTITUTION
Economic    INSTITUTION
Community   INSTITUTION
(           O
EEC         B-INSTITUTION
,           O
or          O
"           O
Common      B-INSTITUTION
Market      INSTITUTION
"           O
)           O
in          O
1973        B-PERIOD
.           O
```

