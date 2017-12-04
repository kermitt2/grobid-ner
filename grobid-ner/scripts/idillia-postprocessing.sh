#!/usr/bin/env bash

## Check for PERIOD separated by a single character like '-' or '.', etc. e.g. 2001-2003
# egrep --color  'PERIOD">\w+<\/ENAMEX>.{1}<ENAMEX type="PERIOD"' *.xml
# find ./ -type f -name *.xml -exec egrep --color  'PERIOD">\w+<\/ENAMEX>.{1}<ENAMEX type="PERIOD"' {} \;

echo "Modifying PERIODs separated by '-', e.g. 2001-2003"
find ./ -type f -name *.xml -exec sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)(.{1})(<ENAMEX type="PERIOD">)/\1\3/g' {} \;

echo "Counting how many are left"
find ./ -type f -name *.xml -exec egrep --color  'PERIOD">\w+<\/ENAMEX>.{1}<ENAMEX type="PERIOD"' {} \; | wc -l


## Check for PERIOD separated by ' - ', e.g. 2001 - 2003
# egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ -]{3}<ENAMEX type="PERIOD"' *.xml

echo "Modifying PERIODs separated by '-' with spaces, e.g. 2001 - 2003"
find ./ -type f -name *.xml -exec sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([ -]{3})(<ENAMEX type="PERIOD">)/\1\3/g' {} \;

echo "Counting how many are left"
find ./ -type f -name *.xml -exec egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ -]{3}<ENAMEX type="PERIOD"' {} \; | wc -l 


## Check for PERIOD list separated by ', ' e,g, 2001, 2003 (need to be relaunched several times)
# egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ ,]{2}<ENAMEX type="PERIOD"' *.xml

echo "Modifying PERIODs separated by ', ' with spaces, e.g. 2001, 2003"
find ./ -type f -name *.xml -exec sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([ ,]{2})(<ENAMEX type="PERIOD">)/\1\3/g' {} \;

echo "Counting how many are left"
find ./ -type f -name *.xml -exec egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ ,]{2}<ENAMEX type="PERIOD"' {} \; | wc -l


## Check for PERIOD list separated by ' and ' e.g. 2001 and 2003 (need to be launched several times)
# egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ and]{5}<ENAMEX type="PERIOD"' *.xml

echo "Modifying PERIODs separated by ' and ' with spaces, e.g. 2001 and 2003"
find ./ -type f -name *.xml -exec sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([ and]{5})(<ENAMEX type="PERIOD">)/\1\3/g' {} \;

echo "Counting how many are left"
find ./ -type f -name *.xml -exec egrep --color  'PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[ and]{5}<ENAMEX type="PERIOD"' {} \; | wc -l 

## check for command separated period after other commas separated periods 
# egrep --color  'PERIOD">[0-9, ]+<\/ENAMEX>.{2}<ENAMEX type="PERIOD"' *.xml
# sed -r -i -- 's/(PERIOD">[0-9, ]+)(<\/ENAMEX>)([ ,]{2})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

## check for 'from $year1 to $year2':
#egrep --color  'from <ENAMEX type="PERIOD">[0-9 ,-]+<\/ENAMEX>[to ]{4}<ENAMEX type="PERIOD">' {} \;


## Check for 'from $date1 to $date2'
# egrep --color  'from <ENAMEX type="PERIOD">[0-9A-Za-z ,-\.]+<\/ENAMEX>[to ]{4}<ENAMEX type="PERIOD">' *.xml

echo "Modifying PERIODs intervals like 'from date1 to date2'"
find ./ -type f -name *.xml -exec sed -r -i -- 's/([fF]rom )(<ENAMEX type="PERIOD">)([0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([to ]{4})(<ENAMEX type="PERIOD">)/\2\1\3\5/g' {} \;


## Check for '$date1 to $date2'
# egrep --color  'PERIOD">\w+<\/ENAMEX>[to ]{3,4}<ENAMEX type="PERIOD"' *.xml
#sed -r -i -- 's/(PERIOD">\w+)(<\/ENAMEX>)([to ]{3,4})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml

echo "Modifying PERIODs intervals like 'date1 to date2'"
find ./ -type f -name *.xml -exec sed -r -i -- 's/(PERIOD">[0-9A-Za-z ,-\.]+)(<\/ENAMEX>)([to ]{3,4})(<ENAMEX type="PERIOD">)/\1\3/g' {} \;

## Replace inline (general command)
# sed -r -i -- 's/(PERIOD">)(<\/ENAMEX>)(.{1})(<ENAMEX type="PERIOD">)/\1\3/g' *.xml