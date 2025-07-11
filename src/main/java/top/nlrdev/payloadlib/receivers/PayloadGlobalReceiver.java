package top.nlrdev.payloadlib.receivers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.nlrdev.payloadlib.Payload;

@FunctionalInterface
public interface PayloadGlobalReceiver<T extends Payload> {
    void handlePayload(@NotNull Player sender, @NotNull T payload);
}
