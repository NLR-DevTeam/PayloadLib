# PayloadLib

Easier approach for **Paper** servers to send & handle custom payloads, simple & powerful.

We support a wide range of types, please see [[Supported Data Types]](#supported-data-types).

> [!WARNING]
> CraftBukkit and Spigot servers are not supported, and this plugin is likely to crash when running on them.

## Importing

> [!NOTE]
> PayloadLib hasn't been published to the maven central yet, but you can use the snapshot repository.

To access the snapshot version of PayloadLib, please add this in your `build.gradle`:

```groovy
repositories {
  maven {
    name = 'Central Portal Snapshots'
    url = 'https://central.sonatype.com/repository/maven-snapshots/'
  }
}

dependencies {
    implementation 'top.nlrdev:payloadlib:0.0.1-SNAPSHOT'
    
    // Optional dependency, providing ByteBuf
    implementation 'io.netty:netty-buffer:4.2.2.Final'
}
```

<!--
Please add this in your `build.gradle` (remember to replace `<VERSION>` with the latest release):

```groovy
dependencies {
    // ...
    implementation 'top.nlrdev:payloadlib:<VERSION>'
}
```
-->

Embedding this plugin within other plugin JARs is not recommended, as it can lead to various issues.

## Supported Versions

Bukkit's API has an annoying limit, so we use NMS to send payloads directly.

Please notice that we do only support recent versions of Minecraft (excluding some versions that nobody cares).

| NMS Target | Compatible With            |
| ---------- | -------------------------- |
| 1.21.4     | 1.21.4, _1.21.5_, _1.21.7_ |
| 1.21.1     | _1.21_, 1.21.1             |
| 1.20.6     | _1.20.5_, 1.20.6           |
| 1.20.4     | _1.20.3_, 1.20.4           |
| 1.20.1     | _1.20_, 1.20.1             |

Note: _Italic_ means the version is **not fully tested**, but **may** be usable.

## Usage

First, add `PayloadLib` in the `plugin.yml` as an dependency:

```yml
# ...
depend: [PayloadLib]
```

Then, declare your payload like this:

```java
import top.nlrdev.payloadlib.Payload;
import top.nlrdev.payloadlib.types.Identifier;

public record MyPayload(int id, String data) implements Payload {
    public static final Identifier ID = Identifier.of("namespace", "path");
    // Or: public static final Identifier ID = Identifier.parse("namespace:path");

    @Override
    public Identifier getId() {
        return ID;
    }
}
```

Next, register your packets as follows:

> [!NOTE]
> Both C2S (Serverbound) and S2C (Clientbound) payloads must be registered for serialization.

```java
import top.nlrdev.payloadlib.PayloadLib;

// In your JavaPlugin implementation
@Override
public void onEnable() {
    // ...
    PayloadLib.registerPayload(MyPayload.ID, MyPayload.class);
}
```

If this is a C2S (Serverbound) payload, you can register a handler like this:

```java
PayloadLib.<MyPayload>registerGlobalReceiver(MyPayload.ID, (/* Bukkit Player */ sender, /* MyPayload */ payload) -> {
    sender.sendMessage("ID: %s, Data: %s".formatted(payload.id(), payload.data()));
});
```

Else, if this is a S2C (Clientbound) payload, you can send it in two methods:

```java
Payload payload = new MyPayload(1234, "some-data");
payload.sendTo(player1, player2, player3, ...);

// Or
PayloadLib.sendPayload(payload, player1, player2, player3, ...);
```

## Supported Data Types

> [!NOTE]
> We only support types with a `PacketCodec` in vanilla Minecraft.

Please refer to [SerializationImpl.java](/core/src/main/java/top/nlrdev/payloadlib/serialization/SerializationImpl.java) for more details.

### Primitive

| Primitive | Packaged  | Array         | Unsigned Implementation                     |
| --------- | --------- | ------------- | ------------------------------------------- |
| boolean   | Boolean   | _Unsupported_ | _None_                                      |
| byte      | Byte      | byte[]        | _None_                                      |
| short     | Short     | _Unsupported_ | `top.nlrdev.payloadlib.types.UnsignedShort` |
| char      | Character | _Unsupported_ | _None_                                      |
| int       | Integer   | _Unsupported_ | _None_                                      |
| long      | Long      | _Unsupported_ | _None_                                      |
| float     | Float     | _Unsupported_ | _None_                                      |
| double    | Double    | _Unsupported_ | _None_                                      |

### Non-Primitive

| Type                   | PayloadLib  |
| ---------------------- | ----------- |
| String                 | _Unchanged_ |
| UUID                   | _Unchanged_ |
| `org.joml.Vector3f`    | _Unchanged_ |
| `org.joml.Quaternionf` | _Unchanged_ |

| Minecraft (Official)                       | Minecraft (Yarn)                   | PayloadLib                               |
| ------------------------------------------ | ---------------------------------- | ---------------------------------------- |
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.util.Identifier`    | `top.nlrdev.payloadlib.types.Identifier` |
| `ByteBufCodecs#VAR_INT`                    | `PacketCodecs#VAR_INT`             | `top.nlrdev.payloadlib.types.VarInt`     |
| `ByteBufCodecs#VAR_LONG`                   | `PacketCodecs#VAR_LONG`            | `top.nlrdev.payloadlib.types.VarLong`    |
| `net.minecraft.world.phys.Vec3`            | `net.minecraft.util.math.Vec3d`    | `org.joml.Vector3d`                      |
| `net.minecraft.core.BlockPos`              | `net.minecraft.util.math.BlockPos` | `top.nlrdev.payloadlib.types.BlockPos`   |

## Advanced Usage

### Handling ByteBuf Directly

There's a high-level API called `registerRawReceiver`, here's its usage:

```java
import top.nlrdev.payloadlib.encoding.StringEncoding;

PayloadLib.registerRawReceiver(MyPayload.ID, (/* Bukkit Player */ sender, /* ByteBuf */ buf) -> {
    int id = buf.readInt();
    String data = StringEncoding.decode(buf, /* Max Length */ 65535);

    sender.sendMessage("ID: %s, Data: %s".formatted(id, data));
});
```

### Declaring Custom (De)Serializer

> [!WARNING]
> This is a dangerous operation, if your requirement is quite simple, please refer to [Registering Custom Data Type](#registering-custom-data-type).

You need to use annotations to implement custom serializer / deserialzer, here's an example.

The type is a Bukkit Player, and we will use its UUID for serialization.

```java
import io.netty.Buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.Payload;
import top.nlrdev.payloadlib.types.Identifier;
import top.nlrdev.payloadlib.serialization.PayloadSerializer;
import top.nlrdev.payloadlib.serialization.PayloadDeserializer;
import top.nlrdev.payloadlib.serialization.SerializationImpl;

import java.util.UUID;

public record MyPayloadTwo(Player player) implements Payload {
    public static final Identifier ID = Identifier.of("namespace", "path");

    @Override
    public Identifier getId() {
        return ID;
    }

    @PayloadSerializer
    public static ByteBuf serialize(MyPayloadTwo instance) {
        ByteBuf buf = Unpooled.buffer();
        SerializationImpl.getInternalSerializer(UUID.class).accept(buf, instance.player.getUniqueId());
        return buf;
    }

    @PayloadDeserializer
    public static MyPayloadTwo deserialize(ByteBuf buf) {
        UUID uuid = SerializationImpl.getInternalDeserializer(UUID.class).apply(buf);
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;

        return new MyPayloadTwo(player);
    }
}
```

### Registering Custom Data Type

Overwriting the (de)serializer is dangerous, and registering custom data type is more convenient. Here's an instance:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.nlrdev.payloadlib.serialization.SerializationImpl;

import java.util.UUID;

SerializationImpl.registerType(
    Player.class,
    /* Serializer */ (/* ByteBuf */ buf, player) -> SerializationImpl.getInternalSerializer(UUID.class).accept(buf, player.getUniqueId()),
    /* Deserializer */ buf -> {
        UUID uuid = SerializationImpl.getInternalDeserializer(UUID.class).apply(buf);
        return Bukkit.getPlayer(uuid);
    }
);
```

## Building

Thanks to paperweight-userdev, building this plugin will cost a lot of RAM. You need about `8 GiB` of free RAM to complete the whole compiling process.

To build, run:

```shell
./gradlew build --no-daemon
```

And you'll see the plugin JAR inside the folder `build/libs`.

## Contributing

We welcome your contributions!

Please feel free to open an Issue or a Pull Request.

## License

This mod is licensed under [MIT License](/LICENSE).
