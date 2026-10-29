# Mi TV: TCL 55Q77K (Google TV 14)

Esta carpeta tiene todo lo de **tu** TV: el diseño nuevo (estilo "Snoopy"), cómo instalarlo y cómo arreglar
Alexa y el botón de Google del control.

| Archivo | Qué es |
|---|---|
| `config.json` | El tema: íconos cuadrados flotantes en una sola fila, reproductor de música arriba, botón de fondo, botones redondos |
| `wallpapers/` | 12 fondos de paisajes (créditos en `CREDITS.md`). Pon aquí tus propias imágenes `.jpg` (1920×1080, menos de 10 MB) |
| `PROMPT.md` | Lo que le pegas a Claude Code **en tu PC** para que se conecte a la TV y haga todo |

## Lo nuevo del launcher (programado en este repo)

| Opción en `config.json` | Qué hace |
|---|---|
| `square_tiles` | Las apps son íconos cuadrados con esquinas redondeadas, una sombra suave debajo y un brillo de vidrio arriba: se ven flotando sobre el fondo, estilo Apple |
| `now_playing` | Cuando suena música (Spotify, YouTube Music, etc.) aparece una cápsula arriba con la **carátula**, el título, el artista, unas barritas que se mueven y los botones ⏮ ⏯ ⏭. Si no suena nada, se esconde |
| `wallpaper_button` | Un botón redondo arriba con un dibujo de imagen: **OK** pasa al siguiente fondo y **mantener OK** abre los ajustes del launcher para escoger uno |

El reproductor necesita un permiso que se da al instalar con `NOW_PLAYING=1` (en Windows: `-NowPlaying`).

## Instalar (desde tu PC, en el mismo Wi-Fi que la TV)

```bash
git clone https://github.com/sterling-ai34/clean-you-tv-2
cd clean-you-tv-2
git checkout claude/admiring-albattani-w7z3jp
```

Lo más fácil: abre Claude Code en esa carpeta y pégale [`PROMPT.md`](PROMPT.md). A mano sería así (cambia la IP):

```bash
NOW_PLAYING=1 HOME_KEY_SERVICE=1 INPUT_KEY_SERVICE=1 TV_HOST=192.168.1.50 launcher/build-and-install.sh
```

```bash
TV_HOST=192.168.1.50 tools/theme.sh mi-tv
```

En Windows (PowerShell):

```powershell
powershell -ExecutionPolicy Bypass -File launcher\build-and-install.ps1 -TvHost 192.168.1.50 -NowPlaying -HomeKeyService -InputKeyService
```

## El botón de Google (micrófono) no funciona

En las TCL con Google TV 14 esto pasa casi siempre por una de estas cuatro cosas. Se revisan en este orden:

| Causa | Cómo se ve | Arreglo |
|---|---|---|
| `com.google.android.katniss` apagado (es el Asistente de Google) | El botón no hace nada, o abre la búsqueda de Play Store | `pm enable com.google.android.katniss` |
| `com.google.android.tv.remote.service` apagado (lleva el micrófono del control) | El Asistente abre pero nunca te oye | `pm enable com.google.android.tv.remote.service` |
| `com.google.android.apps.tv.launcherx` apagado (el inicio original de Google TV) | Dice "No disponible en este perfil" o "El Asistente tiene problemas" | Volver a encender launcherx y dejar este launcher encima con `stock_home_redirect` (abajo) |
| El micrófono bloqueado en Ajustes > Privacidad > Micrófono | No sale el indicador de escucha | Activar el acceso al micrófono |

Para ver cuál es (con la IP de tu TV):

```bash
TV_HOST=192.168.1.50 tools/tv.py "pm list packages -d | grep -E 'katniss|remote.service|launcherx|smartalexa'"
```

Lo que salga en esa lista está **apagado**. Encender uno no borra nada ni rompe el launcher.

**Si el problema es launcherx:** Google TV muestra los resultados de voz en su propia pantalla de inicio, así que
sin launcherx el Asistente no puede contestar. Se deja launcherx encendido y este launcher sigue saliendo cuando
presionas Home. Se agregan estas dos líneas al `config.json`:

```json
"key_filter": false,
"stock_home_redirect": "com.google.android.apps.tv.launcherx"
```

Luego se instala con solo `HOME_KEY_SERVICE=1` (sin `INPUT_KEY_SERVICE`). Así lo dejó funcionando el dueño de una
TCL Q7C con el mismo firmware (ver `examples/tcl-q7c-google-tv/README.md`, "Working alternative").

## Alexa no se conecta ("no disponible en tu país")

Son dos cosas distintas:

1. **El puente de Alexa de TCL (`com.tcl.smartalexa`) está apagado.** Casi todas las listas de CleanYourTV para
   TCL lo apagan. Sin él, Alexa no puede controlar la TV. Se arregla con
   `pm enable com.tcl.smartalexa`.
2. **El mensaje del país viene de Amazon, no del launcher.** La función de Alexa para TVs TCL solo está en
   algunos países, y la app de Alexa revisa el país de tu cuenta de Amazon y la región de la TV. Si tu país no
   está en la lista de Amazon, ningún cambio en la TV lo arregla. Lo que se puede probar: que la región de la TV
   (Ajustes > Sistema > Ubicación/País) y el país de tu cuenta de Amazon coincidan, por ejemplo Estados Unidos.
   No es seguro que funcione, porque eso lo decide Amazon.

## Volver todo atrás

Nada se desinstaló. Para dejar la TV como venía de fábrica:

```bash
TV_HOST=192.168.1.50 tools/restore.sh <la lista que se usó> com.google.android.apps.tv.launcherx
```
