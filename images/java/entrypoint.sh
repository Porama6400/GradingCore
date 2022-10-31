#!/bin/bash
pwd
ls -l
sudo -u grader whoami

sleep 5

sudo -u grader javac Main.java &&
time -o timing.txt sudo -u grader java Main

cat timing.txt
cat stdout.txt

sleep 5