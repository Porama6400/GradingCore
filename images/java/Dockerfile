FROM eclipse-temurin:19-jdk-alpine
RUN apk add sudo
RUN adduser -D --uid 1100 grader
WORKDIR /home/grader/
ADD compile.sh .