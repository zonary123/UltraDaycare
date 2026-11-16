# UltraDayCare - Comprehensive Administrator & Developer Documentation

An enterprise-grade Pokémon Daycare, Breeding, and Egg Incubation ecosystem for Cobblemon networks. UltraDayCare re-engineers Cobblemon breeding with dual operation modes (Standard Pokémon vs Hardcore PokeMMO parent-consumption economies), multi-slot daycare plots, an independent step-tracking incubator system, 3D animated egg entities with progressive shell-crack models, international Masuda Method geo-tracking, comprehensive genetic inheritance (IVs, Natures, Abilities, Poké Balls, Egg Moves, Regional Forms), and multi-database persistence (JSON, MongoDB, SQL).

---

## Table of Contents
1. [Architectural Overview & Breeding Flow](#1-architectural-overview--breeding-flow)
2. [Dual Breeding Operation Modes](#2-dual-breeding-operation-modes)
   - [2.1 Standard Pokémon Mode (`pokemon`)](#21-standard-pokémon-mode-pokemon)
   - [2.2 PokeMMO Economy Mode (`pokemmo`)](#22-pokemmo-economy-mode-pokemmo)
3. [Genetic Inheritance Subsystems](#3-genetic-inheritance-subsystems)
   - [3.1 Individual Values (IVs) & Held Item Inheritance](#31-individual-values-ivs--held-item-inheritance)
   - [3.2 Nature Inheritance (Everstone)](#32-nature-inheritance-everstone)
   - [3.3 Ability & Hidden Ability (HA) Propagation](#33-ability--hidden-ability-ha-propagation)
   - [3.4 Poké Ball Heritage & Master/Cherish Restrictions](#34-poké-ball-heritage--mastercherish-restrictions)
   - [3.5 Egg Moves & Mirror Herb Mechanics](#35-egg-moves--mirror-herb-mechanics)
   - [3.6 Masuda Method & International Geo-Tagging](#36-masuda-method--international-geo-tagging)
   - [3.7 Regional Forms & Baby Incenses](#37-regional-forms--baby-incenses)
4. [3D Animated Egg Entities & Datapack Features](#4-3d-animated-egg-entities--datapack-features)
   - [4.1 Custom Egg Models & Cracking Stages](#41-custom-egg-models--cracking-stages)
   - [4.2 The `breedable` Custom Pokémon Property](#42-the-breedable-custom-pokemon-property)
5. [Egg Incubation & Step Engine (`/incubators`)](#5-egg-incubation--step-engine-incubators)
   - [5.1 Hatch Methods (Walk Steps vs Real-Time Ticks)](#51-hatch-methods-walk-steps-vs-real-time-ticks)
   - [5.2 Movement Listener & Flame Body Accelerators](#52-movement-listener--flame-body-accelerators)
   - [5.3 Incubator Slot Unlock Progression](#53-incubator-slot-unlock-progression)
6. [Commands & Permissions Reference](#6-commands--permissions-reference)
7. [Database Architecture & Data Models](#7-database-architecture--data-models)
   - [7.1 Storage Engines (JSON, MongoDB, SQL)](#71-storage-engines-json-mongodb-sql)
   - [7.2 User Profile Schema (`User` & `Plot`)](#72-user-profile-schema-user--plot)
8. [Configuration Reference & Directory Layout](#8-configuration-reference--directory-layout)
   - [8.1 `config.json` Specification](#81-configjson-specification)
   - [8.2 `incubators.json` Specification](#82-incubatorsjson-specification)
   - [8.3 `language.json` Reference](#83-languagejson-reference)
9. [Complete Production Configuration Templates](#9-complete-production-configuration-templates)
   - [9.1 Standard RPG Balanced `config.json`](#91-standard-rpg-balanced-configjson)
   - [9.2 Hardcore PokeMMO Economy `config.json`](#92-hardcore-pokemmo-economy-configjson)
   - [9.3 Production `incubators.json`](#93-production-incubatorsjson)
10. [Troubleshooting & Frequently Asked Questions](#10-troubleshooting--frequently-asked-questions)

---

## 1. Architectural Overview & Breeding Flow

UltraDayCare replaces the basic Pasture Block mechanics of Cobblemon with an asynchronous, server-friendly Daycare and Incubation suite.

```
                           [ Player Opens /daycare ]
                                       │
                                       ▼
                         [ Select Daycare Plot GUI ]
                                       │
                     ┌─────────────────┴─────────────────┐
                     ▼                                   ▼
             Select Parent 1                     Select Parent 2
          (Male / Female / Ditto)             (Compatible Egg Group)
                     │                                   │
                     └─────────────────┬─────────────────┘
                                       │
                                       ▼
                       [ Validate Compatibility & Items ]
                      (Everstone, Destiny Knot, Incenses)
                                       │
                                       ▼
                       [ Periodic Breeding Task Cycle ]
                     (Compatibility Rate x World Ticks)
                                       │
                                       ▼
                           [ Egg Produced in Plot ]
                                       │
                         PokeMMO or Standard Mode?
                        ┌──────────────┴──────────────┐
                   PokeMMO                         Standard
                        │                             │
               [ Parents Consumed ]          [ Parents Retained ]
                        │                             │
                        └──────────────┬──────────────┘
                                       │
                                       ▼
                         [ Move Egg to /incubators ]
                                       │
                                       ▼
                          [ Walk Steps Accumulation ]
                       (Flame Body / Magma Armor Boosts)
                                       │
                                       ▼
                            [ 3D Egg Cracks & Hatches ]
                          (Newborn Claimed to Party/PC)
```

---

## 2. Dual Breeding Operation Modes

The operational mode is defined in `config.json` via the `mode` parameter.

### 2.1 Standard Pokémon Mode (`pokemon`)
- **Parent Safety**: Parents remain safe inside the daycare plot and can continuously produce eggs over time.
- **Classic Inheritance**: Standard 3 random IVs inherited (5 with Destiny Knot), Everstone passes nature, and species follows the female parent.
- **Target Audience**: Casual, survival, and standard Cobblemon servers focusing on collection and exploration.

### 2.2 PokeMMO Economy Mode (`pokemmo`)
- **Parent Consumption**: Both parents are permanently consumed upon egg generation!
- **Strict Deterministic IV Inheritance**:
  - Power / Brace items guarantee exact 31 IV inheritance for specific stats.
  - Non-braced IVs are chosen randomly from either parent (or averaged depending on configuration).
- **Target Audience**: Competitive MMO servers requiring resource sinks to maintain long-term Pokémon market value and trade economies.

---

## 3. Genetic Inheritance Subsystems

### 3.1 Individual Values (IVs) & Held Item Inheritance
- **Default Inheritance**: 3 IVs chosen randomly from both parents; 3 IVs generated completely at random (0–31).
- **Destiny Knot**: When held by either parent, guarantees that **5 IVs** are chosen directly from the combined parental stat pool.
- **Power Items**: Holding a Power item guarantees that the holder's corresponding stat is passed directly to the child:
  - *Power Weight*: HP
  - *Power Bracer*: Attack
  - *Power Belt*: Defense
  - *Power Lens*: Special Attack
  - *Power Band*: Special Defense
  - *Power Anklet*: Speed

### 3.2 Nature Inheritance (Everstone)
- If a parent holds an **Everstone**, the child has a **100% guarantee** to inherit that parent's Nature.
- If both parents hold an Everstone, the child has a 50/50 chance between the two parental Natures.

### 3.3 Ability & Hidden Ability (HA) Propagation
- **Female Parent with HA**: 60% chance to pass Hidden Ability to the offspring; 40% chance for standard abilities.
- **Male Parent with HA**: Can only pass Hidden Ability when bred with **Ditto**.
- **Standard Abilities**: 80% chance to inherit the female parent's active ability slot.

### 3.4 Poké Ball Heritage & Master/Cherish Restrictions
- **Same Species Breeding**: 50% chance to inherit the mother's Poké Ball, 50% chance to inherit the father's Poké Ball.
- **Different Species**: Child always inherits the mother's Poké Ball.
- **Ditto Breeding**: Child inherits the non-Ditto parent's Poké Ball.
- **Anti-Dupe Safeguard**: Offspring bred from parents caught in **Master Balls** or **Cherish Balls** automatically revert to standard `cobblemon:poke_ball`.

### 3.5 Egg Moves & Mirror Herb Mechanics
- Children automatically inherit compatible moves present in the father's or mother's current moveset that qualify as Egg Moves.
- **Mirror Herb**: If a Pokémon with an empty move slot holds a Mirror Herb during Daycare placement, it can learn Egg Moves directly from any other Pokémon sharing the plot, even outside battle.

### 3.6 Masuda Method & International Geo-Tagging
UltraDayCare inspects the `country` tag on both parents (`DayCareCountry`):
- If the two parents possess different ISO country codes, the **Masuda Method** activates automatically.
- Multiplies the base Shiny probability by the configured Masuda multiplier (default: $6\times$ higher Shiny chance).

### 3.7 Regional Forms & Baby Incenses
- **Regional Forms**: Holding an Everstone ensures the offspring matches the regional form of the holding parent (e.g., Alolan Vulpix holding Everstone hatches Alolan Vulpix).
- **Baby Incenses**: Certain species require specific incense items held by a parent to produce baby forms instead of base forms:
  - *Full Incense*: Snorlax -> Munchlax
  - *Wave Incense*: Mantine -> Mantyke
  - *Sea Incense*: Marill/Azumarill -> Azurill
  - *Rose Incense*: Roselia/Roserade -> Budew
  - *Odd Incense*: Mr. Mime -> Mime Jr.
  - *Rock Incense*: Sudowoodo -> Bonsly
  - *Pure Incense*: Chimecho -> Chingling
  - *Lax Incense*: Wobbuffet -> Wynaut
  - *Luck Incense*: Chansey/Blissey -> Happiny

---

## 4. 3D Animated Egg Entities & Datapack Features

UltraDayCare bundles a custom datapack providing true 3D animated egg entities in Cobblemon:

### 4.1 Custom Egg Models & Cracking Stages
Eggs placed in the world or viewed in menus feature interactive visuals:
- **Crack Stages**: 4 distinct visual states (`egg_crack.json`): Uncracked (0%), Hairline Fracture (25%), Noticeable Cracks (50%), Imminent Hatch (75%+).
- **Percentage Hologram**: Real-time overhead text displays the exact hatch progress percentage.
- **Elemental Coloration**: Egg shell patterns tint dynamically based on the primary elemental type of the baby inside.

### 4.2 The `breedable` Custom Pokémon Property
UltraDayCare registers the `breedable` property type with Cobblemon:
- `breedable=true` / `breedable=false`
- Allows server administrators to issue tournament rewards, starter kits, or legendary event Pokémon that are explicitly barred from being used in daycare plots:
```mcfunction
/givepokemon AshKetchum charizard breedable=false
```

---

## 5. Egg Incubation & Step Engine (`/incubators`)

Rather than forcing players to dedicate half their active party slots to hatching eggs, UltraDayCare provides an intuitive GUI incubator menu accessed via `/incubators`.

### 5.1 Hatch Methods (Walk Steps vs Real-Time Ticks)
Configured via `EggHatchMethod`:
- `STEPS` (Default): Requires the player to walk, run, or ride mounts in the world. Monitored via `WalkBreedingMixin`.
- `TIME`: Eggs accumulate progress passively over real-world seconds or minutes.

### 5.2 Movement Listener & Flame Body Accelerators
When a player moves across blocks, their active incubator eggs receive step credits.
- If the player's active party contains a Pokémon with **Flame Body**, **Magma Armor**, or **Steam Engine**, all step increments are doubled ($2\times$ acceleration).

### 5.3 Incubator Slot Unlock Progression
- Players start with 1 permanent default incubator slot.
- Up to 6 additional slots can be unlocked via permission nodes:
  - `cobbledaycare.incubator.1`
  - `cobbledaycare.incubator.2`
  - `cobbledaycare.incubator.3` ... up to `6`.

---

## 6. Commands & Permissions Reference

The base command defaults to `/daycare` (configurable in `config.json`).

### 6.1 Player Commands

| Command | Permission Node | Default Level | Description |
|---|---|---|---|
| `/daycare` | `cobbledaycare.user` | Level 2 (All) | Opens the primary Daycare plots management interface. |
| `/incubators` | `cobbledaycare.user` | Level 2 (All) | Opens the Egg Incubators GUI to deposit or withdraw eggs. |
| `/egginfo` | `cobbledaycare.user` | Level 2 (All) | Displays step count, parents, and hatch percentage of held egg. |

### 6.2 Administrative Commands

| Command | Arguments | Permission Node | Default Level | Description |
|---|---|---|---|---|
| `/daycare reload` | *None* | `cobbledaycare.admin` | Level 2 (OP) | Hot-reloads configuration files, language strings, and mechanics. |
| `/daycare other` | `<player>` | `cobbledaycare.admin` | Level 2 (OP) | Opens and manages the daycare plots of another player. |
| `/daycare steps` | `<player> <amount>` | `cobbledaycare.admin` | Level 2 (OP) | Adds or modifies steps for all active eggs of a player. |
| `/hatch` | *None* | `cobbledaycare.admin` | Level 2 (OP) | Instantly hatches all eggs in the player's active incubators. |
| `/breed` | `<player>` | `cobbledaycare.admin` | Level 2 (OP) | Forces a player's active daycare plot to immediately generate an egg. |

---

## 7. Database Architecture & Data Models

### 7.1 Storage Engines (JSON, MongoDB, SQL)
Configured in `config.json` via `database.type`:
- `JSON`: Local files in `config/ultradaycare/data/users/<uuid>.json`.
- `MONGODB`: Centralized cluster storage for Velocity / BungeeCord networks.
- `SQL`: Relational database storage supporting SQLite and MySQL.

### 7.2 User Profile Schema (`User` & `Plot`)
- `playerUUID`: Player unique identifier.
- `plots`: Map of active breeding plots containing parent entity data, held items, compatibility ratings, and generated eggs.
- `incubators`: Array of `IncubatorSlot` objects tracking egg data, accumulated steps, and completion states.

---

## 8. Configuration Reference & Directory Layout

```
config/ultradaycare/
├── config.json                     # Core settings, breeding mode, plot limits
├── incubators.json                 # Incubator slot rules & step requirements
├── language.json                   # UI labels, button lores, and chat notifications
└── data/
    └── users/                      # Local JSON player profiles
```

### 8.1 `config.json` Specification

| Field | Type | Default | Description |
|---|---|---|---|
| `mode` | String | `"pokemon"` | Operational mode (`"pokemon"` for classic or `"pokemmo"` for sacrifice). |
| `maxPlots` | Integer | `3` | Maximum number of simultaneous breeding plots a player can unlock. |
| `stepMultiplier` | Double | `1.0` | Global multiplier applied to egg step requirements. |
| `masudaMultiplier` | Double | `6.0` | Shiny chance multiplier when breeding parents from different countries. |
| `database.type` | String | `"JSON"` | Storage backend (`JSON`, `MONGODB`, `SQL`). |

---

## 9. Complete Production Configuration Templates

### 9.1 Standard RPG Balanced `config.json`

```json
{
  "mode": "pokemon",
  "maxPlots": 3,
  "stepMultiplier": 1.0,
  "masudaMultiplier": 6.0,
  "requireEverstoneForNature": true,
  "allowDitto": true,
  "eggProductionInterval": "5m",
  "database": {
    "type": "JSON",
    "database": "ultradaycare",
    "url": "mongodb://localhost:27017",
    "user": "",
    "password": ""
  }
}
```

### 9.2 Hardcore PokeMMO Economy `config.json`

```json
{
  "mode": "pokemmo",
  "maxPlots": 2,
  "stepMultiplier": 1.25,
  "masudaMultiplier": 4.0,
  "consumeParentsOnEgg": true,
  "guaranteeBraceIv": true,
  "allowDitto": true,
  "eggProductionInterval": "10m",
  "database": {
    "type": "MONGODB",
    "database": "ultradaycare",
    "url": "mongodb://admin:password@mongo.internal:27017/ultradaycare?authSource=admin"
  }
}
```

### 9.3 Production `incubators.json`

```json
{
  "hatchMethod": "STEPS",
  "flameBodyMultiplier": 2.0,
  "slots": [
    {
      "id": 1,
      "permission": "cobbledaycare.user",
      "speedMultiplier": 1.0
    },
    {
      "id": 2,
      "permission": "cobbledaycare.incubator.2",
      "speedMultiplier": 1.0
    },
    {
      "id": 3,
      "permission": "cobbledaycare.incubator.3",
      "speedMultiplier": 1.25
    },
    {
      "id": 4,
      "permission": "cobbledaycare.incubator.vip",
      "speedMultiplier": 1.5
    }
  ]
}
```

---

## 10. Troubleshooting & Frequently Asked Questions

### Q: Why isn't a plot producing an egg?
**A:** Verify that the two deposited Pokémon share at least one common Egg Group (or one is Ditto), that their genders are opposite (Male + Female), and that neither has the `breedable=false` property tag. Also ensure that neither Pokémon belongs to the "Undiscovered" egg group (e.g. Legendary, Mythical, or Baby Pokémon).

### Q: Can players hatch eggs while riding a mount or flying?
**A:** Yes. `WalkBreedingMixin` registers player positional delta vectors regardless of whether the player is sprinting, riding a Pokémon mount, or flying, provided their chunk position changes.

### Q: Why didn't the child inherit its mother's Cherish Ball?
**A:** To preserve economy balance and prevent counterfeit event distribution, Cherish Balls and Master Balls never pass down genetically; the offspring automatically hatches in a standard red-and-white Poké Ball.
