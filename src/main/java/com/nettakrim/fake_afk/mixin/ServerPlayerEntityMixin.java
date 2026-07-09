package com.nettakrim.fake_afk.mixin;

import com.nettakrim.fake_afk.FakeAFK;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "disconnect")
    private void onDisconnect(CallbackInfo ci) {
        //regular disconnect listener doesn't trigger for fake players
        FakeAFK.instance.connection.logFakeDeath((ServerPlayer)(Object)this);
    }
}
