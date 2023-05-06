package ashondiscord.hud.mixin.client;

import ashondiscord.hud.ExampleModClient;
import ashondiscord.hud.util.Combo;
import ashondiscord.hud.util.Hit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ashondiscord.hud.ExampleModClient.comboCounter;
import static ashondiscord.hud.ExampleModClient.hitDistances;
import static ashondiscord.hud.util.DistanceCalculator.getAttackDistance;

@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
    @Inject(
            method = {"send(Lnet/minecraft/network/Packet;)V"},
            at = {@At("HEAD")}
    )
    private void onPacketSend(Packet<?> packet, CallbackInfo info) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (packet instanceof PlayerInteractEntityC2SPacket interactPacket) {
            interactPacket.handle(new PlayerInteractEntityC2SPacket.Handler() {
                public void interact(Hand hand) {
                }

                public void interactAt(Hand hand, Vec3d pos) {
                }

                public void attack() {
                    HitResult hitResult = mc.crosshairTarget;
                    if (hitResult != null) {
                        if (hitResult.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            Entity entity = entityHitResult.getEntity();
                            assert MinecraftClient.getInstance().player != null;
                            Entity player = MinecraftClient.getInstance().player;
                            double reach = getAttackDistance(player, entity);

                            final long time = System.currentTimeMillis();

                            if (hitDistances.size() >= 1) {
                                hitDistances.set(0, new Hit(reach, time));
                            } else {
                                hitDistances.add(new Hit(reach, time));
                            }

                            // distance is number of hits in a row
                            // get id of the entity and check if it's the same as the last one
                            // if it is, increment the counter

                            int id = entity.getId();

                            if (comboCounter.size() >= 1) {
                                if (comboCounter.get(0).id == id) {
                                    comboCounter.get(0).count++;
                                } else {
                                    comboCounter.set(0, new Combo(1, id, time));
                                }
                            } else {
                                comboCounter.add(new Combo(1, id, time));
                            }
                        }
                    }
                }
            });
        }
    }
}