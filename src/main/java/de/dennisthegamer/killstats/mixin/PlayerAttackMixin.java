package de.dennisthegamer.killstats.mixin;

import de.dennisthegamer.killstats.tracker.KillSession;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttackMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        if (target instanceof LivingEntity && !target.level().isClientSide()) {
            KillSession session = KillSession.getInstance();
            if (session.isRunning()) {
                session.recordFirstHit(target.getUUID());
            }
        }
    }
}
