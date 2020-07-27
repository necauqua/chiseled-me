# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog], and this project uses [Forge versioning scheme].

Also dates in this file are in [Holocene Calendar] because it is amazing, logical and I want more people to know about it.

## Unreleased
### Fixed
 - Improved general mod compatibility (fixed a ton of unreported mod compat crashes in advance)
 - Fix the crash with Spigot (also Mohist, #52)
 - Fix mod compat crash with Quark (#32)
 - Fix mod compat crash with MalisisDoors and most other Mixin mods (#21)
 - Fix the borked recipe that any two items without a recipe together made `Antipolarized Concentrated Pym essence` (#28)
 - Fix the crash with old Forge (the IRarity class not found error)

## [1.12.2-2.0.0.1] 12019-12-16
### Fixed
 - Fixed startup crashes
 
## [1.12.2-2.0.0.0] 12019-12-16
### Changed
 - Updated this mod to work with Minecraft 1.12.2

## [1.12.1-2.0.0.0] 12019-12-16
### Changed
 - Updated this mod to work with Minecraft 1.12.1

## [1.12-2.0.0.0] 12019-12-16
### Added
 - Size is shown in F3 debug output when its not 1
### Changed
 - Changed the size command to be `/sizeof entity` instead of `/sizeof entity get`
### Fixed
 - Entity position shifts on resize
 - (possibly) player model limbs missing after some resizes
 - Resize command output when resulting size is out of limits
 - `Null returned as 'hitResult', this shouldn't happen!` console spam when reach distance is lesser than 1
 - Config for fall damage was reverted this whole time and defaulted to scale damage by size when big (which is unusable lol)
 - Disabled supersmalls or bigs in config broke the advancemetns (actual Forge/Vanilla bug)


## [1.12-2.0.0.0-beta2] 12019-12-13
### Fixed
 - A really stupid regression which caused this mod to crash with almost any other mod (the ones who use `@SubscribeEvent` annotation and thats like all of them)
### Changed
 - /getsizeof and /setsizeof commands are replaced with /sizeof command, which is made more versatile and useful

## [1.12-2.0.0.0-beta1] 12019-12-09
### Added
 - Any recalibrator can be made into a reset one by putting it in the crafting field
### Changed
 - Increased the number of charges in 16 times for each recalibrator type (so the most powerful ones give you at least 16 uses now)
 - Proper implementation of recipes and advancements (to replace old achievements)
 - Yet another version scheme - now using the [Forge Versioning], since [Semantic Versioning] works poorly in scenario when you are modding other versioned product
### Fixed
 - Any item ID or translation key containing 'essense' was fixed to 'essence' (**THIS WILL DELETE ANY ESSENCES IN YOUR SAVES ON UPDATE**)
 - Entering the nether portal on dedicated server when small now does not cause crashes
### Removed
 - Due to the server-crash fix the bounding box of the nether portal is not altered and takes a full block as in vanilla

## [1.1.5-final] 12019-12-16
All of the below fixes were backported from 1.12 beta
### Fixed
 - Sun clipping when size of player is around 1/8-1/16
 - Resizing process was broken - it was twice as fast, had glitches line disappearing limbs and falling through blocks (this was due to the size being negative for a tick or two)
 - Small but noticeable position shifts on resizes (might've also caused collision glitches)
### Changed
 - /getsizeof and /setsizeof commands are replaced with /sizeof command, which is made more versatile and useful

## [1.1.4] 12019-12-06
### Fixed
 - Server-side entity bounding box not being properly set on logging in with non-standard size

## [1.1.3] 12019-12-05
### Changed
 - The whole core was rewritten to use an actual field for the size in Entity class, not a Forge capability, which is a huge optimization
 - This also means that all sizes in your worlds will be reset since they are now stored directly in entity NBT and not in a forgecap one
 - This version of the mod works with 1.11, 1.11.1 and 1.11.2 Minecraft versions
### Fixed
 - Clipping distance not being changed with size when clouds were enabled (fixes #15)
 - Pistons, shulker and other players now move resized player properly
 - Teleports to enormous negative y values when becoming big (fixes #2)
 - Little jumpy bounce when falling on block when small (caused by #2)
 - Wall-climbing-like behavior when small (caused by #2)
 - Network code was revised and the de-syncs should be gone now (fixes #17)

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

[Forge versioning scheme]: https://mcforge.readthedocs.io/en/latest/conventions/versioning/ "Forge versioning scheme"
[Semantic Versioning]: https://semver.org/spec/v2.0.0.html "Semantic Versioning"
[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/ "Keep a Changelog"
[Holocene Calendar]: https://en.wikipedia.org/wiki/Holocene_calendar "Holocene Calendar"
[maven naming conventions]: https://maven.apache.org/guides/mini/guide-naming-conventions.html "maven naming conventions"
