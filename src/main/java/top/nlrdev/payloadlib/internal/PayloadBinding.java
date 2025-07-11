package top.nlrdev.payloadlib.internal;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.nlrdev.payloadlib.Payload;
import top.nlrdev.payloadlib.exceptions.MalformedPayloadException;
import top.nlrdev.payloadlib.serialization.PayloadDeserializer;
import top.nlrdev.payloadlib.serialization.PayloadSerializer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.Arrays;

public record PayloadBinding(Class<? extends Payload> type, VarHandle[] fieldHandles, MethodHandle constructor,
                             @Nullable MethodHandle customSerializer, @Nullable MethodHandle customDeserializer) {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static PayloadBinding create(@NotNull Class<? extends Payload> targetClass) throws MalformedPayloadException {
        MethodHandles.Lookup privateLookup;
        try {
            privateLookup = MethodHandles.privateLookupIn(targetClass, LOOKUP);
        } catch (IllegalAccessException ex) {
            throw new MalformedPayloadException("Failed to get Lookup for class: " + targetClass.getName(), ex);
        }

        // Create Fields VarHandle
        Field[] fields = Arrays.stream(targetClass.getDeclaredFields())
                .filter(it -> !Modifier.isTransient(it.getModifiers()) && !Modifier.isStatic(it.getModifiers()))
                .toArray(Field[]::new);

        Field field = null;
        VarHandle[] varHandles = new VarHandle[fields.length];
        try {
            for (int i = 0; i < fields.length; i++) {
                field = fields[i];
                varHandles[i] = privateLookup.unreflectVarHandle(field);
            }
        } catch (IllegalAccessException ex) {
            throw new MalformedPayloadException("Failed to get VarHandle for field: " + field.getName(), ex);
        }

        // Find custom serializers
        Method serializerMethod = null;
        Method deserializerMethod = null;
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PayloadSerializer.class)) {
                if (serializerMethod != null) {
                    throw new MalformedPayloadException("Found unexpected multiple custom serializers");
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new MalformedPayloadException("Custom serializer method \"" + method.getName() + "\" must be static");
                }

                if (method.getReturnType() != ByteBuf.class) {
                    throw new MalformedPayloadException("Custom serializer method \"" + method.getName() + "\" must return a ByteBuf");
                }

                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1 || paramTypes[0] != targetClass) {
                    throw new MalformedPayloadException("Custom serializer method \"" + method.getName() + "\" must accept a single parameter of type " + targetClass.getSimpleName());
                }

                serializerMethod = method;
                continue;
            }

            if (method.isAnnotationPresent(PayloadDeserializer.class)) {
                if (deserializerMethod != null) {
                    throw new MalformedPayloadException("Found unexpected multiple custom deserializers");
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new MalformedPayloadException("Custom deserializer method \"" + method.getName() + "\" must be static");
                }

                if (method.getReturnType() != targetClass) {
                    throw new MalformedPayloadException("Custom deserializer method \"" + method.getName() + "\" must return a " + targetClass.getSimpleName());
                }

                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1 || paramTypes[0] != ByteBuf.class) {
                    throw new MalformedPayloadException("Custom deserializer method \"" + method.getName() + "\" must accept a single parameter of type ByteBuf");
                }

                deserializerMethod = method;
            }
        }

        MethodHandle serializerHandle = null;
        if (serializerMethod != null) {
            try {
                serializerHandle = privateLookup.unreflect(serializerMethod);
            } catch (IllegalAccessException ex) {
                throw new MalformedPayloadException("Failed to get serializer MethodHandle for \"" + serializerMethod.getName() + "\"", ex);
            }
        }

        MethodHandle deserializerHandle = null;
        if (deserializerMethod != null) {
            try {
                deserializerHandle = privateLookup.unreflect(deserializerMethod);
            } catch (IllegalAccessException ex) {
                throw new MalformedPayloadException("Failed to get deserializer MethodHandle for \"" + deserializerMethod.getName() + "\"", ex);
            }
        }

        // Find a valid constructor
        Constructor<?> validConstructor = null;

        ctorLoop:
        for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length == fields.length) {
                for (int i = 0; i < fields.length; i++) {
                    if (parameters[i].getType() != fields[i].getType()) {
                        continue ctorLoop;
                    }
                }

                validConstructor = constructor;
                break;
            }
        }

        if (validConstructor == null) {
            throw new MalformedPayloadException("No valid constructor found for class " + targetClass.getName() + ", there should be a constructor parameterized with: " + Arrays.stream(fields)
                    .map(it -> it.getType().getName()).toList());
        }

        MethodHandle constructorHandle;
        try {
            constructorHandle = privateLookup.unreflectConstructor(validConstructor);
        } catch (IllegalAccessException ex) {
            throw new MalformedPayloadException("Failed to get constructor MethodLookup for \"" + validConstructor.getName() + "\"", ex);
        }

        return new PayloadBinding(targetClass, varHandles, constructorHandle, serializerHandle, deserializerHandle);
    }
}
