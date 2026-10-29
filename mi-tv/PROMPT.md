# Prompt para Claude Code en tu PC

Abre Claude Code **en tu PC** (la que está en el mismo Wi-Fi que la TV), dentro de la carpeta de este repo
(`clean-you-tv-2`, rama `claude/admiring-albattani-w7z3jp`), y pega todo lo que está debajo de la línea.
Cambia la IP por la de tu TV (Ajustes > Red, en la TV). En la TV, la depuración USB tiene que estar encendida.

---

Mi TV es una TCL 55Q77K con Google TV 14 (firmware V8-0012T02-LF1V333). Hace un tiempo le instalé CleanYourTV
y ya no tengo la lista de paquetes que se usó. La IP de la TV es `<IP DE LA TV>`.

Trabaja en la TV real y sigue las reglas de seguridad de `AGENTS.md` y `PROMPT.md` (deshabilitar, nunca
desinstalar; respaldo antes de cambiar nada; preguntarme antes de tocar algo que uso). Háblame en español.
Todo lo de mi TV está en `mi-tv/`: lee `mi-tv/LEEME.md` antes de empezar.

1. **Conectar y respaldar.** Conéctate por ADB (puerto 5555 o depuración inalámbrica, como dice `PROMPT.md`).
   Guarda en `backup/` la lista de paquetes, los paquetes deshabilitados (`pm list packages -d`), `getprop` y qué
   app es Home. Toma una captura de cómo se ve ahora (`tools/tv.py --screenshot shots/antes.png`).
2. **Diagnosticar el botón de Google y Alexa.** Revisa lo que dice `mi-tv/LEEME.md`: si `katniss`,
   `com.google.android.tv.remote.service`, `launcherx` o `com.tcl.smartalexa` están deshabilitados, y el
   permiso del micrófono. Dime lo que encontraste y qué propones antes de cambiar nada. Después de cada
   arreglo pídeme que presione el botón real del control (una tecla inyectada no sirve de prueba).
3. **Instalar el launcher nuevo de este repo** con
   `NOW_PLAYING=1 HOME_KEY_SERVICE=1 INPUT_KEY_SERVICE=1 TV_HOST=<IP> launcher/build-and-install.sh`
   (o el `.ps1` en Windows con `-NowPlaying -HomeKeyService -InputKeyService`). Si para el botón de Google hay
   que dejar launcherx encendido, usa la variante de `mi-tv/LEEME.md` (`stock_home_redirect`, `key_filter`
   false, solo `HOME_KEY_SERVICE=1`). Si la compilación falla, arregla el código y dime qué cambiaste.
4. **Poner mi tema.** Antes de mandarlo, busca en la TV los nombres de paquete de mis apps del dock: Magis TV,
   Spotify, YouTube y Seekee, en ese orden. Ponlos en `dock_apps` de `mi-tv/config.json` y luego corre
   `TV_HOST=<IP> tools/theme.sh mi-tv`.
5. **Revisar y mejorar.** Toma capturas (`tools/tv.py --screenshot shots/despues.png DPAD_RIGHT`), mira que los
   íconos se vean cuadrados y flotando, y pon música en Spotify para probar el reproductor de arriba (la
   carátula y los botones ⏮ ⏯ ⏭). Prueba el botón de fondo (OK = siguiente fondo). Ajusta el tema conmigo hasta
   que se parezca a mi foto de referencia: fondo limpio de paisaje, una fila de íconos chiquitos abajo y el
   reproductor arriba al lado de los botones redondos.
6. **Al final**, haz commit de los cambios en esta rama (nunca `backup/`, `shots/` ni la llave ADB) y apaga la
   depuración como dice `AGENTS.md`, salvo que yo te diga que la deje.
