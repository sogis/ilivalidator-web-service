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

### Additional models

Ilivalidator needs a toml file if you want to apply an additional model for your additional checks. The toml file must be all lower case, placed in the `toml` folder and named like the base model itself, e.g. `SO_Nutzungsplanung_20171118` -> `so_nutzungsplanung_20171118.toml`. The additional model can be placed in the `ili` folder or in any model repository that ilivalidator finds out-of-the-box.

### Ilivalidator custom functions

Your very own (Java) custom functions need be registered to ilivalidator during runtime. For this it is not enough to put the jar file containing the custom function classes available into the classpath (Guess it's because of knowing the qualified INTERLIS function name). The Gradle task `copyToLibsExt` will copy the Jar file from a defined maven repository into the `libs-text` folder before the `build` task. Use `--refresh-dependencies` if you need to update the custom function jar.

### Testing

Since ilivalidator is heavily tested in its own project, there are only functional tests of the web service implemented.

`./gradlew clean test` will run all tests by starting the web service and uploading an INTERLIS transfer file.

### Building

`./gradlew clean build` will create an executable JAR.

### Release management / versioning

It uses a simple release management and versioning mechanism: Local builds are tagged as `1.0.LOCALBUILD`. Builds on Travis or Jenkins will append the build number, e.g. `1.0.48`. Major version will be increased after "major" changes. From every commit to the repository a docker image will be build and pushed to `hub.docker.com`. It will be tagged as `latest` and with the build number (`1.0.48`).

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

