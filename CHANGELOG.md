# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is managed by [release-please](https://github.com/googleapis/release-please).
Do not edit it manually — new entries are prepended automatically when a release PR is merged.

<!-- releasing: start -->
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

## [1.0.0] — 2026-05-13

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
