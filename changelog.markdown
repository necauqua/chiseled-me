# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog], and this project adheres to
[Semantic Versioning].

Also dates in this file are in [Holocene Calendar]
because it is amazing, logical and I want more people to know about it.

## [1.1.3] 12019-07-14
### Changed
 - This version of the mod works with 1.11, 1.11.1 and 1.11.2 Minecraft versions
### Fixed
 - Clipping distance not being changed with size when clouds were enabled (fixes #15)
 - Pistons, shulker and other players now move resized player properly

## [1.1.2] 12019-04-22
### Changed
 - Updated the mod to work with minecraft version 1.11.2

## [1.1.1] 12019-04-22
### Changed
 - Updated the mod to work with minecraft version 1.11

## [1.1.0] 12019-04-22
### Added
 - Recalibrator placed in a dispenser makes it capable of changing the size of
   entities it's facing.
 - Throwables (at least vanilla ones), like arrows, snowballs, potions or ender
   pearls are now affected by thrower size (potion area of effect is not yet 
   affected nor are the particles)
 - Recalibrator reach for entities now defaults to 64 blocks and can be 
   configured (or disabled if set to 0)
 - Support for the Forge version checker (you will see in-game if a new version
   of the mod was released)
 - `getRenderSize` API call with partialTick (for modders)
 - Improved ASM errors when something goes wrong with bytecode manipulation
 - Russian translation
 - German translation (by Vexatos)
 - Simplified Chinese translation (by 3TUSK)
### Fixed
 - Fixed (possible) server crashes
 - Fixed size being reset after traveling to another dimension
 - Fixed skeleton bbox not being scaled
 - Fixed hand bobbing movement
 - Distance required to move to hear a step sound is scaled
 - Items dropped by entities not by death are now scaled too (e.g. wool from a
   sheared sheep)
 - Reach distance not being scaled when small (this was intended in the first
   place thus is not configurable)
 - Distance between feet and the ground that affects the shadow alpha not being
   multiplied
 - Fall damage multiplier can now depend on size and be configured
 - The speed of the size change process, now it is consistently
   `2*|log2(prev) - log2(new)|` ticks (each scale up/down by a factor 2 adds up
   to time linearly)
 - Changed incorrect portal bbox config entry name
### Changed
 - This project now adheres to [Semantic Versioning] starting with
   version 1.1.0
 - Mod package is changed from `necauqua.mods.cm` to `dev.necauqua.mods.cm` to
   comply with the [maven naming conventions] since I own the domain
   `necauqua.dev`. This is a huge API change (and a commit touching all source
   files sadly) but the project follows [semver][Semantic Versioning] only starting
   from this version and the actual API is minimal so it's fine.

## [1.0] - 12016-06-20
### Added
 - The 2016 version of the mod, when it was first released


[Semantic Versioning]: https://semver.org/spec/v2.0.0.html "Semantic Versioning"
[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/ "Keep a Changelog"
[Holocene Calendar]: https://en.wikipedia.org/wiki/Holocene_calendar "Holocene Calendar"
[maven naming conventions]: https://maven.apache.org/guides/mini/guide-naming-conventions.html "maven naming conventions"
