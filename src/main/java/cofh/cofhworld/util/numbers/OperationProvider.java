package cofh.cofhworld.util.numbers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Random;

public class OperationProvider implements INumberProvider {

	protected final INumberProvider valueA;
	protected final INumberProvider valueB;
	protected final Operation operation;

	public OperationProvider(INumberProvider valueA, INumberProvider valueB, String type) {

		this.valueA = valueA;
		this.valueB = valueB;
		this.operation = Operation.valueOf(type.toUpperCase(Locale.US));
	}

	public int intValue(World world, Random rand, BlockPos pos) {

		return (int) longValue(world, rand, pos);
	}

	public long longValue(World world, Random rand, BlockPos pos) {

		return operation.perform(valueA.longValue(world, rand, pos), valueB.longValue(world, rand, pos));
	}

	public float floatValue(World world, Random rand, BlockPos pos) {

		return (float) doubleValue(world, rand, pos);
	}

	public double doubleValue(World world, Random rand, BlockPos pos) {

		return operation.perform(valueA.doubleValue(world, rand, pos), valueB.doubleValue(world, rand, pos));
	}

	private static enum Operation {

		ADD {
			@Override
			public long perform(long a, long b) {

				return a + b;
			}

			@Override
			public double perform(double a, double b) {

				return a + b;
			}
		}, SUBTRACT {
			@Override
			public long perform(long a, long b) {

				return a - b;
			}

			@Override
			public double perform(double a, double b) {

				return a - b;
			}
		}, MULTIPLY {
			@Override
			public long perform(long a, long b) {

				return a * b;
			}

			@Override
			public double perform(double a, double b) {

				return a * b;
			}
		}, DIVIDE {
			@Override
			public long perform(long a, long b) {

				return a / b;
			}

			@Override
			public double perform(double a, double b) {

				return a / b;
			}
		}, MODULO {
			@Override
			public long perform(long a, long b) {

				return a % b;
			}

			@Override
			public double perform(double a, double b) {

				return a % b;
			}
		}, MINIMUM {
			@Override
			public long perform(long a, long b) {

				return a <= b ? a : b;
			}

			@Override
			public double perform(double a, double b) {

				return a <= b ? a : b;
			}
		}, MAXIMUM {
			@Override
			public long perform(long a, long b) {

				return a >= b ? a : b;
			}

			@Override
			public double perform(double a, double b) {

				return a >= b ? a : b;
			}
		};

		public abstract long perform(long a, long b);

		public abstract double perform(double a, double b);
	}

}
