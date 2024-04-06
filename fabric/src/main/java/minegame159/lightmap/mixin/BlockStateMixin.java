package minegame159.lightmap.mixin;

import minegame159.lightmap.mixininterface.IBlockState;
import minegame159.lightmap.platform.FabricBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IBlockState {
    @Unique
    private FabricBlock lightBlock;

    @Override
    public FabricBlock lightmap$getBlock() {
        return lightBlock;
    }

    @Override
    public void lightmap$setBlock(FabricBlock block) {
        lightBlock = block;
    }
}
