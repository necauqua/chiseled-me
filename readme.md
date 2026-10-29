<!--
tags:
  - archive
  - minecraft/mod
  - lang/java
-->

> [!note]
> Current state of the repo is my my unfinished work of rewriting and updating
> chiseled me to newer versions of minecraft (1.16 at the time) and making new
> features etc.
>
> Don't mind parts of the the commit history being AI-made, this is just a
> recreation from several different backups of unfinished work (the wip/*
> branches) on different machines with approximate timestamps of when it
> actually happened - everything was made here way before AI was a thing
> ¯\_(ツ)_/¯

# Chiseled-Me

The best resizing mod for Minecraft entities.
Only 1.12.2 for now, game version updates are coming soon.

It uses the [Mixin](https://github.com/SpongePowered/Mixin) library to heavily modify
the game internals to allow entity (and particle) resizing which is the most realistic
and as glitch-free as possible in this game given that other mods exist, and the universe is not perfect.

## Installation
This mod uses ForgeGradle 3 so installation of the project it pretty straightforward:
- clone this repository
- import it into the IDE of your choice, or just modify the code with notepad or something
- compile by running `gradlew build`, the resulting (unsigned) jar will be in the `build/libs` folder

## Contribution
Localization PRs are welcome and accepted, same with obvious mini-bugs/typos, for anything greater
than that feel free to open an [issue](https://github.com/necauqua/chiseled-me/issues) (search first for similar ones)
or contact me (and other people who are enthusiastic about the mod) on my [Discord](https://discord.com/invite/pZrbJNb) server.

# License
It was MIT - you can use it however you like if you preserve the LICENSE file
which has my name on top of it.
