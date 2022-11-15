#!/bin/bash
# wait for file to be add
until [ -f Main.java ]
do
     echo "Waiting add!"
     sleep 1
done
chmod a+rw .
sudo -u grader javac Main.java &&
time -o timing.txt sudo -u grader java Main | tee output.txt

ls -la

cat timing.txt
cat output.txt

# wait for file removal
until [ -f done.lock ]
do
     echo "Waiting remove!"
     sleep 1
done