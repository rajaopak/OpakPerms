package id.rajaopak.opakperms.redis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.enums.LpActionType;
import id.rajaopak.opakperms.manager.NodeExtractor;
import id.rajaopak.opakperms.messager.UserUpdateMessageImpl;
import id.rajaopak.opakperms.util.ExpiringSet;
import id.rajaopak.opakperms.util.GsonProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PubSubListener extends JedisPubSub {

    private final OpakPerms core;
    private final ExpiringSet<UUID> receivedMessage;

    public PubSubListener(OpakPerms core) {
        this.core = core;
        this.receivedMessage = new ExpiringSet<>(30, TimeUnit.MINUTES);
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(this.core.getRedisChannel())) {
            return;
        }

        this.consumeIncomingMessageAsString(message);
    }

    public boolean consumeIncomingMessageAsString(@NonNull String encodedString) {
        try {
            return consumeIncomingMessageAsString0(encodedString);
        } catch (Exception e) {
            this.core.getLogger().log(Level.SEVERE, "Unable to decode incoming messaging service message: '" + encodedString + "'", e);
            return false;
        }
    }

    private boolean consumeIncomingMessageAsString0(String encodedString) {
        JsonObject parsed = Objects.requireNonNull(GsonProvider.normal().fromJson(encodedString, JsonObject.class), "parsed");
        JsonObject json = parsed.getAsJsonObject();

        // extract id
        JsonElement idElement = json.get("id");
        if (idElement == null) {
            throw new IllegalStateException("Incoming message has no id argument: " + encodedString);
        }
        UUID id = UUID.fromString(idElement.getAsString());

        // ensure the message hasn't been received already
        if (!this.receivedMessage.add(id)) {
            return false;
        }

        // extract type
        JsonElement typeElement = json.get("type");
        if (typeElement == null) {
            throw new IllegalStateException("Incoming message has no type argument: " + encodedString);
        }
        String type = typeElement.getAsString();

        // extract action
        JsonElement actionElement = json.get("action");
        if (actionElement == null) {
            throw new IllegalStateException("Incoming message has no action argument: " + encodedString);
        }
        LpActionType action = LpActionType.getAction(actionElement.getAsString());

        // extract content
        JsonElement content = json.get("content");

        UserUpdateMessageImpl decoded;
        if (type.equals("userupdate")) {
            decoded = UserUpdateMessageImpl.decode(id, action, content);
        } else return false;

        processIncomingMessage(decoded);
        return true;
    }

    private void processIncomingMessage(UserUpdateMessageImpl message) {
        User user = this.core.getLuckPerms().getUserManager().getUser(message.getUserName());
        if (user == null) {
            return;
        }

        this.core.getLogger().info("[Messaging] Received user update ping for '" + user.getUsername() + "' with id: " + message.getId());

        if (message.getExtractor().getType().name().equals(NodeType.INHERITANCE.name()) && this.core.isSyncRank() && message.getExtractor() instanceof NodeExtractor.InheritanceNodeExtractor g) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(g.getKey());

            if (group == null) {
                this.core.getLogger().info("[Messaging] Group with name " + message.getExtractor().getKey() + " is null.");
                return;
            }

            this.core.getLuckPerms().getUserManager().modifyUser(user.getUniqueId(), u -> {
                InheritanceNode.Builder node = InheritanceNode.builder(group).value(g.getValue());

                if (g.getContexts() != null) {
                    node.context(g.getContexts());
                }

                if (g.getExpiry() != null) {
                    node.expiry(g.getExpiry());
                }

                if (message.getActionType() == LpActionType.ADD) {
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.SET) {
                    u.data().clear(NodeType.INHERITANCE::matches);
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.REMOVE) {
                    u.data().remove(node.build());
                } else if (message.getActionType() == LpActionType.CLEAR) {
                    u.data().clear(NodeType.INHERITANCE::matches);
                }
            });
        } else if (message.getExtractor().getType().name().equals(NodeType.PREFIX.name()) && this.core.isSyncPrefix() && message.getExtractor() instanceof NodeExtractor.PrefixNodeExtractor p) {
            String prefix = p.getKey();
            int priority = p.getPriority();

            this.core.getLuckPerms().getUserManager().modifyUser(user.getUniqueId(), u -> {
                PrefixNode.Builder node = PrefixNode.builder(prefix, priority);

                if (p.getContexts() != null) {
                    node.context(p.getContexts());
                }

                if (p.getExpiry() != null) {
                    node.expiry(p.getExpiryDuration());
                }

                if (message.getActionType() == LpActionType.ADD) {
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.SET) {
                    u.data().clear(NodeType.PREFIX::matches);
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.REMOVE) {
                    u.data().remove(node.build());
                } else if (message.getActionType() == LpActionType.CLEAR) {
                    u.data().clear(NodeType.PREFIX::matches);
                }
            });
        } else if (message.getExtractor().getType().name().equals(NodeType.SUFFIX.name()) && this.core.isSyncSuffix() && message.getExtractor() instanceof NodeExtractor.SuffixNodeExtractor s) {
            String suffix = s.getKey();
            int priority = s.getPriority();

            this.core.getLuckPerms().getUserManager().modifyUser(user.getUniqueId(), u -> {
                SuffixNode.Builder node = SuffixNode.builder(suffix, priority);

                if (s.getContexts() != null) {
                    node.context(s.getContexts());
                }

                if (s.getExpiry() != null) {
                    node.expiry(s.getExpiry());
                }

                if (message.getActionType() == LpActionType.ADD) {
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.SET) {
                    u.data().clear(NodeType.SUFFIX::matches);
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.REMOVE) {
                    u.data().remove(node.build());
                } else if (message.getActionType() == LpActionType.CLEAR) {
                    u.data().clear(NodeType.SUFFIX::matches);
                }
            });
        } else if (message.getExtractor().getType().name().equals(NodeType.PERMISSION.name()) && this.core.isSyncPermission() && message.getExtractor() instanceof NodeExtractor.PermissionNodeExtractor per) {
            String permission = per.getKey();

            this.core.getLuckPerms().getUserManager().modifyUser(user.getUniqueId(), u -> {
                PermissionNode.Builder node = PermissionNode.builder(permission);

                if (per.getContexts() != null) {
                    node.context(per.getContexts());
                }

                if (per.getExpiry() != null) {
                    node.expiry(per.getExpiryDuration());
                }

                if (message.getActionType() == LpActionType.ADD) {
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.SET) {
                    u.data().clear(NodeType.PERMISSION::matches);
                    u.data().add(node.build());
                } else if (message.getActionType() == LpActionType.REMOVE) {
                    u.data().remove(node.build());
                } else if (message.getActionType() == LpActionType.CLEAR) {
                    u.data().clear(NodeType.PERMISSION::matches);
                }
            });
        }
    }
}
