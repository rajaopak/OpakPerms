package id.rajaopak.opakperms.enums;

import java.util.Arrays;

public enum LpActionType {

    ADD("add"),
    SET("set"),
    REMOVE("remove"),
    CLEAR("clear");

    private final String name;

    LpActionType(String name) {
        this.name = name;
    }

    public static LpActionType getAction(String name) {
        return Arrays.stream(values()).filter(actionType -> actionType.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }
}
