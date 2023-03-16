package id.rajaopak.opakperms.manager;

import com.google.gson.JsonElement;
import id.rajaopak.opakperms.util.GsonProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NodeExtractor {

    private final Extractor<?> extractor;

    public NodeExtractor(Node node) {
        this.extractor = parseNode(node);
    }

    public static Extractor<?> parseNode(Node node) {
        if (node == null) return null;

        if (node instanceof InheritanceNode inheritanceNode) {
            return new InheritanceNodeExtractor(inheritanceNode);
        } else if (node instanceof PrefixNode prefixNode) {
            return new PrefixNodeExtractor(prefixNode);
        } else if (node instanceof SuffixNode suffixNode) {
            return new SuffixNodeExtractor(suffixNode);
        } else if (node instanceof PermissionNode permissionNode) {
            return new PermissionNodeExtractor(permissionNode);
        } else {
            throw new IllegalArgumentException("No extractor has been found for " + node + ".");
        }
    }

    public static JsonElement serialize(Extractor<?> extractor) {
        return GsonProvider.normal().toJsonTree(extractor, extractor.getClass());
    }

    public static Extractor<?> deserialize(JsonElement json) {
        String type = json.getAsJsonObject().get("type").getAsString();

        if (type.equals(NodeType.INHERITANCE.name())) {
            return GsonProvider.normal().fromJson(json, InheritanceNodeExtractor.class);
        } else if (type.equals(NodeType.PREFIX.name())) {
            return GsonProvider.normal().fromJson(json, PrefixNodeExtractor.class);
        } else if (type.equals(NodeType.SUFFIX.name())) {
            return GsonProvider.normal().fromJson(json, SuffixNodeExtractor.class);
        } else if (type.equals(NodeType.PERMISSION.name())) {
            return GsonProvider.normal().fromJson(json, PermissionNodeExtractor.class);
        } else {
            throw new IllegalArgumentException("No extractor has been founded for deserializing: " + json);
        }
    }

    public Extractor<?> getExtractor() {
        return this.extractor;
    }

    public interface Extractor<T extends Extractor<T>> {
        @NonNull String getType();

        @NonNull String getKey();

        default boolean getValue() {
            return true;
        }

        default long getExpiry() {
            return 0;
        }

        @Nullable ImmutableContextSet getContexts();
    }

    public static class InheritanceNodeExtractor implements Extractor<InheritanceNodeExtractor> {

        private final String type;
        private final String key;
        private final boolean value;
        private final transient ImmutableContextSet contexts;
        private long expiry;

        public InheritanceNodeExtractor(InheritanceNode node) {
            this.type = node.getType().name();
            this.key = node.getGroupName();
            this.value = node.getValue();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry().toEpochMilli();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull String getType() {
            return type;
        }

        @Override
        public @NonNull String getKey() {
            return key;
        }

        @Override
        public boolean getValue() {
            return value;
        }

        @Override
        public long getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }

    public static class PrefixNodeExtractor implements Extractor<PrefixNodeExtractor> {

        private final String type;
        private final String key;
        private final int priority;
        private final boolean value;
        private final ImmutableContextSet contexts;
        private long expiry;

        public PrefixNodeExtractor(PrefixNode node) {
            this.type = node.getType().name();
            this.key = node.getKey();
            this.priority = node.getPriority();
            this.value = node.getValue();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry().toEpochMilli();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull String getType() {
            return type;
        }

        @Override
        public @NonNull String getKey() {
            return key;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public boolean getValue() {
            return value;
        }

        @Override
        public long getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }

    public static class SuffixNodeExtractor implements Extractor<SuffixNodeExtractor> {

        private final String type;
        private final String key;
        private final int priority;
        private final boolean value;
        private final ImmutableContextSet contexts;
        private long expiry;

        public SuffixNodeExtractor(SuffixNode node) {
            this.type = node.getType().name();
            this.key = node.getKey();
            this.priority = node.getPriority();
            this.value = node.getValue();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry().toEpochMilli();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull String getType() {
            return type;
        }

        @Override
        public @NonNull String getKey() {
            return key;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public boolean getValue() {
            return value;
        }

        @Override
        public long getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }

    public static class PermissionNodeExtractor implements Extractor<PermissionNodeExtractor> {

        private final String type;
        private final String key;
        private final boolean value;
        private final String permission;
        private final ImmutableContextSet contexts;
        private long expiry;

        public PermissionNodeExtractor(PermissionNode node) {
            this.type = node.getType().name();
            this.key = node.getKey();
            this.value = node.getValue();
            this.permission = node.getPermission();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry().toEpochMilli();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull String getType() {
            return type;
        }

        @Override
        public @NonNull String getKey() {
            return key;
        }

        @Override
        public boolean getValue() {
            return value;
        }

        public String getPermission() {
            return permission;
        }

        @Override
        public long getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }
}
