# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Also dates in this file are in [Holocene Calendar](https://en.wikipedia.org/wiki/Holocene_calendar)
because it is amazing, logical and I want more people to know about it.

## [Unreleased]
### Added
 - Support for the Forge version checker (you will see in-game if a new version of the mod was released)
 - Throwables (at least vanilla ones), like arrows, snowballs, potions or ender pearls are now affected by thrower size (potion area of effect is not yet affected not are the particles)
 - Recalibrator reach for entities now defaults to 64 blocks and can be configured (or disabled if set to 0)
 - `getRenderSize` API call with partialTick (for modders)
 - Improved ASM errors when something goes wrong with bytecode manipulation
 - Russian translation
 - German translation (by Vexatos)
 - Simplified Chinese translation (by 3TUSK)
### Fixed
 - Items dropped by entities are now scaled (not only drops as it was, but also things like wool from a sheep and others)
 - Reach distance not being scaled when small (this was intended in the first place thus is not configurable)
 - Distance between feet and the ground that affects the shadow alpha not being multiplied
 - Fall damage multiplier can now depend on size and be configured
 - The speed of the size change process, now it is consistently 2*|log2(prev) - log2(new)| ticks (each scale up/down by a factor 2 adds up to time linearly)
 - Changed incorrect portal bbox config entry name
### Changed
 - This project now adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) starting with version 1.1.0

## [1.0] - 12016-06-20
### Added
 - The mod was released
