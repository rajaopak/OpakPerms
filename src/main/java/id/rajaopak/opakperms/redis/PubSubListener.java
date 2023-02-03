package id.rajaopak.opakperms.redis;

import id.rajaopak.opakperms.OpakPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.OfflinePlayer;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class PubSubListener extends JedisPubSub {

    private final OpakPerms core;

    public PubSubListener(OpakPerms core) {
        this.core = core;
    }

    @Override
    public void onMessage(String channel, String message) {
        JSONObject object;

        try {
            object = new JSONObject(message);
        } catch (JSONException e) {
            return;
        }

        UUID uuid = UUID.fromString(object.getString("uuid"));
        String name = object.getString("name");
        String key = object.getString("key");
        String type = object.getString("type");
        Boolean value = object.getBoolean("value");
        Boolean negated = object.getBoolean("negated");
        Boolean hasExpired = object.getBoolean("hasExpired");
        Boolean hasExpiry = object.getBoolean("hasExpiry");
        long expiry = 0L;
        String context = null;

        if (object.has("expiry")) {
            expiry = object.getLong("expiry");
        }

        if (object.has("context")) {
            context = object.getString("context");
        }

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(uuid);
        if (!player.hasPlayedBefore()) {
            player = this.core.getServer().getOfflinePlayer(name);
        }

        if (type.equalsIgnoreCase("INHERITANCE")) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(key.split("\\.")[1]);
            Node node = InheritanceNode.builder(group).build();

            this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().add(node);
                System.out.println("sync player data");
            });
        } else if (type.equalsIgnoreCase("PREFIX")) {
            PrefixNode node = PrefixNode.builder(key.split("\\.")[2], Integer.parseInt(key.split("\\.")[1])).build();
        }

    }
}
