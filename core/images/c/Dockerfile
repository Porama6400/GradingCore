FROM alpine:3.16.2
RUN apk add gcc=11.2.1_git20220219-r2 g++=11.2.1_git20220219-r2
RUN adduser -D --uid 1100 grader
WORKDIR /home/grader/
ADD entrypoint.sh .