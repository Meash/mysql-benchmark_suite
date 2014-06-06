package suite.common;

import com.beust.jcommander.JCommander;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public abstract class Main {
	private String[] args;

	public Main(final String[] args) {
		this.args = args;
		try {
			init();
			run();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				close();
			} catch (Exception ignored) {
			}
		}
	}

	protected void mapArgs(Object o) {
		new JCommander(o, args);
	}

	protected abstract void init() throws Exception;

	protected abstract void run() throws Exception;

	protected abstract void close() throws Exception;
}
