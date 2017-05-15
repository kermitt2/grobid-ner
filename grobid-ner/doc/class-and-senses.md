GROBID NER identifies named-entities and classifies them in 27 classes, as compared to the 4-classes or 7-classes model of most of the existing NER open source tools (usually using the Reuters/CoNLL 2003 annotated corpus, or the MUC annotated corpus).

In addition the entities are often enriched with WordNet sense annotations to help further disambiguation and resolution of the entity. GROBID NER has been developed for the purposed of disambiguating and resolving entities against knowledge bases such as Wikipedia and FreeBase. Sense information can help to disambiguate the entity, because they refine based on contextual clues the entity class.

## Named entity classes

### Short description

The following table describes the 27 named entity classes produced by the model.

| Class name |  Description | Examples |
| ------------ | ----------- | ---------- |
| <a style="text-decorations:none; #color:#265C83" href=#acronym> ACRONYM | acronym that doesn't belong to another class | _DIY, BYOD, IMHO_ |
| ANIMAL | individual name of an animal | _Hachikō_, _Jappeloup_ |
| <a style="text-decorations:none; #color:#265C83" href=#artifact> ARTIFACT | human-made object, including softwares | _FIAT 634_, _Microsoft Word_ |
| AWARD | award for art, science, sport, etc. | _Balon d'or_, _Nobel prize_|
| BUSINESS | company / commercial organisation | _Air Canada_, _Microsoft_ |
| <a style="text-decorations:none; #color:#265C83" href=#concept> CONCEPT | abstract concept not included in another class | _English_ (as language), _Communism_, _Zionism_ |
| CONCEPTUAL | entity relating to a concept | _Greek_ myths, _European Union membership_ |
| <a style="text-decorations:none; #color:#265C83" href=#creation> CREATION | artistic creation, such as song, movie, book, TV show, etc. | _Monna Lisa_, _Mullaholland drive_, _Kitchen Nightmares_, _EU Referendum: The Great Debate_, _Europe: The Final Debate_ |
| EVENT | event | _World War 2_, _Battle of France_ |
| IDENTIFIER | systematized identifier such as phone number, email address, ISBN |  |
| INSTALLATION | structure built by humans | _Strasbourg Cathedral_, _Sforza Castle_ |
| <a style="text-decorations:none; #color:#265C83" href=#institution> INSTITUTION | organization of people and a location or structure that share the same name | _Yale University_, _European Patent Office_, the _British government_, _European Union_, _City Police_ |
| <a style="text-decorations:none; color:#08c" href=#legal> LEGAL | legal mentions such as article of law, convention, cases, treaty., etc. | _European Patent Convention_;  _Maastricht Treaty_; _Article 52(2)(c) and (3)_; _Roe v. Wade, 410 U.S. 113 (1973)_; _European Union Referendum Act 2015_ |
| <a style="text-decorations:none; color:#08c" href=#location> LOCATION | physical location, including planets and galaxies. | _Los Angeles_, _Northern Madagascar_, _Southern Thailand_, _Channel Islands_, _Earth_, _Milky Way_, _West Mountain_ |
| <a style="text-decorations:none; color:#08c" href=#measure> MEASURE | numerical amount, including an optional unit of measure | _1,500_, _six million_, _72%_,  |
| MEDIA | media organization or publication | _Le monde_, _The New York Times_ |
| <a style="text-decorations:none; color:#08c" href=#national> NATIONAL | relating to a location | _North American_, _German_, _British_ |		
| <a style="text-decorations:none; color:#08c" href=#organisation> ORGANISATION | organized group of people, with some sort of legal entity and concrete membership | _Alcoholics Anonymous_ |
| <a style="text-decorations:none; color:#08c" href=#period> PERIOD | date, historical era or other time period, time expressions | _January_, the _2nd half of 2010_, _1985-1989_, _from 1930 to 1945_, _since 1918_, the _first four years_ |
| PERSON | first, middle, last names and aliases of people and fictional characters | _John Smith_ |
| <a style="text-decorations:none; color:#08c" href=#person_type> PERSON_TYPE | person type or role classified according to group membership | _African-American_, _Asian_, _Conservative_, _Liberal_, _Jews_, _Communist_ |
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
Human-made object, including softwares.

