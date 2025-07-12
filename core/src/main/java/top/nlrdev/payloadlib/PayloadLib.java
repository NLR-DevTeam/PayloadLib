package top.nlrdev.payloadlib;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.nlrdev.payloadlib.internal.PayloadBinding;
import top.nlrdev.payloadlib.internal.PluginInitializer;
import top.nlrdev.payloadlib.internal.nms.NMSBindings;
import top.nlrdev.payloadlib.receivers.PayloadGlobalReceiver;
import top.nlrdev.payloadlib.receivers.PayloadRawReceiver;
import top.nlrdev.payloadlib.serialization.SerializationImpl;
import top.nlrdev.payloadlib.types.Identifier;

import java.util.HashMap;

/**
 * The most used utility class, which allows you to register, listen and send a payload.
 */
public class PayloadLib {
    private static final Messenger MESSENGER = Bukkit.getMessenger();
    private static final HashMap<Identifier, PayloadBinding> PAYLOAD_BINDINGS = new HashMap<>();
    private static final Multimap<Identifier, PayloadGlobalReceiver<?>> PAYLOAD_GLOBAL_RECEIVERS = HashMultimap.create();
    private static final Multimap<Identifier, PayloadRawReceiver> PAYLOAD_RAW_RECEIVERS = HashMultimap.create();

    public static void registerPayload(@NotNull Identifier id, @NotNull Class<? extends Payload> payloadType) {
        if (PAYLOAD_BINDINGS.containsKey(id)) {
            // Silent fail
            if (PAYLOAD_BINDINGS.get(id).type() == payloadType) {
                return;
            }

            throw new IllegalStateException("Another payload has already been registered with identifier " + id);
        }

        PAYLOAD_BINDINGS.put(id, PayloadBinding.create(payloadType));
    }

    @Nullable
    public static PayloadBinding internalBindingById(@NotNull Identifier id) {
        return PAYLOAD_BINDINGS.get(id);
    }

    public static <T extends Payload> void registerGlobalReceiver(@NotNull Identifier id, @NotNull PayloadGlobalReceiver<T> receiver) {
        // Silent fail
        if (PAYLOAD_GLOBAL_RECEIVERS.containsKey(id) && PAYLOAD_GLOBAL_RECEIVERS.get(id).contains(receiver)) {
            return;
        }

        PAYLOAD_GLOBAL_RECEIVERS.put(id, receiver);

        // Submit to bukkit
        MESSENGER.registerIncomingPluginChannel(PluginInitializer.getInstance(), id.toString(), (_channel, player, message) -> {
            T payload = SerializationImpl.deserialize(id, Unpooled.wrappedBuffer(message));
            receiver.handlePayload(player, payload);
        });
    }

    public static void registerRawReceiver(@NotNull Identifier id, @NotNull PayloadRawReceiver receiver) {
        // Silent fail
        if (PAYLOAD_RAW_RECEIVERS.containsKey(id) && PAYLOAD_RAW_RECEIVERS.get(id).contains(receiver)) {
            return;
        }

        PAYLOAD_RAW_RECEIVERS.put(id, receiver);

        // Submit to bukkit
        MESSENGER.registerIncomingPluginChannel(PluginInitializer.getInstance(), id.toString(), (_channel, player, message) -> receiver.handleByteBuf(player, Unpooled.wrappedBuffer(message)));
    }

    public static void sendPayload(@NotNull Payload payload, @NotNull Player... players) {
        ByteBuf serialized = SerializationImpl.serialize(payload);
        byte[] data = serialized.slice(0, serialized.readableBytes()).array();

        for (Player player : players) {
            NMSBindings.get().sendPayload(player, payload.getId(), data);
        }
    }
}
