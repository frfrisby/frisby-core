# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is managed by [release-please](https://github.com/googleapis/release-please).
Do not edit it manually — new entries are prepended automatically when a release PR is merged.

<!-- releasing: start -->
## [1.1.1](https://github.com/frfrisby/frisby-core/compare/v1.1.0...v1.1.1) (2026-05-21)


### Bug Fixes

* Missing null guard in several of(GenericType) methods and add null-safety tests for all fluent GenericType overloads. ([d84b426](https://github.com/frfrisby/frisby-core/commit/d84b426862067cc2fa37b923e856ab647b89d7d6))


### Documentation

* Add per-method throws documentation to validation.md and create util.md. ([#56](https://github.com/frfrisby/frisby-core/issues/56)) ([7dc8e23](https://github.com/frfrisby/frisby-core/commit/7dc8e23ca4c8cec6d92e84001470ec708ade6c4c))

## [1.1.0](https://github.com/frfrisby/frisby-core/compare/v1.0.11...v1.1.0) (2026-05-18)


### Features

* Add Automatic-Module-Name to support JPMS automatic modules. ([#53](https://github.com/frfrisby/frisby-core/issues/53)) ([1fd199b](https://github.com/frfrisby/frisby-core/commit/1fd199bccfd437a06f528cf7fe28bbc96e808c3a))


### Bug Fixes

* Remove unused K type parameter from GroupBlock interface. ([#54](https://github.com/frfrisby/frisby-core/issues/54)) ([904ca51](https://github.com/frfrisby/frisby-core/commit/904ca51a784a16f2ab257dfedc528da0fb3fb78f))
* Resolved several code analysis warnings. ([#51](https://github.com/frfrisby/frisby-core/issues/51)) ([d3b52bf](https://github.com/frfrisby/frisby-core/commit/d3b52bffff6e7fdb5287d82c8ed9c9b55288bc84))

## [1.0.11](https://github.com/frfrisby/frisby-core/compare/v1.0.10...v1.0.11) (2026-05-15)


### Bug Fixes

* Require CI, SonarCloud, and CodeQL to pass before creating a release. ([#48](https://github.com/frfrisby/frisby-core/issues/48)) ([b1c7e00](https://github.com/frfrisby/frisby-core/commit/b1c7e0089a292cade818ee02e3729bd1e4cb03bc))

## [1.0.10](https://github.com/frfrisby/frisby-core/compare/v1.0.9...v1.0.10) (2026-05-15)


### Bug Fixes

* Remove status polling — exit on 201 upload confirmation. ([#45](https://github.com/frfrisby/frisby-core/issues/45)) ([a2fb7a8](https://github.com/frfrisby/frisby-core/commit/a2fb7a8cb4cd013ac691d592f73a353811b084b3))

## [1.0.9](https://github.com/frfrisby/frisby-core/compare/v1.0.8...v1.0.9) (2026-05-15)


### Bug Fixes

* Exit 0 after confirmed upload when status API is unavailable. ([#42](https://github.com/frfrisby/frisby-core/issues/42)) ([3614cd6](https://github.com/frfrisby/frisby-core/commit/3614cd69bfea5202e31763cb983317188cb2ca75))

## [1.0.8](https://github.com/frfrisby/frisby-core/compare/v1.0.7...v1.0.8) (2026-05-15)


### Bug Fixes

* Strip whitespace from deployment ID to prevent malformed status API URL. ([#39](https://github.com/frfrisby/frisby-core/issues/39)) ([dec47c2](https://github.com/frfrisby/frisby-core/commit/dec47c293f6283b980594f7db6dbe5ed3c5ec3b9))

## [1.0.7](https://github.com/frfrisby/frisby-core/compare/v1.0.6...v1.0.7) (2026-05-15)


### Bug Fixes

* Improve Central Portal upload error logging to expose API response body. ([#36](https://github.com/frfrisby/frisby-core/issues/36)) ([3a2a665](https://github.com/frfrisby/frisby-core/commit/3a2a66529ae7d9b3612b1f624a3b1092cdfdd1bf))

## [1.0.6](https://github.com/frfrisby/frisby-core/compare/v1.0.5...v1.0.6) (2026-05-15)


### Bug Fixes

* Remove duplicate release trigger that fired publish twice when using PAT. ([#33](https://github.com/frfrisby/frisby-core/issues/33)) ([b5ff705](https://github.com/frfrisby/frisby-core/commit/b5ff7056b00d17f96ca975636d098d0f42d68154))

## [1.0.5](https://github.com/frfrisby/frisby-core/compare/v1.0.4...v1.0.5) (2026-05-15)


### Bug Fixes

* Replace central-publishing-maven-plugin with direct REST API upload to avoid multi-module deployment conflicts. ([#29](https://github.com/frfrisby/frisby-core/issues/29)) ([09847c6](https://github.com/frfrisby/frisby-core/commit/09847c63f30126e35339b5474367bffa4d7c0697))

## [1.0.4](https://github.com/frfrisby/frisby-core/compare/v1.0.3...v1.0.4) (2026-05-15)


### Bug Fixes

* Publish all modules as a single Central Portal deployment bundle. ([#26](https://github.com/frfrisby/frisby-core/issues/26)) ([950ebdd](https://github.com/frfrisby/frisby-core/commit/950ebddea780742386f1644e490dd55081135888))

## [1.0.3](https://github.com/frfrisby/frisby-core/compare/v1.0.2...v1.0.3) (2026-05-15)


### Bug Fixes

* Upgrade central-publishing-maven-plugin to 0.10.0 to resolve multi-module deployment conflict. ([#23](https://github.com/frfrisby/frisby-core/issues/23)) ([d6e2f8e](https://github.com/frfrisby/frisby-core/commit/d6e2f8e5007066cdae6dbf29d694271460955792))

## [1.0.2](https://github.com/frfrisby/frisby-core/compare/v1.0.1...v1.0.2) (2026-05-15)


### Bug Fixes

* Resolved java:S1066 code analysis warnings across the concurrence module. ([#21](https://github.com/frfrisby/frisby-core/issues/21)) ([68ba3e0](https://github.com/frfrisby/frisby-core/commit/68ba3e0404a5d7ab126ef2980d79f8da1ee62352))
* Resolving minor code analysis warnings. ([#17](https://github.com/frfrisby/frisby-core/issues/17)) ([87b2e73](https://github.com/frfrisby/frisby-core/commit/87b2e739bbd11a8a8576acbaf25f82c3d722a607))
* Restore publish workflow trigger and fix README version regex. ([#14](https://github.com/frfrisby/frisby-core/issues/14)) ([24eafc7](https://github.com/frfrisby/frisby-core/commit/24eafc7d4a7962a34dacf5a6c395d105a9f7b1f2))

## [1.0.1](https://github.com/frfrisby/frisby-core/compare/v1.0.0...v1.0.1) (2026-05-14)


### Bug Fixes

* Add explicit url to child module POMs to prevent ossrh flatten mode appending module name. ([47620a5](https://github.com/frfrisby/frisby-core/commit/47620a57d472c30c42bff516f6ff76aafcdeea90))
* Use validated wait state for Maven Central publish to avoid 30-minute propagation timeout. ([b32c26b](https://github.com/frfrisby/frisby-core/commit/b32c26b3832b3301f4e0255ccbdc3019ff3b39bc))

## 1.0.0 (2026-05-13)

### Added

- `validation` module — zero-dependency validation utilities (`Strings`, `TrimmedStrings`,
  `Numbers`, `Durations`, `Periods`, `Instants`, `LocalDates`, `LocalDateTimes`, `LocalTimes`,
  `OffsetDateTimes`, `OffsetTimes`, `ZonedDateTimes`, `Sequences`, `Maps`, `Parameters`,
  `FieldGroups`) with a complete exception hierarchy.
- `util` module — general-purpose utilities (`StopWatch`, `Decimals`).
- `concurrency` module — async pipeline blocks (`SourceBlock`, `BufferBlock`, `BatchBlock`,
  `GroupBlock`, `ActionBlock`, `BroadcastBlock`, `BranchBlock`, `RouterBlock`, `ExpandBlock`,
  `DelayBlock`, `PriorityBufferBlock`) with fluent `Pipeline` and `OpenPipeline` builders,
  and `NamedExecutorService`.
- `bom` module — Bill of Materials for consumers who manage all versions centrally.
<!-- releasing: end -->
