# CLAUDE.md - Easier Sleeping

## Projekt-Übersicht

**Easier Sleeping** ist ein NeoForge Minecraft Mod für Minecraft 26.1 - 26.3 (Branch `develop_26.1`; 1.21.11 auf `develop_1.21.11`, ältere Versionen auf `develop_1.21.1` usw.).
- **Mod ID**: `easier_sleeping`
- **Package**: `de.geheimagentnr1.easier_sleeping`
- **Java Version**: 25 (Gradle-Wrapper 9.2.1, Lombok 1.18.48)
- **NeoForge Version**: kompiliert gegen `26.1.0.19-beta` (niedrigste Zielversion), `neoforge_version_range=[26.1,)`
- **Minecraft-Range**: `[26.1,27)` - ein Jar für 26.1, 26.1.1, 26.1.2, 26.2, 26.3 (getestet 2026-09-30; `minecraft_versions` in `gradle.properties` führt nur getestete Versionen)

Die Nacht wird über die World-Clock-API übersprungen (`EventHooks.onSleepFinished` + `ClockAdjustment.Marker( ClockTimeMarkers.WAKE_UP_FROM_SLEEP )`, wie Vanilla `ServerLevel.tick`), siehe `../Docs/migrations/1.21.11-to-26.1.md`.

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
├── elements/commands/
│   ├── ModArgumentTypesRegisterFactory.java  # Registriert DimensionListTypeArgument
│   ├── ModCommandsRegisterFactory.java
│   └── sleep/                  # /sleep-Command (OP-Level 2) + DimensionListType-Argument
└── sleeping/
    └── SleepingManager.java    # Schlaf-Logik (Tick alle 20 Ticks)
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

## Testing

### Java-Versionen

Verschiedene Java-Versionen sind unter `C:\Program Files\Eclipse Adoptium` installiert. Für einen Gradle-Build muss die passende Java-Version gewählt werden:

```powershell
# Java 25 für MC 26.x (Branch develop_26.1)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
./gradlew build

# Kompatibilität gegen weitere 26.x-Versionen prüfen (baut kein zusätzliches Jar)
./gradlew compileJava --rerun-tasks -Pminecraft_version=26.3 -Pneoforge_version=26.3.0.36-beta
```

### Unit Tests (JUnit 5)

Für reine Logik-Tests ohne Minecraft-Abhängigkeiten:

```bash
./gradlew test
```

Tests liegen unter `src/test/java/`. Ergebnisse: `build/reports/tests/test/index.html`

### NeoForge GameTest Framework

Für Integration Tests in einer echten Minecraft-Umgebung:

```bash
./gradlew runGameTestServer
```

Auf `develop_1.21.11` und `develop_26.1` gibt es keine GameTests: das Annotations-Framework (`@GameTest`, `@GameTestHolder`) wurde in 1.21.11 entfernt, der triviale Smoke-Test wurde ersatzlos gelöscht (siehe `../Docs/migrations/1.21.10-to-1.21.11.md`).

### CI/CD (GitHub Actions)

Der Workflow `.github/workflows/build-and-test.yml` führt automatisch aus:
1. **Build**: Kompiliert den Mod
2. **Unit Tests**: Führt JUnit Tests aus
3. **GameTests**: Startet GameTestServer (optional)

### Was kann automatisiert getestet werden?

| Aspekt | Automatisiert? | Methode |
|--------|----------------|---------|
| Utility-Klassen | ✅ | JUnit |
| Config-Parsing | ✅ | JUnit |
| Commands | ✅ | GameTest |
| Block/Item-Verhalten | ✅ | GameTest |
| Multi-MC-Version | ⚠️ Pro Branch | CI Matrix |

## Referenzen

- [NeoForge Migration Primer](https://docs.neoforged.net/primer/docs/) — Dokumentiert API-Aenderungen zwischen Minecraft/NeoForge-Versionen; nuetzlich fuer die Pruefung von Breaking Changes beim Upgrade auf neue Versionen

---

## Wissensdatenbank

Versionsübergreifende Migrations- und Entwicklungs-Erkenntnisse (Breaking Changes, Fixes, Testumgebungs-Patterns) werden zentral in [`../Docs/`](../Docs/) gepflegt. Bei neuen relevanten Erkenntnissen dort ergänzen, nicht nur hier.
