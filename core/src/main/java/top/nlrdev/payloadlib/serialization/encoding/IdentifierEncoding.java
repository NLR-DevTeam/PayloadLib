package top.nlrdev.payloadlib.serialization.encoding;

import io.netty.buffer.ByteBuf;
import top.nlrdev.payloadlib.serialization.SerializationImpl;
import top.nlrdev.payloadlib.types.Identifier;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class IdentifierEncoding {
    private static final BiConsumer<ByteBuf, String> STRING_SERIALIZER = Objects.requireNonNull(SerializationImpl.getInternalSerializer(String.class));
    private static final Function<ByteBuf, String> STRING_DESERIALIZER = Objects.requireNonNull(SerializationImpl.getInternalDeserializer(String.class));

    public static Identifier decode(ByteBuf buf) {
        return Identifier.parse(STRING_DESERIALIZER.apply(buf));
    }

    public static void encode(ByteBuf buf, Identifier id) {
        STRING_SERIALIZER.accept(buf, id.toString());
    }
}
