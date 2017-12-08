import os
import sys
import xml.etree.ElementTree as ET
from glob import glob


def main(args):
    if len(args) != 1:
        print("1 argument needed")

        sys.exit(-1)

    try:
        # files = os.listdir(args[0])
        files = [y for x in os.walk(args[0]) for y in glob(os.path.join(x[0], '*.xml'))]
    except:
        print("path not found (be sure to put an absolute path)")
        sys.exit()

    frequencies = {}
    for file in files:
        enamex = []
        absPath = file
        print("parsing " + absPath)
        try:
            root = ET.ElementTree().parse(absPath)
            tree = root
        except ET.ParseError:
            print("Cannot parse " + absPath + ". Ignoring it.")
            continue

        for p in tree.findall("subcorpus/document/p"):
            for sent in p.findall("sentence"):
                enamex = enamex + sent.findall("ENAMEX")

        for elt in enamex:
            nerClass = elt.get("type")
            if nerClass in frequencies:
                frequencies[nerClass] = frequencies.get(nerClass) + 1
            else:
                frequencies[nerClass] = 1

    sortedFrequencies = [(k, frequencies[k]) for k in sorted(frequencies, key=frequencies.get, reverse=True)]

    print("{")
    for ner in sortedFrequencies:
        print("\t\"" + str(ner[0]) + "\": " + str(ner[1]))

    print("}")


if __name__ == "__main__":
    main(sys.argv[1:])
