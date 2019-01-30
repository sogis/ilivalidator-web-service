FROM java:8
VOLUME /tmp

ENV USER_NAME ilivalidator
ENV APP_HOME /home/$USER_NAME/app

RUN useradd -ms /bin/bash $USER_NAME
RUN mkdir $APP_HOME

ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
RUN chown -R $USER_NAME /app
USER $USER_NAME
ENTRYPOINT ["java","-cp","app:app/lib/*","ch.so.agi.ilivalidator.IlivalidatorApplication"]