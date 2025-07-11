package top.nlrdev.payloadlib.types;

import io.netty.buffer.ByteBuf;
import top.nlrdev.payloadlib.encoding.VarInts;

public class VarInt extends Number {
    private final int value;

    public VarInt(int value) {
        this.value = value;
    }

    public static VarInt read(ByteBuf buf) {
        return new VarInt(VarInts.read(buf));
    }

    public static ByteBuf write(ByteBuf buf, VarInt value) {
        return VarInts.write(buf, value.intValue());
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}
