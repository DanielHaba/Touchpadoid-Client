package pl.danielhaba.touchpadoid;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class TouchpadActivity extends ActionBarActivity {


	final String defaultAddress = "192.168.8.101";
	final int port = 13623;

	private SurfaceView touchpad;

	private Button connectButton;
	private EditText ipField;

	private Client client;
	private CommandWriter writer;

	private boolean select;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_touchpad);

		client = new UDPClient();

		writer = new CommandWriter(client);
		writer.add((byte) 0, ExitCommand.class);
		writer.add((byte) 1, LogoutCommand.class);
		writer.add((byte) 2, MouseMove.class);
		writer.add((byte) 3, MouseClick.class);
		writer.add((byte) 4, MouseWheel.class);

		writer.setListener(new CommandWriter.Listener() {

			@Override
			public void onStarted() {

			}

			@Override
			public void onStopped() {

				client.close();
			}
		});




		select = false;


		connectButton = (Button) findViewById(R.id.connectButton);
		ipField = (EditText) findViewById(R.id.ipField);
		touchpad = (SurfaceView) findViewById(R.id.touchpad);



		touchpad.setOnTouchListener(new TouchListener());


		ipField.setText(defaultAddress);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (!client.ready()) {

					if (client.open(ipField.getText().toString(), port)) {

						Toast.makeText(TouchpadActivity.this, "Connected", Toast.LENGTH_SHORT).show();
						writer.unlock();
						writer.start();
						setControlMode();
					} else {

						Toast.makeText(TouchpadActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
					}

				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

			writer.push(new MouseWheel(120));
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

			writer.push(new MouseWheel(-120));
			return true;
		}
		else {

			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		setConnectMode();
	}

	@Override
	protected void onPause() {

		logout();
		super.onPause();
	}



	protected void setConnectMode() {

		connectButton.setVisibility(View.VISIBLE);
		ipField.setVisibility(View.VISIBLE);
		touchpad.setVisibility(View.INVISIBLE);
	}

	protected void setControlMode() {

		connectButton.setVisibility(View.INVISIBLE);
		ipField.setVisibility(View.INVISIBLE);
		touchpad.setVisibility(View.VISIBLE);
	}


	protected void logout() {

		writer.push(new LogoutCommand());
		shutdown();
	}

	protected void exit() {

		writer.push(new ExitCommand());
		shutdown();
	}

	protected void shutdown() {

		writer.lock();
		writer.stop();
		setConnectMode();
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_touchpad, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {

			case R.id.action_disconnect:

				if (client.ready()) {

					logout();

				} else {

					Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
				}
				return true;

			case R.id.action_select:
				select = !item.isChecked();
				item.setChecked(select);

				if(select) {

					Toast.makeText(this, "Select mode ON", Toast.LENGTH_LONG).show();
				}
				else {

					Toast.makeText(this, "Select mode OFF", Toast.LENGTH_LONG).show();
				}
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	class TouchListener implements View.OnTouchListener {

		private final int SHORT_CLICK_DURATION = 200;
		private final int LONG_CLICK_DURATION = 1000;
		private final float MIN_MOVE_DISTANCE = 10.0f;

		private BackgroundTask task = null;

		private float lx = 0.0f;
		private float ly = 0.0f;
		private float sx = 0.0f;
		private float sy = 0.0f;


		private boolean click = false;
		private long downTime = 0;



		public TouchListener() {

			//task = new BackgroundTask();
			//task.execute();
		}

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {


			if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

				sx = lx = motionEvent.getX();
				sy = ly = motionEvent.getY();

				click = true;
				downTime = motionEvent.getEventTime();
			}
			if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {


				float cx = motionEvent.getX();
				float cy = motionEvent.getY();

				float dx = cx - lx;
				float dy = cy - ly;
				float mx = cx - sx;
				float my = cy - sy;
				float distance = (float) Math.sqrt(mx * mx + my * my);

				if(distance > MIN_MOVE_DISTANCE)
					click = false;

				if(!click) {

					writer.push(new MouseMove(dx, dy));
				}

				lx = cx;
				ly = cy;
			}
			if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

				if(click) {

					long duration = motionEvent.getEventTime() - downTime;

					if(duration > LONG_CLICK_DURATION) {

						writer.push(new MouseClick(MouseClick.Button.Right, MouseClick.Action.Click));
					}
					else {

						writer.push(new MouseClick(MouseClick.Button.Left, MouseClick.Action.Click));
					}
				}
			}
			return true;
		}

		class BackgroundTask extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... voids) {

				return null;
			}
		}
	}
}
