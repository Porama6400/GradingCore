#!/bin/bash
# input
# source - source file
# test.zip - testcase file

# /testcase
#    1.in
#    1.out
#    ...
# /work
#    main.c
#    main

# wait for file to be add
until [ -f start.lock ]
do
     echo "Waiting add!"
     sleep 1
done

unzip testcase.zip
rm testcase.zip
chmod go-r testcase -R

cat source
mv source ./work/main.c

cd work
sudo -u grader gcc main.c -o main > compile.txt 2> compile.txt
cd ..

mv work/compile.txt compile.txt

ls -al
ls work -al
ls testcase -al
sudo -u grader ls
sudo -u grader cat ./testcase/1.in
sudo -u grader cat ./entrypoint.sh

#
#
#

for i in ./testcase/*.in ; do
    cd work
    eval "cat ../$i | time -v -o ../timing.txt sudo -u grader ./main" > ../out.txt 2> ../err.txt
    cd ..
    eval "diff out.txt ./testcase/$(basename $i .in).out" > diff.txt
    chmod go-r diff.txt
    ls -al
    cat out.txt
    cat err.txt
    cat timing.txt
    cat diff.txt
    echo "========="
    if [ -s ./diff.txt ] ; then
        echo "1" >> result.txt
    else
        echo "0" >> result.txt
    fi
done

cat result.txt | tr -d '\n' > result2.txt
mv result2.txt result.txt
cat result.txt

#
#
#

touch executed.lock

# wait for file removal
until [ -f done.lock ]
do
     echo "Waiting remove!"
     sleep 1
done