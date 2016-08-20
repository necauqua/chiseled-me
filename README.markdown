# Chiseled-Me

This is (obviously) a coremod.
It adds some items, recipes to which you can see with JEI/NEI.
These items are pretty self-explanatory, but to get the recalibrator that makes you normal size again,
you need to deplete any other recalibrator(so the cheeper one you make - the more you click).

Known issues:
 * Liquids are funky
 * Ladders are funky
 * No riding(but will be in the future)

About coremods:
* They DO NOT EVER corrupt worlds.
* They DO NOT EVER corrupt you jar-files, the whole point of them is not to modify
the jar but still modify the program, at runtime.
* They MAY not work just for you for unknown reasons, but most likely they will.

If you have any issues, feel free to report them at issues page, ALWAYS including your *fml-client-latest.log* file.

## Installation

If you want to modify/compile, you can just run the following commands(assuming you have git installed):

```
git clone https://github.com/necauqua/Chiseled-Me.git
cd Chiseled-Me
```

If you want to change anything, do this:
```
gradlew setupDecompWorkspace
```
Then apply the changes.


And if you want and compile it - you can with:
```
gradlew build
```
The jars will be in build/libs folder.

## Contribution
I'll accept pull requests if they fix small and obvious bugs or something like that.

If you want to suggest an idea - just create an issue about it.

## License
This project is licensed under Apache License v2.0 as it says in every source file. About it you can read *by just googling*. Kappa :D