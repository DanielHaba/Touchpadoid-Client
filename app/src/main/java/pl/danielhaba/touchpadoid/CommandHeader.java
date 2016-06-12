package pl.danielhaba.touchpadoid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Daniel on 2015-11-13.
 */
public class CommandHeader {

	private byte code;
	private int index;
	private int size;

	public CommandHeader(byte code, int index, int size) {

		this.code = code;
		this.index = index;
		this.size = size;
	}

	public byte[] getBytes() {

		return ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
				.put(code).putInt(index).putInt(size).array();
	}
}
