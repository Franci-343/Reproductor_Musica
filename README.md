# 🎵 Reproductor de Música - Java/JavaFX

Un reproductor de música simple y funcional desarrollado con JavaFX.

## ✨ Características

- 🎼 Búsqueda automática de archivos de música en carpetas comunes
- ▶️ Reproducir, pausar y detener canciones
- ⏮️⏭️ Navegar entre canciones (anterior/siguiente)
- 📊 Barra de progreso en tiempo real
- ⏱️ Contador de tiempo (actual / total)
- 🔊 Control de volumen con slider
- 🎨 Visualizador de audio con espectro de frecuencias
- 🔄 Reproducción automática de siguiente canción al finalizar
- 📋 Lista de canciones con información de nombre, ruta y tamaño
- 🪟 Interfaz con diseño glassmorphism y controles de ventana personalizados
- 📱 Diseño responsive con tamaño mínimo de ventana

## 🎵 Formatos Soportados

- ✅ MP3 (recomendado)
- ✅ WAV
- ✅ M4A/AAC
- ✅ AIFF

## 🚀 Cómo Ejecutar

### Requisitos Previos

1. **Descargar JavaFX SDK 25.0.1** (o superior):
   - Visita: https://openjfx.io/
   - Descarga el SDK correspondiente a tu sistema operativo
   - Extrae el archivo en una ubicación de tu preferencia

### Ejecución en Eclipse

1. **Configurar VM Arguments**:
   - Click derecho en `Main.java` → **Run As** → **Run Configurations...**
   - En la pestaña **Arguments**, en el campo **VM arguments**, agrega:
   ```
   --module-path "TU_RUTA"
   --add-modules javafx.controls,javafx.fxml,javafx.media
   --enable-native-access=javafx.graphics,javafx.media
   ```
   - **Importante**: Ajusta la ruta `--module-path` según donde hayas instalado JavaFX SDK

2. **Ejecutar el Proyecto**:
   - Click en **Apply** y luego **Run**
   - O simplemente ejecuta `Main.java` si ya configuraste el `Main.launch`

3. **Usar el Reproductor**:
   - Selecciona una canción de la lista
   - Usa los botones de control para reproducir


## 🎮 Controles

| Botón | Función |
|-------|---------|
| ⏮ Anterior | Reproduce la canción anterior |
| ▶ Play | Reproduce o resume la canción seleccionada |
| ⏸ Pause | Pausa la reproducción |
| ⏹ Stop | Detiene la reproducción |
| ⏭ Siguiente | Reproduce la siguiente canción |

## 📁 Estructura del Proyecto

```
src/
├── application/
│   ├── Main.java              # Clase principal
│   ├── Controller.java         # Controlador con lógica del reproductor
│   ├── Main.fxml              # Interfaz gráfica
│   ├── MusicFinder.java       # Búsqueda de archivos de música
│   ├── AudioVisualizer.java   # Visualizador de espectro de audio
│   ├── application.css        # Estilos principales
│   └── responsive.css         # Estilos responsivos
└── resources/                 # Recursos e imágenes
    └── Duke256.png            # Icono de la aplicación
```

## 🔧 Requisitos

- Java 11 o superior
- JavaFX 25.0.1 o superior (no incluido en el JDK)
- Eclipse con e(fx)clipse plugin (opcional pero recomendado)

## 📝 Notas Técnicas

- El programa busca automáticamente música en: Music, Downloads, Desktop, Documents
- La búsqueda está limitada a 200 archivos por carpeta para optimizar rendimiento
- El reproductor usa `javafx.scene.media.MediaPlayer` internamente
- Auto-play está habilitado al finalizar cada canción
- El visualizador de audio usa `AudioSpectrumListener` para analizar frecuencias en tiempo real
- La interfaz usa un diseño glassmorphism con ventana sin bordes nativos

## 🐛 Solución de Problemas

### Error: "JavaFX runtime components are missing"
- Asegúrate de haber descargado JavaFX SDK 25.0.1 desde https://openjfx.io/
- Verifica que la ruta en `--module-path` apunte correctamente a la carpeta `lib` de JavaFX
- Confirma que los argumentos de VM incluyan todos los módulos necesarios: `javafx.controls,javafx.fxml,javafx.media`

### Error: "Module javafx.graphics not found"
- Revisa que la ruta del `--module-path` sea correcta y esté entre comillas
- Verifica que la carpeta JavaFX SDK contenga los archivos JAR necesarios
- En Eclipse, revisa Run Configurations → Arguments → VM arguments

### Advertencias de "native-access"
- Asegúrate de incluir `--enable-native-access=javafx.graphics,javafx.media` en los VM arguments
- Esto es necesario para JavaFX 25+ debido a cambios en la seguridad de acceso nativo

### No se encuentran canciones
- Verifica que tengas archivos MP3/WAV en las carpetas: Music, Downloads, Desktop, Documents
- Asegúrate de que los archivos tengan las extensiones correctas

### No se reproduce el audio
- Verifica que el formato sea soportado (MP3, WAV, M4A, AIFF)
- Intenta con otro archivo de audio para descartar archivo corrupto
- Verifica que tu sistema tenga los códecs de audio instalados

## 📄 Licencia

Proyecto educativo - Uso libre

