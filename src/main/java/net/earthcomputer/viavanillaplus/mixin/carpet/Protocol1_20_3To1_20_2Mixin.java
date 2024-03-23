package net.earthcomputer.viavanillaplus.mixin.carpet;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPacket1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.Protocol1_20_3To1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.util.Key;
import net.earthcomputer.viavanillaplus.carpet.TickRateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Protocol1_20_3To1_20_2.class, remap = false)
public class Protocol1_20_3To1_20_2Mixin extends AbstractProtocol<ClientboundPacket1_20_2, ClientboundPacket1_20_3, ServerboundPacket1_20_2, ServerboundPacket1_20_3> {
    @SuppressWarnings("deprecation")
    public Protocol1_20_3To1_20_2Mixin() {
    }

    @Inject(method = "registerPackets", at = @At("RETURN"))
    private void registerExtraPackets(CallbackInfo ci) {
        appendClientbound(ClientboundPackets1_20_2.PLUGIN_MESSAGE, wrapper -> {
            wrapper.resetReader();
            String channel = Key.namespaced(wrapper.passthrough(Type.STRING));
            if ("carpet:hello".equals(channel)) {
                CompoundTag data = wrapper.read(Type.COMPOUND_TAG);

                Float tickRate = null;
                Tag tickRateTag = data.remove("TickRate");
                if (tickRateTag instanceof NumberTag tickRateNumberTag) {
                    tickRate = tickRateNumberTag.asFloat();
                }

                Boolean isTickFrozen = null;
                Tag tickingStateTag = data.remove("TickingState");
                if (tickingStateTag instanceof CompoundTag tickingStateCompound) {
                    NumberTag isTickFrozenNumber = tickingStateCompound.getNumberTag("is_frozen");
                    if (isTickFrozenNumber != null) {
                        isTickFrozen = isTickFrozenNumber.asBoolean();
                    }
                }

                if (tickRate != null || isTickFrozen != null) {
                    TickRateState tickRateState = wrapper.user().get(TickRateState.class);
                    assert tickRateState != null;
                    if (tickRate == null) {
                        tickRate = tickRateState.tickRate;
                    } else {
                        tickRateState.tickRate = tickRate;
                    }
                    if (isTickFrozen == null) {
                        isTickFrozen = tickRateState.isFrozen;
                    } else {
                        tickRateState.isFrozen = isTickFrozen;
                    }
                    System.out.println("Sending " + tickRate + ", " + isTickFrozen);
                    PacketWrapper tickStatePacket = wrapper.create(ClientboundPackets1_20_3.TICKING_STATE);
                    tickStatePacket.write(Type.FLOAT, tickRate);
                    tickStatePacket.write(Type.BOOLEAN, isTickFrozen);
                    tickStatePacket.send(Protocol1_20_3To1_20_2.class);
                }

                data.remove("SuperHotState");
                data.remove("TickPlayerActiveTimeout");
                if (data.isEmpty()) {
                    wrapper.cancel();
                } else {
                    wrapper.write(Type.COMPOUND_TAG, data);
                }
            }
        });
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(UserConnection connection, CallbackInfo ci) {
        connection.put(new TickRateState());
    }
}
