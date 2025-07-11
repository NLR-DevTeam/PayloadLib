package top.nlrdev.payloadlib.receivers;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PayloadRawReceiver {
    void handleByteBuf(@NotNull Player sender, @NotNull ByteBuf byteBuf);
}
