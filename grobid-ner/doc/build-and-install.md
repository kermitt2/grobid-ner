GROBID NER is a module of [Grobid](https://github.com/kermitt2/grobid) . 

## Grobid Installation

GROBID is library for extracting bibliographical information from technical and scientific documents. 
The tool offers a convenient environment for creating efficient text mining tool based on CRF.

Clone source code from github:

```bash
> git clone https://github.com/kermitt2/grobid.git
```

Or download directly the zip file:

```bash
> https://github.com/kermitt2/grobid/zipball/master
```

<!--- ## [Build the project](https://github.com/kermitt2/grobid/wiki/Build-the-project) -->

The standard method for building the project is to use maven. In the main directory:

```bash
> mvn -Dmaven.test.skip=true clean install
```

Grobid should then be installed and ready.

It is also possible to build the project with ant.

```bash
> ant package
```

## Grobid NER Settings

Clone source code from github:

```bash
> git clone https://github.com/kermitt2/grobid-ner.git
```

Or download directly the zip file:

```bash
> https://github.com/kermitt2/grobid/zipball/master
```

GROBID NER is actually a sub-project of GROBID. 
Although GROBID NER might be merged with GROBID in the future, at this point the GROBID NER sub-module simply needs to be added manually to the main directory of GROBID: 

```bash
> cp -r grobid-ner /path/to/grobid/

> cp -r grobid-home/models/* /path/to/grobid/grobid-home/models/
```

Then build the GROBID NER subproject:

```bash
> cd /path/to/grobid/grobid-ner

> mvn clean install
```
