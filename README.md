# TNT Ring

**Light one TNT. Get a hundred.**

TNT Ring makes every TNT you light far more dangerous. The moment a TNT is lit, a ring of **100 more lit TNT** appears around it, 16 blocks out. All 101 go off at the same moment, and every one of them is **4× stronger than normal TNT**. The result is a single massive blast that leaves a huge circular crater.

## Features

- **Works with any TNT.** Flint and steel, redstone, fire or a dispenser all set off the ring.
- **Everything goes off together.** The ring copies the center TNT's fuse, so the whole ring explodes in one blast.
- **4× power on every TNT.** Each explosion reaches about four times as far as vanilla TNT.
- **No runaway chains.** Normal TNT blocks caught in the blast just explode. They don't spawn rings of their own, so a pile of TNT won't destroy your whole world.
- **Server-side only.** Install it on the server and players can join with plain vanilla Minecraft. It works in singleplayer too.

## Download

Get it from [Modrinth](https://modrinth.com/mod/tnt-ring) or from this repo's [Releases](https://github.com/p1e2r/tnt-ring/releases).

Available for **Minecraft 26.2 and 26.3**:

| Loader | File | Also needs |
|---|---|---|
| Fabric | `tnt-ring-…-fabric.jar` | Fabric API |
| Quilt | use the Fabric file | Fabric API (QFAPI doesn't work on 26.x) |
| Forge | `tnt-ring-…-forge.jar` | nothing else |
| NeoForge | `tnt-ring-…-neoforge.jar` | nothing else |
| Paper / Purpur | `tnt-ring-…-paper.jar` (goes in `plugins`) | nothing else |

All versions need Java 25.

## Performance

101 big explosions at once is heavy. Expect the game to freeze for about 2 seconds when a ring goes off. Setting off many rings in a row can overload a server.

## Changing the ring

The numbers are constants at the top of the source file for each loader (`RingCore.java` for Forge/NeoForge, `TntRing.java` for Fabric, `TntRingPaper.java` for Paper):

- `RING_COUNT` – how many TNT in the ring (default 100)
- `RING_RADIUS` – ring radius in blocks (default 16)
- `POWER` – explosion power of every TNT (default 16; vanilla TNT is 4)

## Source layout

```
fabric/     Fabric mod (Gradle + Fabric Loom). Also runs on Quilt.
common/     RingCore.java - the ring logic shared by Forge and NeoForge (plain Minecraft code only)
forge/      Forge entry point + mods.toml template
neoforge/   NeoForge entry point + neoforge.mods.toml template
paper/      Paper plugin + plugin.yml template
```

### Building

**Fabric:** `cd fabric && ./gradlew build`. The jar lands in `fabric/build/libs/`. Set `minecraft_version` and `fabric_api_version` in `fabric/gradle.properties` to build for 26.3.

**Forge / NeoForge / Paper:** these folders hold the source and metadata, without a Gradle setup yet. The released jars were compiled with Java 25 against each loader's server libraries:

- Forge and NeoForge: compile `common/` plus the loader folder, then fill in the `@…@` placeholders in the `.toml.in` template (loader version, Minecraft version, and the next Minecraft version as the upper bound) and save it as `mods.toml` / `neoforge.mods.toml`.
- Paper: compile `paper/` against `paper-api`, then fill in `@MC@` in `plugin.yml.in` and save it as `plugin.yml`.

## License

MIT
