[![CI/CD](https://github.com/sogis/ilivalidator-web-service/actions/workflows/main.yml/badge.svg)](https://github.com/sogis/ilivalidator-web-service/actions/workflows/main.yml)

# ilivalidator-web-service

The ilivalidator web service is a [spring boot](https://projects.spring.io/spring-boot/) application and uses [ilivalidator](https://github.com/claeis/ilivalidator) for the INTERLIS transfer file validation.

## TODO
- Expose Volumes...
- Funktioniert directory listing mit reverse proxy
- update readme
- update docs:
 * wie werden die Verzeichnisse erstellt (d.h. falls vorhanden, nicht).
 * log-Dir muss geshared sein (das work dir mindestens)
 * naming convention, damit toml greifen.
 * env-Vars
 * es kann keine alpine image verwendet werden, wegen sqlite lib
 * ...



```
2022-07-16 14:58:53.988  INFO 1 --- [nio-8888-exec-9] c.s.a.ilivalidator.IlivalidatorService   : Validation end.
2022-07-16 14:58:53.990  INFO 1 --- [nio-8888-exec-9] ch.so.agi.ilivalidator.WebSocketHandler  : ************: {host=geo-t.so.ch, upgrade=WebSocket, connection=upgrade, pragma=no-cache, cache-control=no-cache, user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36, origin=https://geo-t.so.ch, sec-websocket-version=13, accept-encoding=gzip, deflate, br, accept-language=en,es;q=0.9,de;q=0.8,en-US;q=0.7,fr;q=0.6,nb;q=0.5, sec-websocket-key=dcXQM/DvOFqVkxxWYCtU6A==, sec-websocket-extensions=permessage-deflate; client_max_window_bits, hsp_client_addr=46.255.172.215, hsp-listeneruri=https://geo-t.so.ch, requestcorrelator=7f0100-1326-2022.07.16_1458.46.451-001, connectioncorrelator=7f0100-1326-2022.07.16_1458.46.451-001, hsp_client_city=Koppigen, hsp_client_country=CH, hsp_client_continent=EU, x-forwarded-server=geo-t.so.ch, 127.0.0.1, clientcorrelator=NThxRHP4V9E$}
2022-07-16 14:58:53.990  INFO 1 --- [nio-8888-exec-9] ch.so.agi.ilivalidator.WebSocketHandler  : ************: ilivalidator-web-service-34-2j4fr
2022-07-16 14:58:53.990  INFO 1 --- [nio-8888-exec-9] ch.so.agi.ilivalidator.WebSocketHandler  : ************: ilivalidator-web-service-34-2j4fr
2022-07-16 14:58:53.990  INFO 1 --- [nio-8888-exec-9] ch.so.agi.ilivalidator.WebSocketHandler  : ************: wss://geo-t.so.ch/ilivalidator//socket
2022-07-16 14:58:53.990  INFO 1 --- [nio-8888-exec-9] ch.so.agi.ilivalidator.WebSocketHandler  : log file: /tmp/ilivalidator_14573654641354684883/254900.itf.log

```

## Features

* checks INTERLIS 1+2 transfer files
* uses server saved config files for validation tailoring
* see [https://github.com/claeis/ilivalidator](https://github.com/claeis/ilivalidator) for all the INTERLIS validation magic of ilivalidator 

## License

ilivalidator web service is licensed under the [GNU General Public License, version 2](LICENSE).

## Status

ilivalidator web service is in development state.

## System Requirements

For the current version of ilivalidator web service, you will need a JRE (Java Runtime Environment) installed on your system, version 1.8 or later.

## Configuration
See `application.properties`. 

`AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` must be set as ENV vars directly. Also for testing!

## Developing

ilivalidator web service is build as a Spring Boot Application.

`git clone https://github.com/sogis/ilivalidator-web-service-websocket.git` 

Use your favorite IDE (e.g. [Spring Tool Suite](https://spring.io/tools/sts/all)) for coding.

### Log files
It uses S3 for storing the log files to be independent of the deployment: If we run more than one pod and the pods have on common volume, it is possible that the link to the log file after the validation will end on the pod that did not the validation. 

### Additional models

Ilivalidator needs a toml file if you want to apply an additional model for your additional checks. The toml file must be all lower case, placed in the `toml` folder and named like the base model itself, e.g. `SO_Nutzungsplanung_20171118` -> `so_nutzungsplanung_20171118.toml`. The additional model can be placed in the `ili` folder or in any model repository that ilivalidator finds out-of-the-box.

### Ilivalidator custom functions

Custom-Funktionen können in zwei Varianten verwendet werden. Die Jar-Datei mit den Funktionen muss in einem Verzeichnis liegen und vor jeder Prüfung werden die Klassen dynamisch geladen. Das hat den Nachteil, dass man so kein Native-Image (GraalVM) mit Custom-Funktionen herstellen kann und man z.B. bei einem Webservice die Klassen nicht einfach als Dependency definiert kann, sondern die Jar-Datei muss in einem Verzeichnis liegen, welches beim Aufruf von _ilivalidator_ als Option übergeben wird. Bei der zweiten (neueren) Variante kann man die Custom-Funktionen als normale Dependency im Java-Projekt definieren. Zusätzlich müssen die einzelnen Klassen als Systemproperty der Anwendung bekannt gemacht werden. 

Im vorliegenden Fall wird die zweite Variante gewählt. Das notwendige Systemproperty wird in der `AppConfig`-Klasse gesetzt. Falls man die erste Variante vorzieht oder aus anderen Gründen verwenden will, macht man z.B. ein Verzeichnis `src/main/resources/libs-ext/` und kopiert beim Builden die Jar-Datei in dieses Verzeichnis. Dazu wird eine Gradle-Konfiguration benötigt. Zur Laufzeit (also wenn geprüft wird) muss man die Jar-Datei auf das Filesystem kopieren und dieses Verzeichnis als Options _ilivalidator_ übergeben. Siehe dazu Code vor dem "aot"-Merge.

#### Land use planning

Für die Validierung der Nutzungsplanung werden zusätzliche Prüfungen vorgenommen. Sowohl mit "einfachen", zusätzlichen Constraints, aber auch mit zusätzlichen Java-Funktionen. 

- https://github.com/claeis/ilivalidator/issues/180 (fixed)
- https://github.com/claeis/ilivalidator/issues/196 (fixed)
- https://github.com/claeis/ilivalidator/issues/203 (fixed)
- https://github.com/claeis/ilivalidator/issues/204
- https://github.com/claeis/ilivalidator/issues/205
- https://github.com/claeis/ili2c/issues/6 (fixed)

Wegen früheren Bugs musste das Originalmodell angepasst werden, damit die Constraints funktioneren. Das ist nicht mehr der Fall. Sämtliche Constraints sind im Validierungsmodell. Beide Modell werden zur Laufzeit von der Modellablage bezogen.

### Testing

Since ilivalidator is heavily tested in its own project, there are only functional tests of the web service implemented.

`./gradlew clean test` will run all tests by starting the web service and uploading an INTERLIS transfer file.

### Building

`./gradlew clean build` will create an executable JAR. Ilivalidator custom functions will not work. Not sure why but must be something with how the plugin loader works. 

### Release management / versioning

It uses a simple release management and versioning mechanism: Local builds are tagged as `1.0.LOCALBUILD`. Builds on Travis or Jenkins will append the build number, e.g. `1.0.48`. Major version will be increased after "major" changes. After every commit to the repository a docker image will be build and pushed to `hub.docker.com`. It will be tagged as `latest` and with the build number (`1.0.48`).

## Running

### JVM
TODO


### Docker
```
docker run -p 8888:8888 -e AWS_ACCESS_KEY_ID=xxxx -e AWS_SECRET_ACCESS_KEY=yyyy sogis/ilivalidator-web-service
```

### SO!GIS
TODO: Link to Openshift stuff.


## ilivalidator configuration files

The ilivalidator configurations files (aka `toml` files) are part of the distributed application and cannot be changed or overriden at the moment. There can be only one configuration file per INTERLIS model.

These configuration files can be found in the resource directory of the source tree.




