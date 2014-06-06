package suite.haswell;

import com.beust.jcommander.Parameter;
import suite.common.Main;
import suite.common.entities.InReader;
import suite.common.message.MasterHaswellMessage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class HaswellMain extends Main {
	public static void main(String[] args) {
		new HaswellMain(args);
	}

	public HaswellMain(String[] args) {
		super(args);
	}

	private ServerSocket masterServer;
	private Socket master;
	private InReader<MasterHaswellMessage> masterIn;
	private ObjectOutputStream masterOut;

	@Override
	public void init() throws IOException {
		Configuration config = new Configuration();
		mapArgs(config);

		System.out.println("Waiting for master connection on port " + config.masterPort);
		masterServer = new ServerSocket(config.masterPort);
		master = masterServer.accept();
		masterIn = new InReader<MasterHaswellMessage>(new ObjectInputStream(master.getInputStream()));
		new Thread(masterIn, "Haswell inReader").start();
		masterOut = new ObjectOutputStream(master.getOutputStream());
		System.out.println("Connected to master");
	}

	@Override
	public void run() throws Exception {
		MasterHaswellMessage msg;
		boolean run = true;
		while (run) {
			msg = masterIn.next();
			System.out.println("Received " + msg);
			switch (msg.type) {
				case START_MYSQL:
					String mysqlVersion = msg.argument;
					if (mysqlVersion == null || mysqlVersion.isEmpty())
						throw new IllegalArgumentException("No MySQL version provided");
					System.out.println("Starting MySQL " + mysqlVersion);
					startMySQL(mysqlVersion);
					send(new MasterHaswellMessage(MasterHaswellMessage.Type.START_MYSQL_OK));
					break;
				case STOP_MYSQL:
					System.out.println("Stopping MySQL");
					stopMySQL();
					send(new MasterHaswellMessage(MasterHaswellMessage.Type.STOP_MYSQL_OK));
					break;
				case START_PROFILING:
					System.out.println("Starting profiling");
					startProfiling();
					break;
				case STOP_PROFILING:
					System.out.println("Stopping profiling");
					stopProfiling();
					break;
				case END:
					File profilingResultsFile = new File(""); // TODO
					String content = "some profiling results";
					send(new MasterHaswellMessage(MasterHaswellMessage.Type.PROFILING_RESULTS, content.getBytes()));
					run = false;
					break;
			}
		}
	}

	private void send(final MasterHaswellMessage msg) throws IOException {
		System.out.println("Sending " + msg.type.name() + ": " + msg);
		masterOut.writeObject(msg);
	}

	private void startMySQL(final String type) {
		// TODO
	}

	private void stopMySQL() {
		// TODO
	}

	private void startProfiling() {
		// TODO
	}

	private void stopProfiling() {
		// TODO
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
		public int masterPort = 42422;
	}
}
