# AGENTS.md - Easier Sleeping

## Projekt-Übersicht

**Easier Sleeping** ist ein NeoForge Minecraft Mod für Minecraft 1.21.1.
- **Mod ID**: `easier_sleeping`
- **Package**: `de.geheimagentnr1.easier_sleeping`
- **Java Version**: 21
- **NeoForge Version**: 21.1.x

Nur ein Prozentsatz der Spieler muss schlafen, um die Nacht zu überspringen.

## Abhängigkeiten

Keine Mod-Abhängigkeiten - eigenständiger Mod.

## Projektstruktur

```
src/main/java/de/geheimagentnr1/easier_sleeping/
├── EasierSleeping.java         # Haupt-Mod-Klasse
├── config/
│   ├── DimensionListType.java  # Enum für Dimensions-Listen
│   └── ServerConfig.java       # Server-Konfiguration
└── sleeping/
    └── SleepingManager.java    # Schlaf-Logik
```

## Besonderheiten

- **Server-Konfiguration**: Konfigurierbare Schlaf-Prozentsätze
- **Dimensions-Filter**: Konfigurierbare Dimensions-Listen

## Code-Stil

- **Annotations**: `@NotNull` aus `org.jetbrains.annotations`
- **Lombok**: Projekt nutzt Lombok
- **Formatierung**: Leerzeichen nach `(` und vor `)` bei Methodenaufrufen

## Build & Test

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

## Deployment

- **CurseForge**: `./gradlew curseforge`
- **Modrinth**: `./gradlew modrinth`
