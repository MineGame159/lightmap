package minegame159.lightmap.mixin;

import minegame159.lightmap.events.ChunkUpdateEvent;
import minegame159.lightmap.mixininterface.IServerWorld;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow protected ServerWorld world;

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", shift = At.Shift.AFTER))
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        ChunkUpdateEvent.invoke(
                ((IServerWorld) world).lightmap$getWorld(),
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getZ())
        );
    }
}
