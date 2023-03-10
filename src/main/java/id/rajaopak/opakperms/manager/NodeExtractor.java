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

import java.time.Duration;
import java.time.Instant;

public class NodeExtractor {


    private final Extractor extractor;

    public NodeExtractor(Node node) {
        this.extractor = parseNode(node);
    }

    public static Extractor parseNode(Node node) {
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

    public static JsonElement serialize(Extractor extractor) {
        return GsonProvider.normal().toJsonTree(extractor);
    }

    public static Extractor deserialize(String json) {
        return GsonProvider.normal().fromJson(json, Extractor.class);
    }

    public Extractor getExtractor() {
        return this.extractor;
    }

    public interface Extractor {
        @NonNull NodeType<?> getType();

        @NonNull String getKey();

        boolean getValue();

        @Nullable Instant getExpiry();

        @Nullable Duration getExpiryDuration();

        @Nullable ImmutableContextSet getContexts();
    }

    public static class InheritanceNodeExtractor implements Extractor {


        private final NodeType<?> type;
        private final String key;
        private final boolean value;
        private final ImmutableContextSet contexts;
        private Instant expiry;
        private Duration expiryDuration;

        public InheritanceNodeExtractor(InheritanceNode node) {
            this.type = node.getType();
            this.key = node.getKey();
            this.value = node.getValue();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry();
            }

            if (node.getExpiryDuration() != null) {
                this.expiryDuration = node.getExpiryDuration();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull NodeType<?> getType() {
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
        public @Nullable Instant getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable Duration getExpiryDuration() {
            return expiryDuration;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }

    public static class PrefixNodeExtractor implements Extractor {

        private final NodeType<?> type;
        private final String key;
        private final int priority;
        private final boolean value;
        private final ImmutableContextSet contexts;
        private Instant expiry;
        private Duration expiryDuration;

        public PrefixNodeExtractor(PrefixNode node) {
            this.type = node.getType();
            this.key = node.getKey();
            this.priority = node.getPriority();
            this.value = node.getValue();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry();
            }

            if (node.getExpiryDuration() != null) {
                this.expiryDuration = node.getExpiryDuration();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull NodeType<?> getType() {
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
        public @Nullable Instant getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable Duration getExpiryDuration() {
            return expiryDuration;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }

    public static class SuffixNodeExtractor implements Extractor {

        private final NodeType<?> type;
        private final String key;
        private final int priority;
        private final boolean value;
        private final ImmutableContextSet contexts;
        private Instant expiry;
        private Duration expiryDuration;

        public SuffixNodeExtractor(SuffixNode node) {
            this.type = node.getType();
            this.key = node.getKey();
            this.priority = node.getPriority();
            this.value = node.getValue();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry();
            }

            if (node.getExpiryDuration() != null) {
                this.expiryDuration = node.getExpiryDuration();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull NodeType<?> getType() {
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
        public @Nullable Instant getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable Duration getExpiryDuration() {
            return expiryDuration;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }

    public static class PermissionNodeExtractor implements Extractor {

        private final NodeType<?> type;
        private final String key;
        private final boolean value;
        private final String permission;
        private final ImmutableContextSet contexts;
        private Instant expiry;
        private Duration expiryDuration;

        public PermissionNodeExtractor(PermissionNode node) {
            this.type = node.getType();
            this.key = node.getKey();
            this.value = node.getValue();
            this.permission = node.getPermission();

            if (node.getExpiry() != null) {
                this.expiry = node.getExpiry();
            }

            if (node.getExpiryDuration() != null) {
                this.expiryDuration = node.getExpiryDuration();
            }

            this.contexts = node.getContexts();
        }

        @Override
        public @NonNull NodeType<?> getType() {
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
        public @Nullable Instant getExpiry() {
            return expiry;
        }

        @Override
        public @Nullable Duration getExpiryDuration() {
            return expiryDuration;
        }

        @Override
        public @Nullable ImmutableContextSet getContexts() {
            return contexts;
        }
    }
}
