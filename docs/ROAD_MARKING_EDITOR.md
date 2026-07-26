# Dynamisches Straßenmarkierungs-System

Diese Dokumentation beschreibt Bedienung, Architektur, Datenhaltung und
Erweiterung des Straßenmarkierungs-Editors. Das System speichert ausschließlich
Parameter und erzeugt Kurven und Rendergeometrie bei Bedarf.

## Bedienung

### Markierung erstellen

1. Im Kreativtab **TerraNexus: Straßenbau** den
   **Straßenmarkierungs-Editor** auswählen.
2. Einen Block anvisieren. Auf seiner Oberfläche erscheinen Raster,
   Einrastpunkte, aktiver Punkt und die Live-Vorschau.
3. Mit Rechtsklick Start-, Zwischen- und Endpunkte setzen.
4. Mit `Enter` speichern. `Backspace` nimmt den letzten Punkt zurück.
5. `M` öffnet bewusst das Einstellungsmenü. Maus- und Shift-Kombinationen
   öffnen das Menü nicht und beenden keine laufende Markierung.

### Bestehende Markierung bearbeiten

1. Mit `B` in den Modus **Bearbeiten** wechseln.
2. Eine bestehende Markierung mit Rechtsklick auswählen.
3. Über `M` **Bewegen**, **Einfügen** oder **Hinzufügen** wählen.
4. Den Zielpunkt mit Rechtsklick festlegen. Die Vorschau wird schon vor dem
   Klick aktualisiert.
5. `Entf` entfernt den ausgewählten Kontrollpunkt; bei nur zwei Punkten ist
   dies gesperrt. `X` löscht die komplette ausgewählte Markierung.
6. Änderungen mit `Enter` speichern.

### Präzisionshilfen

- Oberflächenraster: 0,25, 0,5 oder 1 Block; **Aus** deaktiviert das Einrasten.
- Das Raster richtet sich an Ober-, Unter- und Seitenflächen des Zielblocks aus.
- Achsensperre: Welt-X, Welt-Z oder Richtung des ersten Segments.
- Winkelraster: 15°, 30°, 45° oder 90°.
- Numerische horizontale Länge und numerischer Winkel.
- Seitenoffset: zentriert, leicht/ganz links oder rechts und frei numerisch.
- Kurvenradius für automatisch gerundete Übergänge mit wenigen Punkten.
- `L` aktiviert den Fahrspur-Trennstreifen mit 3/6-Block-Strichmuster.

## Architektur

| Bereich | Verantwortung |
| --- | --- |
| `marking` | Unveränderliches Parametermodell, Stil und Typ-Registry |
| `marking.spline` | Kurvenvorbereitung, zentripetale Catmull-Rom-Spline, bogenlängenbasiertes Sampling |
| `marking.geometry` | Austauschbare Geometriegeneratoren, Quads und Revisionscache |
| `marking.storage` | Dimensionsbezogener `PersistentState`, ohne Rendersegmente |
| `marking.network` | Validierte Upsert-/Delete-Nachrichten und Synchronisation |
| `marking.support` | Serverseitige Prüfung des tragenden Untergrunds |
| `client.marking` | Editorzustand, Eingaben, Bereichsindex, Grid, Vorschau und Rendering |

### Datenfluss

```text
Rechtsklick auf Oberfläche
  -> Oberflächenraster und Präzisionsregeln
  -> EditorSession / Kontrollpunkte
  -> Live-Markierungsparameter
  -> Spline-Sampling
  -> MarkingType-Geometriegenerator
  -> Geometriecache
  -> Frustum-/Distanzprüfung und gemeinsamer Renderbuffer

Enter
  -> validiertes Netzwerk-Upsert
  -> dimensionsbezogener PersistentState
  -> Broadcast an Clients
  -> Chunk-/Bereichsindex und Cache
```

### Zentrale Klassen

