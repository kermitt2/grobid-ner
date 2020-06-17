grobid-ner is a sub-module of [Grobid](https://github.com/kermitt2/grobid). 

## Grobid installation

Grobid is library for extracting and structuring the content of technical and scientific documents. 
The tool offers a convenient environment for creating efficient text mining tool based on CRF.

Clone Grobid source code from github, release 0.6.0:

```bash
> git clone --branch 0.6.0 https://github.com/kermitt2/grobid
```

Or download directly the zip file of this release:

```bash
> wget https://github.com/kermitt2/grobid/archive/0.6.0.zip

> unzip 0.6.0.zip
```

Then build Grobid, in the main directory:

```bash
> cd grobid

> ./gradlew clean install
```

For further explanations, see the [Install Grobid documentation page](https://grobid.readthedocs.io/en/latest/Install-Grobid/)

## grobid-ner settings

Current grobid-ner version is `0.6.0`, the version should be in sync with Grobid version. 

Clone source code of grobid-ner from github:

```bash
> cd grobid/

> git clone https://github.com/kermitt2/grobid-ner.git
```

Or download directly the zip file:

```bash
> https://github.com/kermitt2/grobid/zipball/master
```

Copy the provided pre-trained model in the standard grobid-home path:

```bash
> ./gradlew copyModels 
```

Then build the grobid-ner subproject:

```bash
> ./gradlew clean install
```
