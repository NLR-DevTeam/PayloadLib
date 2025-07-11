package top.nlrdev.payloadlib.encoding;

import io.netty.buffer.ByteBuf;

/**
 * VarLong r/w utilities extracted from Minecraft, rewritten, as the same as {@link VarInts}.
 */
public class VarLongs {
    private static final int MAX_BYTES = 10;
    private static final int DATA_BITS_MASK = 127;
    private static final int MORE_BITS_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static long read(ByteBuf buf) {
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

        return result;
    }

    public static ByteBuf write(ByteBuf buf, long value) {
        while (true) {
            if ((value & ~DATA_BITS_MASK) == 0L) {
                buf.writeByte((int) value);
                return buf;
            } else {
                buf.writeByte((int) (value & DATA_BITS_MASK) | MORE_BITS_MASK);
                value >>>= DATA_BITS_PER_BYTE;
            }
        }
    }
}
