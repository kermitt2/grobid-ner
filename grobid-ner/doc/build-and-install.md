## Grobid Installation

Grobid needs first to be installed. Grobid is library for extracting bibliographical information from technical and scientific documents. he tool offers a convenient environment for creating efficient text mining tool based on CRF.

Clone source code from github:
> git clone https://github.com/kermitt2/grobid.git

Or download directly the zip file:
> https://github.com/kermitt2/grobid/zipball/master

<!--- ## [Build the project](https://github.com/kermitt2/grobid/wiki/Build-the-project) -->

The standard method for building the project is to use maven. In the main directory:
> mvn -Dmaven.test.skip=true clean install

Grobid should then be installed and ready.

It is also possible to build the project with ant.
> ant package

## Grobid NER Settings

Clone source code from github:
> git clone https://github.com/kermitt2/grobid-ner.git

Or download directly the zip file:
> https://github.com/kermitt2/grobid/zipball/master

Grobid NER is actually a sub-project of Grobid. Although Grobid NER will be merged with Grobid in the future, at this point the Grobid NER sub-module simply need to added manually. In the main directory of Grobid NER:

> cp -r grobid-ner /path/to/grobid/

> cp -r grobid-home/models/* /path/to/grobid/grobid-home/

Then build the Grobid NER subproject:

> cd /path/to/grobid/grobid-ner

> mvn clean install`
