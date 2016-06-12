package pl.danielhaba.touchpadoid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Daniel on 2015-11-15.
 */
public class MouseClick implements Command {

	public enum Button {

		Left,
		Right,
	}

	public enum Action {
		Press,
		Release,
		Click,
	}

	private Button button;
	private Action action;




	public MouseClick(Button button, Action action) {

		this.button = button;
		this.action = action;
	}



	@Override
	public byte[] getBytes() {

		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
				.put((byte) button.ordinal()).put((byte) action.ordinal()).array();
	}
}
