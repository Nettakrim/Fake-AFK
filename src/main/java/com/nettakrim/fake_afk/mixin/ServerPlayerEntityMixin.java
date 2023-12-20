package com.nettakrim.fake_afk.mixin;

import com.nettakrim.fake_afk.FakeAFK;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        FakeAFK.instance.deathTest((ServerPlayerEntity)(Object)this);
    }
}
