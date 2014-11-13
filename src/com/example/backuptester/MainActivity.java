package com.example.backuptester;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	Messenger myService = null;
	boolean isBound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = new Intent("com.example.backupmanager");
	    bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

	}

	public void sendMessage(View view)
	{
		  if (!isBound) {
			  Log.d("ERROR", "STILL NOT INIT'ed");
			  return;
		  }
	        
	        Message msg = Message.obtain();
	        
	        Bundle bundle = new Bundle();
	        bundle.putString("MyString", "Message Received");
	        
	        msg.setData(bundle);
	        
	        try {
	            myService.send(msg);
	        } catch (RemoteException e) {
	            e.printStackTrace();
	        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

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
