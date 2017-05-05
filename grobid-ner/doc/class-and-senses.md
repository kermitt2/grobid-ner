GROBID NER identifies named-entities and classifies them in 27 classes, as compared to the 4-classes or 7-classes model of most of the existing NER open source tools (usually using the Reuters/CoNLL 2003 annotated corpus, or the MUC annotated corpus).

In addition the entities are often enriched with WordNet sense annotations to help further disambiguation and resolution of the entity. GROBID NER has been developed for the purposed of disambiguating and resolving entities against knowledge bases such as Wikipedia and FreeBase. Sense information can help to disambiguate the entity, because they refine based on contextual clues the entity class.

## Named entity classes

### Short description

The following table describes the 27 named entity classes produced by the model.

| Class name |  Description | Examples |
| ------------ | ----------- | ---------- |
| <a style="text-decorations:none; #color:#265C83" href=#acronym> ACRONYM | acronym that doesn't belong to another class | _DIY, BYOD, IMHO_ |
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
| <a style="text-decorations:none; color:#08c" href=#legal> LEGAL | legal mentions such as article of law, convention, cases, treaty., etc. | _European Patent Convention_,  _Maastricht Treaty_, _Article 52(2)(c) and (3)_, _Roe v. Wade, 410 U.S. 113 (1973)_ |
| LOCATION | physical location, including planets and galaxies. | _Los Angeles_, _Northern Madagascar_, _Southern Thailand_, _Channel Islands_, _Earth_, _Milky Way_ |
| MEASURE | numerical amount including an optional unit of measure | _1,500_ |
| MEDIA | media organization or publication | _Le monde_, _The New York Times_ |
| NATIONAL | relating to a location | _North American_, _German_, _Britain_ |		
| ORGANISATION | organized group of people | _Alcoholics Anonymous_ |
| <a style="text-decorations:none; color:#08c" href=#period> PERIOD | date, historical era or other time period | _January, the 2nd half of 2010, 1985-1989, from 1930 to 1945, since 1918_ |
| PERSON | first, middle, last names and aliases of people and fictional characters | _John Smith_ |
| <a style="text-decorations:none; color:#08c" href=#person_type> PERSON_TYPE | person type or role classified according to group membership | _African-American_, _Asian_, _Conservative_, _Liberal_, _Jews_ |
| PLANT | name of a plant | _Ficus religiosa_ |
| SPORT_TEAM | sport group or organisation | _The Yankees_ |
| SUBSTANCE | natural substance | |
| <a style="text-decorations:none; color:#08c" href=#title> TITLE | personal or honorific title | _Mr._, _Dr._, _General_, _President_, _chairman_, _doctor_, _member_, _founder_|
| UNKNOWN | entity not belonging to any previous classes|  |
| WEBSITE | website URL or name | _Wikipedia_, http://www.inria.fr |

### Classes Specific guidelines
#### ACRONYM
Acronyms that don't belong to another class. For example:

* _**DIY**_: ACRONYM

but

* _**the United Nations** (**UN**)_: _United Nations_ and _UN_ are tagged ORGANISATION
* _**WW1**_: EVENT

[issue #20](https://github.com/kermitt2/grobid-ner/issues/20)

---
#### ANIMAL

---
#### ARTIFACT

---
#### AWARD

---
#### BUSINESS

---
#### CONCEPT

---
#### CONCEPTUAL

---
#### CREATION

---
#### EVENT

---
#### IDENTIFIER

---
#### INSTALLATION

---
#### INSTITUTION

---
#### LEGAL
➡ Legal mentions such as article of law, convention, cases, treaty., etc.

➡ there is a graduation between too general and more specific terms, for example:

  * _**Nazi policies**_, _**Nazi social policies**_: too general, not to be annotated (except for _**Nazi**_ as PERSON_TYPE)
  * _**anti-Jewish legislation**_, _**anti-semitic policy**_: are more specific and annotated LEGAL

[issue #17](https://github.com/kermitt2/grobid-ner/issues/17)

---
#### LOCATION

---
#### MEASURE

---
#### MEDIA

---
#### NATIONAL

---
#### ORGANISATION
➡ Ethnic communities are not included in the class ORGANISATION [(issue #28)](https://github.com/kermitt2/grobid-ner/issues/28).

---
#### PERIOD

➡ Date, historical era or other time period.

➡ Sometimes preceding elements must be included in the NE, but not always:

* _**since 1930**_: all PERIOD, because _since_ qualifies the range of period and changes the period type.
* _**from 1930**_, _**from 1930 to 1945**_: both all PERIOD.

but

* _as early as the **1930s**_: only _**1930s**_ is tagged PERIOD, because _as early as_ doesn't change the period (the 1930s).
* _during **1930**_ and _in **1930**_: the prepositions don't change the period interval, only _**1930**_ is tagged PERIOD.


➡ PERIOD vs. EVENT: an event defines a period, but a period is not necessarily an event, so we annotate as EVENT, for example:

  * _during the time of the **Nazi occupation**_: EVENT

[issue #13](https://github.com/kermitt2/grobid-ner/issues/13)

#### PERSON

---
#### PERSON_TYPE
➡ Even though it's an approximation, entities like _**Jewry**_ (which means Jewish community) are included in this class. [(issue #28)](https://github.com/kermitt2/grobid-ner/issues/28)

---
#### PLANT

---
#### SPORT_TEAM

---
#### SUBSTANCE

---
#### TITLE
➡ Personal or honorific title, with a relatively loose definition. For example the following entities are annotated as TITLE: _**chairman**_, _**member**_, _**founder**_.

➡ The [Wikipedia page](https://en.wikipedia.org/wiki/Title) examples can be useful.

➡ To decide between TITLE and PERSON:

* if only the TITLE is mentioned, it's annotated TITLE, even though it refers to a person. Examples of entities annotated TITLE:

    * _He is the **President of the United States**._
    * _He is **CEO** of this company._
    * _The **Chinese Prime minister** said this._
    * _The **Queen**_ <br />
<br />

* In case of the largest entity match of TITLE + PERSON, the priority goes to PERSON. For example _**The President of the United States Barack Obama**_ as a whole is annotated PERSON.

issues [#12](https://github.com/kermitt2/grobid-ner/issues/12) and [#33](https://github.com/kermitt2/grobid-ner/issues/33)

---
#### UNKNOWN

---
#### WEBSITE

---

### Miscellaneous

➡ Punctuation (like quotation marks) are to be left outside the tags, for example: `"<ENAMEX type="PERSON_TYPE">socialists</ENAMEX>"` [(issue #26)](https://github.com/kermitt2/grobid-ner/issues/26).

➡ **Currencies** alone (_pound sterling_, _US dollar_) should not be annotated [(issue #23)](https://github.com/kermitt2/grobid-ner/issues/23).

➡ When there is a **dash**, it can be considered a space, for example _**Nobel prize-winning economist**_ is annotated [(issue #31)](https://github.com/kermitt2/grobid-ner/issues/31):
```xml
</ENAMEX type="AWARD">Nobel prize</ENAMEX>-winning economist
```



## Conventions

For the class assignation to entities, GROBID NER follows the longest match convention. For instance, the entity _University of Minnesota_ as a whole (longest match) will belong to the class INSTITUTION. Its component _Minnesota_ is a LOCATION, but as it is part of a larger entity chunk, it will not be identified.


## Sense information

When possible, senses information are also assigned to entities in the form of one or several WordNet synsets.
