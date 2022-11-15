FROM eclipse-temurin:19-jdk-alpine
RUN apk add sudo bash
RUN adduser -D --uid 1100 grader
WORKDIR /home/grader/
ADD entrypoint.sh .