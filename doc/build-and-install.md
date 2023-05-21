grobid-ner is a sub-module of [Grobid](https://github.com/kermitt2/grobid). 

## Grobid installation

Grobid is library for extracting and structuring the content of technical and scientific documents. 
The tool offers a convenient environment for creating efficient text mining tool based on CRF.

Clone Grobid source code from github, latest stable version (currently 0.7.3):

```bash
> git clone https://github.com/kermitt2/grobid.git --branch 0.7.3
```

Then build Grobid, in the main directory:

```bash
> cd grobid

> ./gradlew clean install
```

For further explanations, see the [Install Grobid documentation page](https://grobid.readthedocs.io/en/latest/Install-Grobid/)

## grobid-ner settings

Use a grobid-ner version in sync with Grobid version. 

Clone source code of current master grobid-ner from github:

```bash
> cd grobid/

> git clone https://github.com/kermitt2/grobid-ner.git
```

Copy the provided pre-trained model in the standard grobid-home path:

```bash
> ./gradlew copyModels 
```

Then build the grobid-ner subproject:

```bash
> ./gradlew clean install
```
