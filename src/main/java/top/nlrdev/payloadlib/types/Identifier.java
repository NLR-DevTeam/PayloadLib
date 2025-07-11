package top.nlrdev.payloadlib.types;

import java.util.regex.Pattern;

/**
 * Represents a Minecraft Identifier, consisting of a namespace and a path.
 * <br><br>
 * Corresponding Minecraft Classes:
 * <ul>
 *     <li>Mojang: <code>net.minecraft.resources.ResourceLocation</code></li>
 *     <li>Yarn: <code>net.minecraft.util.Identifier</code></li>
 * <ul/>
 */
public class Identifier {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z0-9_.-]+$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^[a-z0-9_/.-]+$");

    private final String namespace;
    private final String path;

    private Identifier(String namespace, String path) {
        if (!isNamespaceValid(namespace)) {
            throw new IllegalArgumentException("Invalid identifier namespace: " + namespace);
        }

        if (!isPathValid(path)) {
            throw new IllegalArgumentException("Invalid identifier path: " + path);
        }

        this.namespace = namespace;
        this.path = path;
    }

    /**
     * Create from a valid namespace and a valid path.
     */
    public static Identifier of(String namespace, String path) {
        return new Identifier(namespace, path);
    }

    /**
     * Create from an identifier string.
     * <br><br>
     * Usage: <code>Identifier.parse("namespace:path")</code>
     *
     * @param identifier A valid identifier string, splitting by a colon
     */
    public static Identifier parse(String identifier) {
        String[] split = identifier.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid identifier string: " + identifier);
        }

        return new Identifier(split[0], split[1]);
    }

    public static boolean isNamespaceValid(String namespace) {
        return namespace != null && !namespace.isBlank() && NAMESPACE_PATTERN.matcher(namespace).matches();
    }

    public static boolean isPathValid(String path) {
        return path != null && !path.isBlank() && PATH_PATTERN.matcher(path).matches();
    }

    public String namespace() {
        return namespace;
    }

    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Identifier identifier)) {
            return false;
        }

        return namespace.equals(identifier.namespace) && path.equals(identifier.path);
    }
}
