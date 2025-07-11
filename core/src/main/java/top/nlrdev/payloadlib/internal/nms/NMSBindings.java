package top.nlrdev.payloadlib.internal.nms;

import org.bukkit.Bukkit;

import java.util.HashMap;

public class NMSBindings {
    private static final HashMap<String, String> BINDINGS = new HashMap<>();
    private static final NMSBinding BINDING;

    static {
        register("1.21.4", "1.21.4", "1.21.5", "1.21.7");
        register("1.21.1", "1.21", "1.21.1");
        register("1.20.6", "1.20.5", "1.20.6");
        register("1.20.4", "1.20.3", "1.20.4");
        register("1.20.1", "1.20", "1.20.1");

        String version = Bukkit.getBukkitVersion().split("-")[0];
        String target = BINDINGS.getOrDefault(version, null);
        if (target == null) {
            throw new RuntimeException("PayloadLib doesn't support your server version!");
        }

        try {
            BINDING = (NMSBinding) Class.forName("top.nlrdev.payloadlib.internal.nms.NMSBindingV" + target.replace(".", "_"))
                    .getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to bind NMS", ex);
        }
    }

    private static void register(String target, String... accepted) {
        for (String version : accepted) {
            BINDINGS.put(version, target);
        }
    }

    public static NMSBinding get() {
        return BINDING;
    }
}
