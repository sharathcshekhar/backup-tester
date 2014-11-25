package com.example.backuptester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private HandlerThread handlerThread;
	private IncomingHandler handler;
	private Messenger mClientMessenger;
	static final String CACHE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "backup_cache";
	static final String DEFAULT_FILE_DATA = "data: ";
	public static final int REMOTE_READ = 0;
	public static final int REMOTE_WRITE = 1;
	public static final int REMOTE_READ_DONE = 2;
	public static final int REMOTE_WRITE_DONE = 3;
	public static final int REMOTE_READ_FAILED = 4;
	public static Integer sync = new Integer(0);
	private MyServiceConnection myConnection = new MyServiceConnection();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		File cacheDirectory = new File(CACHE_PATH);
		boolean ret = cacheDirectory.mkdir();
		Log.d("TEST", "CACHE PATH = " + CACHE_PATH + " ret: " + ret);

		handlerThread = new HandlerThread("IPChandlerThread");
        handlerThread.start();
        handler = new IncomingHandler(handlerThread);
        mClientMessenger = new Messenger(handler);

        Intent intent = new Intent("com.example.backupmanager");
	    bindService(intent, myConnection , Context.BIND_AUTO_CREATE);
	 //   myService = myConnection.getMyService();
	}
	
	public void readFile(View view) {

		EditText edit = (EditText) findViewById(R.id.editText1);
		String filename = edit.getText().toString();
		Log.d("TEST", "READ button pressed, filename : " + filename);
		TextView txtView = (TextView)findViewById(R.id.textView1);
	    
		File myFile = new File(CACHE_PATH, filename);

		if (!myFile.exists()) {
			Log.d("TEST", "File not found in SD card, downloading from dropbox");
			Bundle bundle = new Bundle();
	        bundle.putString("filename", filename);
	   		sendMessage(bundle, REMOTE_READ);
	   		try {
	   			Log.d("TEST", "Wating for read to complete");
				synchronized (sync) {
					sync.wait();
				}
				if (sync.intValue() != 0) {
					Log.d("TEST", "File not found");
					return;
				}
	   		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	   		Log.d("TEST", "downloaded file from dropbox");
	   		/*TODO: wait here */
	   		myFile = new File(CACHE_PATH, filename);
		} else {
			Log.d("TEST", "Doing a local read");
		}

		StringBuilder text = new StringBuilder();

		try {
		    BufferedReader br = new BufferedReader(new FileReader(myFile));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}

		txtView.setText(text);
	}
	
	public void writeFile(View view) {
		
		EditText edit = (EditText)findViewById(R.id.editText1);
		String filename = edit.getText().toString();
		Log.d("TEST", "WRITE button pressed, filename : " + filename);

		try {
			File myFile = new File(CACHE_PATH, filename);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
								new OutputStreamWriter(fOut);
		
			myOutWriter.append(DEFAULT_FILE_DATA + filename);
		 	myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
    	sendMessage(bundle, REMOTE_WRITE); // 0 - READ, 1 - WRITE
  	}
	
	public void deleteFile(View view) {
		EditText edit = (EditText)findViewById(R.id.editText1);
		String filename = edit.getText().toString();
		Log.d("TEST", "DELETE button pressed, filename : " + filename);

		File myFile = new File(CACHE_PATH, filename);
		if (myFile.delete()) {
			Log.d("TEST", "File delete");
		} else {
			Log.d("TEST", "Failed to delete File");
		}
	}
	
	public void sendMessage(Bundle bundle, int msg_type)
	{
		if (!myConnection.isBound()) {
			Log.d("ERROR", "STILL NOT INIT'ed");
			return;
		}
		Message msg = Message.obtain();
		
		msg.setData(bundle);
		msg.what = msg_type;
		msg.replyTo = mClientMessenger;

		try {
			myConnection.getMyService().send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/*
	class IncomingHandler extends Handler {

        public IncomingHandler(HandlerThread thr) {
            super(thr.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REMOTE_READ_DONE:
                Log.d("TEST", "READ completed");
            	sync = 0;
                synchronized (sync) {
                   	sync.notify();
				}
                break;
            case REMOTE_READ_FAILED:
                Log.d("TEST", "READ failed");
                sync = 404;
                synchronized (sync) {
                	sync.notify();
				}
                break;
            case REMOTE_WRITE_DONE:
                Log.d("TEST", "WRITE completed");
                break;
            default:
                break;
            }
        }
    }
	*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
/*
	private ServiceConnection myConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, 
                                                IBinder service) {
			Log.d("DBG", "Binding message");
			myService = new Messenger(service);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            myService = null;
            isBound = false;
        }

		
	};	
*/
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

//private ServiceConnection myConnection = new ServiceConnection() {
