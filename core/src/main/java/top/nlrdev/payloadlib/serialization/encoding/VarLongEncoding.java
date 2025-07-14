package top.nlrdev.payloadlib.serialization.encoding;

import io.netty.buffer.ByteBuf;
import top.nlrdev.payloadlib.types.VarLong;

/**
 * VarLong r/w utilities extracted from Minecraft, rewritten, as the same as {@link VarIntEncoding}.
 */
public final class VarLongEncoding {
    private static final int MAX_BYTES = 10;
    private static final int DATA_BITS_MASK = 127;
    private static final int MORE_BITS_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static VarLong decode(ByteBuf buf) {
        long result = 0L;
        int position = 0;
        byte currentByte;

        do {
            currentByte = buf.readByte();
            result |= (long) (currentByte & DATA_BITS_MASK) << position;

            if (position >= DATA_BITS_PER_BYTE * MAX_BYTES) {
                throw new RuntimeException("VarLong is too big");
            }

            position += DATA_BITS_PER_BYTE;
        } while ((currentByte & MORE_BITS_MASK) == MORE_BITS_MASK);

        return new VarLong(result);
    }

    public static void encode(ByteBuf buf, VarLong varLong) {
        long value = varLong.longValue();
        while (true) {
            if ((value & ~DATA_BITS_MASK) == 0L) {
                buf.writeByte((int) value);
                return;
            } else {
                buf.writeByte((int) (value & DATA_BITS_MASK) | MORE_BITS_MASK);
                value >>>= DATA_BITS_PER_BYTE;
            }
        }
    }
}
