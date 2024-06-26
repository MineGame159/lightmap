package minegame159.lightmap.mixin;

import minegame159.lightmap.events.ChunkUpdateEvent;
import minegame159.lightmap.mixininterface.IServerWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;", shift = At.Shift.BEFORE))
    private void onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> info) {
        if (!context.getWorld().isClient()) {
            ChunkUpdateEvent.invoke(
                    ((IServerWorld) context.getWorld()).lightmap$getWorld(),
                    ChunkSectionPos.getSectionCoord(context.getBlockPos().getX()),
                    ChunkSectionPos.getSectionCoord(context.getBlockPos().getZ())
            );
        }
    }
}
