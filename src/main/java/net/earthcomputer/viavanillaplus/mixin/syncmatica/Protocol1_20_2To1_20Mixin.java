package net.earthcomputer.viavanillaplus.mixin.syncmatica;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage.ConfigurationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Protocol1_20_2To1_20.class, remap = false)
public class Protocol1_20_2To1_20Mixin extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_20_2, ServerboundPackets1_19_4, ServerboundPackets1_20_2> {
    @SuppressWarnings("deprecation")
    public Protocol1_20_2To1_20Mixin() {
    }

    @Inject(method = "transform", at = @At(value = "FIELD", target = "Lcom/viaversion/viaversion/protocols/protocol1_20_2to1_20/packet/ClientboundConfigurationPackets1_20_2;CUSTOM_PAYLOAD:Lcom/viaversion/viaversion/protocols/protocol1_20_2to1_20/packet/ClientboundConfigurationPackets1_20_2;"))
    private void onClientboundPluginMessage(Direction direction, State state, PacketWrapper wrapper, CallbackInfo ci) throws Exception {
        String channel = wrapper.passthrough(Type.STRING);
        if (channel.startsWith("syncmatica:")) {
            wrapper.user().get(ConfigurationState.class).addPacketToQueue(wrapper, true);
            throw CancelException.generate();
        }
    }
}