- `RoadMarking`: ID, Typ, Kontrollpunkte, Stil, Revision und Aktivstatus.
- `MarkingStyle`: alle visuellen und verhaltensbezogenen Parameter.
- `MarkingType`: Erweiterungsschnittstelle für eine Geometriestrategie.
- `MarkingTypes`: offene Registry der eingebauten Strategien.
- `CatmullRomSpline`: glatte Kurve durch sämtliche Kontrollpunkte.
- `CurvePointPreprocessor`: optionale Eckrundung.
- `MarkingGeometryCache`: Geometrie nur bei neuer Revision neu berechnen.
- `RoadMarkingState`: Speicherung pro Dimension.
- `RoadMarkingEditorSession`: Entwurf und Bearbeitungszustand.
- `RoadMarkingRenderer`: Beleuchtung, Grid, Vorschau und Batch-Ausgabe.
- `RoadMarkingSupportManager`: Prüfung, Deaktivierung, Anpassung oder Löschung.

## Markierungstypen

| ID | Verwendung |
| --- | --- |
| `solid` | Durchgezogene Linie |
| `dashed` | Gestrichelte Linie |
| `double` | Doppelte durchgezogene Linie |
| `double_dashed` | Doppelte gestrichelte Linie |
| `guide_line` | Leitlinie |
| `lane_divider` | Wiederholter Fahrspur-Trennstreifen |
| `stop_line` | Haltelinie |
| `hatched_area` | Sperrfläche |
| `parking` | Parkmarkierung |
| `crosswalk` | Fußgängerüberweg |
| `bike_lane` | Fahrradmarkierung |
| `bus_lane` | Busspur |
| `direction_arrow` | Richtungspfeil |
| `turn_arrow` | Abbiegepfeil |

Alle Typen verwenden dieselbe Spline-, Speicher-, Netzwerk- und
Renderinfrastruktur. Unterschiede liegen nur in der Geometriestrategie.

## Parameter und Speicherung

Eine Markierung speichert:

- stabile UUID,
- Typ-ID,
- Kontrollpunkte als Weltkoordinaten,
- Stil,
- monoton steigende Revision,
- `enabled` für vorübergehend deaktivierte Markierungen.

Der Stil enthält Breite, ARGB-Farbe, Material, Deckkraft, Abnutzung,
Verschmutzung, Strich- und Lückenlänge, Höhen- und Seitenoffset,
Kurvenradius, Renderreihenfolge und Kollisionswunsch.

Die Geometrie wird **nicht** gespeichert. Beim Laden erzeugt der Client sie
aus den Parametern. Alte Daten bleiben kompatibel: Fehlende Stilfelder werden
neutral initialisiert (`lateral_offset = 0`, `corner_radius = 0`) und eine
Markierung ohne `enabled`-Feld gilt als aktiviert.

## Grid und Live-Vorschau

Das Grid ist rein clientseitig und verändert keine Weltblöcke. Es wird nur
angezeigt, wenn der Editor gehalten, kein Menü geöffnet und eine Blockfläche
anvisiert wird. Basisvektoren und Oberflächennormale werden aus der getroffenen
Blockseite bestimmt. Dadurch liegen Rasterlinien und Punkte auch auf
vertikalen Flächen korrekt.

Der aktive Rasterpunkt durchläuft anschließend Achsen-, Winkel-, Längen- und
Grid-Snapping. `previewWithPoint` baut daraus eine nicht persistierte Vorschau
für Hinzufügen, Einfügen oder Verschieben. Erst ein Rechtsklick verändert den
Entwurf; erst `Enter` schreibt in den Weltzustand.

## Rendering und Beleuchtung

- Markierungen verwenden keine Emission und geben kein Licht ab.
- Weil der gebündelte Quad-Layer selbst unbeleuchtet ist, wird je Quad das
  Vanilla-Himmel- und Blocklicht an dessen Mittelpunkt gelesen.
- Die Himmelshelligkeit berücksichtigt `ambientDarkness` und damit den
  Tag-/Nachtzyklus. Blocklicht (zum Beispiel Laternen oder Fahrzeugscheinwerfer)
  beleuchtet die Farbe ebenfalls.
- `reflective` ist lediglich ein geringer Albedo-Faktor und keine
  Eigenbeleuchtung; `temporary` ist etwas matter.
- Deckkraft, Abnutzung und Schmutz werden nach der Lichtberechnung angewendet.
- Chunk-/Bereichsindex, Renderdistanz und Frustum-Culling reduzieren die
  Kandidaten. Sichtbare Quads teilen sich einen Renderbuffer.

