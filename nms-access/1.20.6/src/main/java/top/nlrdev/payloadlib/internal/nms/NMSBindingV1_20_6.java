package top.nlrdev.payloadlib.internal.nms;

import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.types.Identifier;

public class NMSBindingV1_20_6 implements NMSBinding {
    @Override
    public void sendPayload(Player player, Identifier id, byte[] data) {
        ResourceLocation identifier = new ResourceLocation(id.toString());
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(identifier, Unpooled.wrappedBuffer(data))));
    }
}
