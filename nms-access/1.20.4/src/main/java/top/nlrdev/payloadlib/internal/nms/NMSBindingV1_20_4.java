package top.nlrdev.payloadlib.internal.nms;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.nlrdev.payloadlib.types.Identifier;

public class NMSBindingV1_20_4 implements NMSBinding {
    @Override
    public void sendPayload(Player player, Identifier id, byte[] data) {
        ResourceLocation identifier = new ResourceLocation(id.toString());
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundCustomPayloadPacket(new CustomPacketPayload() {
            @Override
            public void write(@NotNull FriendlyByteBuf buf) {
                buf.writeBytes(data);
            }

            @Override
            public @NotNull ResourceLocation id() {
                return identifier;
            }
        }));
    }

    @Override
    public Identifier getWorldIdentifier(World world) {
        ResourceLocation dimensionId = ((CraftWorld) world).getHandle().dimension().location();
        return Identifier.of(dimensionId.getNamespace(), dimensionId.getPath());
    }
}
