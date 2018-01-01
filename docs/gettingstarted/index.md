Getting Started with Mist Core
==========================

This is a simple guide to get you started with Mist Core. This guide covers adding Mist Core as a dependency in your dev environment using 
gradle and maven. And it covers setting Mist Core as a minecraft mod dependency.

Adding Mist Core as a Maven dependency
--------------------

In order to reference parts of the Mist Core API you will need to add the mod as a dependecy using gradle.

1. Open your build.gradle file and after the `minecraft` block add this:
```
repositories {
  maven { // Mist Core
    name 'Hennamann Repo'
    url "https://maven.henrikstabell.com/"
  }
}
```
This will tell Gradle to search for dependecies you request in the Mist Core maven repository.

2. Enter the `dependencies` block and add this:
```
dependencies {
  deobfProvided "com.henrikstabell.mistcore:MistCore-1.12:1.1.1-67:dev"
}
```
This will tell Gradle what dependency it should look for in the maven repositroy we added earlier.
The first part (parts are separated by colons) is the package, aka all the folders before the main one. In this instance, it's `com.henrikstabell.mistcore`. Next you add the artifact ID, or the mod name, in this case 'MistCore'. Next, you add the version, this is the folder inside the mod name folder. Finally you can add an identifier, for instance `:dev` for a deobfuscated dev jar, or `:sources` for the sources jar
For development purposes the :dev identifier is recommended.

3. Run the setupDecompWorkspace gradle command in your mod directory in order to download and add the new Mist Core dependency. 
if everything worked you should see the Mist Core dev jar in the External Libraries section of your IDE.

Adding Mist Core as a Minecraft mod dependency
--------------------
When using Mist Core in your code it´s important that Mist Core loads before your mod does, and that Mist Core is present, if not your mod will likely crash
with a Nullpointer Exception.
To add Mist Core as a mod dependency go to your main mod class and find the `@Mod` annotation add a new field called `dependencies` and make it equal: "required-after:mistcore"
your `@Mod` annotation should now look like this:

```java
@Mod(modid = MainModClass.MODID, version = MainModClass.VERSION, dependencies = "required-after:mistcore")
public class MainModClass
```

Minecraft Forge will now make sure your mod loads after Mist Core and warn users when attempting to load your mod without Mist Core being present.

!!! note

    If you are using Mist Core as an optional dependency 
    adding Mist Core as a Mod dependency will not be necessary.
    
    If you are unsure if your mod is using Mist Core as an optional dependency it might be smart to play it
    safe and add Mist Core as a mod dependency.    .
