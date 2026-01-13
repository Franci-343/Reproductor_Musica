package application;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility to find music files on the user's PC.
 *
 * Usage examples:
 * - Call MusicFinder.findMusicInCommonDirs() to search the common user folders.
 * - Call MusicFinder.findMusic(Paths.get("C:\\"), extensions, maxDepth, maxFiles) to scan a specific path.
 *
 * The class is defensive: it handles IO exceptions and skips unreadable directories.
 */
public class MusicFinder {

    private static final Set<String> DEFAULT_EXTENSIONS = new HashSet<String>(Arrays.asList(
            "mp3", "wav", "m4a", "flac", "aac", "ogg", "wma", "alac"
    ));

    /**
     * Finds music files under common user folders (Music, Downloads, Desktop, Documents).
     * Returns a list of matched Paths. This uses a default maxDepth of 10 and maxFiles of 1000.
     */
    public static List<Path> findMusicInCommonDirs() {
        String userHome = System.getProperty("user.home");
        List<Path> starts = new ArrayList<>();
        if (userHome != null) {
            starts.add(Paths.get(userHome, "Music"));
            starts.add(Paths.get(userHome, "Downloads"));
            starts.add(Paths.get(userHome, "Desktop"));
            starts.add(Paths.get(userHome, "Documents"));
        }
        // Also try all root drives (C:\, D:\)
        try {
            for (Path root : FileSystems.getDefault().getRootDirectories()) {
                starts.add(root);
            }
        } catch (Exception e) {
            // ignore
        }

        Set<String> ext = DEFAULT_EXTENSIONS;
        List<Path> result = new ArrayList<>();
        for (Path start : starts) {
            if (start == null) continue;
            if (!Files.exists(start)) continue;
            try {
                result.addAll(findMusic(start, ext, 6, 200)); // limit depth and files for roots
            } catch (Exception e) {
                // keep going with other starts
                System.err.println("Error scanning " + start + ": " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Find music files under a starting path.
     *
     * @param start the root path to start scanning
     * @param extensions set of lowercase extensions without dot (e.g. "mp3")
     * @param maxDepth maximum directory depth to traverse (use Integer.MAX_VALUE for no limit)
     * @param maxFiles maximum number of files to collect before stopping
     * @return list of matched Paths (may be empty)
     * @throws IOException on IO errors
     */
    public static List<Path> findMusic(Path start, Set<String> extensions, int maxDepth, int maxFiles) throws IOException {
        List<Path> found = new ArrayList<>();
        if (start == null || !Files.exists(start)) return found;

        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            private int filesCollected = 0;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs != null && attrs.isRegularFile()) {
                    String name = file.getFileName().toString();
                    int dot = name.lastIndexOf('.');
                    if (dot >= 0 && dot < name.length() - 1) {
                        String ext = name.substring(dot + 1).toLowerCase();
                        if (extensions.contains(ext)) {
                            found.add(file);
                            filesCollected++;
                            if (filesCollected >= maxFiles) {
                                return FileVisitResult.TERMINATE;
                            }
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // skip system or hidden directories to speed up
                try {
                    if (Files.isHidden(dir)) return FileVisitResult.SKIP_SUBTREE;
                } catch (IOException ignored) {
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // skip files we can't read
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(start, java.util.EnumSet.noneOf(java.nio.file.FileVisitOption.class), maxDepth, visitor);
        } catch (IOException e) {
            // rethrow so caller may handle or log
            throw e;
        }

        return found;
    }

    // Convenience overload
    public static List<Path> findMusic(Path start) throws IOException {
        return findMusic(start, DEFAULT_EXTENSIONS, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // Small CLI for quick tests
    public static void main(String[] args) {
        System.out.println("Starting music search (this may take a while)...");
        List<Path> found = new ArrayList<>();
        try {
            if (args.length > 0) {
                Path p = Paths.get(args[0]);
                found = findMusic(p, DEFAULT_EXTENSIONS, 10, 1000);
            } else {
                found = findMusicInCommonDirs();
            }
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Found " + found.size() + " music files:");
        for (Path p : found) {
            System.out.println(p.toString());
        }
    }
}