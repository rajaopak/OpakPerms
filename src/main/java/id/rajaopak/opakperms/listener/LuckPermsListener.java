package id.rajaopak.opakperms.listener;

import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.enums.LpActionType;
import id.rajaopak.opakperms.manager.NodeExtractor;
import id.rajaopak.opakperms.messager.UserUpdateMessageImpl;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;

import java.util.UUID;

public class LuckPermsListener {

    private final OpakPerms core;

    public LuckPermsListener(OpakPerms core) {
        this.core = core;
    }

    public void register() {
        EventBus eventBus = this.core.getLuckPerms().getEventBus();
        eventBus.subscribe(this.core, NodeAddEvent.class, this::nodeAddListener);
        eventBus.subscribe(this.core, NodeRemoveEvent.class, this::nodeRemoveListener);
    }

    public void nodeAddListener(NodeAddEvent e) {
        if (e.isUser()) {
            Node node = e.getNode();
            User user = (User) e.getTarget();

            if (this.core.isListenerEnable()) {
                if (node instanceof InheritanceNode) {
                    this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString());
                } else if (node instanceof PrefixNode) {
                    this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString());
                } else if (node instanceof SuffixNode) {
                    this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString());
                } else if (node instanceof PermissionNode) {
                    this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString());
                }
            }
        }
    }

    public void nodeRemoveListener(NodeRemoveEvent e) {
        if (!e.isUser()) return;

        Node node = e.getNode();
        User user = (User) e.getTarget();

        if (this.core.isListenerEnable()) {
            if (node instanceof InheritanceNode) {
                this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString());
            } else if (node instanceof PrefixNode) {
                this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString());
            } else if (node instanceof SuffixNode) {
                this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString());
            } else if (node instanceof PermissionNode) {
                this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(generatePingId(), user.getUsername(), LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString());
            }
        }
    }

    private UUID generatePingId() {
        return UUID.randomUUID();
    }

}
