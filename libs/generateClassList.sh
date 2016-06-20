#!/bin/bash
list="classList.txt"
if [[ -f $list ]]; then
	rm $list
fi

for file in `ls sootOutput`
do
    echo "${file:0:${#file}-7}" >> classList.txt
done