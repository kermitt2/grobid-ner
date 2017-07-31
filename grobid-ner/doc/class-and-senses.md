
GROBID NER identifies named-entities and classifies them in 27 classes, as compared to the 4-classes or 7-classes model of most of the existing NER open source tools (usually using the Reuters/CoNLL 2003 annotated corpus, or the MUC annotated corpus).

In addition the entities are often enriched with WordNet sense annotations to help further disambiguation and resolution of the entity. GROBID NER has been developed for the purposed of disambiguating and resolving entities against knowledge bases such as Wikipedia and FreeBase. Sense information can help to disambiguate the entity, because they refine based on contextual clues the entity class.

<h1>Named entity classes</h1>

## Classes quick overview

<!-- alt color for links #08c -->

The following table describes the 27 named entity classes produced by the model.

| Class name |  Description | Examples |
| ------------ | ----------- | ---------- |
| <a style="text-decorations:none; color:#265C83" href=#acronym> ACRONYM | acronym that doesn't belong to another class | _DIY, BYOD, IMHO_ |
| ANIMAL | individual name of an animal | _Hachikō_, _Jappeloup_ |
| <a style="text-decorations:none; color:#265C83" href=#artifact> ARTIFACT | human-made object, including softwares | _FIAT 634_, _Microsoft Word_ |
| AWARD | award for art, science, sport, etc. | _Balon d'or_, _Nobel prize_|
| BUSINESS | company / commercial organisation | _Air Canada_, _Microsoft_ |
| <a style="text-decorations:none; color:#265C83" href=#concept> CONCEPT | abstract concept not included in another class | _English_ (as language), _Communism_, _Zionism_ |
| <a style="text-decorations:none; color:#265C83" href=#conceptual> CONCEPTUAL | entity relating to a concept | _Greek_ myths, _eurosceptic_ doctrine |
| <a style="text-decorations:none; color:#265C83" href=#creation> CREATION | artistic creation, such as song, movie, book, TV show, etc. | _Monna Lisa_, _Mullaholland drive_, _Kitchen Nightmares_, _EU Referendum: The Great Debate_, _Europe: The Final Debate_ |
| <a style="text-decorations:none; color:#265C83" href=#event> EVENT | event | _World War 2_, _Battle of France_, _Brexit referendum_|
| IDENTIFIER | systematized identifier such as phone number, email address, ISBN |  |
| <a style="text-decorations:none; color:#265C83" href=#installation>INSTALLATION | structure built by humans | _Strasbourg Cathedral_, _Sforza Castle_, _Auschwitz camp_ |
| <a style="text-decorations:none; color:#265C83" href=#institution> INSTITUTION | organization of people and a location or structure that share the same name | _Yale University_, _European Patent Office_, the _British government_, _European Union_, _City Police_ |
| <a style="text-decorations:none; color:#265C83" href=#legal> LEGAL | legal mentions such as article of law, convention, cases, treaty., etc. | _European Patent Convention_;  _Maastricht Treaty_; _Article 52(2)(c) and (3)_; _Roe v. Wade, 410 U.S. 113 (1973)_; _European Union Referendum Act 2015_ |
| <a style="text-decorations:none; color:#265C83" href=#location> LOCATION | physical location, including planets and galaxies. | _Los Angeles_, _Northern Madagascar_, _Southern Thailand_, _Channel Islands_, _Earth_, _Milky Way_, _West Mountain_, _Warsaw Ghetto_ |
| <a style="text-decorations:none; color:#265C83" href=#measure> MEASURE | numerical amount, including an optional unit of measure | _1,500_, _six million_, _72%_, _50°2′9″N 19°10′42″E_ |
| MEDIA | media organization or publication | _Le monde_, _The New York Times_ |
| <a style="text-decorations:none; color:#265C83" href=#national> NATIONAL | relating to a location | _North American_, _German_, _British_ |		
| <a style="text-decorations:none; color:#265C83" href=#organisation> ORGANISATION | organized group of people, with some sort of legal entity and concrete membership | _Alcoholics Anonymous_, _Jewish resistance_, _Polish undergound_ |
| <a style="text-decorations:none; color:#265C83" href=#period> PERIOD | date, historical era or other time period, time expressions | _January_, the _2nd half of 2010_, _1985-1989_, _from 1930 to 1945_, _since 1918_, the _first four years_ |
| PERSON | first, middle, last names and aliases of people and fictional characters | _John Smith_ |
| <a style="text-decorations:none; color:#265C83" href=#person_type> PERSON_TYPE | person type or role classified according to group membership | _African-American_, _Asian_, _Conservative_, _Liberal_, _Jews_, _Communist_ |
| PLANT | name of a plant | _Ficus religiosa_ |
| SPORT_TEAM | sport group or organisation | _The Yankees_ |
| SUBSTANCE | natural substance | |
| <a style="text-decorations:none; color:#265C83" href=#title> TITLE | personal or honorific title, for a person | _Mr._, _Dr._, _General_, _President_, _chairman_, _doctor_, _Secretary of State_, _MP_, _Prime Minister_ |
| <a style="text-decorations:none; color:#265C83" href=#unknown> UNKNOWN | entity not belonging to any previous classes| _Plan Marshall_, _ParSiTi_, _Horizon 2020_ |
| WEBSITE | website URL or name | _Wikipedia_, http://www.inria.fr |

