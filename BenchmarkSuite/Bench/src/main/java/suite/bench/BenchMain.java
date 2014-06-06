package suite.bench;

import com.beust.jcommander.Parameter;
import suite.common.Main;
import suite.common.ProtocolException;
import suite.common.entities.InReader;
import suite.common.message.MasterBenchMessage;
import suite.common.message.MasterBenchMessageHaswell;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class BenchMain extends Main {
	public static void main(String[] args) {
		new BenchMain(args);
	}

	public BenchMain(final String[] args) {
		super(args);
	}

	private ServerSocket masterServer;
	private Socket master;
	private ObjectOutputStream masterOut;
	private InReader masterIn;

	private static String benchConfigPath = "config.txt";

	@Override
	protected void init() throws Exception {
		File benchConfigFile = new File(benchConfigPath);
		if (!benchConfigFile.exists())
			throw new IllegalStateException("Config file '" + benchConfigFile.getAbsolutePath() + "' does not exist");

		Configuration config = new Configuration();
		mapArgs(config);

		masterServer = new ServerSocket(config.masterPort);
		// connect master and get haswell connect info
		System.out.println("Waiting for master connection on port " + config.masterPort);
		master = masterServer.accept();
		masterOut = new ObjectOutputStream(master.getOutputStream());
		masterIn = new InReader(new ObjectInputStream(master.getInputStream()));
		new Thread(masterIn, "Bench inReader").start();
		System.out.println("Connected to master");

		final MasterBenchMessageHaswell haswellInformation = (MasterBenchMessageHaswell) masterIn.next();
		System.out.println("Received Haswell information: " + haswellInformation);
		Properties props = new Properties();
		props.load(new FileInputStream(benchConfigPath));
		props.put("DB.Url", "jdbc:mysql://" + haswellInformation.address + ":" + haswellInformation.port);
		FileOutputStream propsOut = new FileOutputStream(benchConfigPath);
		props.store(propsOut, "Set Haswell information");
		propsOut.flush();
		propsOut.close();
		System.out.println("Wrote Haswell information to config file '" + benchConfigPath + "'");
		masterOut.writeObject(new MasterBenchMessage(MasterBenchMessage.Type.HASWELL_INFO_OK));
		System.out.println("Sent HASWELL INFO OK");
	}

	@Override
	public void run() throws Exception {
		MasterBenchMessage msg;
		// wait for init
		expect(MasterBenchMessage.Type.INIT);
		System.out.println("Initializing benchmark");
		initBenchmark();
		// send INIT OK
		send(MasterBenchMessage.Type.INIT_OK);

		// wait for start
		expect(MasterBenchMessage.Type.BENCH_START);
		masterIn.stop();
		System.out.println("Running benchmark");
		runBenchmark();
		// send bench finished
		send(MasterBenchMessage.Type.BENCH_FINISHED);

		// send benchmark results
		File benchResultsFile = new File(""); // TODO
		String content = "some bench results";
		send(new MasterBenchMessage(MasterBenchMessage.Type.BENCHMARK_RESULTS, content.getBytes()));
	}

	private void initBenchmark() {
		// TODO
	}

	private void runBenchmark() {
		// TODO
	}

	private void send(MasterBenchMessage.Type type) throws IOException {
		MasterBenchMessage msg = new MasterBenchMessage(type);
		send(msg);
	}
	private void send(MasterBenchMessage msg) throws IOException {
		System.out.println("Sending " + msg.type.name() + ": " + msg);
		masterOut.writeObject(msg);
	}

	private void expect(MasterBenchMessage.Type type) throws Exception {
		MasterBenchMessage msg = (MasterBenchMessage) masterIn.next();
		if (msg.type != type)
			throw new ProtocolException("Expected " + type.name() + ", got: " + msg);
	}

	@Override
	public void close() throws IOException {
		masterOut.flush();
		masterOut.close();
		masterIn.close();
		master.close();
		masterServer.close();
	}

	private static class Configuration {
		@Parameter(names = {"-mp", "--master_port"}, description = "Port for master connection")
		public int masterPort = 42424;
	}
}
