#!/bin/bash

cd c
docker build -t grader-c .
cd ..

cd java
docker build -t grader-java .
cd ..2