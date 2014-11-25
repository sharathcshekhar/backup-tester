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
	
	/**********************
	 ** INSTRUMENT START **
	 **********************/
	private HandlerThread handlerThread;
	private IncomingHandler handler;
	private Messenger mClientMessenger;
	private MyServiceConnection myConnection;
	/********************
	 ** INSTRUMENT END **
	 ********************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		File cacheDirectory = new File(BackupGlobals.CACHE_PATH);
		boolean ret = cacheDirectory.mkdir();
		Log.d("TEST", "CACHE PATH = " + BackupGlobals.CACHE_PATH + " ret: " + ret);

		/**********************
		 ** INSTRUMENT START **
		 **********************/
		backupInit();
		/********************
		 ** INSTRUMENT END **
		 ********************/
	}

	
	public void readFile(View view) {

		EditText edit = (EditText) findViewById(R.id.editText1);
		String filename = edit.getText().toString();
		Log.d("TEST", "READ button pressed, filename : " + filename);
		TextView txtView = (TextView)findViewById(R.id.textView1);
	    
		File myFile = new File(BackupGlobals.CACHE_PATH, filename);

		/**********************
		 ** INSTRUMENT START **
		 **********************/
		if (!myFile.exists()) {
			download(filename);
	   		myFile = new File(BackupGlobals.CACHE_PATH, filename);
		} else {
			Log.d("TEST", "Doing a local read");
		}
		/********************
		 ** INSTRUMENT END **
		 ********************/
		
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
			File myFile = new File(BackupGlobals.CACHE_PATH, filename);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
								new OutputStreamWriter(fOut);
		
			myOutWriter.append(BackupGlobals.DEFAULT_FILE_DATA + filename);
		 	myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**********************
		 ** INSTRUMENT START **
		 **********************/
		upload(filename);
		/********************
		 ** INSTRUMENT END **
		 ********************/
	}

	public void deleteFile(View view) {
		EditText edit = (EditText)findViewById(R.id.editText1);
		String filename = edit.getText().toString();
		Log.d("TEST", "DELETE button pressed, filename : " + filename);

		File myFile = new File(BackupGlobals.CACHE_PATH, filename);
		if (myFile.delete()) {
			Log.d("TEST", "File delete");
		} else {
			Log.d("TEST", "Failed to delete File");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

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
	
	/***************************************************
	 * INSTRUMENTATION HELPER FUNCTIONS
	 * INCLUDE THESE FUNCTIONS IN MAIN ACTIVITY
	 * *************************************************/
	
	private void backupInit() {
		myConnection = new MyServiceConnection();
		handlerThread = new HandlerThread("IPChandlerThread");
        handlerThread.start();
        handler = new IncomingHandler(handlerThread);
        mClientMessenger = new Messenger(handler);

        Intent intent = new Intent("com.example.backupmanager");
	    bindService(intent, myConnection , Context.BIND_AUTO_CREATE);
	}

	private void sendMessage(Bundle bundle, int msg_type) {
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
	
	private void upload(String filename) {
		//TODO: Before calling this code, the file has to be copied into SD card
		Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
    	sendMessage(bundle, BackupGlobals.REMOTE_WRITE); // 0 - READ, 1 - WRITE
	}
	
	private int download(String filename) {
		Log.d("TEST", "File not found in SD card, downloading from dropbox");
		Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
   		sendMessage(bundle, BackupGlobals.REMOTE_READ);
   		try {
   			Log.d("TEST", "Wating for read to complete");
			synchronized (BackupGlobals.sync) {
				BackupGlobals.sync.wait();
			}
			if (BackupGlobals.sync.intValue() != 0) {
				Log.d("TEST", "File not found");
				return 1;
			}
   		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
   		Log.d("TEST", "downloaded file from dropbox");
   		//TODO: The file has to be copied from SD card cache back to the original location
   		return 0;
   }
}