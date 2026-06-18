# Android Launcher (Dotrino)

Launcher (pantalla de inicio) de Android, en Kotlin + Jetpack Compose, parte del
ecosistema **Dotrino**. Es un launcher con carrusel de apps y la capacidad de
**ocultar apps** detrás de biometría / patrón.

> ⚠️ **Alcance honesto:** un launcher controla *su propia* interfaz. Las apps
> ocultas siguen instaladas y se pueden encontrar fuera del launcher (Ajustes →
> Aplicaciones, buscador del sistema, otro launcher). Para ocultarlas a nivel
> sistema haría falta Shizuku/root (`pm disable-user`), fuera del alcance actual.

## Funciones

- **Carrusel de páginas** con swipe infinito; paginación **alineada por letra**
  (una letra no se parte entre páginas salvo que no entre en una).
- **Índice lateral** de rangos de letras (botones que llenan la altura, letras
  apiladas) para saltar de página.
- **2 filas fijas de recientes** arriba (rastreo propio, sin permiso de uso).
- **Dock fijo** abajo: teléfono y cámara por defecto del sistema.
- **Dos pseudo-apps** ordenadas alfabéticamente como una más:
  - **Configuración** (logo Dotrino + candado): elegir qué ocultar + camuflaje.
  - **Apps ocultas** (camuflada, nombre e icono configurables, default "Fútbol").
- **Seguridad** con BiometricPrompt: huella / rostro o **patrón/PIN del
  dispositivo**; PIN propio como respaldo.
- **Mantener presionada** una app → Ocultar/Mostrar, Información, **Desinstalar**.
- **Moneda de soporte** del ecosistema (`<dotrino-support>`) en Configuración.

## Requisitos

- JDK 17, Android SDK con plataforma **android-34** y build-tools 34.
- `local.properties` con `sdk.dir=<ruta del SDK>`.
- Para firmar release: `keystore.properties` + el `.jks` (no commiteados).

## Compilar

```bash
./gradlew :app:assembleDebug     # APK debug (pruebas)
./gradlew :app:assembleRelease   # APK release firmado (necesita keystore.properties)
```

## Instalar

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Luego pulsá HOME y elegí **Android Launcher** como pantalla de inicio. (La app
también ofrece un botón para ponerse como launcher por defecto.)

## Descarga

APK publicado en
[releases](https://github.com/imdotrino/android-launcher/releases/latest).
