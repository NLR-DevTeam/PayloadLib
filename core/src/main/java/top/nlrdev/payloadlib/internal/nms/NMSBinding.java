package top.nlrdev.payloadlib.internal.nms;

import org.bukkit.World;
import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.types.Identifier;

public interface NMSBinding {
    void sendPayload(Player player, Identifier id, byte[] data);

    Identifier getWorldIdentifier(World world);
}
