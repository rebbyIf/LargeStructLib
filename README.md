Library of Large structures & structure sets, including:

Large Jigsaw Structures
-------

Modified Jigsaw structures that can span a large radius. Formed by pregenerating the structure & placing it per chunk. Relies on a Grid Structure Placement structure set in order to generate. **Any other structure set that doesn't generate per chunk will result in a broken structure**

Mostly follows standard jigsaw structure format. Refer to [the minecraft wiki](https://minecraft.wiki/w/Jigsaw_structure#Config) for examples.

Example definition:
``` json5
{
  // ID for Large Jigsaw Structures
  "type": "largestructlib:large_jigsaw",
  // Would recommend setting biomes to
  // all within a specific dimension.
  "biomes": "#minecraft:is_overworld",
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "spawn_overrides": {},
  "start_height": {
    "absolute": 0
  },
  "start_jigsaw_name": "minecraft:city_anchor",
  "start_pool": "minecraft:ancient_city/city_center",
  "step": "surface_structures",
  "terrain_adaptation": "none",
  "use_expansion_hack": true,
  // Max distance from the center expanded up to 4096.
  "max_distance_from_center": 2048,
  // Size expanded up to 512.
  "size": 256,
  // Unique to Large Jigsaw Structures. Specifies the
  // padding between structures of the same type. Distance
  // between each structure is:
  // max_distance_from_center + paddding
  //
  // Range from 1-4096
  "padding": 4
  // Chance of a structure spawning within the padding
  // + distance from center grid.
  // Range from 0.0 to 1.0.
  // Optional
  "frequency": 1.0
}
```

Grid Structure Placement
-------

Places structures per chunk. Recommended to use with Large Jigsaw Structures.

Example definition:
``` json5
{
  "placement": {
    // ID for Grid Structure Sets.
    // I would not recommend adding any extra features as it may
    // result in irregular terrain-gen, especially frequency.
    "type": "largestructlib:grid",
    "salt": 10387312
  },
  "structures": [
    {
      "structure": "testing:village",
      "weight": 1
    }
  ]
}
```

Dependencies
=====

This project utliizes [Structure Layout Optimizer](https://github.com/TelepathicGrunt/StructureLayoutOptimizer) and [its dependency](https://github.com/Team-Resourceful/Resourceful-Config) for improved structure-gen performance.

Copyright
=====

I personally don't care for copyright so long as you credit me. You may fork & add to this project as you so like so long as you credit me. You may also distribute said versions commercially so long as there's no paywall behind your mod. You may utilize my mod as a dependency as you so like. **However, you may not commercially distribute this mod as is, without use as an api, unless I give my blessing.** Basically don't steal my mod for commercial use unless it's used for something else. This does not include using it in a modpack.

Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
