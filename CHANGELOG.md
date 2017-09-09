# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

- Checkbox in the GUI for applying the configuration file is hidden at the moment in the gui but is exposed in the api. 

## [0.0.9] - 2017-09-XX

### Added

- ilivalidator configuration file (aka `toml`) support. One configuration file for one INTERLIS model only (baked into resources directory). It will be used by default if one is present. It looks for a *.toml file with the same name as the data model name. The configuration file name must be lower-case.
- Added and adjusted some scripts for AWS Codepipeline with deployment on EBS.
- Add Spring Boot Actuators with a custom endpoint for showing the application's classpath.
- INTERLIS models can be stored in `src/main/resources/ili`. These models will be copied into the folder where the transfer file stored and will be picked up with highest priority.

### Changed

- Spring Boot 1.5.6
- ilivalidator-1.4.0
- Use sogeo.services artifactory repository instead of static maven repository.
- Use `java.io.tmpdir` as default temporary directory. Can be overriden with the `ch.so.agi.ilivalidator.uploadedFiles` in the `application.properties` file.
- Result output is now UTF-8.

