package id.rajaopak.opakperms.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class JsonBuilder implements JElement {
    private final JsonObject object = new JsonObject();

    @Override
    public JsonObject toJson() {
        return this.object;
    }

    public JsonBuilder add(String key, JsonElement value) {
        this.object.add(key, value);
        return this;
    }

    public JsonBuilder add(String key, String value) {
        if (value == null) {
            return add(key, JsonNull.INSTANCE);
        }
        return add(key, new JsonPrimitive(value));
    }

    public JsonBuilder add(String key, Number value) {
        if (value == null) {
            return add(key, JsonNull.INSTANCE);
        }
        return add(key, new JsonPrimitive(value));
    }

    public JsonBuilder add(String key, Boolean value) {
        if (value == null) {
            return add(key, JsonNull.INSTANCE);
        }
        return add(key, new JsonPrimitive(value));
    }

    public JsonBuilder add(String key, JElement value) {
        if (value == null) {
            return add(key, JsonNull.INSTANCE);
        }
        return add(key, value.toJson());
    }

    public JsonBuilder add(String key, Supplier<? extends JElement> value) {
        return add(key, value.get().toJson());
    }

    public JsonBuilder consume(Consumer<? super JsonBuilder> consumer) {
        consumer.accept(this);
        return this;
    }
}
