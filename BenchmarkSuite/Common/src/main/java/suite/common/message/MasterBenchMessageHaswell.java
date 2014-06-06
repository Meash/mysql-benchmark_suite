package suite.common.message;

import java.io.Serializable;

/**
 * Local sends this message to bench server to tell it the information to connect to the Haswell-server.
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class MasterBenchMessageHaswell implements Serializable {
	public final String address;
	public final int port;

	public MasterBenchMessageHaswell(final String address, final int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public String toString() {
		return "LocalBenchMessageHaswell{" +
				"address='" + address + '\'' +
				", port=" + port +
				'}';
	}
}
