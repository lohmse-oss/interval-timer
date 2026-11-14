# Intervall-Timer (Android)

Timer für Intervalltraining. Aufbau: **Training → Sätze → Intervalle → Segmente**.

Beispiel: 10 Intervalle mit je *Übung 45 s* (roter Hintergrund) und *Pause 15 s* (grüner Hintergrund).

- Beliebig viele Segmente pro Intervall (Übung/Pause), Sätze mit Satzpause, Vorbereitung und Cooldown
- Countdown-Piepser in den letzten Sekunden (einstellbar, Standard 5 s), langer Ton + weißer Blitz bei jedem Segmentwechsel
- Läuft per Foreground-Service auch bei gesperrtem Bildschirm; Pause/Stopp in der Benachrichtigung
- Trainings werden lokal gespeichert (Vorlagen anlegen, bearbeiten, kopieren, löschen)

## APK bauen

    ./gradlew assembleDebug   # -> app/build/outputs/apk/debug/app-debug.apk

Oder: GitHub → Actions → „Build APK“ → Artifact `IntervallTimer-debug-apk` herunterladen.
Zum Installieren auf dem Handy „Installation aus unbekannten Quellen“ erlauben.
