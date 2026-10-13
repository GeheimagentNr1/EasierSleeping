# CLAUDE.md - Easier Sleeping

Nur ein einstellbarer Prozentsatz der Spieler muss schlafen, um die Nacht zu überspringen; dazu Chat-Meldungen beim Hinlegen, Aufstehen und am Morgen. Mod-ID `easier_sleeping`, Package `de.geheimagentnr1.easier_sleeping`. Client+Server (`displayTest=MATCH_VERSION`, keine Dist-Beschränkung): die Logik läuft rein serverseitig, der Mod registriert aber einen eigenen Befehls-Argumenttyp (`DimensionListTypeArgument`), daher gehört er auch auf den Client.

Diese Datei ist auf allen `develop_*`-Branches identisch; Quelle: `../Docs/mod-claude/EasierSleeping.md` (Änderungen dort, dann `Docs/tools/sync_mod_claude_md.sh`). Versionsstand: [`../Docs/VERSIONEN.md`](../Docs/VERSIONEN.md).

## Versionen

- Forge: `develop_1.14.4` bis `develop_1.21`, dazu `develop_1.21.1_forge`. `develop_1.21.3` ist ein alter, nur lokaler Forge-Stand, kein NeoForge-Port.
- NeoForge: `develop_1.21.1` (1.21.1 - 1.21.10), `develop_1.21.11`, `develop_26.1` (ein Jar 26.1 - 26.3).
- Jar-Grenzen: 1.21.11 (`Identifier`, GameRules, `RespawnConfig`, Rechtesystem), 26.1 (World Clocks). Ranges und Tags: [`../Docs/VERSIONEN.md`](../Docs/VERSIONEN.md).

## Abhängigkeiten

- Keine Mod-Abhängigkeiten, keine gebündelten Bibliotheken.
- Forge ab 1.20: MinecraftForgeAPI per `jarJar` (siehe [`../Docs/forge/README.md`](../Docs/forge/README.md)).

## Architektur

- `EasierSleeping` - Hauptklasse; registriert die Server-Config, die Argumenttypen, `ModCommandsRegisterFactory` und `SleepingManager`.
- `config/ServerConfig` - Server-Config inkl. Prüfung/Korrektur von Dimensionen und Bett-Blacklist beim Serverstart (unbekannte Einträge werden entfernt und zurückgeschrieben); `DimensionListType` (`SLEEP_ACTIVE`/`SLEEP_INACTIVE`).
- `elements/commands/` - `ModArgumentTypesRegisterFactory` (registriert `DimensionListTypeArgument` über `ArgumentTypeInfos`), `sleep/SleepCommand` (`/sleep`), `MessageActionType`, `sleep/dimension_list_type/` (Argument + Parser).
- `sleeping/SleepingManager` - NeoForge: `ServerTickEvent.Post`, alle 20 Ticks pro Dimension schlafende Spieler zählen (Zuschauer zählen nicht mit, Betten aus der Blacklist werden ignoriert), bei Erreichen des Prozentsatzes Zeit vorspulen, Respawnpunkt setzen, Spieler wecken, Wetter zurücksetzen, optional `TIME_SINCE_REST` aller Spieler zurücksetzen.
  - bis 1.21.10: `EventHooks.onSleepFinished( level, newTime, currentTime )` + `level.setDayTime(..)`.
  - ab 26.1: World Clocks - `EventHooks.onSleepFinished( level, new ClockAdjustment.Marker( ClockTimeMarkers.WAKE_UP_FROM_SLEEP ) )` + `adjustment.apply( level.clockManager(), defaultClock )`, `resetWeatherCycle()`.
- Forge-Branches: zusätzlich `sleeping/SleepingWorker` (`WorldWorkerManager.IWorker`) statt Tick-Event.

## Funktionen, Befehle, Konfiguration

- `/sleep ...` - Rechtestufe 2 (ab 1.21.11 `Commands.LEVEL_GAMEMASTERS`): `sleep_percent [0-100]`, `messages wake|sleep|morning [add|remove <message>]`, `all_players_rest [bool]`, `dimension [add|remove <dimension>]`, `dimension list_type [<type> <invert_list>]`.
- Server-Config `world/serverconfig/easier_sleeping-server.toml`: `sleep_percent` (Standard 50), `sleep_messages`/`wake_messages` (Platzhalter `%player%`), `morning_messages`, `all_players_rest` (false), `dimensions` (Standard Overworld) + `dimension_list_type` (`SLEEP_ACTIVE`), `block_blacklist` (Bett-Blöcke, die nicht zählen).
- Keine Welt-Daten.

## Fallstricke

- **Config speichern:** alle Setter in `ServerConfig` rufen nach `set(..)` `save()` auf - ohne das gingen per `/sleep` gesetzte Werte beim Neustart verloren (gefixt in 4.0.2 auf allen NeoForge-Branches). Bei neuen Settern nicht vergessen.
- 1.21.10 → 1.21.11: `ResourceKey.location()` → `identifier()`, GameRules typisiert (`RULE_DAYLIGHT` → `ADVANCE_TIME`, `RULE_WEATHER_CYCLE` → `ADVANCE_WEATHER`), `setRespawnPosition` mit `RespawnConfig`, `sendSystemMessage` nur noch auf `ServerPlayer`. Details: [`../Docs/migrations/1.21.10-to-1.21.11.md`](../Docs/migrations/1.21.10-to-1.21.11.md).
- 1.21.11 → 26.1: `getDayTime`/`setDayTime` entfernt (World Clocks), `displayClientMessage(c, false)` → `sendSystemMessage(c)`. Details: [`../Docs/migrations/1.21.11-to-26.1.md`](../Docs/migrations/1.21.11-to-26.1.md).
- NeoForge 21.11+: `WorldWorkerManager.IWorker` entfernt - deshalb `ServerTickEvent.Post` statt Worker.

## Testen

- Ingame (auch per mcpfabric, siehe [`../Docs/testing/docker-vs-native.md`](../Docs/testing/docker-vs-native.md), Abschnitt mcpfabric-Praxis): `time set night` + `weather rain` per RCON, Bett benutzen (`place_block` auf das Bett), Zeit/Wetter und Chat-Meldungen prüfen, Respawnpunkt per `data get entity <Spieler> respawn`.
- Config-Speichern per RCON: Wert mit `/sleep` setzen, `easier_sleeping-server.toml` prüfen, Neustart, Wert abfragen.
- GameTests gibt es ab `develop_1.21.11` nicht mehr, nur JUnit. Allgemein: [`../Docs/testing/README.md`](../Docs/testing/README.md).

## Links

- Wiki: https://github.com/GeheimagentNr1/EasierSleeping/wiki
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/easier-sleeping (Projekt-ID 388364)
- Modrinth: https://modrinth.com/mod/yTTpfxdS
- [`../Docs/migrations/BREAKING_CHANGES.md`](../Docs/migrations/BREAKING_CHANGES.md), [`../MOD_KOMPATIBILITAET.md`](../MOD_KOMPATIBILITAET.md) (Config-`save()`)
