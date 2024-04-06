package minegame159.lightmap.platform;

import minegame159.lightmap.utils.LightId;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;

public class FabricBlock implements LightBlock {
    private final BlockState state;
    private final LightId id;

    public FabricBlock(BlockState state) {
        this.state = state;

        Identifier identifier = Registries.BLOCK.getId(state.getBlock());
        this.id = new LightId(identifier.getNamespace(), identifier.getPath());
    }

    @Override
    public LightId getId() {
        return id;
    }

    @Override
    public boolean isAir() {
        return state.isAir();
    }

    @Override
    public boolean isWater() {
        Fluid fluid = state.getFluidState().getFluid();
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    @Override
    public boolean hasCollisions() {
        return !state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN).isEmpty();
    }

    @Override
    public int getMapColor() {
        MapColor color = state.getMapColor(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
        return color == MapColor.CLEAR ? 0 : color.getRenderColor(MapColor.Brightness.HIGH);
    }
}
