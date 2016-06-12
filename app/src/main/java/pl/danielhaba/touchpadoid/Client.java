package pl.danielhaba.touchpadoid;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Daniel on 2016-06-11.
 */
public interface Client extends Stream {

	boolean open(String host, int port);
	boolean close();
}
