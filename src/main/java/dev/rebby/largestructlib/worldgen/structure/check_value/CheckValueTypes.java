package dev.rebby.largestructlib.worldgen.structure.check_value;

import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.piece.DefaultDensityStructure;
import dev.rebby.largestructlib.worldgen.structure.piece.StructurePieceType;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface CheckValueTypes {
    DeferredRegister<CheckValueType<?>> TYPES =
            DeferredRegister.create(CheckValueType.REGISTRY, LargeStructureLib.MODID);

    DeferredHolder<CheckValueType<?>, CheckValueType<CheckDefaultValue>> DEFAULT =
            TYPES.register("default",
                    () -> () -> CheckDefaultValue.DEFAULT_VALUE_CODEC);
    DeferredHolder<CheckValueType<?>, CheckValueType<CheckPlaneForSingleValue>> PLANE_FOR_SINGLE_VALUE =
            TYPES.register("plane_for_value",
            () -> () -> CheckPlaneForSingleValue.PLANE_SINGLE_VALUE_CODEC);
    DeferredHolder<CheckValueType<?>, CheckValueType<CheckDefaultValue.CheckAxisForSingleValue>> AXIS_FOR_SINGLE_VALUE =
            TYPES.register("axis_for_value",
                    () -> () -> CheckDefaultValue.CheckAxisForSingleValue.AXIS_SINGLE_VALUE_CODEC);

    DeferredHolder<CheckValueType<?>, CheckValueType<MultiNoiseCheckValue>> MULTI_NOISE =
            TYPES.register("multi_noise",
                    () -> () -> MultiNoiseCheckValue.CODEC);
    DeferredHolder<CheckValueType<?>, CheckValueType<MultiNoiseCheckValue.Axis>> MULTI_NOISE_AXIS =
            TYPES.register("multi_noise_axis",
                    () -> () -> MultiNoiseCheckValue.Axis.CODEC);
    DeferredHolder<CheckValueType<?>, CheckValueType<MultiNoiseCheckValue.Plane>> MULTI_NOISE_PLANE =
            TYPES.register("multi_noise_plane",
                    () -> () -> MultiNoiseCheckValue.Plane.CODEC);

}
