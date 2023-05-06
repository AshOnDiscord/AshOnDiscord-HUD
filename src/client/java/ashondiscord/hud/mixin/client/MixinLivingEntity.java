package ashondiscord.hud.mixin.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ashondiscord.hud.ExampleModClient.comboCounter;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageTime:J"))
    private void onDamage(DamageSource source, float damage, CallbackInfoReturnable<Boolean> ci) {
        // check if the source is the person being comboed
       // if so reset the combo counter
        if (source.getAttacker() == null || comboCounter.size() == 0) {
            return;
        }

        if (source.getAttacker().getId() == comboCounter.get(0).id) {
            comboCounter.clear();
        }
    }
}
