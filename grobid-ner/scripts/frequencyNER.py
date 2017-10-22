import os
import sys
import xml.etree.ElementTree as ET


def main(args):
    if len(args) != 1:
        print("1 argument needed")

        sys.exit(-1)

    try:
        files = os.listdir(args[0])
    except:
        print("path not found (be sure to put an absolute path)")
        sys.exit()

    frequencies = {}
    for file in files:
        enamex = []
        absPath = args[0] + os.sep + file
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

    print(frequencies)


if __name__ == "__main__":
    main(sys.argv[1:])
