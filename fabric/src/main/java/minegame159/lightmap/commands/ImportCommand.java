package minegame159.lightmap.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import minegame159.lightmap.LightMap;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class ImportCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register(LiteralArgumentBuilder<ServerCommandSource> root) {
        return root.then(literal("import").executes(context -> {
            LightMap.get().importWorld();

            return 1;
        }));
    }
}
