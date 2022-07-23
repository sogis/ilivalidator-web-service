[![CI/CD](https://github.com/sogis/ilivalidator-web-service/actions/workflows/main.yml/badge.svg)](https://github.com/sogis/ilivalidator-web-service/actions/workflows/main.yml)

# ilivalidator-web-service

The ilivalidator web service is a [Spring Boot](https://projects.spring.io/spring-boot/) application and uses [ilivalidator](https://github.com/claeis/ilivalidator) for the INTERLIS transfer file validation.

## Features

* checks INTERLIS 1+2 transfer files: see [https://github.com/claeis/ilivalidator](https://github.com/claeis/ilivalidator) for all the INTERLIS validation magic of ilivalidator
* uses server saved config files for validation tailoring
* user can deploy own config files for validation tailoring
* ReST-API
* simple clustering for horizontal scaling

## License

ilivalidator web service is licensed under the [GNU General Public License, version 2](LICENSE).

## Status

ilivalidator web service is in development state.

## System Requirements

For the current version of ilivalidator web service, you will need a JRE (Java Runtime Environment) installed on your system, version 17 or later.

## Developing

ilivalidator web service is build as a Spring Boot Application.

`git clone https://github.com/sogis/ilivalidator-web-service.git` 

Use your favorite IDE (e.g. [Spring Tool Suite](https://spring.io/tools/sts/all)) for coding.

### Testing

Since ilivalidator is heavily tested in its own project, there are only functional tests of the web service implemented.

`./gradlew clean test` will run all tests by starting the web service and uploading an INTERLIS transfer file.

If you want to test the Docker image as well, you have to run:

```
./gradlew clean build -x test
./gradlew buildImage4Test dockerTest
```

The tests are the same.

### Building

`./gradlew clean build` will create an executable JAR. Ilivalidator custom functions will not work. Not sure why but must be something with how the plugin loader works. 

Since there is no sqlite native driver on Alpine Linu for `aarch64` x, we will use `bellsoft/liberica-openjdk-centos:17.0.3` as base image (instead of `bellsoft/liberica-openjdk-alpine-musl:17.0.3`).

### Release management / versioning

It uses a simple release management and versioning mechanism: Local builds are tagged as `2.x.LOCALBUILD`. Builds on Github Action will append the build number, e.g. `2.0.127`. Major version will be increased after "major" changes. After every commit to the repository a docker image will be build and pushed to `hub.docker.com`. It will be tagged as `latest` and with the build number (`2`, `2.0` and `2.0.127`).

## Configuration and running

Die Anwendung kann als gewöhnliche Spring Boot Anwendung gestartet werden:

```
java -jar build/libs/ilivalidator-web-service-<VERSION>-exec.jar
```

Konfiguration via application.properties im Verzeichnis in dem der Service gestartet wird. Oder entsprechende alternative Konfigurationsmöglichkeiten von [Spring Boot](https://docs.spring.io/spring-boot/docs/2.7.1/reference/htmlsingle/#features.external-config).

Die Anwendung beinhaltet bereits eine _application.properties_-Datei. Siehe [application.properties](src/main/resources/application.properties), welche beim obigen Aufruf verwendet wird.

Das Dockerimage wird wie folgt gestartet:

```
docker run -p8080:8080 sogis/ilivalidator-web-service:<VERSION>
```

Der Dockercontainer verwendet eine leicht angepasster Konfiguration ([application-docker.properties](src/main/resources/application.properties)), damit das Mounten von Verzeichnissen hoffentlich einfacher fällt und weniger Fehler passieren.

Die allermeisten Optionen sind via Umgebungsvariablen exponiert und somit veränderbar. Im Extremfall kann immer noch ein neues Dockerimage erstellt werden mit einer ganz eigenen Konfiguration.

### Optionen (Umgebungsvariablen)

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `MAX_FILE_SIZE` | Die maximale Grösse einer Datei, die hochgeladen werden kann in Megabyte. Sowohl für die ReST-API wie auch via GUI über Websocket. | `100` |
| `LOG_LEVEL_FRAMEWORK` | Das Logging-Level des Spring Boot Frameworks. | `info` |
| `LOG_LEVEL_DB_CONNECTION_POOL` | Das Logging-Level des DB-Connection-Poolsocket. | `info` |
| `LOG_LEVEL_APPLICATION` | Das Logging-Level der Anwendung (= selber geschriebener Code). | `info` |
| `CONNECT_TIMEOUT` | Die Zeit in Millisekunden, die bis zu einem erfolgreichem Connect gewartet wird. Betrifft sämtliche Methoden, welche `sun.net.client.defaultConnectTimeout` berücksichtigen. Die Option dient dazu damit langsame INTERLIS-Modellablage schneller zu einem Timeout führen. | `5000` |
| `READ_TIMEOUT` | Die Zeit in Millisekunden, die bis zu einem erfolgreichem Lesen gewartet wird. Betrifft sämtliche Methoden, welche `sun.net.client.defaultConnectTimeout` berücksichtigen. Die Option dient dazu damit langsame INTERLIS-Modellablage schneller zu einem Timeout führen. | `5000` |
| `DOC_BASE` | Verzeichnis auf dem Filesystem, das als Root-Verzeichnis für das Directory-Listing des Webservers dient. Das Root-Verzeichnis selber ist nicht sichtbar. | `/docbase/` |
| `CONFIG_DIRECTORY_NAME` | Unterverzeichnis im `DOC_BASE`-Verzeichnis, welches die _toml_- und _ili_-Verzeichnisse enthält. Dieses Verzeichnis ist unter http://localhost:8080/config erreichbar. Es muss nicht manuell erstellt werden. Es wird beim Starten der Anwendung erstellt. Das Verzeichnis muss bei einem Betrieb mit mehreren Containern geteilt werden, falls zusätzliche _toml_- und _ili_-Dateien in die entsprechenden Verzeichnisse kopiert werden. | `config` |
| `UNPACK_CONFIG_FILES` | In der Anwendung enthaltene _toml_- und _ili_-Dateien werden bei jedem Start der Anwendung in die entsprechenden Verzeichnisse kopiert. | `true` |
| `WORK_DIRECTORY` | Verzeichnis, in das die zu prüfenden INTERLIS-Transferdatei und die Logdateien kopiert werden (in ein temporäres Unterverzeichnis). Es ist das einzige Verzeichnis, welches bei einem Betrieb mit mehreren Containern zwingend geteilt werden muss. Sonst ist nicht sichergestellt, dass man die Logdatei(en) herunterladen kann. | `/work/` |
| `FOLDER_PREFIX` | Für jede zu prüfende Datei wird im `WORK`-Verzeichnis ein temporäres Verzeichnis erstellt. Der Prefix wird dem Namen des temporären Verzeichnisses vorangestellt. | `ilivalidator_` |
| `CLEANER_ENABLED` | Dient zum Ein- und Ausschalten des Aufräumprozesses, der alte, geprüfte Dateien (INTERLIS-Transferdateien, Logfiles) löscht. | `true` |
| `REST_API_ENABLED` | Dient zum Ein- und Ausschalten des ReST-API-Controllers und damit der eigentlichen Funktionalität (auch wenn Jobrunr trotzdem initialisiert wird). | `true` |
| `JDBC_URL` | Die JDBC-Url der Sqlite-Datei, die dem Speichern der Jobs dient, welche mittels ReST-API getriggert wurden. Die Datei wird im Standard-`WORK`-Verzeichnis gespeichert, da dieses beim Multi-Container-Betrieb geteilt werden muss. Andere JDBC-fähige Datenbanken sind ebenfalls möglich. Dann müssten noch mindestens Login und Password exponiert werden. | `jdbc:sqlite:/work/jobrunr_db.sqlite` |
| `JOBRUNR_SERVER_ENABLED` | Dient die Instanz als sogenannter Background-Jobserver, d.h. werden mittels ReST-API hochgeladene INTERLIS-Transferdateien validiert. Wird nur eine Instanz betrieben, muss die Option zwingen `true` sein, da sonst der Job nicht ausgeführt wird. | `true` |
| `JOBRUNR_DASHBOARD_ENABLED` | Das Jobrunr-Dashboard wird auf dem Port 8000 gestartet. | `true` |
| `JOBRUNR_DASHBOARD_USER` | Username für Jobrunr-Dasboard. Achtung: Basic Authentication. | `admin` |
| `JOBRUNR_DASHBOARD_PWD` | Passwort für Jobrunr-Dasboard. Achtung: Basic Authentication. | `admin` |

Ein `docker-run`-Befehl könnte circa so aussehen:

```
docker run --rm -p8080:8080 -p8000:8000 -v /shared_storage/docbase:/docbase/ -v /shared_storage/work:/work/ sogis/ilivalidator-web-service:2
```

Es werden zwei Ports gemapped. Der Port 8080 ist der Port der Anwendung und zwingend notwendig. Der Port 8000 dient dazu, dass das Jobrunr-Dashboard verfügbar ist.

Im lokalen Filesystem (oder Kubernetes-PV-Whatever etc.) müssen die beiden Verzeichnisse _/shared_storage/docbase/_ und _/shared_storage/work/_ vorhanden sein. Die beiden Verzeichnisse _/docbase/_ und _/work/_ werden unter den lokalen Verzeichnissen gemountet. Im _docbase_-Verzeichnis wird das Verzeichnis _config_ erstellt, falls es nicht existiert. Im selbigen wiederum werden die beiden Verzeichnisse _ili_ und _toml_ erstellt und in diese einige Dateien kopiert. Die Sqlite-Datenbank, die dazu dient die ReST-API-Jobs zu koordinieren, befindet sich im _/shared_storage/work/_-Verzeichnis. 

### Clean up

Ein Scheduler löscht jede Stunde (momentan hardcodiert) alle temporären Verzeichnisse, die älter als 60x60 Sekunden sind.

### Additional models

Ilivalidator needs a toml file if you want to apply an additional model for your additional checks. The toml file must be all lower case, placed in the `toml` folder and named like the base model itself, e.g. `SO_Nutzungsplanung_20171118` -> `so_nutzungsplanung_20171118.toml`. The additional model can be placed in the `ili` folder or in any model repository that ilivalidator finds out-of-the-box.

### Ilivalidator custom functions

Custom-Funktionen können in zwei Varianten verwendet werden. Die Jar-Datei mit den Funktionen muss in einem Verzeichnis liegen und vor jeder Prüfung werden die Klassen dynamisch geladen. Das hat den Nachteil, dass man so kein Native-Image (GraalVM) mit Custom-Funktionen herstellen kann und man z.B. bei einem Webservice die Klassen nicht einfach als Dependency definiert kann, sondern die Jar-Datei muss in einem Verzeichnis liegen, welches beim Aufruf von _ilivalidator_ als Option übergeben wird. Bei der zweiten (neueren) Variante kann man die Custom-Funktionen als normale Dependency im Java-Projekt definieren. Zusätzlich müssen die einzelnen Klassen als Systemproperty der Anwendung bekannt gemacht werden. 

Im vorliegenden Fall wird die zweite Variante gewählt. Das notwendige Systemproperty wird in der `AppConfig`-Klasse gesetzt. Falls man die erste Variante vorzieht oder aus anderen Gründen verwenden will, macht man z.B. ein Verzeichnis `src/main/resources/libs-ext/` und kopiert beim Builden die Jar-Datei in dieses Verzeichnis. Dazu wird eine Gradle-Konfiguration benötigt. Zur Laufzeit (also wenn geprüft wird) muss man die Jar-Datei auf das Filesystem kopieren und dieses Verzeichnis als Options _ilivalidator_ übergeben. Siehe dazu Code vor dem "aot"-Merge (welches Repo?).

### Clustering
**TODO**

Disclaimer: Das Clustering funktioniert momentan nur via ReST-Schnittstelle. Bei der Bedienung mittels GUI (Websocket) wird die Ausführung des Validierungs-Prozesses noch nicht via _Jobrunr_ gesteuert.

Lorem ipsum... Jobrunr...



## Configuration and running (SO!GIS)
**FIXME**

## User manual

- GUI: [docs/user-manual-de.md](docs/user-manual-de.md)
- Nutzungsplanung: [docs/user-manual-de-nplso.md](docs/user-manual-de-nplso.md)
- **TODO** IPW-Validator: [docs/user-manual-de-ipw.md](docs/user-manual-de-ipw.md)
- ReST_API: [docs/rest-api-de.md](docs/rest-api-de.md)




