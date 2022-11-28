FROM alpine:3.16.2
RUN apk add sudo
RUN apk add gcc=11.2.1_git20220219-r2 g++=11.2.1_git20220219-r2
RUN adduser -D --uid 1100 grader
WORKDIR /home/grader/
#ADD daemon.cpp .
#RUN g++ -O3 -o daemon daemon.cpp && rm daemon.cpp
ADD entrypoint.sh .
CMD ["sh", "entrypoint.sh"]
