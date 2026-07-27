# Straßen- und Asphalt-System

## Materialdefinitionen

Physikalische Eigenschaften und die logische Grundfarbe einer Straßenoberfläche
liegen zentral in `RoadSurfaceMaterials`. Eine `RoadSurfaceMaterial`-Definition
enthält:

- stabile Textur-ID,
- ARGB-Grundfarbe für Renderer und zukünftige Werkzeuge,
- Quelle der Kartenfarbe,
- Härte und Explosionswiderstand,
- Blocksound.

Asphaltblock, Asphaltstufe, Asphalttreppe und Asphalt-Höhenlagen beziehen ihre
Eigenschaften aus derselben Definition. Neue Materialien sollen ebenfalls dort
registriert werden, damit Farben und Blockeigenschaften nicht mehrfach gepflegt
werden.

Die JSON-Modelle verwenden dieselbe Textur-ID:
`terranexus:block/road_surface/asphalt`.

## Deterministische Texturvariation

Die Blockstates von normalem und abgenutztem Asphalt sowie deren Stufen listen
vier gleich gewichtete Modellvarianten mit 0°, 90°, 180° und 270° Y-Rotation.
Minecraft wählt eine Variante anhand des positionsabhängigen Blockmodell-Seeds.

Dadurch gilt:

- benachbarte Blöcke erhalten unterschiedliche Ausrichtungen,
- die Auswahl verursacht keine zusätzlichen Tick- oder Renderberechnungen,
- dieselbe Position behält ihre Rotation nach Neustart und erneutem Laden,
- es werden keine Texturen zur Laufzeit kopiert oder verändert.

Gerichtete Blöcke wie Linienasphalt, Regenlauf und Schächte werden absichtlich
nicht zufällig gedreht, da ihre Orientierung eine bauliche Bedeutung besitzt.
Treppen behalten ebenfalls ihre Platzierungsrichtung; eine zusätzliche
Blockrotation würde ihre Geometrie verdrehen.

## Vordefinierte Asphalt-Höhen

`FixedRoadHeightBlock` stellt direkt auswählbare Höhen in Schritten von 1/16
Block bereit. Jede Höhe besitzt eine stabile Block-ID und wird mit einem
einzigen Klick in der gewünschten Höhe platziert. Modell, Auswahlform und
Kollisionsform stimmen exakt überein.

Vorhandene Ressourcen werden wiederverwendet:

- `terranexus:asphalt_layer` bleibt die bestehende Höhe 1/16,
- `terranexus:asphalt_slab` bleibt die Vanilla-kompatible Höhe 8/16,
- nur 2–7/16 und 9–15/16 besitzen neue Registry-Einträge.

Die neuen IDs folgen dem Schema
`terranexus:asphalt_height_<höhe>_16`. Die Blockstates referenzieren die
jeweiligen gemeinsamen `asphalt_layer_<höhe>`-Modelle; 8/16 verwendet direkt
das vorhandene Slab-Modell. Textur- oder Modellgeometrie wird nicht dupliziert.

Typische Übergangsfolge:

```text
1/16 -> 2/16 -> 3/16 -> ... -> 8/16 -> ... -> 15/16 -> Vollblock
```

So lassen sich sowohl Übergänge unterhalb einer Stufe als auch zwischen Stufe
und Vollblock bauen. Für Fahrzeuge sollten aufeinanderfolgende Straßenfelder
jeweils nur um wenige Höhenstufen steigen. Die bestehende Asphaltstufe und
Asphalttreppe bleiben aus Kompatibilitätsgründen unverändert verfügbar.

Ein Asphaltblock kann am Steinschneider direkt in die gewünschte feste Höhe
umgewandelt werden. Die Ausgabemenge orientiert sich am Materialvolumen.

## Grid- und Geometrie-Rendering

Das Platzierungsraster verwendet zwei strikt getrennte Renderbuffer:

- flache Rasterstreifen: `debugQuads`,
- Rasterpunkte und aktive Hervorhebung: `debugFilledBox`.

Die Trennung ist wichtig, weil beide Renderer eine unterschiedliche
Vertex-Gruppierung erwarten. Werden Punktwürfel in den Quad-Stream geschrieben,
verbindet der Renderer unzusammengehörige Vertices zu großen Dreiecken.

Das Raster liegt mit einem kleinen Normalenoffset über der getroffenen Fläche.
Dieser Offset verhindert Z-Fighting, ohne die gespeicherte Position einer
Markierung zu beeinflussen.

## Erweiterung

Für ein neues Straßenmaterial:

1. Eine Definition in `RoadSurfaceMaterials` ergänzen.
2. Alle zugehörigen Blöcke mit `material.settings()` registrieren.
3. Modelle auf die Textur-ID der Definition ausrichten.
4. Nur bei rotationssymmetrischer Geometrie vier Blockstate-Varianten anlegen.
5. Gerichtete Texturen und Geometrien nicht zufällig rotieren.
6. Loot Table, Rezept, Tags, Itemdefinition und Übersetzungen ergänzen.

## Performance

Die Texturvariation wird vollständig vom bestehenden Blockmodell-System
gecacht. Alle Höhen verwenden dieselbe schlanke Blockklasse und jeweils eine
unveränderliche, einmal erstellte Kollisionsform. Es gibt keine BlockEntities,
dynamischen Meshes, Zufallsticks oder zusätzliche Netzwerkpakete.
