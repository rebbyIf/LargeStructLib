package dev.rebby.largestructlib.worldgen.density_function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Interpolate implements DensityFunction {

    public static final MapCodec<Interpolate> CODEC;

    private Map<Vec3i, Double> values;
    private final DensityFunction input;
    private final int dx;
    private final int dy;
    private final int dz;
    private final Interpolation interpolation;

    public Interpolate(DensityFunction input, int dx, int dy, int dz, Interpolation interpolation) {
        this.input = input;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.interpolation = interpolation;
        values = new HashMap<>();
    }

    @Override
    public double compute(@NotNull FunctionContext functionContext) {

        if (values.size() > 1000000) {
            values.clear();
        }

        Vec3i origin = new Vec3i(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());

        Vec3i pos1 = new Vec3i(dx != 0 ? (functionContext.blockX() / dx) * dx : 0,
                dy != 0 ? (functionContext.blockY() / dy) * dy : 0,
                dz != 0 ? (functionContext.blockZ() / dz) * dz : 0);

        if (interpolation == Interpolation.FLOOR) {
            return input.compute(new FunctionContextWrapper(functionContext, pos1));
        }

        Vec3i pos2 = new Vec3i(dx != 0 ? (functionContext.blockX() / dx + 1) * dx : 0,
                dy != 0 ? (functionContext.blockY() / dy + 1) * dy : 0,
                dz != 0 ? (functionContext.blockZ() / dz + 1) * dz : 0);

        if (interpolation == Interpolation.CEIL) {
            return input.compute(new FunctionContextWrapper(functionContext, pos2));
        }

        Vec3i p000 = new Vec3i(pos1.getX(), pos1.getY(), pos1.getZ());
        Vec3i p100 = new Vec3i(pos2.getX(), pos1.getY(), pos1.getZ());
        Vec3i p010 = new Vec3i(pos1.getX(), pos2.getY(), pos1.getZ());
        Vec3i p110 = new Vec3i(pos2.getX(), pos2.getY(), pos1.getZ());
        Vec3i p001 = new Vec3i(pos1.getX(), pos1.getY(), pos2.getZ());
        Vec3i p101 = new Vec3i(pos2.getX(), pos1.getY(), pos2.getZ());
        Vec3i p011 = new Vec3i(pos1.getX(), pos2.getY(), pos2.getZ());
        Vec3i p111 = new Vec3i(pos2.getX(), pos2.getY(), pos2.getZ());

        boolean f000 = values.containsKey(p000);
        double v000 = f000 ? values.get(p000) :
                input.compute(new FunctionContextWrapper(functionContext, p000));
        if (!f000) {
            values.put(p000, v000);
        }

        boolean f100 = values.containsKey(p100);
        double v100 = f100 ? values.get(p100) :
                input.compute(new FunctionContextWrapper(functionContext, p100));
        if (!f100) {
            values.put(p100, v100);
        }

        boolean f010 = values.containsKey(p010);
        double v010 = f010 ? values.get(p010) :
                input.compute(new FunctionContextWrapper(functionContext, p010));
        if (!f010) {
            values.put(p010, v010);
        }

        boolean f110 = values.containsKey(p110);
        double v110 = f110 ? values.get(p110) :
                input.compute(new FunctionContextWrapper(functionContext, p110));
        if (!f110) {
            values.put(p110, v110);
        }

        boolean f001 = values.containsKey(p001);
        double v001 = f001 ? values.get(p001) :
                input.compute(new FunctionContextWrapper(functionContext, p001));
        if (!f001) {
            values.put(p001, v001);
        }

        boolean f101 = values.containsKey(p101);
        double v101 = f101 ? values.get(p101) :
                input.compute(new FunctionContextWrapper(functionContext, p101));
        if (!f101) {
            values.put(p101, v101);
        }

        boolean f011 = values.containsKey(p011);
        double v011 = f011 ? values.get(p011) :
                input.compute(new FunctionContextWrapper(functionContext, p011));
        if (!f011) {
            values.put(p011, v011);
        }

        boolean f111 = values.containsKey(p111);
        double v111 = f111 ? values.get(p111) :
                input.compute(new FunctionContextWrapper(functionContext, p111));
        if (!f111) {
            values.put(p111, v111);
        }

        double deltaX = dx == 0 ? 0 : ((double) (origin.getX() - pos1.getX())) / dx;
        double deltaY = dy == 0 ? 0 : ((double) (origin.getY() - pos1.getY())) / dy;
        double deltaZ = dz == 0 ? 0 : ((double) (origin.getZ() - pos1.getZ())) / dz;
        return interpolate3(deltaX, deltaY, deltaZ, v000, v100, v010, v110, v001, v101, v011, v111);
    }

    private double interpolate3(double delta1, double delta2, double delta3, double start1, double end1, double start2, double end2, double start3, double end3, double start4, double end4) {
        return interpolation.calculate(
                interpolate2(delta1, delta2, start1, end1, start2, end2),
                interpolate2(delta1, delta2, start3, end3, start4, end4),
                delta3);
    }

    private double interpolate2(double delta1, double delta2, double start1, double end1, double start2, double end2) {
        return interpolation.calculate(interpolation.calculate(start1, end1, delta1), interpolation.calculate(start2, end2, delta1), delta2);
    }

    @Override
    public void fillArray(double @NotNull [] doubles, @NotNull ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
    }

    @Override
    public @NotNull DensityFunction mapAll(@NotNull Visitor visitor) {
        values = new HashMap<>();
        return visitor.apply(new Interpolate(input.mapAll(visitor), dx, dy, dz, interpolation));
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KeyDispatchDataCodec.of(ModDensityFunctions.INTERPOLATE.get());
    }

    public DensityFunction input() {
        return input;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int dz() {
        return dz;
    }

    public Interpolation interpolation() {
        return interpolation;
    }


    static {
        CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(Interpolate::input),
                Codec.intRange(0, 4096).optionalFieldOf("dx", 1).forGetter(Interpolate::dx),
                Codec.intRange(0, 4096).optionalFieldOf("dy", 1).forGetter(Interpolate::dy),
                Codec.intRange(0, 4096).optionalFieldOf("dz", 1).forGetter(Interpolate::dz),
                Interpolation.CODEC.fieldOf("interpolation").forGetter(Interpolate::interpolation)
        ).apply(instance, Interpolate::new));
    }

    private record FunctionContextWrapper(FunctionContext originalContext, Vec3i newLoc) implements FunctionContext {

        @Override
        public int blockX() {
            return newLoc.getX();
        }

        @Override
        public int blockY() {
            return newLoc.getY();
        }

        @Override
        public int blockZ() {
            return newLoc.getZ();
        }

        @Override
        public @NotNull Blender getBlender() {
            return originalContext.getBlender();
        }
    }

    public enum Interpolation implements StringRepresentable {
        FLOOR((a, b, d) -> a, "floor"),
        CEIL((a, b, d) -> b, "ceil"),
        LINEAR((a, b, d) -> d * a + (1 - d) * b, "linear");

        private static final Codec<Interpolation> CODEC = StringRepresentable.fromEnum(Interpolation::values);

        private final TriFunction<Double, Double, Double, Double> function;
        private final String name;

        Interpolation(TriFunction<Double, Double, Double, Double> function, String name) {
            this.function = function;
            this.name = name;
        }

        public double calculate(double a, double b, double d) {
            return function.apply(a, b, d);
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
