package suite.local;

import com.beust.jcommander.Parameter;
import suite.common.Main;
import suite.common.ProtocolException;
import suite.common.entities.InReader;
import suite.common.message.MasterBenchMessageHaswell;
import suite.common.message.MasterHaswellMessage;
import suite.common.message.MasterBenchMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class MasterMain extends Main {
	public static void main(String[] args) {
		new MasterMain(args);
	}

	private Socket haswellSocket, benchSocket;
	private InReader<MasterHaswellMessage> haswellIn;
	private ObjectOutputStream haswellOut;
	private ObjectOutputStream benchOut;
	private InReader<MasterBenchMessage> benchIn;

	public MasterMain(String[] args) {
		super(args);
	}


	private Configuration config;

	@Override
	protected void init() throws Exception {
		config = new Configuration();
		mapArgs(config);

		// create and connect
		System.out.println("Connecting to Haswell (" + config.haswellAddress + ":" + config.haswellPort + ")");
		this.haswellSocket = new Socket(config.haswellAddress, config.haswellPort);
		this.haswellOut = new ObjectOutputStream(haswellSocket.getOutputStream());
		this.haswellIn = new InReader<MasterHaswellMessage>(new ObjectInputStream(haswellSocket.getInputStream()));
		new Thread(haswellIn, "Haswell inReader").start();
		System.out.println("Connected to haswell server");
		System.out.println("Connecting to bench (" + config.benchAddress + ":" + config.benchPort + ")");
		this.benchSocket = new Socket(config.benchAddress, config.benchPort);
		this.benchOut = new ObjectOutputStream(benchSocket.getOutputStream());
		this.benchIn = new InReader<MasterBenchMessage>(new ObjectInputStream(benchSocket.getInputStream()));
		new Thread(benchIn, "Bench inReader").start();
		System.out.println("Connected to bench server");
		// send haswell information
		MasterBenchMessageHaswell msgInit =
				new MasterBenchMessageHaswell(haswellSocket.getRemoteSocketAddress().toString(),
						haswellSocket.getPort());
		System.out.println("Sending Haswell information " + msgInit + " to bench server");
		benchOut.writeObject(msgInit);
	}

	@Override
	public void run() throws Exception {
		/* benchmark */
		// start mysql
		send(new MasterHaswellMessage(MasterHaswellMessage.Type.START_MYSQL, config.mysqlVersion));
		// wait until mysql started
		expect(MasterHaswellMessage.Type.START_MYSQL_OK);
		// wait until haswell information ok
		expect(MasterBenchMessage.Type.HASWELL_INFO_OK);
		// init bench
		send(MasterBenchMessage.Type.INIT);
		// wait until bench initialized
		expect(MasterBenchMessage.Type.INIT_OK);

		// start bench
		send(new MasterBenchMessage(MasterBenchMessage.Type.BENCH_START, config.warmup, config.duration,
				config.threads));
		// start profiling
		Thread.sleep(config.warmup);
		send(MasterHaswellMessage.Type.START_PROFILING);

		Thread.sleep(config.duration - 5); // stop profiling a bit earlier to avoid being too late

		// stop profiling
		send(MasterHaswellMessage.Type.STOP_PROFILING);
		// wait until bench finished
		expect(MasterBenchMessage.Type.BENCH_FINISHED);
		// stop MySQL
		send(MasterHaswellMessage.Type.STOP_MYSQL);
		expect(MasterHaswellMessage.Type.STOP_MYSQL_OK);
		// signal end
		send(MasterHaswellMessage.Type.END);


		/* retrieve results */
		MasterBenchMessage benchResult = expect(MasterBenchMessage.Type.BENCHMARK_RESULTS);
		MasterHaswellMessage profilingResult = expect(MasterHaswellMessage.Type.PROFILING_RESULTS);
		saveResults(benchResult.data, profilingResult.data);
	}

	private void saveResults(final byte[] benchmark, final byte[] profiling) {
		System.out.println("Benchmark results: " + new String(benchmark));
		System.out.println("Profiling results: " + new String(profiling));
		// TODO
	}

	private void send(MasterBenchMessage.Type type) throws IOException {
		MasterBenchMessage msg = new MasterBenchMessage(type);
		send(msg);
	}

	private void send(MasterBenchMessage msg) throws IOException {
		System.out.println("Sending " + msg.type.name() + ": " + msg);
		benchOut.writeObject(msg);
	}

	private void send(MasterHaswellMessage.Type type) throws IOException {
		MasterHaswellMessage msg = new MasterHaswellMessage(type);
		send(msg);
	}

	private void send(MasterHaswellMessage msg) throws IOException {
		System.out.println("Sending " + msg.type.name() + ": " + msg);
		haswellOut.writeObject(msg);
	}

	private MasterHaswellMessage expect(final MasterHaswellMessage.Type type) throws Exception {
		MasterHaswellMessage msg = haswellIn.next();
		if (msg.type != type)
			throw new ProtocolException("Expected " + type.name() + ", got: " + msg);
		System.out.println("Received " + msg.type.name() + ": " + msg);
		return msg;
	}

	private MasterBenchMessage expect(MasterBenchMessage.Type type) throws Exception {
		MasterBenchMessage msg = benchIn.next();
		if (msg.type != type)
			throw new ProtocolException("Expected " + type.name() + ", got: " + msg);
		System.out.println("Received " + msg.type.name() + ": " + msg);
		return msg;
	}

	@Override
	public void close() throws IOException {
		haswellIn.close();
		benchIn.close();
		haswellOut.flush();
		haswellOut.close();
		benchOut.flush();
		benchOut.close();
		haswellSocket.close();
		benchSocket.close();
	}


	private static class Configuration {
		@Parameter(names = {"-ha", "--haswell_address"}, description = "Address of the Haswell-Server")
		public String haswellAddress = "localhost";
		@Parameter(names = {"-ba", "--bench_address"}, description = "Address of the Bench-Server")
		public String benchAddress = "localhost";

		@Parameter(names = {"-hp", "--haswell_port"}, description = "Port for Haswell connection")
		public int haswellPort = 42422;
		@Parameter(names = {"-bp", "--bench_port"}, description = "Port for bench connection")
		public int benchPort = 42424;

		@Parameter(names = {"-myv", "--type", "--mysql_version"}, description = "MySQL version")
		public String mysqlVersion = "unmodified";

		@Parameter(names = {"-w", "--warmup"}, description = "Warmup (seconds)")
		public int warmup = 10;
		@Parameter(names = {"-d", "--duration"}, description = "Duration (seconds)")
		public int duration = 150;
		@Parameter(names = {"-t", "--threads", "-c", "--connections"}, description = "Concurrent connections")
		public int threads = 4;
	}
}
