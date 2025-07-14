package top.nlrdev.payloadlib.serialization.encoding;

import io.netty.buffer.ByteBuf;
import top.nlrdev.payloadlib.types.VarInt;

/**
 * VarInt r/w utilities extracted from Minecraft, rewritten.
 */
public final class VarIntEncoding {
    private static final int MAX_BYTES = 5;
    private static final int DATA_BITS_MASK = 127;
    private static final int MORE_BITS_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static VarInt decode(ByteBuf buf) {
        int result = 0;
        int position = 0;
        byte currentByte;

        do {
            currentByte = buf.readByte();
            result |= (currentByte & DATA_BITS_MASK) << position;

            if (position >= DATA_BITS_PER_BYTE * MAX_BYTES) {
                throw new RuntimeException("VarInt is too big");
            }

            position += DATA_BITS_PER_BYTE;
        } while ((currentByte & MORE_BITS_MASK) == MORE_BITS_MASK);

        return new VarInt(result);
    }

    public static void encode(ByteBuf buf, VarInt varInt) {
        int value = varInt.intValue();
        while (true) {
            if ((value & ~DATA_BITS_MASK) == 0) {
                buf.writeByte(value);
                return;
            } else {
                buf.writeByte((value & DATA_BITS_MASK) | MORE_BITS_MASK);
                value >>>= DATA_BITS_PER_BYTE;
            }
        }
    }
}
