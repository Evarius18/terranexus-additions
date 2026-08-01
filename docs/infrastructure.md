# Infrastructure systems

The additions mod owns the garage door, traffic control and imported decorative blocks.

## Architecture and operation

- Vertically connected `sectional_garage_door` blocks animate together on right-click or a redstone signal. Four intermediate states lift the segmented panels and guide them below the ceiling; the final state keeps a ceiling collision shape instead of making the model disappear.
- Digital displays and traffic lights register persistently when placed and unregister when broken.
- Operators open the central UI from `traffic_control_pc` or `traffic_control_screen`. It edits names, intersections, groups, areas, schedules, display values/text, signal aspects and manual/automatic operation. Group changes can be applied to every member at once.
- `/tntraffic` remains available for automation and administration. Use `/tntraffic list` for registered positions and command completion below `/tntraffic device <x> <y> <z>` for groups, areas, display modes, values, text, signal programs and manual control.
- Signal programs use comma-separated phases such as `red:10,red_yellow:2,green:12,yellow:3` (seconds).
- Reusable display presets are saved with `/tntraffic template <id> <mode> <value> <text>` and applied with `/tntraffic device <position> apply_template <id>`.
- Configuration is written to `config/tnadditions/infrastructure.json`.
- `garageRemoteRange` configures remote-control range (default 64 blocks; accepted range 4-512).
- Sneak-use a garage key or remote on a door to bind it. A bound key operates the selected door directly; a remote also operates it from a distance. Controllers are bound to the player who paired them.
- Door owners manage additional users with `/tngarage grant <door> <player>` and `/tngarage revoke <door> <player>`. Redstone, buttons, levers, pressure plates and observer updates feed the same state transition logic.
- If TerraNexus Core is installed, door and console interactions use its public property protection through an optional reflection bridge. Additions remains usable without the core mod.

## Persistent data

- `TrafficControlState` stores only device identity, dimension/position, group/area/intersection, program/manual state, display parameters, reusable templates and schedule. Runtime block states are regenerated from it.
- `GarageAccessState` stores dimension/position, owner UUID and authorized UUIDs. No player inventory or property data is duplicated.
- Both states use Minecraft `PersistentState` codecs and therefore survive server restarts. New traffic fields are optional in the codec, keeping data from the initial implementation loadable.

## Extension points

- Add a new `TrafficDeviceType`, register it through `TrafficControlState.register`, and expose its mutable fields through the existing payload action instead of creating a second control system.
- Add display content by extending `TrafficDisplayMode` and providing one block model variant. Scheduled and grouped behavior is inherited automatically.
- Add signal programs through `TrafficProgram`/`TrafficPhase`; the traffic light tick reads the selected program centrally.
- Infrastructure access must go through `InfrastructureAccess`, so TerraNexus property rules continue to apply when the core mod is present.

## Imported assets

Integrated: the supplied rusted ladder plus Andesite, cobblestone, deepslate, diorite, coarse-dirt and stone boulders/plates/chunks. The ladder is climbable; decorations have compact rotated collision shapes.

Also integrated: traffic light, digital traffic display, sectional garage door, traffic-control PC/screen, garage key and remote. Every registered block has a blockstate, item definition/model, loot table, translations and a creative-inventory entry. Functional infrastructure blocks have recipes; natural stone decorations intentionally do not manufacture loose natural formations through crafting.

Not integrated because the supplied source is unusable or incomplete:

- `sprossen_leicht_rost.json`, `sprossen_stark_rost.json`, `Sprossen_verzinkt.json`: empty files (0 bytes)
- `gravel.png`: no corresponding model was supplied

The four paving-stone source models were not duplicated as separate blocks because this mod already contains the corresponding road-surface blocks and textures (`pflastersteine` and `kopfsteinpflaster`).

## Verification

- `gradlew build`: successful, including client compilation and 14 road-marking core checks.
- Dedicated-server initialization: successful through mod registration; the isolated smoke test then stopped normally at the unaccepted test EULA.
- Client smoke test: resource loading reached the main client without missing-model, missing-texture or registry messages in `run/logs/latest.log`.
- Registry audit: all block IDs registered by `ModBlocks` have blockstates, item definitions and loot tables.
