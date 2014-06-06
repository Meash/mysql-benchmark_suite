package suite.common.message;

import java.io.Serializable;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class MasterHaswellMessage implements Serializable {
	public enum Type {
		START_MYSQL, START_MYSQL_OK, STOP_MYSQL, STOP_MYSQL_OK, START_PROFILING, STOP_PROFILING, PROFILING_RESULTS, END
	}

	public final Type type;
	public final String argument;
	public final byte[] data;

	public MasterHaswellMessage(final Type type) {
		this(type, null, null);
	}

	public MasterHaswellMessage(final Type type, final byte[] data) {
		this(type, null, data);
	}

	public MasterHaswellMessage(final Type type, final String argument) {
		this(type, argument, null);
	}

	public MasterHaswellMessage(final Type type, final String argument, final byte[] data) {
		this.type = type;
		this.argument = argument;
		this.data = data;
	}

	@Override
	public String toString() {
		return "MasterHaswellMessage{" +
				"type=" + type +
				", argument='" + argument + '\'' +
				", data=" + (data != null ? data.length + " bytes" : null) +
				'}';
	}
}
