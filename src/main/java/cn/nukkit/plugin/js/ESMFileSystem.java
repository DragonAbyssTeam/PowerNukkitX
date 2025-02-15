package cn.nukkit.plugin.js;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.utils.SeekableInMemoryByteChannel;
import org.graalvm.polyglot.io.FileSystem;

import java.io.*;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class ESMFileSystem implements FileSystem {
    final File baseDir;
    private final int pluginId;
    private ClassLoader mainClassLoader;

    private final static Map<String, byte[]> innerModuleCache = new WeakHashMap<>(1, 1f);
    final static Map<String, Class<?>> javaClassCache = new ConcurrentHashMap<>();

    public ESMFileSystem(File baseDir, int pluginId) {
        this.baseDir = baseDir;
        this.pluginId = pluginId;
    }

    @Override
    public Path parsePath(URI uri) {
        return parsePath(uri.toString());
    }

    @Override
    public Path parsePath(String path) {
        if (path.startsWith("@")) {
            return Path.of(Server.getInstance().getPluginPath(), path);
        } else if (path.startsWith(":")) {
            return Path.of("inner-module", path.substring(1));
        } else if ((!path.equals(".js") && getDots(path) > 1)) {
            if (mainClassLoader == null)
                mainClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                if (javaClassCache.containsKey(path)) {
                    return Path.of("java-class", path);
                }
                var clazz = mainClassLoader.loadClass(path);
                if (clazz != null) {
                    javaClassCache.put(path, clazz);
                }
                return Path.of("java-class", path);
            } catch (ClassNotFoundException ignore) {

            }
        }
        return baseDir.toPath().resolve(path);
    }

    private static int getDots(String originStr) {
        var res = 0;
        var i = originStr.indexOf('.');
        while (i != -1) {
            i = originStr.indexOf('.', i + 1);
            res++;
        }
        return res;
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        if (path.startsWith("inner-module")) {
            for (var each : modes) {
                if (each != AccessMode.READ) {
                    throw new IOException("Inner module cannot be accessed.");
                }
            }
        } else if (path.startsWith("java-class")) {
            for (var each : modes) {
                if (each != AccessMode.READ) {
                    throw new IOException("Java class cannot be accessed.");
                }
            }
        } else {
            path = path.toRealPath(linkOptions);
            for (var each : modes) {
                if (each == AccessMode.READ && !Files.isReadable(path)) {
                    throw new AccessDeniedException(path.toString());
                } else if (each == AccessMode.WRITE && !Files.isWritable(path)) {
                    throw new AccessDeniedException(path.toString());
                } else if (each == AccessMode.EXECUTE && !Files.isExecutable(path)) {
                    throw new AccessDeniedException(path.toString());
                }
            }
        }
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        if (dir.startsWith("inner-module") || dir.startsWith("java-class")) {
            throw new IOException("Inner module cannot be accessed.");
        }
        Files.createDirectories(dir, attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        if (path.startsWith("inner-module") || path.startsWith("java-class")) {
            throw new IOException("Inner module cannot be accessed.");
        }
        Files.delete(path);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        if (path.startsWith("inner-module")) {
            byte[] contents = new byte[0];
            var moduleName = path.toString().substring(13);
            if ("plugin-id".equals(moduleName)) {
                contents = ("export const id = " + pluginId).getBytes(StandardCharsets.UTF_8);
            } else {
                if (innerModuleCache.containsKey(moduleName)) {
                    contents = innerModuleCache.get(moduleName);
                } else {
                    try (var ins = Nukkit.class.getClassLoader().getResourceAsStream("inner-module/" + moduleName + ".js")) {
                        if (ins != null)
                            contents = ins.readAllBytes();
                    }
                    if (contents.length != 0) {
                        innerModuleCache.put(moduleName, contents);
                    }
                }
            }
            return new SeekableInMemoryByteChannel(contents);
        } else if (path.startsWith("java-class")) {
            var className = path.toString().substring(11);
            return new SeekableInMemoryByteChannel(ESMJavaExporter.exportJava(javaClassCache.get(className)).getBytes(StandardCharsets.UTF_8));
        }
        return Files.newByteChannel(path, options, attrs);
    }

    public Reader newReader(Path path) throws IOException {
        if (path.startsWith("inner-module")) {
            String contents = "";
            var moduleName = path.toString().substring(13);
            if ("plugin-id".equals(moduleName)) {
                contents = "export const id = " + pluginId;
            } else {
                if (innerModuleCache.containsKey(moduleName)) {
                    contents = new String(innerModuleCache.get(moduleName));
                } else {
                    try (var ins = Nukkit.class.getClassLoader().getResourceAsStream("inner-module/" + moduleName + ".js")) {
                        if (ins != null)
                            return new InputStreamReader(ins);
                    }
                }
            }
            return new StringReader(contents);
        } else if (path.startsWith("java-class")) {
            var className = path.toString().substring(11);
            return new StringReader(ESMJavaExporter.exportJava(javaClassCache.get(className)));
        }
        return Files.newBufferedReader(path, StandardCharsets.UTF_8);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        if (dir.startsWith("inner-module") || dir.startsWith("java-class")) {
            throw new IOException("Inner module cannot be accessed.");
        }
        return Files.newDirectoryStream(dir, filter);
    }

    @Override
    public Path toAbsolutePath(Path path) {
        if (path.startsWith("inner-module") || path.startsWith("java-class")) {
            return path;
        }
        return path.toAbsolutePath();
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        if (path.startsWith("inner-module") || path.startsWith("java-class")) {
            return path;
        }
        return path.toRealPath(linkOptions);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        if (path.startsWith("inner-module") || path.startsWith("java-class")) {
            throw new IOException("Inner module cannot be accessed.");
        }
        return Files.readAttributes(path, attributes, options);
    }
}
