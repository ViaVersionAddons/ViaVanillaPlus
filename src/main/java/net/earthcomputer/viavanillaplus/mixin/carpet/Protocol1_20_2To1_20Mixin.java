package net.earthcomputer.viavanillaplus.mixin.carpet;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Protocol1_20_2To1_20.class, remap = false)
public class Protocol1_20_2To1_20Mixin extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_20_2, ServerboundPackets1_19_4, ServerboundPackets1_20_2> {
    @SuppressWarnings("deprecation")
    public Protocol1_20_2To1_20Mixin() {
    }

    @Inject(method = "lambda$registerPackets$0", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onClientboundPluginMessage(PacketWrapper wrapper, CallbackInfo ci, String channel) throws Exception {
        if (channel.equals("carpet:hello")) {
            int command = wrapper.read(Type.VAR_INT);
            if (command == 69) { // hi
                String version = wrapper.read(Type.STRING);
                CompoundTag dataTag = new CompoundTag();
                dataTag.put("69", new StringTag(version));
                wrapper.write(Type.COMPOUND_TAG, dataTag);
            } else if (command == 1) {
                wrapper.write(Type.COMPOUND_TAG, wrapper.read(Type.NAMED_COMPOUND_TAG));
            } else {
                wrapper.cancel();
            }
        }
    }

    @Inject(method = "lambda$registerPackets$1", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onServerboundPluginMessage(PacketWrapper wrapper, CallbackInfo ci, String channel) throws Exception {
        if (channel.equals("carpet:hello")) {
            CompoundTag dataTag = wrapper.read(Type.COMPOUND_TAG);
            Tag hello = dataTag.remove("420");
            if (hello instanceof StringTag versionTag) {
                PacketWrapper newPacket = PacketWrapper.create(ServerboundPackets1_19_4.PLUGIN_MESSAGE, wrapper.user());
                newPacket.write(Type.STRING, "carpet:hello");
                newPacket.write(Type.VAR_INT, 420); // hello
                newPacket.write(Type.STRING, versionTag.getValue());
                newPacket.sendToServer(Protocol1_20_2To1_20.class);
            }
            if (dataTag.isEmpty()) {
                wrapper.cancel();
            } else {
                wrapper.write(Type.VAR_INT, 1); // data
                wrapper.write(Type.NAMED_COMPOUND_TAG, dataTag);
            }
        }
    }
}