## Untergrund und physikalisches Verhalten

`RoadMarkingSupportManager` prüft die gesampelte Kurve periodisch gegen die
tatsächlichen Kollisionsflächen. Nicht geladene Chunks werden übersprungen und
nicht zwangsweise geladen. Fehlt die tragende Fläche, gilt das konfigurierte
Verhalten:

- `REMOVE`: Markierung löschen und die Löschung synchronisieren.
- `DISABLE`: Parameter behalten, aber unsichtbar schalten; bei erneut gültiger
  Fläche automatisch wieder aktivieren.
- `ADAPT`: Kontrollpunkte innerhalb der erlaubten Vertikaldistanz auf die
  nächste Kollisionsoberfläche verschieben; ohne Ziel wird gelöscht.

Die Prüfung ist für Straßenmarkierungen auf horizontalen Trägerflächen
ausgelegt. Das Seitenflächen-Grid dient der präzisen Vorschau, ändert aber
nicht dieses physikalische Straßenmodell.

## Konfiguration

Datei: `config/tnadditions/strassenmarkierungen/support.json`

| Option | Standard | Wirkung |
| --- | ---: | --- |
| `enabled` | `true` | Schaltet die Untergrundprüfung vollständig ein/aus |
| `unsupportedBehavior` | `"REMOVE"` | `REMOVE`, `DISABLE` oder `ADAPT` |
| `checkIntervalTicks` | `20` | Prüfintervall; gültig 5 bis 1200 Ticks |
| `sampleSpacing` | `0.5` | Abstand der Stützproben; gültig 0,1 bis 4 Blöcke |
| `maxAdaptDistance` | `2.0` | Maximale vertikale Anpassungsdistanz; 0,25 bis 16 Blöcke |

Kleinere Intervalle und Abstände reagieren schneller beziehungsweise prüfen
feiner, kosten auf sehr großen Karten aber mehr Serverzeit. Die Datei wird
beim ersten Start mit sicheren Standardwerten erstellt.

Die Editorhilfen Grid, Achsensperre, Winkelraster sowie exakte Länge und Winkel
sind aktuell sitzungsbezogene Clientoptionen und werden nicht in einer
Markierung gespeichert. Ihre Ergebnisse sind normale exakte Kontrollpunkte.

## Neuen Markierungstyp hinzufügen

1. Eine Klasse erstellen, die `MarkingType` implementiert.
2. Eine stabile, namespacete ID wählen.
3. In `generate(samples, style)` ausschließlich Geometrie aus den bereits
   gesampelten Kurvenpunkten erzeugen.
4. Die Instanz bei der Initialisierung über `MarkingTypes.register(...)`
   registrieren.
5. Einen übersetzten Namen und optional einen Editor-Preset ergänzen.
6. Core-Test um die neue Strategie erweitern beziehungsweise sicherstellen,
   dass sie nichtleere, endliche Geometrie erzeugt.

Spline, Speicherung, Netzwerk, Editor und Renderer benötigen dafür keine
Sonderfälle.

### Best Practices

- Keine einzelnen Segmente in NBT speichern.
- Rechenintensive Ergebnisse nach ID und Revision cachen.
- Unveränderliche Parameterobjekte verwenden und bei Änderungen die Revision
  erhöhen.
- Keine Chunks für Render- oder Trägerprüfungen erzwingen.
- Typ-spezifische Logik im `MarkingType`, nicht im Editor, unterbringen.
- Neue persistente Felder immer mit rückwärtskompatiblem Standard laden.
- Clientvorschau und serverautorisierte Speicherung strikt trennen.

## Qualitätssicherung

`gradlew testRoadMarkingCore` prüft unter anderem Spline-Endpunkte,
Bogenlängen, Tangenten, Kreisnaht, Rundung, alle registrierten Typen,
Seitenoffset und NBT-Kompatibilität. `gradlew build` führt diese Prüfung über
`check` ebenfalls aus. Zusätzlich sollte ein Clienttest Tag/Nacht,
Blockbeleuchtung, alle sechs Blockseiten des Grids und die drei
Untergrundmodi abdecken.
