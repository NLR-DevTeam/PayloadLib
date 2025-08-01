package top.nlrdev.payloadlib.internal.nms;

import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.types.Identifier;

public class NMSBindingV1_21_1 implements NMSBinding {
    @Override
    public void sendPayload(Player player, Identifier id, byte[] data) {
        ResourceLocation identifier = ResourceLocation.parse(id.toString());
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(identifier, Unpooled.wrappedBuffer(data))));
    }

    @Override
    public Identifier getWorldIdentifier(World world) {
        ResourceLocation dimensionId = ((CraftWorld) world).getHandle().dimension().location();
        return Identifier.of(dimensionId.getNamespace(), dimensionId.getPath());
    }
}
