#!/usr/bin/env bash

if [ -d "tmp" ]; then
    echo "removing previous tmp directory"
    rm -rf tmp
fi

mkdir tmp

echo "Extracting paths"
find . -type f -name *.xml -exec egrep -l -e 'ENAMEX type="PLANT"' -e 'ENAMEX type="ANIMAL"' -e 'ENAMEX type="SUBSTANCE"' -e 'ENAMEX type="AWARD"' {} \; >tmp/output.tmp.txt
cp tmp/output.tmp.txt .

echo "Aggregating, sorting and unifying content"
sort ./output.tmp.txt > output.txt.sorted
uniq output.txt.sorted > output.final.txt

