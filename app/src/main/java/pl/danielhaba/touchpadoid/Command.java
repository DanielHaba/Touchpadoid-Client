package pl.danielhaba.touchpadoid;

import android.os.SystemClock;

import java.io.OutputStream;

/**
 * Created by Daniel on 2015-11-13.
 */
public interface Command {

	byte[] getBytes();
}
