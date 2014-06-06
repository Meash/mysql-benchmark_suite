package suite.common.entities;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * @author Martin Schrimpf
 * @created 06.06.2014
 */
public class InReader<T> implements Runnable {
	private ObjectInputStream in;
	private Queue<T> queue;
	private Semaphore itemSemaphore;
	private boolean stop = false;

	public InReader(final ObjectInputStream in) {
		this.in = in;
		this.queue = new ConcurrentLinkedQueue<T>();
		this.itemSemaphore = new Semaphore(0);
	}

	private Exception runException;

	@Override
	public void run() {
		try {
			while (!stop) {
				Object o;
				try {
					o = in.readObject();
				} catch (EOFException e) {
					break; // stop when end has been reached
				}
				queue.add((T) o);
				itemSemaphore.release();
			}
		} catch (Exception e) {
			runException = e;
		}
	}

	public T next() throws Exception {
		if (runException != null)
			throw runException;
		itemSemaphore.acquire();
		return queue.poll();
	}

	public void stop() {
		this.stop = true;
	}

	public void close() throws IOException {
		stop();
		in.close();
	}
}
