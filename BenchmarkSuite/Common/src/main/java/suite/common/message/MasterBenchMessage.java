package suite.common.message;

import java.io.Serializable;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class MasterBenchMessage implements Serializable {
	public enum Type {
		HASWELL_INFO_OK, INIT, INIT_OK, BENCH_START, BENCH_FINISHED, BENCHMARK_RESULTS
	}

	public final Type type;
	public final int warmup, duration, threads;
	public final byte[] data;

	public MasterBenchMessage(final Type type, final byte[] data) {
		this(type, 0, 0, 0, data);
	}

	public MasterBenchMessage(final Type type) {
		this(type, 0, 0, 0);
	}

	public MasterBenchMessage(final Type type, final int warmup, final int duration, final int threads) {
		this(type, warmup, duration, threads, null);
	}

	public MasterBenchMessage(final Type type, final int warmup, final int duration, final int threads,
							  final byte[] data) {
		this.type = type;
		this.warmup = warmup;
		this.duration = duration;
		this.threads = threads;
		this.data = data;
	}

	@Override
	public String toString() {
		return "MasterBenchMessage{" +
				"type=" + type +
				", warmup=" + warmup +
				", duration=" + duration +
				", threads=" + threads +
				", data=" + (data != null ? data.length + " bytes" : null) +
				'}';
	}
}
