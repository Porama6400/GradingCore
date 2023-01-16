#!/bin/bash
# input
# source - source file
# test.zip - testcase file

# output
# result.txt
# status.txt
# compilationLog.txt

# ( temporary files not included )
# result.txt
# status.txt
# compilationLog.txt
# /testcase
#    1.in
#    1.out
#    ...
# /work
#    main.c/cpp
#    main

finalize () {
  touch executed.lock

  # wait for file removal
  echo "Waiting for done.lock"
  until [ -f done.lock ]; do
    sleep 1
  done
  exit
}

# wait for file to be add
echo "Waiting for start.lock"
until [ -f start.lock ]; do
  sleep 1
done

# extract testcase
unzip testcase.zip
rm testcase.zip
chmod go-r testcase -R

#cat source
mv source ./work/main.cpp

cd work || { echo "Missing work directory"; exit; }
# shellcheck disable=SC2024
sudo -u grader g++ main.cpp -O3 -o main > compilationLog.txt 2>&1
cd ..

mv work/compilationLog.txt compilationLog.txt
#cat compilationLog.txt

ls -al
ls work -al
ls testcase -al

# check permission
#sudo -u grader ls
#sudo -u grader cat ./testcase/1.in
#sudo -u grader cat ./entrypoint.sh

if [ ! -d "./testcase" ]; then
 echo "MISSING_TEST" | tee status.txt
 finalize
fi

if [ ! -d "./work" ]; then
 echo "FAILED_CONTAINER" | tee status.txt
 finalize
fi

if [ ! -f "./work/main" ]; then
  echo "FAILED_COMPILATION" | tee status.txt
  finalize
fi

cd work || exit
eval "cat ../testcase/in | time -v -o ../timing.txt sudo -u grader ./main" >../stdout.txt 2>../stderr.txt
cd ..
eval "cat stdout.txt | ./scrubber" >cmpout.txt
eval "cat ./testcase/out | ./scrubber" >cmpref.txt
eval "diff cmpout.txt cmpref.txt" > diff.txt
rm cmpout.txt cmpref.txt
chmod go-r diff.txt

#echo "=============="
#ls -al
#echo "=== stdout ==="
#cat stdout.txt
#echo "=== stderr ==="
#cat stderr.txt
#echo "=== timing ==="
#cat timing.txt
#echo "=== diff ==="
#cat diff.txt
#echo "========="

if [ -s ./diff.txt ]; then
  echo "FAILED_RESULT" | tee status.txt
  finalize
else
  echo "PASSED" | tee status.txt
  finalize
fi