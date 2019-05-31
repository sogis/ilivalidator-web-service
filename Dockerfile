#FROM adoptopenjdk/openjdk11:latest
FROM adoptopenjdk/openjdk8:latest

EXPOSE 8080

WORKDIR /home/ilivalidator

ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /home/ilivalidator/app/lib
COPY ${DEPENDENCY}/META-INF /home/ilivalidator/app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /home/ilivalidator/app
RUN chown -R 1001:0 /home/ilivalidator && \
    chmod -R g=u /home/ilivalidator

USER 1001

ENTRYPOINT ["java","-cp","app:app/lib/*","ch.so.agi.ilivalidator.IlivalidatorApplication"]