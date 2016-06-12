package pl.danielhaba.touchpadoid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Daniel on 2015-11-15.
 */
public class MouseWheel implements Command {

	private int delta;

	public MouseWheel(int delta) {

		this.delta = delta;
	}



	@Override
	public byte[] getBytes() {

		return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(delta).array();
	}
}
