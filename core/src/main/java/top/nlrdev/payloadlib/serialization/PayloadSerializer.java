package top.nlrdev.payloadlib.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a serializer method.
 * <br><br>
 * The method must:
 * <ul>
 *     <li>Be static</li>
 *     <li>Accept a single parameter of the class' instance</li>
 *     <li>Return a {@link io.netty.buffer.ByteBuf}</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PayloadSerializer {
}
