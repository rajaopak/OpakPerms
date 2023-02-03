package id.rajaopak.opakperms.listener;

import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.redis.JsonBuilder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;

import java.util.stream.Collectors;

public class LuckPermsListener {

    private final OpakPerms core;

    public LuckPermsListener(OpakPerms core) {
        this.core = core;
    }

    public void register() {
        EventBus eventBus = this.core.getLuckPerms().getEventBus();
        eventBus.subscribe(this.core, NodeAddEvent.class, this::listener);
    }

    public void listener(NodeAddEvent e) {
        Node node = e.getNode();
        System.out.println(node.getKey());
        System.out.println(node.getType().name());
        System.out.println(node.getContexts().toSet().stream().map(context -> context.getKey() + "-" + context.getValue()).collect(Collectors.joining(", ")));

        if (e.isUser()) {
            User user = (User) e.getTarget();

            if (node.getType() != NodeType.INHERITANCE) {
                return;
            }

            JsonBuilder json = new JsonBuilder();

            json.add("uuid", user.getUniqueId())
                    .add("name", user.getUsername())
                    .add("type", node.getType().name())
                    .add("key", node.getKey())
                    .add("value", node.getValue())
                    .add("negated", node.isNegated())
                    .add("hasExpired", node.hasExpired())
                    .add("hasExpiry", node.hasExpiry());

            if (node.getExpiry() != null) {
                json.add("expiry", node.getExpiry().getEpochSecond());
            }

            if (!node.getContexts().isEmpty()) {
                json.add("context", node.getContexts().toSet().stream().map(context -> context.getKey() + "-" + context.getValue()).collect(Collectors.joining(", ")));
            }

            this.core.getRedisManager().sendRequest(json.get());
        }
    }

}
