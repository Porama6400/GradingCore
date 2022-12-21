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
mv source ./work/main.cpp

cd work
sudo -u grader g++ main.cpp -o main > compile.txt 2> compile.txt
cd ..

mv work/compile.txt compile.txt
cat compile.txt

ls -al
ls work -al
ls testcase -al

# check permission
#sudo -u grader ls
#sudo -u grader cat ./testcase/1.in
#sudo -u grader cat ./entrypoint.sh


if [ -d "./testcase" ] ; then
  for i in ./testcase/*.in ; do
      cd work
      eval "cat ../$i | time -v -o ../timing.txt sudo -u grader ./main" > ../out.txt 2> ../err.txt
      cd ..
      eval "cat out.txt | ./scrubber" > compout.txt
      eval "cat ./testcase/$(basename $i .in).out | ./scrubber" > compref.txt
      eval "diff compout.txt compref.txt" > diff.txt
      rm compout.txt compref.txt
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
else
  echo "1" > result.txt
fi

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