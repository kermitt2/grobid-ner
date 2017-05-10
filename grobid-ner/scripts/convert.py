## Script to convert old connl files to xml Grobid-NER files.
#
# Usage python convert.py inputFile outputFile

# This script will overwrite anything that it's provided as outputFile. No mercy!
#
# Note:
#  - An empty line in the input file is interpreted as a new sentence.
#  - Since there is no information about the paragraph, the output will only contain a single one

import sys
import os

if len(sys.argv) < 2 or len(sys.argv) > 3:
    print("This tool requires one parameter. ")
    sys.exit(-1)

fname = sys.argv[1]
foutput = sys.argv[2]

output = open(foutput, 'w')

isTagOpened = False
isSentenceOpen = True
previousClass = ""
sentenceCount = 1
isFirstToken = True
isFirstSentenceToken = True


def openTag(output, class2, sense):
    output.write('<ENAMEX class="' + class2 + '"')
    if sense != "N/A":
        output.write(' sense="' + sense + '"')
    output.write('>')


def closeTag(output):
    output.write('</ENAMEX>')


# Writing head
output.write('<?xml version="1.0" encoding="UTF-8"?>')
output.write('\n')
output.write('<corpus>\n\t<subcorpus>\n\t\t<document name="' + os.path.basename(fname) + '">\n\n\t\t\t')

output.write('<paragraph xml:id="P0">\n\t\t\t\t')
output.write('<sentence xml:id="P0E' + str(sentenceCount) + '">')


def addSpace(output, token):
    punctuation = [':', ";", ".", ","]
    if any(char in punctuation for char in token):
        output.write("")
    else:
        output.write(" ")


with open(fname) as f:
    for line in f:
        if isFirstToken:
            isFirstToken = False

        strippedLine = line.strip()
        split = strippedLine.split("\t")
        nbColumns = len(split)

        token = split[0]

        if nbColumns < 2 or nbColumns > 3:
            if len(strippedLine) == 0:
                if isSentenceOpen:
                    if isTagOpened:
                        closeTag(output)
                        isTagOpened = False
                    output.write("</sentence>")
                    sentenceCount += 1
                    output.write("\n\t\t\t\t")
                    output.write('<sentence xml:id="P0E' + str(sentenceCount) + '">')
                    isFirstSentenceToken = True;
                else:
                    output.write("<sentence>")
                    isSentenceOpen = True
                    isFirstSentenceToken = True

            continue

        class2 = split[1]
        sense = "N/A"
        if nbColumns > 2:
            sense = split[2]

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
                openTag(output, class2, sense)
                previousClass = class2
                isFirstToken = True
            else:
                if previousClass != class2:
                    closeTag(output)
                    openTag(output, class2, sense)
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
