package top.nlrdev.payloadlib;

import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.types.Identifier;

/**
 * Basic Payload interface. Everything starts here.
 */
public interface Payload {
    Identifier getId();

    default void sendTo(Player... players) {
        PayloadLib.sendPayload(this, players);
    }
}
