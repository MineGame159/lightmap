package minegame159.lightmap.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import minegame159.lightmap.LightMap;
import minegame159.lightmap.utils.LightId;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ImportCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register(LiteralArgumentBuilder<ServerCommandSource> root) {
        return root.then(
                literal("import").then(argument("world", DimensionArgumentType.dimension()).executes(context -> {
                    Identifier id = DimensionArgumentType.getDimensionArgument(context, "world").getRegistryKey().getValue();
                    LightMap.get().importWorld(new LightId(id.getNamespace(), id.getPath()));

                    return 1;
                }))
        );
    }
}
