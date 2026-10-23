package dev.rebby.largestructlib.event;

import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValue;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValueType;
import dev.rebby.largestructlib.worldgen.structure.piece.DefaultDensityStructure;
import dev.rebby.largestructlib.worldgen.structure.piece.DensityStructurePiece;
import dev.rebby.largestructlib.worldgen.structure.piece.StructurePieceType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(modid = LargeStructureLib.MODID)
public class ModEvents {
    @SubscribeEvent // on the mod event bus
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
//        event.dataPackRegistry(
//                // The registry key.
//                StructurePieceType.REGISTRY_KEY,
//                // The codec of the registry contents.
//                StructurePieceType.REGISTRY.byNameCodec()
//        );
        event.dataPackRegistry(
                // The registry key.
                DensityStructurePiece.REGISTRY_KEY,
                // The codec of the registry contents.
                DensityStructurePiece.DIRECT_CODEC
        );
//        event.dataPackRegistry(
//                CheckValueType.REGISTRY_KEY,
//                CheckValueType.REGISTRY.byNameCodec()
//        );
        event.dataPackRegistry(
                CheckValue.REGISTRY_KEY,
                CheckValue.DIRECT_CODEC
        );
    }

    @SubscribeEvent // on the mod event bus
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(StructurePieceType.REGISTRY);
        event.register(CheckValueType.REGISTRY);
    }
}
