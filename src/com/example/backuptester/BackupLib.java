package com.example.backuptester;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BackupLib {
	
	private HandlerThread handlerThread;
	private IncomingHandler handler;
	private Messenger mClientMessenger;
	private MyServiceConnection myConnection;
	private Context mCtx;
	
	public BackupLib(Context ctx) {
		mCtx = ctx;
		myConnection = new MyServiceConnection();
		handlerThread = new HandlerThread("IPChandlerThread");
        handlerThread.start();
        handler = new IncomingHandler(handlerThread);
        mClientMessenger = new Messenger(handler);

        Intent intent = new Intent("com.example.backupmanager");
        
	    mCtx.bindService(intent, myConnection , Context.BIND_AUTO_CREATE);
	}

	public void upload(String filename) {
		//TODO: Before calling this code, the file has to be copied into SD card
		Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
    	sendMessage(bundle, BackupGlobals.REMOTE_WRITE); // 0 - READ, 1 - WRITE
	}
	
	public int download(String filename) {
		Log.d("TEST", "File not found in SD card, downloading from dropbox");
		Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
   		sendMessage(bundle, BackupGlobals.REMOTE_READ);
   		try {
   			Log.d("TEST", "Wating for read to complete");
			synchronized (handler) {
				handler.wait();
			}

			Log.d("TEST", "read completed");
			if (handler.getErrorCode() != 0) {
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
}
