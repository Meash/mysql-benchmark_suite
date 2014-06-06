package suite.common;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class ProtocolException extends Exception {
	public ProtocolException(String msg) {
		super(msg);
	}

	public ProtocolException(String msg, Exception cause) {
		super(msg, cause);
	}
}
