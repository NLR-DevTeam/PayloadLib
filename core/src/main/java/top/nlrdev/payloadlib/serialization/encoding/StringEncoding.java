package top.nlrdev.payloadlib.serialization.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;

/**
 * String encoding utilities extracted from Minecraft, rewritten.
 */
public final class StringEncoding {
    public static String decode(ByteBuf buf, int maxLength) {
        int encodedByteLength = VarIntEncoding.decode(buf).intValue();
        if (encodedByteLength < 0) {
            throw new RuntimeException("The received encoded string buffer length is less than zero! Weird string!");
        }

        int maxAllowedByteLength = ByteBufUtil.utf8MaxBytes(maxLength);
        if (encodedByteLength > maxAllowedByteLength) {
            throw new RuntimeException("The received encoded string buffer length is longer than maximum allowed (" + encodedByteLength + " > " + maxAllowedByteLength + ")");
        }

        int readableBytes = buf.readableBytes();
        if (encodedByteLength > readableBytes) {
            throw new RuntimeException("Not enough bytes in buffer, expected " + encodedByteLength + ", but got " + readableBytes);
        }

        String result = buf.toString(buf.readerIndex(), encodedByteLength, StandardCharsets.UTF_8);
        buf.skipBytes(encodedByteLength);

        if (result.length() > maxLength) {
            throw new RuntimeException("The received string length is longer than maximum allowed (" + result.length() + " > " + maxLength + ")");
        }

        return result;
    }

    public static void encode(ByteBuf buf, CharSequence string, int maxLength) {
        if (string.length() > maxLength) {
            throw new RuntimeException("String too big (was " + string.length() + " characters, max " + maxLength + ")");
        }

        ByteBuf tempBuffer = buf.alloc().buffer(ByteBufUtil.utf8MaxBytes(string));
        try {
            int actualByteLength = ByteBufUtil.writeUtf8(tempBuffer, string);
            int maxAllowedByteLength = ByteBufUtil.utf8MaxBytes(maxLength);
            if (actualByteLength > maxAllowedByteLength) {
                throw new RuntimeException("String too big (was " + actualByteLength + " bytes encoded, max " + maxAllowedByteLength + ")");
            }

            VarIntEncoding.encode(buf, actualByteLength);
            buf.writeBytes(tempBuffer);
        } finally {
            tempBuffer.release();
        }
    }
}
