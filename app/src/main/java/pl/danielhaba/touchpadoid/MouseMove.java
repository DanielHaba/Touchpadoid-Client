package pl.danielhaba.touchpadoid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Daniel on 2015-11-13.
 */
public class MouseMove implements Command {

	private float moveX;
	private float moveY;

	public MouseMove(float moveX, float moveY) {

		this.moveX = moveX;
		this.moveY = moveY;
	}


	@Override
	public byte[] getBytes() {

		return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(moveX).putFloat(moveY).array();
	}

}
