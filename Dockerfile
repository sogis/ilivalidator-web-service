#FROM bellsoft/liberica-openjdk-alpine-musl:17.0.3
FROM bellsoft/liberica-openjdk-centos:17.0.3

ARG UID=1001

# --system kann nicht verwendet werden, weil dann keine Repo-Verzeichnisse angelegt werden können.
RUN adduser -u $UID ilivalidator 

WORKDIR /docbase
RUN chown $UID:0 . && \
    chmod 0775 . && \
    ls -la

WORKDIR /work
RUN chown $UID:0 . && \
    chmod 0775 . && \
    ls -la

ENV HOME=/ilivalidator
WORKDIR $HOME

COPY ./build/libs/ilivalidator-web-service-*-exec.jar ./application.jar
RUN chown $UID:0 . && \
    chmod 0775 . && \
    ls -la

USER $UID
EXPOSE 8080

ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true
CMD java -XX:+UseParallelGC -XX:MaxRAMPercentage=80.0 -jar application.jar