package pl.danielhaba.touchpadoid;

/**
 * Created by Daniel on 2016-06-12.
 */
public class Dump {

	private static final char[] codes = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	static String hex(byte[] data) {

		String result = "";

		for(byte b : data) {

			if(result.length() > 0)
				result += " ";

			result += hex(b);
		}

		return result;
	}

	static String hex(byte b) {

		String result = "";
		int hi = (b & 0xF0) >> 4;
		int lo = (b & 0x0F) >> 0;

		result += codes[hi];
		result += codes[lo];

		return result;
	}
}
