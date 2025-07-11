package top.nlrdev.payloadlib.types;

import io.netty.buffer.ByteBuf;
import top.nlrdev.payloadlib.encoding.VarLongs;

public class VarLong extends Number {
    private final long value;

    public VarLong(long value) {
        this.value = value;
    }

    public static VarLong read(ByteBuf buf) {
        return new VarLong(VarLongs.read(buf));
    }

    public static ByteBuf write(ByteBuf buf, VarLong value) {
        return VarLongs.write(buf, value.longValue());
    }

    @Override
    public int intValue() {
        return (int) value;
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
