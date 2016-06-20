#!/bin/bash
# Use soot to extract jimple files
if [ $# != 2 ] ; then 
  echo "Usage: toJimple android_lib_path apk_file_path!"
  exit 1; 
fi 
lib=$1
apk=$2
java -jar soot-trunk.jar -android-jars $lib -src-prec apk -allow-phantom-refs -f J -process-dir $apk -d ${apk:0:${#apk}-4}

# Generate class list for this apk file
list=${apk/apk/txt}
if [[ -f $list ]]; then
	rm $list
fi

for file in `ls ${apk:0:${#apk}-4}`
do
    echo "${file:0:${#file}-7}" >> $list
done