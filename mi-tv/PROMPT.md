# Prompt para Claude Code en tu PC

Abre Claude Code **en tu PC** (la que está en el mismo Wi-Fi que la TV), dentro de la carpeta de este repo
(`clean-you-tv-2`, rama `claude/admiring-albattani-w7z3jp`), y pega todo lo que está debajo de la línea.
Cambia `<IP DE LA TV>` por la de tu TV (Ajustes > Red, en la TV). En la TV, la depuración USB tiene que estar
encendida.

---

Mi TV es una TCL 55Q77K con Google TV 14 (firmware V8-0012T02-LF1V333). Hace un tiempo le instalé CleanYourTV
y ya no tengo la lista de paquetes que se usó. La IP de la TV es `<IP DE LA TV>`. Háblame en español.

Estás en mi fork de CleanYourTV, rama `claude/admiring-albattani-w7z3jp`. Ya trae programado lo que quiero:
íconos cuadrados flotantes (`square_tiles`), reproductor de música arriba con carátula (`now_playing`) y un
botón para cambiar el fondo (`wallpaper_button`). Todo lo de mi TV está en `mi-tv/`: lee `mi-tv/LEEME.md` y
`AGENTS.md` antes de empezar, y sigue sus reglas de seguridad (deshabilitar, nunca desinstalar; respaldo antes
de cambiar nada; preguntarme antes de tocar algo que uso). No reescribas lo que ya existe en `tools/` y
`launcher/`: úsalo.

1. **Preparar la PC.** Revisa si tengo Python 3, Git, JDK 17 y el Android SDK (platform 34 y build-tools
   34.0.0). Si falta algo, dime exactamente qué instalar o instálalo tú si puedes. Crea el venv de `tools/`
   (`tools/requirements.txt`).
2. **Conectar y respaldar.** Conéctate por ADB (puerto 5555, o depuración inalámbrica si 5555 está cerrado,
   como dice `PROMPT.md`). Guarda en `backup/` la lista de paquetes, los deshabilitados (`pm list packages -d`),
   `getprop` y qué app es Home. Toma una captura de cómo se ve ahora: `tools/tv.py --screenshot shots/antes.png`.
3. **Diagnosticar el botón de Google y Alexa.** Revisa lo que dice `mi-tv/LEEME.md`: si `com.google.android.katniss`,
   `com.google.android.tv.remote.service`, `com.google.android.apps.tv.launcherx` o `com.tcl.smartalexa` están
   deshabilitados, y el permiso del micrófono. Dime lo que encontraste y qué propones antes de cambiar nada.
   Después de cada arreglo pídeme que presione el botón real del control (una tecla inyectada no sirve de prueba).
4. **Instalar el launcher de este repo** con
   `NOW_PLAYING=1 HOME_KEY_SERVICE=1 INPUT_KEY_SERVICE=1 TV_HOST=<IP> launcher/build-and-install.sh`
   (en Windows, `launcher\build-and-install.ps1 -TvHost <IP> -NowPlaying -HomeKeyService -InputKeyService`).
   Si para el botón de Google hay que dejar launcherx encendido, usa la variante de `mi-tv/LEEME.md`
   (`stock_home_redirect`, `key_filter` false y solo `HOME_KEY_SERVICE=1`). Si la compilación falla, arregla el
   código y dime qué cambiaste.
5. **Poner mi tema.** Busca en la TV los nombres de paquete de Magis TV, Spotify, YouTube y Seekee, ponlos en ese
   orden en `dock_apps` de `mi-tv/config.json` y corre `TV_HOST=<IP> tools/theme.sh mi-tv`.
6. **Revisar y mejorar.** Toma capturas (`tools/tv.py --screenshot shots/despues.png DPAD_RIGHT`) y mira que los
   íconos se vean cuadrados y flotando. Pídeme que ponga música en Spotify para probar el reproductor de arriba
   (carátula y botones ⏮ ⏯ ⏭), y prueba el botón de fondo (OK = siguiente fondo). Ajusta el tema conmigo hasta que
   se parezca a mi foto de referencia: fondo limpio de paisaje, una fila de íconos pequeños abajo y el reproductor
   arriba al lado de los botones redondos. Cambia una cosa a la vez y enséñame la captura.
7. **Al final**, haz commit de los cambios en esta rama y súbelos (nunca `backup/`, `shots/` ni la llave ADB), y
   apaga la depuración como dice `AGENTS.md`, salvo que yo te diga que la deje encendida.
