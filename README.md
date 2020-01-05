[![Build Status](https://travis-ci.org/sogis/ilivalidator-web-service.svg?branch=master)](https://travis-ci.org/sogis/ilivalidator-web-service)
[![Build Status](https://github.com/sogis/ilivalidator-web-service/workflows/Java%20CI/badge.svg)](https://github.com/sogis/ilivalidator-web-service/workflows/Java%20CI/badge.svg)

# ilivalidator web service - a minimalist INTERLIS check service

The ilivalidator web service is a [spring boot](https://projects.spring.io/spring-boot/) application and uses [ilivalidator](https://github.com/claeis/ilivalidator) for the INTERLIS transfer file validation.

## TODO
- Find out a smart way to deploy the extension functions models.
- Publish `SO_FunctionsExt.ili` and `SO_Nutzungsplanung_20171118_Validierung_20190129.ili` in official INTERLIS repo. 

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

`git clone https://github.com/sogis/ilivalidator-web-service.git` 

Use your favorite IDE (e.g. [Spring Tool Suite](https://spring.io/tools/sts/all)) for coding.

### Additional models

Ilivalidator needs a toml file if you want to apply an additional model for your additional checks. The toml file must be all lower case, placed in the `toml` folder and named like the base model itself, e.g. `SO_Nutzungsplanung_20171118` -> `so_nutzungsplanung_20171118.toml`. The additional model can be placed in the `ili` folder or in any model repository that ilivalidator finds out-of-the-box.

### Ilivalidator custom functions

Your very own (Java) custom functions need be registered to ilivalidator during runtime. For this it is not enough to put the jar file containing the custom function classes available into the classpath (Guess it's because of knowing the qualified INTERLIS function name). The Gradle task `copyToLibsExt` will copy the Jar file from a defined maven repository into the `libs-text` folder before the `build` task. Use `--refresh-dependencies` if you need to update the custom function jar.

If the custom functions have dependencies, you will need to add them in the ilivalidator web service as dependency as well.

#### Land use planning

Für die Validierung der Nutzungsplanung werden zusätzliche Prüfungen vorgenommen. Sowohl mit "einfachen", zusätzlichen Constraints, aber auch mit zusätzlichen Java-Funktionen. Wegen einigen Bugs resp. fehlenden Funktionen, muss dazu die Originial-INTERLIS-Modell-Datei angepasst werden. Aus diesem Grund ist ein angepasstes `SO_Nutzungsplanung_20171118`-Modell im `ili`-Ordner.

- https://github.com/claeis/ilivalidator/issues/180
- https://github.com/claeis/ilivalidator/issues/196 (fixed)
- https://github.com/claeis/ilivalidator/issues/203 (fixed)
- https://github.com/claeis/ilivalidator/issues/204
- https://github.com/claeis/ilivalidator/issues/205
- https://github.com/claeis/ili2c/issues/6

### Testing

Since ilivalidator is heavily tested in its own project, there are only functional tests of the web service implemented.

`./gradlew clean test` will run all tests by starting the web service and uploading an INTERLIS transfer file.

### Building

`./gradlew clean build` will create an executable JAR. Ilivalidator custom functions will not work. Not sure why but must be something with how the plugin loader works. Therefor the Docker image will not use the the unzipped/unpacked jar (see [Dockerfile](Dockerfile)).

### Release management / versioning

It uses a simple release management and versioning mechanism: Local builds are tagged as `1.0.LOCALBUILD`. Builds on Travis or Jenkins will append the build number, e.g. `1.0.48`. Major version will be increased after "major" changes. After every commit to the repository a docker image will be build and pushed to `hub.docker.com`. It will be tagged as `latest` and with the build number (`1.0.48`).

## Running as Docker Image (SO!GIS)
See [openshift/README.md](openshift/README.md)


## ilivalidator configuration files

The ilivalidator configurations files (aka `toml` files) are part of the distributed application and cannot be changed or overriden at the moment. There can be only one configuration file per INTERLIS model.

These configuration files can be found in the resource directory of the source tree.

