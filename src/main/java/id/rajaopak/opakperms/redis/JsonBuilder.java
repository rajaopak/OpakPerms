package id.rajaopak.opakperms.redis;

import org.json.JSONObject;

public class JsonBuilder {
    private final JSONObject json = new JSONObject();

    public JsonBuilder add(String property, Object value) {
        this.json.put(property, value);
        return this;
    }

    public JSONObject get() {
        return this.json;
    }
}
