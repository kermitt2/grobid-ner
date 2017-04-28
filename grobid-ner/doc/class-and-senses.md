GROBID NER identifies named-entities and classifies them in 27 classes, as compared to the 4-classes or 7-classes model of most of the existing NER open source tools (usually using the Reuters/CoNLL 2003 annotated corpus, or the MUC annotated corpus).

In addition the entities are often enriched with WordNet sense annotations to help further disambiguation and resolution of the entity. GROBID NER has been developed for the purposed of disambiguating and resolving entities against knowledge bases such as Wikipedia and FreeBase. Sense information can help to disambiguate the entity, because they refine based on contextual clues the entity class.

## Named entity classes

### Short description

The following table describes the 27 named entity classes produced by the model.

| Class name |  Description | Examples |
| ------------ | ----------- | ---------- |
| <a style="text-decorations:none; color:#08c" href=#acronym> ACRONYM | acronym that don't belong to another class | _DIY, BYOD, IMHO_ |
| ANIMAL | individual name of an animal | _Hachikō_, _Jappeloup_ |
| ARTIFACT | human-made object | _FIAT 634_ |
| AWARD | award for art, science, sport, etc. | _Balon d'or_, _Nobel prize_|
| BUSINESS | company / commercial organisation | _Air Canada_, _Microsoft_ |
| CONCEPT | abstract concept not included in another class | _English_ (as language) |
| CONCEPTUAL | entity relating to a concept | _Greek_ myths, _European Union membership_ |
| CREATION | artistic creation, such as song, movie, etc. | _Monna Lisa_, _Mullaholland drive_ |
| EVENT | event | _World War 2_, _Battle of France_ |
| IDENTIFIER | systematized identifier such as phone number, email address, ISBN |  |
| INSTALLATION | structure built by humans | _Strasbourg Cathedral_, _Sforza Castle_ |
| INSTITUTION | organization of people and a location or structure that share the same name | _Yale University_, the _European Patent Office_, _The british government_ |
| <a style="text-decorations:none; color:#08c" href=#legal> LEGAL | legal mentions such as article of law, convention, cases, treaty. etc. | _European Patent Convention_,  _Article 52 (2) (c) and (3)_, _Roe v. Wade, 410 U.S. 113 (1973)_  |
| LOCATION | physical location | _Los Angeles_, _Northern Madagascar_, _Southern Thailand_, _Channel Islands_ |
| MEASURE | numerical amount including an optional unit of measure | _1,500_ |
| MEDIA | media organization or publication | _Le monde_, _The New York Times_ |
| NATIONAL | relating to a location | _North American_, _German_, _Britain_ |		
| ORGANISATION | organized group of people | _Alcoholics Anonymous_ |
| <a style="text-decorations:none; color:#08c" href=#period> PERIOD | date, historical era or other time period | _January, the 2nd half of 2010, 1985-1989, from 1930 to 1945, since 1918_ |
| PERSON | first, middle, last names and aliases of people and fictional characters | _John Smith_ |
| PERSON_TYPE | person type or role classified according to group membership | _African-American_, _Asian_, _Conservative_, _Liberal_ |
| PLANT | name of a plant | _Ficus religiosa_ |
| SPORT_TEAM | sport group or organisation | _The Yankees_ |
| SUBSTANCE | natural substance | |
| TITLE | personal title or honorific | _Mr._, _Dr._, _General_ |
| UNKNOWN | entity not belonging to any previous classes|  |
| WEBSITE | website URL or name | _Wikipedia_, http://www.inria.fr |

### Classes Specific guidelines
#### ACRONYM
Acronyms that don't belong to another class. For example:
* _**DIY**_ ➡ ACRONYM

but
* _**the United Nations** (**UN**)_ ➡ _United Nations_ and _UN_ are tagged ORGANISATION
* _**WW1**_ ➡ EVENT

#20

#### ANIMAL

#### ARTIFACT
#### AWARD
#### BUSINESS
#### CONCEPT
#### CONCEPTUAL
#### CREATION
#### EVENT
#### IDENTIFIER
#### INSTALLATION
#### INSTITUTION
#### LEGAL
#### LOCATION
#### MEASURE
#### MEDIA
#### NATIONAL 		
#### ORGANISATION
#### PERIOD
* Date, historical era or other time period.

* Sometimes preceding elements must be included in the NE, but not always:
  * _**since 1930**_ ➡ PERIOD, because _since_ qualifies the range of period and changes the period type.
  * _**from 1930**_, _**from 1930 to 1945**_ ➡ both PERIOD
but
  * _as early as the **1930s**_ ➡ only _**1930s**_ is tagged PERIOD, because _as early as_ doesn't change the period (the 1930s).
  * _during **1930**_ and _in **1930**_ ➡ the prepositions don't change the period interval, only _**1930**_ is tagged PERIOD.

* vs. EVENT: an event defines a period, but a period is not necessarily an event ➡ annotation as EVENT, for example:
  * _during the time of the **Nazi occupation**_ ➡ EVENT

#23

#### PERSON
#### PERSON_TYPE
#### PLANT
#### SPORT_TEAM
#### SUBSTANCE
#### TITLE
#### UNKNOWN
#### WEBSITE

## Conventions

For the class assignation to entities, GROBID NER follows the longest match convention. For instance, the entity _University of Minnesota_ as a whole (longest match) will belong to the class INSTITUTION. Its component _Minnesota_ is a LOCATION, but as it is part of a larger entity chunk, it will not be identified.


## Sense information

When possible, senses information are also assigned to entities in the form of one or several WordNet synsets.
