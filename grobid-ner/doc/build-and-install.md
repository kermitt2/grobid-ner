grobid-ner is a sub-module of [Grobid](https://github.com/kermitt2/grobid). 

## Grobid installation

Grobid is library for extracting and structuring the content of technical and scientific documents. 
The tool offers a convenient environment for creating efficient text mining tool based on CRF.

Clone Grobid source code from github, version 0.6.0:

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

## grobid-ner settings

Current grobid-ner version is 0.6.0, the version should be in sync with Grobid version. 

Clone source code from github:

```bash
> git clone https://github.com/kermitt2/grobid-ner.git
```

Or download directly the zip file:

```bash
> https://github.com/kermitt2/grobid/zipball/master
```

grobid-ner is actually a sub-project of GROBID. The GROBID NER sub-module simply needs to be added manually to the main directory of Grobid: 

```bash
> cp -r grobid-ner /path/to/grobid/

> cp -r grobid-home/models/* /path/to/grobid/grobid-home/models/
```

Then build the grobid-ner subproject:

```bash
> cd /path/to/grobid/grobid-ner

> mvn clean install
```
