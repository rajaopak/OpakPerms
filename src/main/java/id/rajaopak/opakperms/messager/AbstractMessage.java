package id.rajaopak.opakperms.messager;

import net.luckperms.api.messenger.message.Message;
import net.luckperms.api.messenger.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public abstract class AbstractMessage implements Message, OutgoingMessage {
    private final UUID id;

    public AbstractMessage(UUID id) {
        this.id = id;
    }

    @Override
    public @NonNull UUID getId() {
        return this.id;
    }

}
