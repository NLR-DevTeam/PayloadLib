package top.nlrdev.payloadlib.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import top.nlrdev.payloadlib.Payload;
import top.nlrdev.payloadlib.PayloadLib;
import top.nlrdev.payloadlib.serialization.encoding.*;
import top.nlrdev.payloadlib.exceptions.SerializationException;
import top.nlrdev.payloadlib.exceptions.UnsupportedTypeException;
import top.nlrdev.payloadlib.internal.PayloadBinding;
import top.nlrdev.payloadlib.internal.TypeBinding;
import top.nlrdev.payloadlib.types.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SerializationImpl {
    private static final HashMap<Class<?>, BiConsumer<ByteBuf, ?>> INTERNAL_SERIALIZERS = new HashMap<>();
    private static final HashMap<Class<?>, Function<ByteBuf, ?>> INTERNAL_DESERIALIZERS = new HashMap<>();
    private static final ArrayList<TypeBinding<?>> TYPE_BINDINGS = new ArrayList<>();

    // TODO: Collection, Map, Optional, Nullable, Bit Set, Enum Set, NBT(WTF?), Registry,
    // TODO: BlockHitResult
    static {
        // Primitive Types
        registerInternal(ByteBuf::writeBoolean, ByteBuf::readBoolean, boolean.class, Boolean.class);
        registerInternal(ByteBuf::writeInt, ByteBuf::readInt, int.class, Integer.class);
        registerInternal(ByteBuf::writeLong, ByteBuf::readLong, long.class, Long.class);
        registerInternal(ByteBuf::writeFloat, ByteBuf::readFloat, float.class, Float.class);
        registerInternal(ByteBuf::writeDouble, ByteBuf::readDouble, double.class, Double.class);
        registerInternal((buf, o) -> buf.writeByte(o), ByteBuf::readByte, byte.class, Byte.class);
        registerInternal((buf, o) -> buf.writeShort(o), ByteBuf::readShort, short.class, Short.class);
        registerInternal((buf, o) -> buf.writeChar(o), ByteBuf::readChar, char.class, Character.class);

        // Unsigned Primitive
        registerInternal((buf, o) -> buf.writeShort(o.intValue()), buf -> new UnsignedShort(buf.readUnsignedShort()), UnsignedShort.class);

        // Byte Array
        registerInternal((buf, o) -> {
            VarIntEncoding.encode(buf, new VarInt(o.length));
            buf.writeBytes(o);
        }, buf -> {
            byte[] array = new byte[VarIntEncoding.decode(buf).intValue()];
            buf.readBytes(array);
            return array;
        }, byte[].class, Byte[].class);

        // Var Int / Long
        registerInternal(VarIntEncoding::encode, VarIntEncoding::decode, VarInt.class);
        registerInternal(VarLongEncoding::encode, VarLongEncoding::decode, VarLong.class);

        // Non-primitive Types
        registerInternal((buf, o) -> StringEncoding.encode(buf, o, 32767), buf -> StringEncoding.decode(buf, 32767), String.class);
        registerInternal(IdentifierEncoding::encode, IdentifierEncoding::decode, Identifier.class);
        registerInternal(LocationEncoding::encode, LocationEncoding::decode, Location.class);

        registerInternal((buf, o) -> buf.writeLong(o.getMostSignificantBits())
                .writeLong(o.getLeastSignificantBits()), buf -> new UUID(buf.readLong(), buf.readLong()), UUID.class);
        registerInternal((buf, o) -> buf.writeFloat(o.x()).writeFloat(o.y())
                .writeFloat(o.z()), buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()), Vector3f.class);
        /* Bukkit doesn't have Vec3d, using joml instead. */
        registerInternal((buf, o) -> buf.writeDouble(o.x()).writeDouble(o.y())
                .writeDouble(o.z()), buf -> new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), Vector3d.class);
        registerInternal((buf, o) -> buf.writeFloat(o.x()).writeFloat(o.y()).writeFloat(o.z())
                .writeFloat(o.w()), buf -> new Quaternionf(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()), Quaternionf.class);
        registerInternal((buf, o) -> buf.writeLong(o.asLong()), buf -> BlockPos.fromLong(buf.readLong()), BlockPos.class);
    }

    private static <T> void registerInternal(BiConsumer<ByteBuf, T> serializer, Function<ByteBuf, T> deserializer, Class<?>... types) {
        for (Class<?> type : types) {
            INTERNAL_SERIALIZERS.put(type, serializer);
            INTERNAL_DESERIALIZERS.put(type, deserializer);
        }
    }

    @SuppressWarnings("unchecked")
    public static ByteBuf serialize(@NotNull Payload payload) throws UnsupportedTypeException, IllegalStateException, SerializationException {
        PayloadBinding binding = PayloadLib.internalBindingById(payload.getId());
        if (binding == null) {
            throw new IllegalStateException("Payload is not registered");
        }

        MethodHandle customSerializer = binding.customSerializer();
        if (customSerializer != null) {
            try {
                return (ByteBuf) customSerializer.invoke(payload);
            } catch (Throwable ex) {
                throw new SerializationException("Failed to invoke custom serializer", ex);
            }
        }

        ByteBuf buf = Unpooled.buffer();
        fieldLoop:
        for (VarHandle handle : binding.fieldHandles()) {
            Class<?> type = handle.varType();
            if (INTERNAL_SERIALIZERS.containsKey(type)) {
                ((BiConsumer<ByteBuf, Object>) INTERNAL_SERIALIZERS.get(type)).accept(buf, handle.get(payload));
                continue;
            }

            for (TypeBinding<?> typeBinding : TYPE_BINDINGS) {
                if (type == typeBinding.type()) {
                    ((BiConsumer<ByteBuf, Object>) typeBinding.serializer()).accept(buf, handle.get(payload));
                    continue fieldLoop;
                }
            }

            throw new UnsupportedTypeException(type.getName());
        }

        return buf;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Payload> T deserialize(@NotNull Identifier id, @NotNull ByteBuf buf) throws UnsupportedTypeException, IllegalStateException, SerializationException {
        PayloadBinding binding = PayloadLib.internalBindingById(id);
        if (binding == null) {
            throw new IllegalStateException("Payload is not registered");
        }

        Class<?> type = binding.type();

        MethodHandle customDeserializer = binding.customDeserializer();
        if (customDeserializer != null) {
            try {
                return (T) customDeserializer.invoke(buf);
            } catch (Throwable ex) {
                throw new SerializationException("Failed to invoke custom deserializer", ex);
            }
        }

        VarHandle[] handles = binding.fieldHandles();
        Object[] deserialized = new Object[handles.length];
        fieldLoop:
        for (int i = 0; i < handles.length; i++) {
            VarHandle handle = handles[i];

            Class<?> varType = handle.varType();
            if (INTERNAL_DESERIALIZERS.containsKey(varType)) {
                deserialized[i] = INTERNAL_DESERIALIZERS.get(varType).apply(buf);
                continue;
            }

            for (TypeBinding<?> typeBinding : TYPE_BINDINGS) {
                if (varType == typeBinding.type()) {
                    deserialized[i] = typeBinding.deserializer().apply(buf);
                    continue fieldLoop;
                }
            }

            throw new UnsupportedTypeException(varType.getName());
        }

        try {
            return (T) binding.constructor().invokeWithArguments(deserialized);
        } catch (Throwable ex) {
            throw new SerializationException("Failed to construct payload " + type.getSimpleName(), ex);
        }
    }

    public static <T> void registerType(Class<T> type, @NotNull BiConsumer<ByteBuf, T> serializer, @NotNull Function<ByteBuf, T> deserializer) {
        TYPE_BINDINGS.removeIf(binding -> binding.type() == type);
        TYPE_BINDINGS.add(new TypeBinding<>(type, serializer, deserializer));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> BiConsumer<ByteBuf, T> getInternalSerializer(Class<T> type) {
        return (BiConsumer<ByteBuf, T>) INTERNAL_SERIALIZERS.get(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Function<ByteBuf, T> getInternalDeserializer(Class<T> type) {
        return (Function<ByteBuf, T>) INTERNAL_DESERIALIZERS.get(type);
    }
}
