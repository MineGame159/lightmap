package minegame159.lightmap.mixininterface;

import minegame159.lightmap.platform.FabricBlock;

public interface IBlockState {
    FabricBlock lightmap$getBlock();

    void lightmap$setBlock(FabricBlock block);
}
