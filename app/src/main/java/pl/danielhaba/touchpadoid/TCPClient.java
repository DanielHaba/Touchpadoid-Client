package pl.danielhaba.touchpadoid;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Daniel on 2015-11-12.
 */
public class TCPClient implements Client {

	private Socket socket;



	public TCPClient() {

		socket = null;
	}

	public boolean open(String host, int port) {

		if(ready())
			return true;

		try {
			return new ConnectTask(host, port).execute(50, 100).get() && ready();

		} catch (InterruptedException e) {
			e.printStackTrace();

		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}


	public boolean close()
	{
		if(!ready())
			return false;

		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
			socket = null;
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public boolean ready() {

		return socket != null && socket.isConnected();
	}

	@Override
	public void write(byte[] data) {

		if(!ready())
			return;

		try {
			socket.getOutputStream().write(data);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] read(int size) {

		if(!ready())
			return new byte[0];

		byte[] buffer = new byte[size];

		try {
			socket.getInputStream().read(buffer);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer;
	}




	private class ConnectTask extends AsyncTask<Integer, Void, Boolean> {

		private final String host;
		private final int port;


		public ConnectTask(String host, int port) {

			this.host = host;
			this.port = port;
		}

		@Override
		protected Boolean doInBackground(Integer... args) {

			int tries = args[0];
			int wait = args[1];
			Socket socket;

			for(int i = 0; i < tries; ++i) {

				try {
					socket = null;
					InetAddress[] inetAddresses = InetAddress.getAllByName(host);


					for (InetAddress inetAddress : inetAddresses) {

						try {
							socket = new Socket(inetAddress, port);

						} catch (IOException e) {
							e.printStackTrace();
							TCPClient.this.socket = null;
						}

						if (socket != null && socket.isConnected()) {

							TCPClient.this.socket = socket;
							return true;
						}
					}

				} catch (UnknownHostException e) {
					e.printStackTrace();
				}

				SystemClock.sleep(wait);
			}

			return false;
		}
	}


}
