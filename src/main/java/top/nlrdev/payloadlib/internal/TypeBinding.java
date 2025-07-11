package top.nlrdev.payloadlib.internal;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record TypeBinding<T>(Class<T> type, @NotNull BiConsumer<ByteBuf, T> serializer,
                             @NotNull Function<ByteBuf, T> deserializer) {}
