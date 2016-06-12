package pl.danielhaba.touchpadoid;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Daniel on 2016-06-11.
 */
public class UDPClient implements Client {

	private InetAddress address;
	private int port;
	private DatagramSocket socket;

	public UDPClient() {

		socket = null;
		port = 0;
		address = null;
	}

	@Override
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

	@Override
	public boolean close() {

		if(!ready())
			return false;

		Log.d("UDPClient", "Close");

		socket.disconnect();
		socket.close();
		socket = null;
		address = null;
		port = 0;
		return true;
	}

	@Override
	public boolean ready() {

		return socket != null;
	}

	@Override
	public void write(byte[] data) {

		if(!ready() || data.length == 0)
			return;

		try {

			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			socket.send(packet);

			Log.d("UDPClient", "Sent " + packet.getLength() + " bytes [" + Dump.hex(packet.getData()) + "]");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] read(int size) {

		if(size == 0)
			return new byte[0];

		try {

			byte[] data = new byte[size];
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			socket.receive(packet);

			Log.d("UDPClient", "Received " + packet.getLength() + " bytes [" + Dump.hex(packet.getData()) + "]");

			return packet.getData();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
			DatagramSocket socket;

			Log.d("UDPClientTask", "Connecting...");
			for(int i = 0; i < tries; ++i) {

				try {
					socket = null;
					InetAddress[] inetAddresses = InetAddress.getAllByName(host);


					for (InetAddress inetAddress : inetAddresses) {

						try {

							socket = new DatagramSocket(port);

						} catch (IOException e) {

							e.printStackTrace();
							UDPClient.this.socket = null;
							UDPClient.this.address = null;
							UDPClient.this.port = 0;
						}

						if (socket != null) {

							Log.d("UDPClientTask", "Connected to " + inetAddress.getHostAddress() + ":" + port);

							UDPClient.this.socket = socket;
							UDPClient.this.address = inetAddress;
							UDPClient.this.port = port;
							return true;
						}
					}

				} catch (UnknownHostException e) {

					e.printStackTrace();
				}

				SystemClock.sleep(wait);
			}

			Log.d("UDPClientTask", "Not connected");
			return false;
		}
	}
}
