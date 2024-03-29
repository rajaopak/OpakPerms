package id.rajaopak.opakperms.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import id.rajaopak.opakperms.enums.LpActionType;
import id.rajaopak.opakperms.util.GsonProvider;
import id.rajaopak.opakperms.util.JsonBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class MessengerManager {

    public static String encodeMessageAsString(String type, UUID id, String userName, LpActionType actionType, @Nullable JsonElement content) {
        JsonObject json = new JsonBuilder()
                .add("id", id.toString())
                .add("validation", type)
                .add("action", actionType.getName())
                .add("userName", userName)
                .consume(o -> {
                    if (content != null) {
                        o.add("content", content);
                    }
                })
                .toJson();

        return GsonProvider.normal().toJson(json);
    }
}
