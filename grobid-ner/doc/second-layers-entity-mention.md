
# Principle
<!-- TODO add rules and examples -->

A second level of annotation has been added, typically to reduce very long entity (following the [largest entity](http://grobid-ner.readthedocs.io/en/latest/largest-entity-mention/) rules).
<br/>
This second level of annotation is indicated with the subType attribue and his values fixed to "2" in ENAMEX tags (<ENAMEX subType="2" type="SOME_CLASSE" where SOME_CLASS correspond to a class present in the [TAGSET](http://grobid-ner.readthedocs.io/en/latest/class-and-senses/#classes-quick-overview) )
<br/>
An automatic choice beetween largest entities (first annotation process) and a second layer entities (second annotation process) is processed to build a new model. 
<br/>
The principle is to **add or divise** annotations present in a larger annotation, one level bellow (see [examples](http://grobid-ner.readthedocs.io/en/latest/second-layers-entity-mention/#various-examples)).

# Cases where second-layers applied

* Lists of same entities
    - Separated by words like "and", "or" :
```xml
- During the war some <ENAMEX type="LOCATION"><ENAMEX subType="2" type="LOCATION">France</ENAMEX> and <ENAMEX subType="2" type="LOCATION">Germany</ENAMEX></ENAMEX> passed throug [...]
```
    - Separated by ponctuations like commas, semicolon :
```xml
- Those country composed of <ENAMEX type="LOCATION"><ENAMEX subType="2" type="LOCATION">France</ENAMEX>, <ENAMEX subType="2" type="LOCATION">United Kingdom</ENAMEX>, <ENAMEX subType="2" type="LOCATION">Soviet Union</ENAMEX> and the <ENAMEX subType="2" type="LOCATION">United States</ENAMEX></ENAMEX> will lead to [...]
```

* Specific topic related to a larger topic
    - Article and correspondance on a treaty of law separated by words (Warning about this one close to some entity not annotated see [Cases where second-layers not applied](http://grobid-ner.readthedocs.io/en/latest/second-layers-entity-mention/#various-examples)) :
```xml
- [...]<ENAMEX type="LEGAL"><ENAMEX subType="2" type="LEGAL">Article 50</ENAMEX> of the <ENAMEX subType="2" type="LEGAL">Treaty On European Union</ENAMEX></ENAMEX>[...]
```

* Adding of informations (différent classes in one large entity)
    - `INSTITUTION` in `LEGAL`:
```xml
- [...]<ENAMEX type="LEGAL"><ENAMEX subType="2" type="INSTITUTION">UE</ENAMEX> law – <ENAMEX subType="2" type="LEGAL">Article 50</ENAMEX></ENAMEX>[...]
```


# Cases where second-layers not applied 

When we choose to use a second-layers, all informations grouped by the largest entity match should be kept. We do not want to lose informations.

* Shorten annotation

<span style="color:red">We can't do : </span>
`[...] <ENAMEX type="MEASURE"><ENAMEX subType="2" type="MEASURE">eleven</ENAMEX> million</ENAMEX> [...]`</span>

<span style="color:green">We keep : </span>
`[...] <ENAMEX type="MEASURE">eleven million</ENAMEX> [...]`

* Multiple possible annotations sticked

<span style="color:red">We can't do : </span>
`[...] <ENAMEX type="LEGAL"><ENAMEX subType="2" type="LEGAL">European Union Referendum Act</ENAMEX> <ENAMEX subType="2" type="PERIOD">2015</ENAMEX></ENAMEX> [...]`</span>

<span style="color:green">We keep : </span>
`[...] <ENAMEX type="LEGAL">European Union Referendum Act 2015</ENAMEX> [...]`

* Locations occupied by an another location

<span style="color:red">We can't do : </span>
`[...] <ENAMEX type="LOCATION"><ENAMEX subType="2" type="NATIONAL">German</ENAMEX>-occupied <ENAMEX subType="2" type="LOCATION">Poland</ENAMEX></ENAMEX> [...]`</span>

<span style="color:green">We keep : </span>
`[...] <ENAMEX type="LOCATION">German-occupied Poland</ENAMEX> [...]`

* No possibility to keep the original tags

<span style="color:red">We can't do : </span>
`[...] <ENAMEX type="LEGAL"><ENAMEX subType="2" type="NATIONAL">German</ENAMEX>-<ENAMEX subType="2" type="NATIONAL">Soviet agreement</ENAMEX></ENAMEX> [...]`</span>

Because we will lose the original tag (here `LEGAL`), and `agreement` alone cannot be annotated as `LEGAL` (to eventually keep the original tag, made by the largest entity match).

<span style="color:green">We keep : </span>
`[...] <ENAMEX type="LEGAL">German-Soviet agreement</ENAMEX> [...]`

or 

<span style="color:red">We can't do : </span>
`[...] <ENAMEX type="INSTITUTION"><ENAMEX subType="2" type="NATIONAL">French</ENAMEX> Army</ENAMEX> [...]`</span>

Because we will lose the original tag (here `INSTITUTION`).

<span style="color:green">We keep : </span>
`[...] <ENAMEX type="LEGAL">German-Soviet agreement</ENAMEX> [...]`


# Various examples

<!-- TODO add more examples -->

```xml
- <ENAMEX type="EVENT"><ENAMEX subType="2" type="EVENT">World War II</ENAMEX> (<ENAMEX subType="2" type="PERIOD">1948-1945</ENAMEX>)</ENAMEX>

- <ENAMEX type="LEGAL">European Communities Act 1972</ENAMEX>

- <ENAMEX type="MEASURE">over 44</ENAMEX>

- <ENAMEX type="MEASURE"><ENAMEX subType="2" type="MEASURE">3</ENAMEX> or <ENAMEX subType="2" type="MEASURE">over 4</ENAMEX></ENAMEX> houses [...]

- 

- 

- 

```
