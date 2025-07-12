package top.nlrdev.payloadlib.types;

import org.joml.Vector3i;

/**
 * BlockPos parser extracted from Minecraft.
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class BlockPos extends Vector3i {
    public static final int SIZE_BITS_XZ = 26; // Pre-evaluated: 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000))
    public static final int SIZE_BITS_Y = 64 - 2 * SIZE_BITS_XZ;
    private static final long BITS_Y = (1L << SIZE_BITS_Y) - 1L;
    private static final int BIT_SHIFT_Z = SIZE_BITS_Y;
    private static final int BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_XZ;
    private static final long BITS_X = (1L << SIZE_BITS_XZ) - 1L;
    private static final long BITS_Z = (1L << SIZE_BITS_XZ) - 1L;

    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public static BlockPos fromLong(long packedPos) {
        return new BlockPos((int) (packedPos << 64 - BIT_SHIFT_X - SIZE_BITS_XZ >> 64 - SIZE_BITS_XZ), (int) (packedPos << 64 - SIZE_BITS_Y >> 64 - SIZE_BITS_Y), (int) (packedPos << 64 - BIT_SHIFT_Z - SIZE_BITS_XZ >> 64 - SIZE_BITS_XZ));
    }

    public static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= (x & BITS_X) << BIT_SHIFT_X;
        l |= (y & BITS_Y);
        return l | (z & BITS_Z) << BIT_SHIFT_Z;
    }

    public long asLong() {
        return asLong(x, y, z);
    }
}
