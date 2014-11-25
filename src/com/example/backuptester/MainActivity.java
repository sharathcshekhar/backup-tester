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
		BackupGlobals.mLib = new BackupLib(getApplicationContext());
		/********************
		 ** INSTRUMENT END **
		 ********************/
	}

	/* READ EXAMPLE */
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
			int ret = BackupGlobals.mLib.download(filename);
			if (ret != 0) {
				/* FILE NOT FOUND IN DROPBOX. HANDLE THIS */
				txtView.setText("FILE NOT FOUND");
				return;
			}
	   		myFile = new File(BackupGlobals.CACHE_PATH, filename);
		} 
		/********************
		 ** INSTRUMENT END **
		 ********************/
		else {
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

	/* WRITE EXAMPLE */
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
		BackupGlobals.mLib.upload(filename);
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
}