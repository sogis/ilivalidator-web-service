[![Build Status](https://travis-ci.org/sogis/ilivalidator-web-service.svg?branch=master)](https://travis-ci.org/sogis/ilivalidator-web-service)

# ilivalidator web service - a minimalist INTERLIS check service

The ilivalidator web service is a [spring boot](https://projects.spring.io/spring-boot/) application and uses [ilivalidator](https://github.com/claeis/ilivalidator) for the INTERLIS transfer file validation.

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

## Developing

ilivalidator web service is build as a Spring Boot Application.

`git clone https://github.com/edigonzales/ilivalidator-web-service.git` 

Use your favorite IDE (e.g. [Spring Tool Suite](https://spring.io/tools/sts/all)) for coding.

### Testing

Since ilivalidator is heavily tested in its own project, there are only functional tests of the web service implemented.

`./gradlew clean test` will run all tests by starting the web service and uploading an INTERLIS transfer file.

### Building

`./gradlew clean build` will create an executable JAR.

### Release management

It uses the [https://plugins.gradle.org/plugin/org.ajoberstar.reckon](https://plugins.gradle.org/plugin/org.ajoberstar.reckon) plugin:

1. Develop and test and build on your local machine.
2. Commit your changes locally: `git commit -a -m 'some fix'`
3. If you want to release a new SNAPSHOT version: `./gradlew build reckonTagPush -Preckon.scope=patch -Preckon.stage=snapshot`. You can use `patch`, `minor` or `major` for the reckon scope (SemVer alike). No git tag is created. But this step is needed to get proper docker image tags/versions. Then push to repo: `git push`.
4. If you want to create a final release: `./gradlew build reckonTagPush -Preckon.scope=patch -Preckon.stage=final`. A git tag is created and it asks you for the github credentials to push the commmit to the repo automatically.
5. The docker image will be created (and pushed to hub.docker.com) on Travis.

## Running as Docker Image (SO!GIS)
* To be done... 

## Running on Ubuntu (Deprecated)

See also the ["Installing Spring Boot application" section](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html) of the official documentation.

### Installation 

* Copy the executable JAR to an appropriate directory, e.g. `/opt/apps/ilivalidator/ilivalidator.jar`.
* `sudo ln -s /opt/apps/ilivalidator/ilivalidator.jar /etc/init.d/ilivalidator`
* `sudo update-rc.d ilivalidator defaults`

PID can be found at `/var/run/ilivalidator/ilivalidator.pid`.

The log file can be found at ` /var/log/ilivalidator.log`.

### Configuration

You can override the [configuration parameters](src/main/resources/application.properties) of the application (aka `application.properties`) if you place a copy of the file with your desired values next to the JAR. See [https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).


If you need to assign more memory to the app or choose another folder for the log file, you can to that by placing a `ilivalidator.conf` file next to the jar. See [https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-script-customization-when-it-runs](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-script-customization-when-it-runs).

### Filesystem access

ilivalidator web service needs to temporally store the uploaded transfer file and the resulting log file on the file system. For every uploaded file a temporary directory in the operating system temporary directory (`java.io.tmpdir`) will be created. This can be changed by setting a `ch.so.agi.ilivalidator.uploadedFiles` property in the `application.properties` file.

### Watchdog

A small watchdog bash script can be found here: [utils/watchdog.sh](utils/watchdog.sh). You can run it e.g. as cron job.

### Apache

```
ProxyPass /ilivalidator http://127.0.0.1:8888/ilivalidator
ProxyPassReverse /ilivalidator http://127.0.0.1:8888/ilivalidator

ProxyPass /ilivalidator/ http://127.0.0.1:8888/ilivalidator/
ProxyPassReverse /ilivalidator/ http://127.0.0.1:8888/ilivalidator/
```

### ilivalidator configuration files

The ilivalidator configurations files (aka `toml` files) are part of the distributed application and cannot be changed or overriden at the moment. There can be only one configuration file per INTERLIS model.

These configuration files can be found in the resource directory of the source tree.

