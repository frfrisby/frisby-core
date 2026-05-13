# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is managed by [release-please](https://github.com/googleapis/release-please).
Do not edit it manually — new entries are prepended automatically when a release PR is merged.

<!-- releasing: start -->
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

