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

# wait for file to be add
echo "Waiting for start.lock"
until [ -f start.lock ]; do
  sleep 1
done

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

if [ -f "./work/main" ]; then
  if [ -d "./testcase" ]; then

    for i in $(ls -1v ./testcase/*.in); do
      cd work || exit
      eval "cat ../$i | time -v -o ../timing.txt sudo -u grader ./main" >../stdout.txt 2>../stderr.txt
      cd ..
      eval "cat stdout.txt | ./scrubber" >cmpout.txt
      eval "cat ./testcase/$(basename $i .in).out | ./scrubber" >cmpref.txt
      eval "diff cmpout.txt cmpref.txt" > diff.txt
      rm cmpout.txt cmpref.txt
      chmod go-r diff.txt
      echo "===== $1 ====="
#      ls -al
#      echo "=== stdout ==="
#      cat stdout.txt
#      echo "=== stderr ==="
#      cat stderr.txt
#      echo "=== timing ==="
#      cat timing.txt
#      echo "=== diff ==="
#      cat diff.txt
#      echo "========="
      if [ -s ./diff.txt ]; then
        echo "1" >> result.txt
      else
        echo "0" >> result.txt
      fi

      cat result.txt | tr -d '\n' >resultconsolidated.txt
      mv resultconsolidated.txt result.txt
#      cat result.txt
      echo "COMPLETED" > status.txt
    done
  else
    echo "FAILED_MISSING_TEST" > status.txt
  fi
else
  echo "FAILED_COMPILATION" > status.txt
  echo "Compilation failed"
fi

touch executed.lock

# wait for file removal
echo "Waiting for done.lock"
until [ -f done.lock ]; do
  sleep 1
done
