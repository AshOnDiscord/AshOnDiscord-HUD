package ashondiscord.hud.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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

import static ashondiscord.hud.ExampleModClient.LOGGER;

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
//                        if (hitResult.getType() == HitResult.Type.ENTITY) {
//                            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
//                            Entity entity = entityHitResult.getEntity();
//
//                            // get client camera angle
//                            assert MinecraftClient.getInstance().player != null;
//                            ClientPlayerEntity player = MinecraftClient.getInstance().player;
//                            float yaw = player.getYaw();
//                            float pitch = player.getPitch();
//
//                            double playerX = player.getX();
//                            double playerY = player.getY();
//                            double playerZ = player.getZ();
//
//                            double entityX = entity.getX();
//                            double entityY = entity.getY();
//                            double entityZ = entity.getZ();
//
//                            // use the camera angle and entity position to calculate the distance
//                            double distance = Math.sqrt(Math.pow(entityX - playerX, 2) + Math.pow(entityY - playerY, 2) + Math.pow(entityZ - playerZ, 2));
//
//                            // log the distance
//                            LOGGER.info("Distance: " + distance);
//                        }
                        if (hitResult.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            Entity entity = entityHitResult.getEntity();

                            // Get the hitbox size of the entity
                            double entityWidth = entity.getWidth();
                            double entityHeight = entity.getHeight();

                            // Get the center of the entity's hitbox
                            double entityCenterX = entity.getX() + entityWidth / 2;
                            double entityCenterY = entity.getY() + entityHeight / 2;
                            double entityCenterZ = entity.getZ() + entityWidth / 2;

                            // Get the player's position
                            assert MinecraftClient.getInstance().player != null;
                            ClientPlayerEntity player = MinecraftClient.getInstance().player;
                            double playerX = player.getX();
                            double playerY = player.getY() + player.getEyeHeight(player.getPose());
                            double playerZ = player.getZ();

                            // Calculate the distance between the player and the entity, factoring in hitbox size
                            double distance = Math.sqrt(Math.pow(entityCenterX - playerX, 2) + Math.pow(entityCenterY - playerY, 2) + Math.pow(entityCenterZ - playerZ, 2));

                            int accuracy = 1;

                            // Log the distance
                            LOGGER.info("Distance (including hitbox): " + Math.round(distance * Math.pow(10, accuracy)) / Math.pow(10, accuracy));
                        }
                    }
                }
            });
        }

    }
}
