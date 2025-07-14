package top.nlrdev.payloadlib.serialization.encoding;

import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import top.nlrdev.payloadlib.internal.nms.NMSBindings;
import top.nlrdev.payloadlib.serialization.SerializationImpl;
import top.nlrdev.payloadlib.types.BlockPos;
import top.nlrdev.payloadlib.types.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LocationEncoding {
    private static final HashMap<World, Identifier> WORLD_REGISTRY_CACHE = new HashMap<>();
    private static final Function<ByteBuf, Identifier> IDENTIFIER_DESERIALIZER = Objects.requireNonNull(SerializationImpl.getInternalDeserializer(Identifier.class));
    private static final Function<ByteBuf, BlockPos> BLOCK_POS_DESERIALIZER = Objects.requireNonNull(SerializationImpl.getInternalDeserializer(BlockPos.class));
    private static final BiConsumer<ByteBuf, Identifier> IDENTIFIER_SERIALIZER = Objects.requireNonNull(SerializationImpl.getInternalSerializer(Identifier.class));
    private static final BiConsumer<ByteBuf, BlockPos> BLOCK_POS_SERIALIZER = Objects.requireNonNull(SerializationImpl.getInternalSerializer(BlockPos.class));

    /**
     * Returns null if the world requested doesn't exist.
     */
    @Nullable
    public static Location decode(ByteBuf buf) {
        initWorlds();

        Identifier registryValue = IDENTIFIER_DESERIALIZER.apply(buf);
        World target = null;
        for (Map.Entry<World, Identifier> entry : WORLD_REGISTRY_CACHE.entrySet()) {
            if (entry.getValue().equals(registryValue)) {
                target = entry.getKey();
            }
        }

        if (target == null) {
            return null;
        }

        BlockPos blockPos = BLOCK_POS_DESERIALIZER.apply(buf);
        return new Location(target, blockPos.x, blockPos.y, blockPos.z);
    }

    public static void encode(ByteBuf buf, Location location) {
        initWorlds();

        IDENTIFIER_SERIALIZER.accept(buf, WORLD_REGISTRY_CACHE.get(location.getWorld()));
        BLOCK_POS_SERIALIZER.accept(buf, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    private static void initWorlds() {
        for (World world : Bukkit.getWorlds()) {
            WORLD_REGISTRY_CACHE.computeIfAbsent(world, it -> NMSBindings.get().getWorldIdentifier(it));
        }
    }
}
