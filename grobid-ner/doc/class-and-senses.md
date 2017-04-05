GROBID NER identifies named-entities and classifies them in 27 classes, as compared to the 4-classes or 7-classes model of most of the existing NER open source tools (usually using the Reuters/CoNLL 2003 annotated corpus, or the MUC annotated corpus). 

In addition the entities are often enriched with WordNet sense annotations to help further disambiguation and resolution of the entity. GROBID NER has been developed for the purposed of disambiguating and resolving entities against knowledge bases such as Wikipedia and FreeBase. Sense information can help to disambiguate the entity, because they refine based on contextual clues the entity class.

## Named entity classes

The following table describes the 27 named entity classes produced by the model. 

| Class name |  Description | Examples | 
| ------------ | ----------- | ---------- | ------ |
| PERSON | first, middle, last names and aliases of people and fictional characters | _John Smith_ | 
| PERSON_TYPE | person type or role classified according to group membership | _African-American_, _Asian_, _Conservative_, _Liberal_ |
| TITLE | personal title or honorific | _Mr._, _Dr._, _General_ |
| LOCATION | physical location | _Los Angeles_, _Northern Madagascar_, _Southern Thailand_, _Channel Islands_ |
| ORGANISATION | organized group of people | _Alcoholics Anonymous_ | 
| EVENT | event | _World War 2_, _Battle of France_ |
| ARTIFACT | human-made object | _FIAT 634_ |
| ACRONYM | acronym | _SAME_ (Sequence As Mode-No Existing), _NORM_ (Naturally Occurring Radioactive Material), _WW1_ (World War 1)|
| BUSINESS | company / commercial organisation | _Air Canada_, _Microsoft_ |
| INSTITUTION | organization of people and a location or structure that share the same name | _Yale University_, the _European Patent Office_, _The british government_ |
| MEASURE | numerical amount including an optional unit of measure | _1,500_ |
| PERIOD | date, historical era or other time period | _January, the 2nd 2010_, _1985-1989_ |
| NATIONAL | relating to a location | _North American_, _German_, _Britain_ |		
| WEBSITE | website URL or name | _Wikipedia_, http://www.inria.fr |
| ANIMAL | individual name of an animal | _Hachikō_, _Jappeloup_ |
| CREATION | artistic creation, such as song, movie, etc. | _Monna Lisa_, _Mullaholland drive_ |
| IDENTIFIER | systematized identifier such as phone number, email address, ISBN |  |
| AWARD | award for art, science, sport, etc. | _Balon d'or_, _Nobel prize_|		
| MEDIA | media organization or publication |  |	
| SUBSTANCE | natural substance | |
| PLANT | name of a plant | _Ficus religiosa_ |
| SPORT_TEAM | sport group or organisation | _The Yankees_ |	
| INSTALLATION | structure built by humans | _Strasbourg Cathedral_, _Sforza Castle_ |
| CONCEPT | abstract concept not included in another class | _English_ (as language) |
| CONCEPTUAL | entity relating to a concept | _Greek_ myths, _European Union membership_ |
| LEGAL | legal mentions such as article of law, convention, cases, treaty. etc. | _European Patent Convention_,  _Article 52 (2) (c) and (3)_, _Roe v. Wade, 410 U.S. 113 (1973)_  |
| UNKNOWN | entity not belonging to any previous classes|  |
					
## Conventions

For the class assignation to entities, GROBID NER follows the longest match convention. For instance, the entity _University of Minnesota_ as a whole (longest match) will belong to the class INSTITUTION. Its component _Minnesota_ is a LOCATION, but as it is part of a larger entity chunk, it will not be identified. 


## Sense information

When possible, senses information are also assigned to entities in the form of one or several WordNet synsets. 


