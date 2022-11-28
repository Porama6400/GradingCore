#!/bin/bash
# wait for file to be add
until [ -f start.lock ]
do
     echo "Waiting add!"
     sleep 1
done
chmod a+rw .
sudo -u grader gcc main.c -o main && time -v -o timing.txt sudo -u grader ./main > output.txt 2> error.txt
touch executed.lock

# wait for file removal
until [ -f done.lock ]
do
     echo "Waiting remove!"
     sleep 1
done