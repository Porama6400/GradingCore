FROM alpine:3.16.2
RUN apk add sudo
RUN apk add gcc=11.2.1_git20220219-r2 g++=11.2.1_git20220219-r2
RUN adduser -D --uid 1100 grader
WORKDIR /home/grader/
RUN chown root:root . -R && chmod go-r . -R
RUN mkdir work && chown grader:grader work -R && chmod a+rw work -R
ADD entrypoint.sh .
RUN chmod go-r entrypoint.sh
CMD ["sh", "entrypoint.sh"]
