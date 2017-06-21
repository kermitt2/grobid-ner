
[issue#32](https://github.com/kermitt2/grobid-ner/issues/32)

# Principle
<!-- TODO synthétiser cette partie -->

* Let's consider the token _British_. Depending on the context, _British_ in isolation can be labelled with the classes:
    - NATIONAL (when introducing a relation to Great Britain)
    - PERSON_TYPE (for the British people)
    - CONCEPT (when refering to the British English language).
    <br/>
    <br/>

    In contrast, _British Brexit referendum_ is entirely labeled with the class EVENT, because _British_ is part of a larger entity mention. The fact that British here also refers to the country (class NATIONAL) must not be annotated. _British government_ is similarly entirely labeled with class INSTITUTION. <br/>
    <br/>

* Similarly, in order to be consistent, for phrases like _President of the United States_ and _United States President_, the class labeling will be identical, entirely as TITLE. The manual annotator must be careful not to annotated two NE following (in particular for the first case, with _United States_ as LOCATION), and in general to annotate only the largest entity.  

    For instance, the entity _University of Minnesota_ as a whole (longest match) will belong to the class INSTITUTION. Its component _Minnesota_ is a LOCATION, but as it is part of a larger entity chunk, it will not be identified. <br/>
    <br/>

* The overall type is given by the entity which is the **semantic head of the NP**. For example:

```xml
  - <ENAMEX type="PERSON">Australian Prime Minister Malcolm Turnbull</ENAMEX>

  - <ENAMEX type="PERSON">Indonesian president Joko Widodo</ENAMEX>
```

# Exception for the MEASURE class

If a named entity of class MEASURE determines/quantifies a named entity NP, they are annotated separately, because it's a modification of the quantity, not a characteristic of the entity. For example:
```xml
- During the war some <ENAMEX type="MEASURE">900</ENAMEX> <ENAMEX type="PERSON_TYPE">Jews
</ENAMEX> passed through the Banjica concentration camp.

- There are <ENAMEX type="MEASURE">45</ENAMEX> <ENAMEX type="TITLE">presidents of the United
States</ENAMEX>.

- The <ENAMEX type="MEASURE">2</ENAMEX> <ENAMEX type="CREATION">Kill Bill</ENAMEX> movies had
been a success.

- about <ENAMEX type="MEASURE">1.3 million</ENAMEX> <ENAMEX type="NATIONAL">Indian</ENAMEX>
soldiers and labourers served in <ENAMEX type="LOCATION">Europe</ENAMEX>
```
This applies **only if the MEASURE entity is at the beginning of the NE**. If the MEASURE entity is inside the NE, the latter is annotated as a whole, according to the largest entity match principle, such as in:
```xml
- the <ENAMEX type="TITLE">President of the 500 senators</ENAMEX>.

- the <ENAMEX type="TITLE">doctor of the two Presidents of the Republic</ENAMEX>.
```


# Entities separated by non-entities

Non-NEs lexical words are not included in large named entities. Only functional words (like articles or prepositions) can be a part of a large entity. Therefore the largest entity match principle may not apply in certain cases. For example:
```xml
<ENAMEX type="PERSON_TYPE">Republican</ENAMEX> candidate <ENAMEX type="PERSON">Donald
 Trump</ENAMEX>
```

# Coordination

Coordinated words are annotated as one entity. For example:

* Coordination of two modifiers
```xml
  Though the vast majority of the Jews affected and killed during Holocaust were of Ashkenazi
  descent, <ENAMEX type="PERSON_TYPE">Sephardi and Mizrahi Jews</ENAMEX> suffered greatly as
  well.
```

* Coordinations of two NPs
```xml
- first time a party other than the <ENAMEX type="PERSON_TYPE">Conservatives or Labour</ENAMEX>
 had topped a nationwide poll in 108 years

- <enamex type="LOCATION">New-York and Paris</enamex> are big cities.
```

# Apposition

Two named entities in apposition are annotated as one NE. If there is a comma, its role is equivalent to a functional word and introduces an apposition, therefore it does not split the entity. For example:

```xml
- Meanwhile, <ENAMEX type="PERSON">Nigel Farage, leader of the anti-EU UKIP</ENAMEX> stood
down after his party's long-term ambition had been accomplished.

- Meanwhile, the <ENAMEX type="PERSON">leader of the anti-EU UKIP, Nigel Farage</ENAMEX> stood
down after his party's long-term ambition had been accomplished.

- Meanwhile, the <ENAMEX type="PERSON">leader of the anti-EU UKIP Nigel Farage</ENAMEX> stood
down after his party's long-term ambition had been accomplished.
```

# Parentheses

One of the entities in a large match entity may be between parentheses. There are different cases, for example:

* The entity between parentheses does not refer to the others entities in the large match. Example:
```xml
  <ENAMEX type="EVENT">Kristallnacht (1938)</ENAMEX>
```
Here, the date is an adjunct to the event.

* Both have the same referent. Examples:
```xml
  run by the <ENAMEX type="INSTITUTION">Sturmabteilung (SA)</ENAMEX> and the
  <ENAMEX type="INSTITUTION">Schutzstaffel (SS)</ENAMEX>
```

# Choosing between PERSON and TITLE

There may be some ambiguity between PERSON and TITLE. To decide:

* If the patronym of the person is mentioned, it's annotated as PERSON, for example `<ENAMEX type="PERSON">Barack Obama</ENAMEX>`

* If only the title of the person is mentioned, it's annotated as TITLE, for example `the <ENAMEX type="TITLE">President of the United States</ENAMEX>`

* If the large NE contains both the title and the patronym of the person, it's annotated as PERSON, for example `the <ENAMEX type="PERSON">President of the United States Donald Trump</ENAMEX>`

Indeed, a person stays the same person, whereas its title may change. Also, a title is also a person (almost always), so we could use either TITLE or PERSON. But since TITLE is more specific than PERSON and has the semantic focus, it is chosen.

[issue #33](https://github.com/kermitt2/grobid-ner/issues/33)

# Various examples

<!-- TODO add more examples -->

```xml
- While	attending the <ENAMEX type="EVENT">May 2012 NATO summit meeting</ENAMEX>

- <ENAMEX type="PERSON">Obama</ENAMEX> was the <ENAMEX type="TITLE">US President</ENAMEX>.

- She is the <ENAMEX type="TITLE">CEO of IBM</ENAMEX>.

- The <ENAMEX type="TITLE">President of Argentina</ENAMEX> said no.

- <ENAMEX type="LOCATION">German South-West Africa</ENAMEX>

- <ENAMEX type="PERSON_TYPE">American Jewish Holocaust survivors</ENAMEX>

- <ENAMEX type="TITLE">chairman of the Central Committee of the World Sephardi
Federation and a member of the Knesset</ENAMEX>

```

[issue#32](https://github.com/kermitt2/grobid-ner/issues/32)
