# LootableMobCorpses
 Loot mobs with custom drops

![GitHub License](https://img.shields.io/github/license/whiteh4cker-tr/LootableMobCorpses?style=flat)
[![CodeFactor](https://www.codefactor.io/repository/github/whiteh4cker-tr/lootablemobcorpses/badge/main)](https://www.codefactor.io/repository/github/whiteh4cker-tr/lootablemobcorpses/overview/main)

<big>Supported Platforms</big><br>
[![spigot software](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.2.0/assets/compact-minimal/supported/spigot_vector.svg)](https://www.spigotmc.org/)
[![paper software](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/supported/paper_vector.svg)](https://papermc.io/)
[![purpur software](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/supported/purpur_vector.svg)](https://purpurmc.org/)

## Features

-   Customizable drops with drop chance
-   Custom corpse label

-   Enable or disable vanilla drops (disabled by default)
-   Automatically remove looted corpses

![alt text](https://i.imgur.com/5cuVYox.gif)

## Config
```
# The name of the corpse entity, with color code support using '&'
corpse-name: "&eLootable Corpse"
disable-vanilla-loot: true # Set to true to disable default mob drops

# Configuration for each mob type
# The 'corpse-duration' is the time in seconds until the corpse is automatically removed (if not empty)
# The 'loot' section defines the items that can be found in the corpse inventory
# Each loot entry is formatted as "MATERIAL:AMOUNT:CHANCE", where:
#   - MATERIAL is the material name (e.g., ROTTEN_FLESH)
#   - AMOUNT is the number of items
#   - CHANCE is the probability (0.0 to 1.0) of the item appearing in the inventory

ZOMBIE:
  corpse-duration: 900
  loot:
    - ROTTEN_FLESH:1:0.5
    - IRON_INGOT:1:0.1
SKELETON:
  corpse-duration: 600
  loot:
    - BONE:1:0.5
    - ARROW:2:0.3
CREEPER:
  corpse-duration: 1200
  loot:
    - GUNPOWDER:1:0.7
SPIDER:
  corpse-duration: 900
  loot:
    - STRING:1:0.5
    - SPIDER_EYE:1:0.2
ENDERMAN:
  corpse-duration: 1200
  loot:
    - ENDER_PEARL:1:0.5
PIG:
  corpse-duration: 900
  loot:
    - PORKCHOP:1:0.7
COW:
  corpse-duration: 900
  loot:
    - LEATHER:1:0.5
    - BEEF:1:0.7
CHICKEN:
  corpse-duration: 900
  loot:
    - FEATHER:1:0.5
    - CHICKEN:1:0.7
SHEEP:
  corpse-duration: 900
  loot:
    - WHITE_WOOL:1:0.7
    - MUTTON:1:0.5
HORSE:
  corpse-duration: 900
  loot:
    - LEATHER:1:0.3
    - SADDLE:1:0.1
WOLF:
  corpse-duration: 900
  loot:
    - BONE:1:0.4
OCELOT:
  corpse-duration: 900
  loot:
    - TROPICAL_FISH:1:0.3
CAT:
  corpse-duration: 900
  loot:
    - STRING:1:0.4
VILLAGER:
  corpse-duration: 1200
  loot:
    - EMERALD:1:0.2
IRON_GOLEM:
  corpse-duration: 1800
  loot:
    - IRON_INGOT:3:0.8
    - POPPY:1:0.5
WITHER:
  corpse-duration: 900
  loot:
    - NETHER_STAR:1:1.0
ENDER_DRAGON:
  corpse-duration: 900
  loot:
    - DRAGON_EGG:1:1.0
    - ENDER_PEARL:8:0.7
BAT:
  corpse-duration: 300
  loot:
    - COAL:1:0.2
GHAST:
  corpse-duration: 900
  loot:
    - GHAST_TEAR:1:0.5
    - GUNPOWDER:1:0.7
MAGMA_CUBE:
  corpse-duration: 900
  loot:
    - MAGMA_CREAM:1:0.7
SLIME:
  corpse-duration: 900
  loot:
    - SLIME_BALL:1:0.7
BLAZE:
  corpse-duration: 900
  loot:
    - BLAZE_ROD:1:0.8
ZOMBIFIED_PIGLIN:
  corpse-duration: 900
  loot:
    - GOLD_NUGGET:1:0.5
    - ROTTEN_FLESH:1:0.3
HOGLIN:
  corpse-duration: 900
  loot:
    - PORKCHOP:2:0.7
    - CRIMSON_FUNGUS:1:0.3
ZOGLIN:
  corpse-duration: 900
  loot:
    - ROTTEN_FLESH:2:0.7
    - WARPED_FUNGUS:1:0.3
PIGLIN:
  corpse-duration: 900
  loot:
    - GOLD_INGOT:1:0.4
    - GOLD_NUGGET:2:0.6
STRIDER:
  corpse-duration: 900
  loot:
    - STRING:2:0.5
VEX:
  corpse-duration: 900
  loot:
    - EMERALD:1:0.1
GUARDIAN:
  corpse-duration: 900
  loot:
    - PRISMARINE_SHARD:1:0.5
    - COD:1:0.7
ELDER_GUARDIAN:
  corpse-duration: 900
  loot:
    - PRISMARINE_SHARD:2:0.7
    - PRISMARINE_CRYSTALS:1:0.5
    - COD:2:0.8
SHULKER:
  corpse-duration: 900
  loot:
    - SHULKER_SHELL:1:0.5
SILVERFISH:
  corpse-duration: 900
  loot:
    - STONE:1:0.2
ENDERMITE:
  corpse-duration: 900
  loot:
    - END_STONE:1:0.2
WITCH:
  corpse-duration: 900
  loot:
    - GLASS_BOTTLE:1:0.5
    - GLOWSTONE_DUST:1:0.3
    - GUNPOWDER:1:0.4
    - REDSTONE:1:0.4
    - SPIDER_EYE:1:0.2
    - SUGAR:1:0.4
    - STICK:1:0.3
PILLAGER:
  corpse-duration: 900
  loot:
    - ARROW:2:0.6
    - CROSSBOW:1:0.2
RAVAGER:
  corpse-duration: 1800
  loot:
    - SADDLE:1:0.7
VINDICATOR:
  corpse-duration: 900
  loot:
    - EMERALD:1:0.3
    - IRON_AXE:1:0.1
EVOKER:
  corpse-duration: 1800
  loot:
    - TOTEM_OF_UNDYING:1:1.0
    - EMERALD:2:0.5
PHANTOM:
  corpse-duration: 900
  loot:
    - PHANTOM_MEMBRANE:1:0.6
BEE:
  corpse-duration: 900
  loot:
    - HONEYCOMB:1:0.4
CAVE_SPIDER:
  corpse-duration: 900
  loot:
    - STRING:2:0.6
    - SPIDER_EYE:1:0.3
DOLPHIN:
  corpse-duration: 900
  loot:
    - COD:1:0.8
DROWNED:
  corpse-duration: 900
  loot:
    - ROTTEN_FLESH:1:0.5
    - COPPER_INGOT:1:0.2
    - TRIDENT:1:0.05
FOX:
  corpse-duration: 900
  loot:
    - SWEET_BERRIES:1:0.5
LLAMA:
  corpse-duration: 900
  loot:
    - LEATHER:1:0.4
    - LEAD:1:0.2
PANDA:
  corpse-duration: 900
  loot:
    - BAMBOO:1:0.7
PARROT:
  corpse-duration: 900
  loot:
    - FEATHER:2:0.5
POLAR_BEAR:
  corpse-duration: 900
  loot:
    - COD:2:0.7
    - SALMON:2:0.7
RABBIT:
  corpse-duration: 900
  loot:
    - RABBIT_HIDE:1:0.4
    - RABBIT:1:0.6
    - RABBIT_FOOT:1:0.1
SQUID:
  corpse-duration: 900
  loot:
    - INK_SAC:1:0.8
WANDERING_TRADER:
  corpse-duration: 900
  loot:
    - EMERALD:2:0.4
    - LEAD:1:0.5
```

## Requirements

-   Java 21 or higher
-   Spigot/Paper/forks MC v1.21.4