## Classes Specific guidelines
### ACRONYM
Acronyms that don't belong to another class. For example:

* _**DIY**_: ACRONYM

but

* _**the United Nations** (**UN**)_: _United Nations_ and _UN_ are tagged ORGANISATION
* _**WW1**_: EVENT

[issue #20](https://github.com/kermitt2/grobid-ner/issues/20)

---
### ANIMAL

---
### ARTIFACT
Human-made object, including softwares.

[issue #16](https://github.com/kermitt2/grobid-ner/issues/16)

---
### AWARD

---
### BUSINESS

---
### CONCEPT
➡ Sometimes an entity, in isolation, can be ambiguous, for example _**British**_. When it refers to the British English language, it's annotated CONCEPT. (issues [#29](https://github.com/kermitt2/grobid-ner/issues/29) and [#30](https://github.com/kermitt2/grobid-ner/issues/30)).

---
### CONCEPTUAL

➡ entity relating to a concept

➡ desambiguation between PERSON_TYPE and CONCEPTUAL

  |  | PERSON_TYPE | CONCEPTUAL |
  |  | ----------- | ---------- |
  | Criteria | refers to people, a folk, a group of people | modify a common name |
  | Examples | _the **eurosceptics** held a protest_ <br/> _the **Communists** held a protest_ <br/> _the **Jewish** held a protest_ | _the **eurosceptic** doctrine  <br/> the **communist** doctrine <br/> **Zionist** newspapers <br/> **Greek** myths <br/> **Christian** newspapers_ |

  [issue #29](https://github.com/kermitt2/grobid-ner/issues/29)

---
### CREATION
➡ Artistic creation, such as song, movie, book, TV show, etc.

➡ Full bibliographical references are **not** annotated.

[issue #19](https://github.com/kermitt2/grobid-ner/issues/19)

---
### EVENT


➡ PERIOD vs. EVENT: an event defines a period, but a period is not necessarily an event, so we annotate as EVENT, for example:

  * _during the time of the **Nazi occupation**_: EVENT
  * _during the **Czarist regime**_: EVENT

[issue #13](https://github.com/kermitt2/grobid-ner/issues/13)

---
### IDENTIFIER

---
### INSTALLATION
➡ Sometimes a LOCATION name refers to an INSTALLATION name. In that case it's annotated as INSTALLATION. For example Nazi camps [(issue #42)](https://github.com/kermitt2/grobid-ner/issues/42):
```xml
- <ENAMEX type="INTALLATION">Auschwitz</ENAMEX>
- <ENAMEX type="INTALLATION">Lager Nordhausen</ENAMEX>
- <ENAMEX type="INTALLATION">Mittelbau-Dora</ENAMEX>
- <ENAMEX type="INTALLATION">Mauthausen-Gusen concentration camp</ENAMEX>
```

---
### INSTITUTION

➡ Criteria to distinguish between ORGANISATION and INSTITUTION:

|  ORGANISATION | INSTITUTION |
| ----------- | ---------- |
| organised group of persons | group of persons which share a structure/a location |
| group of people within an institution | entity representing -on its own- a stable institution |
| random subset of an organization/institution | something established with some autonomy (ex. _city police_, _train police_, _auxiliary police_) |

  [issue #22](https://github.com/kermitt2/grobid-ner/issues/22)

➡ INSTITUTION vs LOCATION: an INSTITUTION entity is defined as a set of legal entities and not a fixed location. Example:

 **_European Union_**, which is not defined by a (fixed) location, but as a set of legal entities with a treaty and particular instances. It may become a fixed location after a long time of integration (like the USA, where the Federal State is an institution).

[issue #29](https://github.com/kermitt2/grobid-ner/issues/29)

➡ There is no ambiguity between INSTITUTION and PERSON_TYPE. Therefore, even if an INSTITUTION entity applies to a group of people, it won't be annotated PERSON_TYPE, for example:
```xml
<ENAMEX type="INSTITUTION">European Union</ENAMEX> citizens
```
[issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

---
### LEGAL
➡ Legal mentions such as article of law, convention, cases, treaties, etc.

➡ there is a graduation between too general and more specific terms, for example:

  * _**Nazi policies**_, _**Nazi social policies**_: too general, not to be annotated (except for _**Nazi**_ as PERSON_TYPE)
  * _**anti-Jewish legislation**_, _**anti-semitic policy**_: more specific, annotated LEGAL

[issue #17](https://github.com/kermitt2/grobid-ner/issues/17)

---
### LOCATION
➡ LOCATION vs INSTITUTION: an INSTITUTION entity is defined as a set of legal entities (for example **_European Union_**) and not a fixed location [(issue #29)](https://github.com/kermitt2/grobid-ner/issues/29).

➡ There is no disambiguation at this level between the different uses of country names (as location, government, army, etc.) [(issue #29)](https://github.com/kermitt2/grobid-ner/issues/29). For example in:
> _**Austria** invaded **Italy**_

they're both annotated LOCATION event though here _Austria_ refers to _Austria's army_ and _Italy_ to the location.

➡ When there are modifiers (geographical, political, etc.) along the location, they are included in the entity, as long as the result refers to a territory. For example:
```xml
- <ENAMEX type="LOCATION">Suvalkų area</ENAMEX>
- <ENAMEX type="LOCATION">Pakruojis local rural district</ENAMEX>
- <ENAMEX type="LOCATION">coast of Honolulu</ENAMEX>
- <ENAMEX type="LOCATION">German-occupied Poland</ENAMEX>
- <ENAMEX type="LOCATION">Nazi Germany</ENAMEX>
```
issues [#21](https://github.com/kermitt2/grobid-ner/issues/21) and [#32](https://github.com/kermitt2/grobid-ner/issues/32)

➡ The articles and prepositions (_from_, _the_) are not included in the entity.

➡ In some cases surrounding elements are not included in the entity, for example _"west of the"_ in:
```xml
They established safe zones west of the <ENAMEX type="LOCATION">Rocky Mountains</ENAMEX>.
```
[issue #21](https://github.com/kermitt2/grobid-ner/issues/21)

---
### MEASURE
➡ Markers of intervals like _**over**_ or _**more**_ are included in the MEASURE tag, example [(issue #43)](https://github.com/kermitt2/grobid-ner/issues/43):
```xml
<ENAMEX type="MEASURE">Over 7,000</ENAMEX> shops and <ENAMEX type="MEASURE">more
than 1,200</ENAMEX> synagogues were damaged or destroyed.
```

➡ MEASURE is an exception to the Longest Entity Match convention [(issue #32)](https://github.com/kermitt2/grobid-ner/issues/32): a MEASURE entity is annotated separately only if it is at the beginning of the noun phrase, for example:
```xml
- <ENAMEX type="MEASURE">45</ENAMEX><ENAMEX type="PERSON">presidents of the USA</ENAMEX>
- <ENAMEX type="MEASURE">900</ENAMEX><ENAMEX type="PERSON_TYPE">Jews</ENAMEX>
```
➡ **Ordinals (ex. _first, second_)** [(issue #14)](https://github.com/kermitt2/grobid-ner/issues/14)

* They should be annotated as MEASURE, as long as they indicate a numerical order in a scale or quantify something (size, date, etc.) that we can **enumerate**. For example:

      * <span style="color:#848484">_The history can be divided into four periods: the_</span> **_first_**<span style="color:#848484">, _from 1919 to 1940_</span> <br/>

      * <span style="color:#848484">_there occurred a boycott of Jewish businesses, which was the_</span> **_first_** <span style="color:#848484">_national antisemitic campaign_</span> (the "first campaign" is the boycott) <br/>

      * **_second_** <span style="color:#848484">_place in the 2009 European elections and_</span> **_first_** <span style="color:#848484">_place in the 2014 European elections_</span> <br/>

      * <span style="color:#848484">_his was the_</span> **_first_** <span style="color:#848484">_time since the 1910 general election_</span> <br/>
      
      * <span style="color:#848484">_These were their_</span> **_first_** <span style="color:#848484">_elected MPs_</span> <br/> <br/>
      

* But referring expressions, or ordinals not really ordering or quantifying, should **not** be annotated MEASURE.
For example:

      * Phrases like <span style="color:#848484">_among the first to be sent to concentration camps_</span>, or <span style="color:#848484">_one of the first_</span> where there is no notion of scale but rather of "beginning".

      * Plurals like in <span style="color:#848484">_the first jews to be deported_</span>.

        => in these examples it's impossible to enumerate precisely what is « first ». Furthermore, it can't really be replaced by "second" or "third".

➡ Expressions measuring nothing are not to be annotated, for example [(issue #14)](https://github.com/kermitt2/grobid-ner/issues/14):

> _**One** of the founders of the Revisionist movement_

➡ GPS coordinates are a MEASURE (numerical amounts + units), example `50°2′9″N 19°10′42″E`. [(issue #44)](https://github.com/kermitt2/grobid-ner/issues/44)

---
### MEDIA

---
### NATIONAL

➡ desambiguation between PERSON_TYPE and NATIONAL

  |  | PERSON_TYPE | NATIONAL |
  |  | ----------- | ---------- |
  | Criteria | refers to people, a folk, a group of people | refers to a LOCATION |
  | Examples | _the **British** are great people. <br/> the **British** emigrants <br/> the **British** people are not great_ | _a **British** newspaper <br/> a **British** historian_ |

  [issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

---
### ORGANISATION
➡ Ethnic communities are not included in the class ORGANISATION, but in PERSON_TYPE [(issue #28)](https://github.com/kermitt2/grobid-ner/issues/28).

➡ Criteria to distinguish between ORGANISATION and INSTITUTION:

  |  ORGANISATION | INSTITUTION |
  | ----------- | ---------- |
  | organised group of persons | group of persons which share a structure/a location |
  | group of people within an institution | entity representing -on its own- a stable institution |
  | random subset of an organization/institution | something established with some autonomy (ex. _city police_, _train police_, _auxiliary police_) |

  [issue #22](https://github.com/kermitt2/grobid-ner/issues/22)

➡ Sometimes another entity type is included in the ORGANISATION, according to the largest entity match principle, for example:
  ```xml
  <ENAMEX type="ORGANISATION">Zionist movement</ENAMEX>
  <ENAMEX type="ORGANISATION">Central Committee of the Zionist Union</ENAMEX>
  ```
  [(issue #15)](https://github.com/kermitt2/grobid-ner/issues/15)

---
### PERIOD

➡ Date, historical era or other time period, including time measurements like **_a week_**, **_one day_**, which are quantified measures of time (a PERIOD is a MEASURE but the opposite is not always true, so PERIOD, more specific, wins). [(issue #41)](https://github.com/kermitt2/grobid-ner/issues/41)

➡ The PERIOD may include precise elements like:
```xml
<ENAMEX type="PERIOD">mid afternoon on 27 June 2016</ENAMEX>
```
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
* _**seven-year** low_: only _**seven-year**_ is tagged PERIOD, since _low_ has no impact on the PERIOD

➡ some terms may be too vague to annotate them as PERIOD, for example the adjective _**prewar**_. We may annotate it with other elements, for example LOCATION in the following case:
```xml
<ENAMEX type="LOCATION">prewar Nazi Germany</ENAMEX>
```

➡ Intervals of time defined with surrounding elements like _after_ or _since_ are only considered PERIODs **if they are defined regarding to an EVENT**. For example these entities are all PERIODs (surrounding element + EVENT):
```xml
- <ENAMEX type="PERIOD">7 years after the war</ENAMEX> there was a great boom

- <ENAMEX type="PERIOD">Ten years after the official end of the zombie war</ENAMEX>
```
but these aren't (surrounding element and its dependencies excluded):
```xml
- withhold social benefits to new immigrants for the <ENAMEX type="PERIOD">first
 four years</ENAMEX> after they arrived

- The Treaties shall cease to apply to the State in question (...) <ENAMEX type="PERIOD">two
years</ENAMEX>after the notification referred to in paragraph 2

- <ENAMEX type="PERIOD">Seven years</ENAMEX> after the outbreak began
```

➡ PERIOD vs. EVENT: an event defines a period, but a period is not necessarily an event, so we annotate as EVENT, for example:

  * _during the time of the **Nazi occupation**_: EVENT
  * _during the **Czarist regime**_: EVENT

issues [#13](https://github.com/kermitt2/grobid-ner/issues/13) and [#25](https://github.com/kermitt2/grobid-ner/issues/25)

### PERSON

---
### PERSON_TYPE

➡ Even though it's an approximation, entities like _**Jewry**_ (which means Jewish community) are included in this class. [(issue #28)](https://github.com/kermitt2/grobid-ner/issues/28)

➡ Some entities, in isolation, may belong to several classes, depending on the context. For example British in isolation can be labelled:

* NATIONAL when introducing a relation to Great Britain (LOCATION):
```xml
    A <ENAMEX type="NATIONAL">British</ENAMEX> historian
```
* PERSON_TYPE when it is clear that it refers to the folks, not just in relation to a location:
```xml
    The <ENAMEX type="PERSON_TYPE">British</ENAMEX> are great people.
    The <ENAMEX type="PERSON_TYPE">British</ENAMEX> emigrants
    The <ENAMEX type="PERSON_TYPE">British</ENAMEX> people are not great
```
* CONCEPT when refering to the British language

* CONCEPTUAL when refering to a CONCEPT, for example here the British culture:
```xml
    The <ENAMEX type="CONCEPTUAL">British</ENAMEX> folklore.
```
[issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

➡ desambiguation between PERSON_TYPE and NATIONAL

  |  | PERSON_TYPE | NATIONAL |
  |  | ----------- | ---------- |
  | Criteria | refers to people, a folk, a group of people | refers to a LOCATION |
  | Examples | _the **British** are great people. <br/> the **British** emigrants <br/> the **British** people are not great_ | _a **British** newspaper <br/> a **British** historian_ |

  [issue #30](https://github.com/kermitt2/grobid-ner/issues/30)

➡ desambiguation between PERSON_TYPE and CONCEPTUAL

  |  | PERSON_TYPE | CONCEPTUAL |
  |  | ----------- | ---------- |
  | Criteria | refers to people, a folk, a group of people | modify a common name |
  | Examples | _the **eurosceptics** held a protest_ <br/> _the **Communists** held a protest_ <br/> _the **Jewish** held a protest_ | _the **eurosceptic** doctrine  <br/> the **communist** doctrine <br/> **Zionist** newspapers <br/> **Greek** myths <br/> **Christian** newspapers_ |

  [issue #29](https://github.com/kermitt2/grobid-ner/issues/29)



➡ If an entity in isolation cannot be PERSON_TYPE, it's not annotated PERSON_TYPE even if it fits the criteria above. For example the INSTITUTION _**European Union**_ cannot be PERSON_TYPE when alone, so **_EU citizens_** is annotated:
```xml
<ENAMEX type="INSTITUTION">EU</ENAMEX> citizens
```
[issue #30](https://github.com/kermitt2/grobid-ner/issues/30)


---
### PLANT

---
### SPORT_TEAM

---
### SUBSTANCE

---
### TITLE

➡ Personal or honorific title, **applied to a person**, with a relatively loose definition. The [Wikipedia page](https://en.wikipedia.org/wiki/Title) examples can be useful. For example the following entities are annotated as TITLE: _**chairman**_, _**president**_, _**captain**_ .

➡ Generally, the job names (ex. _**economist**_, _**carpenter**_) are not annotated. For some terms, the context will determine the annotation. _**engineer**_ for example can be a TITLE or not depending on the country:

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

➡ Various examples

```xml
- under the direction of the <ENAMEX type="TITLE">National State Archivist</ENAMEX>
(who holds his office in the <ENAMEX type="INSTITUTION">National Archives</ENAMEX>)

- <ENAMEX type="TITLE">Wehrmacht officer</ENAMEX>
- <ENAMEX type="TITLE">Wehrmacht officers</ENAMEX>
- <ENAMEX type="TITLE">German SS officers</ENAMEX>
- <ENAMEX type="TITLE">senior military officers</ENAMEX>
```
| | TITLE | not TITLE |
| | ----- | --------- |
| member | - Member of Parliament <br/> - Member of Congress <br/> - Board member | - members of the British Royal Family <br/> - the Eurozone members <br/> - members of the SS <br/> - party member |
| leader | - Great Leader of North Korea <br/> - Supreme Leader of Iran <br/> | - leader of the Zionist movement <br/> - Nazi leader <br/> - council leaders of the ghetto <br/> - Jewish resistance leaders |

issues [#12](https://github.com/kermitt2/grobid-ner/issues/12) and [#33](https://github.com/kermitt2/grobid-ner/issues/33)

---
### UNKNOWN
➡ Entities not covered by another class.

➡ Examples:

* _**Plan Marshall**_
* _**Horizon 2020**_ (a funding programme)
* _**Antisemitism Yellowbadge logo**_
* _**Yellow badge**_
* _**Aktion T4 euthanasia programme**_
* _**Aktion T4**_

[issue #39](https://github.com/kermitt2/grobid-ner/issues/39)

---
### WEBSITE

---

## Miscellaneous

➡ the classes may apply to fictive entities, for example:
```xml
- a multipurpose hand tool, the <ENAMEX type="ARTIFACT">"Lobotomizer"</ENAMEX> or
 <ENAMEX type="ARTIFACT">"Lobo"</ENAMEX> (...), for close-quarters combat.

- a tactic re-invented (...) during the "<ENAMEX type="EVENT">Great Panic</ENAMEX>"
```
[issue #24](https://github.com/kermitt2/grobid-ner/issues/24)

➡ There is no specific class for foreign words. They are **annotated in one of the existing classes, if relevant (whether they are written in latin or non-latin characters)**. Otherwise they are not annotated. In all cases, they are identified in parallel by another attribute, orthogonal to the entity class [(issue #37)](https://github.com/kermitt2/grobid-ner/issues/37).

➡ Generic terms in referring expressions are **not annotated**, even if they refer to a named entity. Example:

  * _Germany was losing the **war**_ (refers to an EVENT)
  * _broader trends in **world** history_ (refers to a LOCATION)
  * _bringing the first credible news to the **world** of the mass murder that was taking place there_ (refers to a LOCATION)

  [issue #45](https://github.com/kermitt2/grobid-ner/issues/45)

➡ Punctuation (like quotation marks) are to be left outside the tags, for example: `"<ENAMEX type="PERSON_TYPE">socialists</ENAMEX>"` [(issue #26)](https://github.com/kermitt2/grobid-ner/issues/26).

➡ **Currencies** alone (_pound sterling_, _US dollar_) should not be annotated [(issue #23)](https://github.com/kermitt2/grobid-ner/issues/23).

➡ When there is a **dash**, it can be considered a space, for example _**Nobel prize-winning economist**_ is annotated [(issue #31)](https://github.com/kermitt2/grobid-ner/issues/31):
```xml
<ENAMEX type="AWARD">Nobel prize</ENAMEX>-winning economist
```
## Out of scope

➡ Specific but common concepts already enumerated in Wikipedia, for example **_patient zero_**. Indeed, named entity classes correspond more to particular classes of entities that cannot be enumerated exhaustively in advance.

➡ Specialist terminology (biomedical, for example). Other specialized NER are used.

➡ Tables from Wikipedia have been removed from the annotated corpus (issues [#49](https://github.com/kermitt2/grobid-ner/issues/49) and [#50](https://github.com/kermitt2/grobid-ner/issues/50)).

## Sense information

When possible, senses information are also assigned to entities in the form of one or several WordNet synsets.
