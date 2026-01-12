# 🎵 Reproductor de Música - Java/JavaFX

Un reproductor de música simple y funcional desarrollado con JavaFX.

## ✨ Características

- 🎼 Búsqueda automática de archivos de música en carpetas comunes
- ▶️ Reproducir, pausar y detener canciones
- ⏮️⏭️ Navegar entre canciones (anterior/siguiente)
- 📊 Barra de progreso en tiempo real
- ⏱️ Contador de tiempo (actual / total)
- 🔄 Reproducción automática de siguiente canción al finalizar
- 📋 Lista de canciones con información de nombre, ruta y tamaño

## 🎵 Formatos Soportados

- ✅ MP3 (recomendado)
- ✅ WAV
- ✅ M4A/AAC
- ✅ AIFF

## 🚀 Cómo Ejecutar


### Ejecución Normal

1. Abre el proyecto en Eclipse
2. Ejecuta `Main.java`
3. Selecciona una canción de la lista
4. Usa los botones de control para reproducir

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
│   └── application.css        # Estilos
└── resources/                 # Recursos e imágenes
```

## 🔧 Requisitos

- Java 11 o superior
- JavaFX 11 o superior
- Eclipse con e(fx)clipse plugin (opcional pero recomendado)

## 📝 Notas Técnicas

- El programa busca automáticamente música en: Music, Downloads, Desktop, Documents
- La búsqueda está limitada a 200 archivos por carpeta para optimizar rendimiento
- El reproductor usa `javafx.scene.media.MediaPlayer` internamente
- Auto-play está habilitado al finalizar cada canción

## 🐛 Solución de Problemas

### No se encuentran canciones
- Verifica que tengas archivos MP3/WAV en las carpetas: Music, Downloads, Desktop, Documents
- Asegúrate de que los archivos tengan las extensiones correctas

### No se reproduce el audio
- Verifica que el formato sea soportado (MP3, WAV, M4A, AIFF)
- Intenta con otro archivo de audio para descartar archivo corrupto
- Verifica que tu sistema tenga los códecs de audio instalados

## 📄 Licencia

Proyecto educativo - Uso libre

