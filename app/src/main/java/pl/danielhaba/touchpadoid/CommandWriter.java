package pl.danielhaba.touchpadoid;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Daniel on 2015-11-13.
 */
public class CommandWriter {

	public interface Listener {

		void onStarted();
		void onStopped();
	}

	private BackgroundTask task;
	private Queue<Command> commands;
	private Stream stream;
	private int index;
	private boolean blocked;
	private Listener listener;
	private Map<Class<? extends Command>, Byte> codes;


	public CommandWriter(Stream stream){

		this.task = null;
		this.listener = null;
		this.stream = stream;
		this.commands = new LinkedList<Command>();
		this.codes = new HashMap<Class<? extends Command>, Byte>();
		this.index = 0;
		this.blocked = false;
	}


	public boolean add(byte code, Class<? extends Command> command) {

		if(!codes.containsKey(command)) {

			if(!codes.containsValue(code)) {

				codes.put(command, code);
				return true;
			}
			else {

				Log.d("CommandWriter", "Code " + String.valueOf(code) + " aleready in use");
			}
		}
		else {

			Log.d("CommandWriter", "Command " + command.getName() + " already in use");
		}
		return false;
	}

	public boolean has(byte code) {

		return codes.containsValue(code);
	}

	public boolean has(Class command) {

		return codes.containsKey(command);
	}

	public void setListener(Listener listener) {

		this.listener = listener;
	}



	public synchronized void push(Command command) {

		if(!blocked) {

			if(has(command.getClass())) {

				commands.offer(command);
				Log.d("CommandWriter", "Command pushed " + command.getClass().getName());
			}
			else {

				Log.d("CommandWriter", "Command not found " + command.getClass().getName());
			}

		}
		else {

			Log.d("CommandWriter", "Command blocked " + command.getClass().getName());
		}
	}


	protected synchronized void write() {

		if(!stream.ready())
			return;

		if(!commands.isEmpty()) {


			Command command = commands.poll();
			byte code = codes.get(command.getClass());

			Log.d("CommandWriter", "Writing command " + command.getClass().getName());


			byte[] body = command.getBytes();
			byte[] header = new CommandHeader(code, index++, body.length).getBytes();

			stream.write(ByteBuffer.allocate(header.length + body.length)
					.put(header).put(body).array());
		}



	}

	public void lock() {

		Log.d("CommandWriter", "Locked");
		blocked = true;
	}

	public void unlock() {

		Log.d("CommandWriter", "Unlocked");
		blocked = false;
	}


	public void start() {

		if(!running()) {

			task = new BackgroundTask();
			task.start();
			task.execute(64, 0);
		}
	}

	public boolean running() {

		return task != null && task.isRunning();
	}

	public void stop() {

		if(running()) {

			task.stop();
		}
	}


	private class BackgroundTask extends AsyncTask<Integer, Void, Void> {

		private boolean running = false;

		@Override
		protected Void doInBackground(Integer... integers) {

			int chunk = integers[0];
			int sleep = integers[1];


			Log.d("CommandWriterTask", "Started");

			if(listener != null)
				listener.onStarted();

			while(running || !commands.isEmpty()) {

				write();

				if(sleep > 0)
					SystemClock.sleep(sleep);

			}

			Log.d("CommandWriterTask", "Stoped");

			if(listener != null)
				listener.onStopped();

			return null;
		}

		public boolean isRunning() {

			return running;
		}

		public void start() {

			Log.d("CommandWriterTask", "Starting...");
			running = true;
		}

		public void stop() {

			Log.d("CommandWriterTask", "Stoping...");
			running = false;
		}
	}


}
