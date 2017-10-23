#!/usr/bin/env bash


## Check for PERIOD separated by '-', e.g. 2001-2003
# egrep --color  'PERIOD">\w+<\/ENAMEX>.{1}<ENAMEX type="PERIOD"' *.xml
sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)(.{1})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## Check for PERIOD separated by ' - ', e.g. 2001 - 2003
# egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ -]{3}<ENAMEX type="PERIOD"' *.xml
sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([ -]{3})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## Check for PERIOD list separated by ', ' e,g, 2001, 2003 (need to be relaunched several times)
# egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ ,]{2}<ENAMEX type="PERIOD"' *.xml
sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([ ,]{2})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## check for PERIOD list separated by ' and ' e.g. 2001 and 2003 (need to be launched several times)
# egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ and]{5}<ENAMEX type="PERIOD"' *.xml
sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([ and]{5})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## check for command separated period after other commas separated periods 
# egrep --color  'PERIOD">[0-9, ]+<\/ENAMEX>.{2}<ENAMEX type="PERIOD"' *.xml
# sed -r -i -- 's/(PERIOD">[0-9, ]+)(<\/ENAMEX>)([ ,]{2})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## check for 'from $year1 to $year2':
egrep --color  'from <ENAMEX type="PERIOD">[0-9 ,-]+<\/ENAMEX>[to ]{4}<ENAMEX type="PERIOD">' *.xml

## check fro 'from $date1 to $date2'
# egrep --color  'from <ENAMEX type="PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[to ]{4}<ENAMEX type="PERIOD">' *.xml
sed -r -i -- 's/([fF]rom )(<ENAMEX type="PERIOD">)([0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([to ]{4})(<ENAMEX type="PERIOD">)/\2\1\3\5/g' *.xml

## check for '$date1 to $date2'
# egrep --color  'PERIOD">\w+<\/ENAMEX>[to ]{3,4}<ENAMEX type="PERIOD"' *.xml
#sed -r -i -- 's/(PERIOD">\w+)(<\/ENAMEX>)([to ]{3,4})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml
sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([to ]{3,4})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## Replace inline (general command)
# sed -r -i -- 's/(PERIOD">)(<\/ENAMEX>)(.{1})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml