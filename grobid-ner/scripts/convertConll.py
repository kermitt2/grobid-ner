## Script to convert old connl official files to xml Grobid-NER files.
#
# Usage python convert.py inputFile outputFile

# This script will overwrite anything that it's provided as outputFile. No mercy!
#
# Note:
#  - An empty line in the input file is interpreted as a new sentence.
#  - Since there is no information about the paragraph, the output will only contain a single one
#  - the separators are spaces and not tabulation (in the GROBID CONLL files)

import os
import sys
from xml.sax.saxutils import escape

if len(sys.argv) < 2 or len(sys.argv) > 3:
    print("Usage: python convert.py inputFile outputFile")
    sys.exit(-1)

fname = sys.argv[1]
foutput = sys.argv[2]

output = open(foutput, 'w')

isTagOpened = False
isSentenceOpen = True
isDocumentOpen = False
previousClass = ""
sentenceCount = 1
documentCount = 1
isFirstToken = True
isFirstSentenceToken = True


def translateClass(label):
    # print(label)
    if label == "ORG":
        return "ORGANISATION"
    elif label == "PER":
        return "PERSON"
    elif label == "LOC":
        return "LOCATION"
    elif label == "MISC":
        return "UNKNOWN"
    else:
        return "O"


def openTag(output, class2):
    output.write('<ENAMEX type="' + translateClass(class2) + '">')


def closeTag(output):
    output.write('</ENAMEX>')


# Writing head
output.write('<?xml version="1.0" encoding="UTF-8"?>')
output.write('\n')
output.write('<corpus>\n\t<subcorpus>\n')


def addSpace(output, token):
    punctuation = [':', ";", ".", ",", "\"", "(", ")", "[", "]"]
    if any(char in punctuation for char in token):
        output.write("")
    else:
        output.write(" ")


with open(fname) as f:
    for line in f:

        if isFirstToken:
            isFirstToken = False

        if line.startswith("-DOCSTART-"):
            if not isDocumentOpen:
                isDocumentOpen = True
                output.write('\t\t<document name="' + os.path.basename(fname) + '_' + str(documentCount) + '">')
                output.write('\n\t\t\t<paragraph xml:id="P0">')
                output.write('\n\t\t\t\t<sentence xml:id="P0E' + str(sentenceCount) + '">')
            else:
                if isSentenceOpen:
                    output.write('</sentence>')
                    output.write('\n\t\t\t</paragraph>')
                    isSentenceOpen = False
                output.write('\n\t\t</document>')
                output.write('\n\t\t<document name="' + os.path.basename(fname) + '_' + str(documentCount) + '">')
                output.write('\n\t\t\t<paragraph xml:id="P0">')
                # output.write('\n\t\t\t\t<sentence xml:id="P0E' + str(sentenceCount) + '">')

            documentCount = documentCount + 1

            continue

        strippedLine = line.strip()
        split = strippedLine.split(" ")
        nbColumns = len(split)

        token = escape(split[0])

        # if nbColumns < 2 or nbColumns > 3:
        if len(strippedLine) == 0:
            if isSentenceOpen:
                if isTagOpened:
                    closeTag(output)
                    isTagOpened = False
                output.write("</sentence>")
                sentenceCount += 1
                output.write('\n\t\t\t\t<sentence xml:id="P0E' + str(sentenceCount) + '">')
                isFirstSentenceToken = True
            else:
                output.write('\n\t\t\t\t<sentence xml:id="P0E' + str(sentenceCount) + '">')
                isSentenceOpen = True
                isFirstSentenceToken = True

            continue

        class2 = split[3].replace("I-", "")
        beginning = split[3].startswith("B-")
        class2 = class2.replace("B-", "")

        if class2 == "O":
            if isTagOpened:
                closeTag(output)
                isTagOpened = False
                addSpace(output, token)
            else:
                if not isFirstToken and not isFirstSentenceToken:
                    addSpace(output, token)

            output.write(token)
        else:
            if not isTagOpened:
                isTagOpened = True
                if not isFirstSentenceToken:
                    addSpace(output, token)
                openTag(output, class2)
                previousClass = class2
                isFirstToken = True
            else:
                if previousClass != class2 or beginning:
                    closeTag(output)
                    openTag(output, class2)
                    previousClass = class2
                    isFirstToken = True

            if not isFirstToken:
                addSpace(output, token)
            output.write(token)

        if isFirstSentenceToken:
            isFirstSentenceToken = False

output.write("</sentence>")

output.write("\n\t\t\t</paragraph>")
output.write("\n\t\t</document>\n\t</subcorpus>\n</corpus>")

output.close()
