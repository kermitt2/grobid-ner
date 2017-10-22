"""
    Consistency.py
    ======================
 
    Use it to check consistency (very roughly without any context).
 
    :Command:
 
    python consistency.py "absolute_path"
 
    Short resume
    -------------------
 
    Did in Python 2.7, parse each xml files of the inputdirectory to catch all ENAMEX tag and put in a dictionary the token (annotation) as the key and classes as the value.
    It just shows all keys with multiple values (with short example for each values).
 
    Example of results
    ------------------------

    United States Holocaust Memorial Museum  :

    INSTALLATION :  <sentence xml:id="P153E0">The <ENAMEX type="INSTALLATION">United States Holocaust Memorial Museum</ENAMEX> provides the account of <ENAMEX type="MEASURE">one</ENAMEX> survivor of 

    INSTITUTION :  <sentence xml:id="P472E2">According to the <ENAMEX type="INSTITUTION">United States Holocaust Memorial Museum</ENAMEX>, <ENAMEX type="LOCATION">Washington, D.C.</ENAMEX>, &quot;The fate of < 

    _____

    Jew  :

    PERSON :  <sentence xml:id="P157E4">A police marksman came along and shot each <ENAMEX type="PERSON">Jew</ENAMEX> in the neck with a submachine gun ...</sentence> 

    PERSON_TYPE :  <sentence xml:id="P0E84"> <ENAMEX type="PERSON">Class</ENAMEX> defined a <ENAMEX type="PERSON_TYPE">Jew</ENAMEX> as anyone who was a member of the <ENAMEX type="PERSON_TYPE">Jewish</EN 

    _____

    2015 general election  :

    EVENT :  <sentence xml:id="P22E2">At the <ENAMEX type="EVENT">2015 general election</ENAMEX>  <ENAMEX type="ORGANISATION">UKIP</ENAMEX> took <ENAMEX type="MEASURE"> 

    PERIOD :   <ENAMEX type="ORGANISATION">Labour Party</ENAMEX>&apos;s position prior to the <ENAMEX type="PERIOD">2015 general election</ENAMEX> under <ENAMEX type="PERSON">Miliband</ENAMEX>, acting <ENAMEX type="ORG 

 
"""

import os
import re
import sys
import xml.etree.ElementTree as ET


def main(args):
    if len(args) == 1:

        # listing files in the directory (args[1] has to be an absolute path)
        try:
            files = os.listdir(args[0])
        except:
            print "path not found (be sure to put an absolute path)"
            sys.exit()

        # catch all ENAMEX tags
        sfiles = ""  # sfiles is all files in a string variable it serves at line 47
        files = [file_ for file_ in files if ".xml" in file_]

        dic = {}
        for file in files:
            enamex = []  # enamex contain all ENAMEX balises the variable is used at line 26
            with file.open(args[0] + os.sep + file, 'r') as file_:
                sfiles = sfiles + ''.join(file_.readlines())
            # try:
            tree = ET.ElementTree()
            tree.parse(args[0] + os.sep + file)
            tree = tree.getroot()
            for p in tree.findall("subcorpus/document/p"):
                for sent in p.findall("sentence"):
                    enamex = enamex + sent.findall("ENAMEX")

            # the dictionary "ambiguates" containes the text annotation (string) as key and class (list) as values

            for elt in enamex:
                # if dic contain the token as key add new value else initialize token with first value
                if (dic.has_key(elt.text)):
                    dic[elt.text] = list(set(dic.get(elt.text) + [elt.get("type")]))
                else:
                    dic[elt.text] = [elt.get("type")]

        # print part where a dic key has multiple value.
        for tok in dic.keys():
            if len(dic[tok]) > 1:
                print tok, " :\n"
                for class_ in dic[tok]:
                    regex = ur'.{,80}<ENAMEX type="%s">%s<.{,80}' % (class_, tok)
                    try:
                        shortexample = re.search(regex, sfiles).group(0).replace("\t", "")
                    except:
                        shortexample = "/"  # problem with encodage (line 21 opened without utf-8)
                    print class_, ": ", shortexample, "\n"
                print "_____\n"

    else:
        print "1 argument needed"
        sys.exit()


if __name__ == "__main__":
    main(sys.argv[1:])
