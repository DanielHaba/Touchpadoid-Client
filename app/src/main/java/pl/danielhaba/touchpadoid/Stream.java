package pl.danielhaba.touchpadoid;

/**
 * Created by Daniel on 2015-11-15.
 */
public interface Stream {

	boolean ready();
	void write(byte[] data);
	byte[] read(int size);
}
