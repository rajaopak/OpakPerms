package id.rajaopak.opakperms.messager;

import com.google.gson.JsonElement;
import id.rajaopak.opakperms.enums.LpActionType;
import id.rajaopak.opakperms.manager.MessengerManager;
import id.rajaopak.opakperms.manager.NodeExtractor;
import net.luckperms.api.messenger.message.type.UserUpdateMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class UserUpdateMessageImpl extends AbstractMessage implements UserUpdateMessage {
    public static final String TYPE = "userupdate";
    private final String userName;
    private final LpActionType actionType;
    private final NodeExtractor.Extractor extractor;
    public UserUpdateMessageImpl(UUID id, String name, LpActionType action, NodeExtractor.Extractor extractor) {
        super(id);
        this.userName = name;
        this.actionType = action;
        this.extractor = extractor;
    }

    public static UserUpdateMessageImpl decode(UUID id, LpActionType actionType, @Nullable JsonElement content) {
        if (content == null) {
            throw new IllegalStateException("Missing content");
        }

        // extract user name
        JsonElement nameElement = content.getAsJsonObject().get("userName");
        if (nameElement == null) {
            throw new IllegalStateException("Incoming message has no userName argument: " + content);
        }
        String userName = nameElement.getAsString();

        JsonElement typeElement = content.getAsJsonObject().get("type");
        if (typeElement == null) {
            throw new IllegalStateException("Incoming message has no type argument: " + content);
        }
        String type = typeElement.getAsString();

        JsonElement valueElement = content.getAsJsonObject().get("value");
        if (valueElement == null) {
            throw new IllegalStateException("Incoming message has no value argument: " + content);
        }
        String value = valueElement.getAsString();

        return new UserUpdateMessageImpl(id, userName, actionType, NodeExtractor.deserialize(content.getAsString()));
    }

    @Override
    @Deprecated
    public @NonNull UUID getUserUniqueId() {
        return null;
    }

    public @NonNull String getUserName() {
        return this.userName;
    }

    public LpActionType getActionType() {
        return actionType;
    }

    public NodeExtractor.Extractor getExtractor() {
        return extractor;
    }

    @Override
    public @NonNull String asEncodedString() {
        return MessengerManager.encodeMessageAsString(TYPE, getId(), getActionType(), NodeExtractor.serialize(extractor));
    }
}
