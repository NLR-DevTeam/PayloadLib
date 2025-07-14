package top.nlrdev.payloadlib.internal.nms;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.types.Identifier;

public class NMSBindingV1_20_1 implements NMSBinding {
    @Override
    public void sendPayload(Player player, Identifier id, byte[] data) {
        ResourceLocation identifier = new ResourceLocation(id.toString());
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundCustomPayloadPacket(identifier, new FriendlyByteBuf(Unpooled.wrappedBuffer(data))));
    }

    @Override
    public Identifier getWorldIdentifier(World world) {
        ResourceLocation dimensionId = ((CraftWorld) world).getHandle().dimension().location();
        return Identifier.of(dimensionId.getNamespace(), dimensionId.getPath());
    }
}
