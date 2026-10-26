package dev.rebby.largestructlib.worldgen.structure.check_value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.structure.util.MultiNoiseValueHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public class MultiNoiseCheckValue implements CheckValue{
    public static final MapCodec<MultiNoiseCheckValue> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("minimum").forGetter(f -> f.min),
                    Codec.DOUBLE.fieldOf("maximum").forGetter(f -> f.max),
                    Codec.list(Frequency.FREQUENCY_CODEC.codec()).fieldOf("frequencies").forGetter(f -> f.frequencies)
                    ).apply(instance, MultiNoiseCheckValue::new));

    protected final double min;
    protected final double max;
    protected final List<Frequency> frequencies;

    public MultiNoiseCheckValue(double min, double max, List<Frequency> frequencies){
        this.min = min;
        this.max = max;
        this.frequencies = frequencies;
    }

    public void instantiate(Structure.GenerationContext context, NoiseGeneratorSettings settings) {
        for (Frequency frequency : frequencies) {
            frequency.instantiate(context, settings);
        }
    }

    @Override
    public boolean check(Holder<DensityFunction> check, BlockPos loc, BlockPos minLoc, BlockPos maxLoc, int scale) {
        double density = MultiNoiseValueHolder.getInstance().getValue(check, loc);

        if (density < min || density >= max)
            return false;

        for (Frequency frequency : frequencies) {
            if (!frequency.check(loc)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean doesOverrideIfTrue() {
        return false;
    }

    @Override
    public boolean doesOverrideIfFalse() {
        return true;
    }

    @Override
    public CheckValueType<?> getType() {
        return CheckValueTypes.MULTI_NOISE.get();
    }

    public static class Plane extends MultiNoiseCheckValue {

        public static final MapCodec<Plane> CODEC = RecordCodecBuilder.mapCodec( instance ->
                instance.group(
                        Codec.DOUBLE.fieldOf("minimum").forGetter(f -> f.min),
                        Codec.DOUBLE.fieldOf("maximum").forGetter(f -> f.max),
                        Codec.list(Frequency.FREQUENCY_CODEC.codec()).fieldOf("frequencies").forGetter(f -> f.frequencies),
                        Codec.STRING.fieldOf("axis").forGetter(f -> f.axis)
        ).apply(instance, Plane::new));

        private final String axis;

        public Plane(double min, double max, List<Frequency> frequencies, String axis) {
            super(min, max, frequencies);
            this.axis = axis;
        }

        @Override
        public boolean check(Holder<DensityFunction> check, BlockPos loc, BlockPos minLoc, BlockPos maxLoc, int scale) {
            switch (axis) {
                case "x" -> {
                    for (int j = minLoc.getY(); j < maxLoc.getY(); j += scale) {
                        for (int k = minLoc.getZ(); k < maxLoc.getZ(); k += scale) {
                            if (super.check(check, new BlockPos(loc.getX(), j, k), minLoc, maxLoc, scale)) {
                                return true;
                            }
                        }
                    }
                }
                case "y" -> {
                    for (int i = minLoc.getX(); i < maxLoc.getX(); i += scale) {
                        for (int k = minLoc.getZ(); k < maxLoc.getZ(); k += scale) {
                            if (super.check(check, new BlockPos(i, loc.getY(), k), minLoc, maxLoc, scale)) {
                                return true;
                            }
                        }
                    }
                }
                case "z" -> {
                    for (int i = minLoc.getX(); i < maxLoc.getX(); i += scale) {
                        for (int j = minLoc.getY(); j < maxLoc.getY(); j += scale) {
                            if (super.check(check, new BlockPos(i, j, loc.getZ()), minLoc, maxLoc, scale)) {
                                return true;
                            }
                        }
                    }
                }
                default -> throw new IllegalArgumentException("Error: axis does not equal any true axis of location");
            }

            return false;
        }

        @Override
        public CheckValueType<?> getType() {
            return CheckValueTypes.MULTI_NOISE_PLANE.get();
        }
    }



    public static class Axis extends MultiNoiseCheckValue {

        public static final MapCodec<Axis> CODEC = RecordCodecBuilder.mapCodec( instance ->
                instance.group(
                        Codec.DOUBLE.fieldOf("minimum").forGetter(f -> f.min),
                        Codec.DOUBLE.fieldOf("maximum").forGetter(f -> f.max),
                        Codec.list(Frequency.FREQUENCY_CODEC.codec()).fieldOf("frequencies").forGetter(f -> f.frequencies),
                        Codec.STRING.fieldOf("axis").forGetter(f -> f.axis)
                ).apply(instance, Axis::new));

        private final String axis;

        public Axis(double min, double max, List<Frequency> frequencies, String axis) {
            super(min, max, frequencies);
            this.axis = axis;
        }

        @Override
        public boolean check(Holder<DensityFunction> check, BlockPos loc, BlockPos minLoc, BlockPos maxLoc, int scale) {
            switch (axis) {
                case "x" -> {
                    for (int i = minLoc.getX(); i < maxLoc.getX(); i += scale) {
                        if (super.check(check, new BlockPos(i, loc.getY(), loc.getZ()), minLoc, maxLoc, scale)) {
                            return true;
                        }
                    }
                }
                case "y" -> {
                    for (int j = minLoc.getY(); j < maxLoc.getY(); j += scale) {
                        if (super.check(check, new BlockPos(loc.getX(), j, loc.getZ()), minLoc, maxLoc, scale)) {
                            return true;
                        }
                    }
                }
                case "z" -> {
                    for (int k = minLoc.getZ(); k < maxLoc.getZ(); k += scale) {
                        if (super.check(check, new BlockPos(loc.getX(), loc.getY(), k), minLoc, maxLoc, scale)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public CheckValueType<?> getType() {
            return CheckValueTypes.MULTI_NOISE_AXIS.get();
        }
    }

    public static class Frequency {
        private static final MapCodec<Frequency> FREQUENCY_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                                Codec.DOUBLE.fieldOf("minimum").forGetter(f -> f.min),
                                Codec.DOUBLE.fieldOf("maximum").forGetter(f -> f.max),
                            DensityFunction.CODEC.fieldOf("density_function").forGetter(f -> f.densityFunction))
                        .apply(instance, Frequency::new));

        private final double min;
        private final double max;
        private final Holder<DensityFunction> densityFunction;
        private DensityFunction densityFunctionImpl;

        private Frequency(double min, double max, Holder<DensityFunction> densityFunction){
            this.min = min;
            this.max = max;
            this.densityFunction = densityFunction;
            this.densityFunctionImpl = null;
        }

        private void instantiate(Structure.GenerationContext context, NoiseGeneratorSettings settings) {
            MultiNoiseValueHolder.getInstance().instantiate(densityFunction, context, settings);
            //densityFunctionImpl = densityFunction.value().mapAll(new DensityFunctionWrapper(context, settings));
        }

        private boolean check(BlockPos loc) {
            double density = MultiNoiseValueHolder.getInstance().getValue(densityFunction, loc);
            // densityFunctionImpl.compute(new DensityFunction.SinglePointContext(loc.getX(), loc.getY(), loc.getZ()));

            return density >= min && density < max;
        }

    }
}
