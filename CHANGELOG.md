# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

- Checkbox in the GUI for applying the configuration file will be hidden at the moment. Configuration file support will work with e.g. `curl`. Not visible at the moment but the code is there.

## [0.0.9] - 2017-09-XX

### Added

- ilivalidator configuration file (aka `toml`) support. One configuration file for one INTERLIS model only.
- Added and adjusted some scripts for AWS Codepipeline with deployment on EBS.

### Changed

- ilivalidator-1.4.0
- Use `java.io.tmpdir` as default temporary directory. Can be overriden with the `ch.so.agi.ilivalidator.uploadedFiles` in the `application.properties` file.