[issue #16](https://github.com/kermitt2/grobid-ner/issues/16)

---
#### AWARD

---
#### BUSINESS

---
#### CONCEPT
➡ ** CONCEPT vs. PERSON_TYPE / NATIONAL** <br/>
Depending on the context, an entity may belong to the classes:

* NATIONAL when introducing a relation to a LOCATION.
* PERSON_TYPE when it is clear that it refers to the folks, not just in relation to a location.
* CONCEPT when refering to a language.

For example British in isolation can be labelled:

* NATIONAL: when related to the Great-Britain (LOCATION):
```xml
    a <ENAMEX type="NATIONAL">British</ENAMEX> historian
```
* PERSON_TYPE: for the British people, for example:
```xml
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> are great people.
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> emigrants
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> people are not great
```

* CONCEPT: if the entity refers to the British English language

[issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

---
#### CONCEPTUAL

---
#### CREATION
➡ Artistic creation, such as song, movie, book, TV show, etc.

➡ Full bibliographical references are **not** annotated.

[issue #19](https://github.com/kermitt2/grobid-ner/issues/19)

---
#### EVENT

---
#### IDENTIFIER

---
#### INSTALLATION

---
#### INSTITUTION
➡ Criteria to distinguish between ORGANISATION and INSTITUTION:

|  ORGANISATION | INSTITUTION |
| ----------- | ---------- |
| organised group of persons | group of persons which share a structure/a location |
| group of people within an institution | entity representing -on its own- a stable institution |
| random subset of an organization/institution | something established with some autonomy (ex. _city police_, _train police_, _auxiliary police_) |

  [issue #22](https://github.com/kermitt2/grobid-ner/issues/22)

---
#### LEGAL
➡ Legal mentions such as article of law, convention, cases, treaties, etc.

➡ there is a graduation between too general and more specific terms, for example:

  * _**Nazi policies**_, _**Nazi social policies**_: too general, not to be annotated (except for _**Nazi**_ as PERSON_TYPE)
  * _**anti-Jewish legislation**_, _**anti-semitic policy**_: more specific, annotated LEGAL

[issue #17](https://github.com/kermitt2/grobid-ner/issues/17)

---
#### LOCATION
➡ When there are modifiers along the location, they are included in the entity, for example:
```xml
- <ENAMEX type="LOCATION">Suvalkų area</ENAMEX>
- <ENAMEX type="LOCATION">Pakruojis local rural district</ENAMEX>
- <ENAMEX type="LOCATION">coast of Honolulu</ENAMEX>
```
➡ The articles and prepositions (_from_, _the_) are not included in the entity.

➡ In some cases surrounding elements are not included in the entity, for example _"west of the"_ in:
```xml
They established safe zones west of the <ENAMEX type="LOCATION">Rocky Mountains</ENAMEX>.
```
[issue #21](https://github.com/kermitt2/grobid-ner/issues/21)

---
#### MEASURE
➡ MEASURE is an exception to the Longest Entity Match convention [(issue #32)](https://github.com/kermitt2/grobid-ner/issues/32): a MEASURE entity is annotated separately only if it is at the beginning of the noun phrase, for example:
```xml
- <ENAMEX type="MEASURE">45</ENAMEX><ENAMEX type="PERSON">presidents of the United States</ENAMEX>
- <ENAMEX type="MEASURE">900</ENAMEX><ENAMEX type="PERSON_TYPE">Jews</ENAMEX>
```
➡ **Ordinals (ex. _first, second_)**

* They should be annotated as MEASURE, as long as they indicate a numerical order in a scale or quantify something (size, date, etc.).
For example, in:

  > _The history can be divided into four periods: the **first**, from 1919 to 1940_ </br>
  => **_first_** is annotated `<ENAMEX type="MEASURE">first</ENAMEX>`


* But referring expressions, or ordinals not really ordering or quantifying, should **not** be annotated MEASURE.
For example:

  > _of the two courses on linguistics, the **first** is less boring_ <span style="color:black">(referring expression)</span>

  > _there occurred a boycott of Jewish businesses, which was the **first** national antisemitic campaign_ <span style="color:black">(here **_first_** is more a synonym of _"for the first time"_ than an ordinal)</span>

➡ Expressions measuring nothing are not to be annotated, for example:

> _**One** of the founders of the Revisionist movement_

[issue #14](https://github.com/kermitt2/grobid-ner/issues/14)



---
#### MEDIA

---
#### NATIONAL

➡ ** NATIONAL vs. PERSON_TYPE / CONCEPT** <br/>
Depending on the context, an entity may belong to the classes:

* NATIONAL when introducing a relation to a LOCATION.
* PERSON_TYPE when it is clear that it refers to the folks, not just in relation to a location.
* CONCEPT when refering to a language.

For example British in isolation can be labelled:

* NATIONAL: when related to the Great-Britain (LOCATION):
```xml
    a <ENAMEX type="NATIONAL">British</ENAMEX> historian
```
* PERSON_TYPE: for the British people, for example:
```xml
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> are great people.
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> emigrants
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> people are not great
```

* CONCEPT: if the entity refers to the British English language

[issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

---
#### ORGANISATION
➡ Ethnic communities are not included in the class ORGANISATION, but in PERSON_TYPE [(issue #28)](https://github.com/kermitt2/grobid-ner/issues/28).

➡ Criteria to distinguish between ORGANISATION and INSTITUTION:

  |  ORGANISATION | INSTITUTION |
  | ----------- | ---------- |
  | organised group of persons | group of persons which share a structure/a location |
  | group of people within an institution | entity representing -on its own- a stable institution |
  | random subset of an organization/institution | something established with some autonomy (ex. _city police_, _train police_, _auxiliary police_) |

  [issue #22](https://github.com/kermitt2/grobid-ner/issues/22)

---
#### PERIOD

➡ Date, historical era or other time period.

➡ Surrounding elements must be included in the NE only if they qualify the range of period or/and change the period type:

* _**since 1930**_: all PERIOD.
* _**from 1930**_, _**from 1930 to 1945**_: both all PERIOD.
* _**after 1930**_, _**before 1930**_: both all PERIOD.
* _**next decade**_, _**last decade**_: both all PERIOD.
* _**7 years after the war**_: all PERIOD.
* _**between 2010 and 2015**_: all PERIOD.

but

* _as early as the **1930s**_: only _**1930s**_ is tagged PERIOD, because _as early as_ doesn't change the period (the 1930s).
* _during **1930**_ and _in **1930**_: the prepositions don't change the period interval, only _**1930**_ is tagged PERIOD.

➡ some terms may be too vague to annotate them as PERIOD, for example the adjective _**prewar**_. We may annotate it with other elements, for example LOCATION in the following case:
```xml
<ENAMEX type="LOCATION">prewar Nazi Germany</ENAMEX>
```

➡ PERIOD vs. EVENT: an event defines a period, but a period is not necessarily an event, so we annotate as EVENT, for example:

  * _during the time of the **Nazi occupation**_: EVENT
  * _during the **Czarist regime**_: EVENT

issues [#13](https://github.com/kermitt2/grobid-ner/issues/13) and [#25](https://github.com/kermitt2/grobid-ner/issues/25)

#### PERSON

---
#### PERSON_TYPE

➡ Even though it's an approximation, entities like _**Jewry**_ (which means Jewish community) are included in this class. [(issue #28)](https://github.com/kermitt2/grobid-ner/issues/28)

➡ **PERSON_TYPE vs. NATIONAL / CONCEPT** <br/>
Depending on the context, an entity may belong to the classes:

* NATIONAL when introducing a relation to a LOCATION.
* PERSON_TYPE when it is clear that it refers to the folks, not just in relation to a location.
* CONCEPT when refering to a language.

For example British in isolation can be labelled:

* NATIONAL: when related to the Great-Britain (LOCATION):
```xml
    a <ENAMEX type="NATIONAL">British</ENAMEX> historian
```
* PERSON_TYPE: for the British people, for example:
```xml
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> are great people.
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> emigrants
    the <ENAMEX type="PERSON_TYPE">British</ENAMEX> people are not great
```

* CONCEPT: if the entity refers to the British English language

[issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

<!-- TODO
➡ **PERSON_TYPE vs. CONCEPTUAL**

* Even if the entity doesn't modify a person, it's annotated as PERSON_TYPE, for example:
```xml
<ENAMEX type="PERSON_TYPE">Zionist</ENAMEX> events
<ENAMEX type="PERSON_TYPE">Jewish</ENAMEX> problems
```
These entities are annotated PERSON_TYPE as long as they can be substituted for another group of people. For example with `communist, liberal, blond people, diabetic, muffin lovers` **<span style="color:red">/!\ TO CHECK /!\ </span>**.

* Examples where the entity is included in a larger entity:
```xml
<ENAMEX type="ORGANISATION">Zionist movement</ENAMEX>
<ENAMEX type="ORGANISATION">Central Committee of the Zionist Union</ENAMEX>
```
[(issue #15)](https://github.com/kermitt2/grobid-ner/issues/15)

-->

---
#### PLANT

---
#### SPORT_TEAM

---
#### SUBSTANCE

---
#### TITLE

➡ Personal or honorific title, with a relatively loose definition. The [Wikipedia page](https://en.wikipedia.org/wiki/Title) examples can be useful. For example the following entities are annotated as TITLE: _**chairman**_, _**member**_, _**founder**_.

➡ For some terms, the context will determine the annotation. The term `engineer` for example can be a TITLE or not depending on the country:

*  In France or Germany it is linked with a specific diploma so it's annotated as TITLE if the term is linked to these countries.

* In UK or USA, it refers to the job, so it's **not** annotated.

➡ To decide between TITLE and PERSON:

* if only the TITLE is mentioned, it's annotated TITLE, even though it refers to a person. Examples of entities annotated TITLE:

    * _He is the **President of the United States**._
    * _He is **CEO** of this company._
    * _The **Chinese Prime minister** said this._
    * _The **Queen**_ <br />
<br />

* In case of the largest entity match of TITLE + PERSON, the priority goes to PERSON. For example _**The President of the United States Barack Obama**_ as a whole is annotated PERSON.

➡ The same principle applies between TITLE and PERSON_TYPE, for example this case of the largest entity match of TITLE + PERSON_TYPE:
```xml
<ENAMEX type="PERSON_TYPE">Members of the British Royal Family</ENAMEX> had fled.
```

issues [#12](https://github.com/kermitt2/grobid-ner/issues/12) and [#33](https://github.com/kermitt2/grobid-ner/issues/33)

---
#### UNKNOWN

---
#### WEBSITE

---

### Miscellaneous

➡ the classes may apply to fictive entities, for example:
```xml
- a multipurpose hand tool, the <ENAMEX type="ARTIFACT">"Lobotomizer"</ENAMEX> or <ENAMEX type="ARTIFACT">"Lobo"</ENAMEX> (...), for close-quarters combat.
- a tactic re-invented (...) during the "<ENAMEX type="EVENT">Great Panic</ENAMEX>"
```
[issue #24](https://github.com/kermitt2/grobid-ner/issues/24)



## Conventions

➡ For the class assignation to entities, GROBID NER follows the longest match convention. For instance, the entity _University of Minnesota_ as a whole (longest match) will belong to the class INSTITUTION. Its component _Minnesota_ is a LOCATION, but as it is part of a larger entity chunk, it will not be identified.

<!-- TODO
/!\ ATTENTION LE PRINCIPE DE LARGEST ENTITY MATCH A AUSSI UN PARAGRAPHE DANS "ANNOTATION GUIDELINES" !!! À UNIFIER

à ajouter dans largest entity match :
- examples:
issue #7 .
German South-West Africa -> all LOCATION
American Jewish Holocaust survivors -> PERSON_TYPE

- noter qu'il y a une exception au largest entity match bidule : la classe MEASURE lorsque c'est devant, etc., cf issue 32
-->

➡ There is no specific class for foreign words. They are annotated in one of the existing classes, if relevant, otherwise they are not annotated. In all cases, they are identified in parallel by another attribute, orthogonal to the entity class [(issue #37)](https://github.com/kermitt2/grobid-ner/issues/37).


➡ Punctuation (like quotation marks) are to be left outside the tags, for example: `"<ENAMEX type="PERSON_TYPE">socialists</ENAMEX>"` [(issue #26)](https://github.com/kermitt2/grobid-ner/issues/26).

➡ **Currencies** alone (_pound sterling_, _US dollar_) should not be annotated [(issue #23)](https://github.com/kermitt2/grobid-ner/issues/23).

➡ When there is a **dash**, it can be considered a space, for example _**Nobel prize-winning economist**_ is annotated [(issue #31)](https://github.com/kermitt2/grobid-ner/issues/31):
```xml
<ENAMEX type="AWARD">Nobel prize</ENAMEX>-winning economist
```


## Sense information

When possible, senses information are also assigned to entities in the form of one or several WordNet synsets.
