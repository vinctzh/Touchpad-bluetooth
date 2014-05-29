package com.beyare.dztrackpad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothService {

	// message types for different states
	public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_TOAST = 3;
    
    // Key names of messages sent back to activities
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST_INFO = "toast";
	// Debug setting
	private static final String TAG = "BluetoothService";	
	private static final boolean D = true;
	
	// UUID for this application
	private static final UUID M_UUID = UUID.fromString("CAA6519E-F75E-2AC0-B7B8-F80348BC3FC3");
	
	// Member fields
	private final BluetoothAdapter mAdapter;
	private int mState;
	private ConnectingThread mConnectingThread;
	private ConnectedThread mConnectedThread;
	private final Handler mHandler;
	
	// Constants that indicate the current connetion state
	public static final int STATE_NONE = 0;         // we're doing nothing
    public static final int STATE_LISTEN = 1;       // now listening for incoming connections
    public static final int STATE_CONNECTING = 2;   // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;    // now connected to a remote device
	public static final int STATE_CONNETING_FAILED=4; // connection fail
	public static final int STATE_CONNECTION_LOST = 5;// connection lost while communication
    
    public BluetoothService(Context context, Handler handler){
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	mState = STATE_NONE;
    	mHandler = handler;
    	
    }
	// Public interfaces
    public synchronized void start(){    	 
    	 if (D) Log.d("fuck", "service start!~~");
    	 
    	 // cancel any thread which attempts to establish or already established a connection
    	 if (mConnectingThread != null){
    		 mConnectingThread.cancel();
    		 mConnectingThread = null;
    	 }
    	 
    	 if (mConnectedThread != null){
    		 mConnectedThread.cancel();
    		 mConnectedThread = null;
    	 }
    	 
    	 setState(STATE_NONE);
    	 
    	 // if needed add a listening thread below
    	 
    }
     
    private synchronized void setState(int state){
    	if (D) Log.d("fuck", "setState() " + mState + " -> " + state);
    	mState = state;
    	
    	// Tell the activity the state changed, and what's the newest state
    	mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    	
    }
    public int getState(){
		return mState;    	 
    }
     
    public synchronized void connecting(BluetoothDevice device){
    	 
    	if (D) Log.d("Test", "Try conneting to "+device);
    	
    	// stop any thread connected or is conneting
    	if (mConnectingThread != null){
   		 	mConnectingThread.cancel();
   		 	mConnectingThread = null;
   	 	}
   	 
    	if (mConnectedThread != null){
   		 	mConnectedThread.cancel();
   		 	mConnectedThread = null;
   	 	}
    	
    	// Start the thread to connect with the given device
    	if (D) Log.d(TAG, "Create connecting thread..");
    	mConnectingThread = new ConnectingThread(device);
    	mConnectingThread.start();
    	
    	setState(STATE_CONNECTING);
    }
     
    public synchronized void connected(BluetoothSocket btSocket,BluetoothDevice device){
    	 
    	if (D) Log.d(TAG, "connected with device"+device.getName());
    	
    	if (mConnectedThread != null){
    		mConnectedThread.cancel();
    		mConnectedThread = null;
    	}
    	if (mConnectingThread != null){
    		mConnectingThread.cancel();
    		mConnectingThread = null;
    	}
    	
    	// Start the thread to manage the connection and perform transmissions
    	mConnectedThread = new ConnectedThread(btSocket);
    	mConnectedThread.start();
    	
    	
    	
    	// send the name of connected device back to the activity
    	Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
    	Bundle bundle = new Bundle();
    	bundle.putString(DEVICE_NAME, device.getName());
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
    	
    	setState(STATE_CONNECTED) ;
    }
     
    public synchronized void stop(){
    	 if (D) Log.d(TAG, "Stop");
    	 
    	 if (mConnectingThread != null){
    		 mConnectingThread.cancel();
    		 mConnectingThread = null;
    	 }
    	 
    	 if (mConnectedThread != null){
    		 mConnectedThread.cancel();
    		 mConnectedThread = null;
    	 }   	 
    	
    	 setState(STATE_NONE);
    	 
    }
     
    public void write(int operation){
    	 
    	ConnectedThread r;
    	
    	synchronized (this) {
			if (mState != STATE_CONNECTED) return;
			
			r = mConnectedThread;
		}
    	Log.d(TAG, "Write of service called");
    	r.write(operation);
    }
    
    public void write(byte[] bts){
   	 
    	ConnectedThread r;
    	
    	synchronized (this) {
			if (mState != STATE_CONNECTED) return;
			
			r = mConnectedThread;
		}
    	r.write(bts);
    }

    private void connectionFailed(){
    	
    	Log.d(TAG, "Connecting failed");
    	Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
    	Bundle bundle = new Bundle();
    	bundle.putString(TOAST_INFO, "Fail to establish the connection!");
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
    	// Start the server over to restart
    	//BluetoothService.this.start();
    	setState(STATE_CONNETING_FAILED);
    	
    	
    }
    
    public void connectionLost(){   	
    	
    	Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
    	Bundle bundle = new Bundle();
    	bundle.putString(TOAST_INFO, "Connection Lost!");
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
    	setState(STATE_CONNETING_FAILED);
    	//BluetoothService.this.start();
    }
	
    
	/**
	 * Thread trying to connect to a server.
	 * @author Vincent
	 */
	private class ConnectingThread extends Thread{
		
		private final BluetoothSocket clientSocket;
		private final BluetoothDevice mDevice;
		
		public ConnectingThread(BluetoothDevice device){
			
			mDevice = device;
			BluetoothSocket tmp = null;
			
			try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(M_UUID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (D) Log.e(TAG, "fail in creating socket!");
				e.printStackTrace();
			}
			if (D) Log.d(TAG, "client socket created!");
			clientSocket = tmp;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//setName("Connecting Thread"+mDevice.getName());
			
			// Cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();
			
			try {
				if (D) Log.d(TAG, "Try to connect ..");
				clientSocket.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Log.e(TAG, "Fail in connecting!");
				
				try {
					clientSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					Log.e(TAG, "Failed to shutdown connection.", e1);
				}			
				connectionFailed();
				return;
			}
			
			// Reset the ConnectingThread because of this procedure for establishing connection is done
			synchronized (BluetoothService.this){
				mConnectingThread = null;
			}
			
			// Start the connected thread
			connected(clientSocket,mDevice);
		}
		
		public void cancel(){
			try{
				clientSocket.close();
				Log.e(TAG, "cancel~");
			}
			catch (IOException e) {
				// TODO: handle exception
				Log.e(TAG, "close",e);
			}
		}
		
	}
	
	/**
	 * Thread doing operations after connection established
	 * @author Vincent
	 */
	private class ConnectedThread extends Thread{
		
		private final BluetoothSocket btSocket;
		private final InputStream inputStream;
		private final OutputStream outputStream;
		
		
		public ConnectedThread(BluetoothSocket socket){
			btSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			// Get the Bluetooth socket input and output stream
			try {
				tmpIn = btSocket.getInputStream();
				tmpOut= btSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "temp sockets not created", e);
			}
			
			inputStream = tmpIn;
			outputStream= tmpOut;			
		}
		byte[] buffer = new byte[1024];
		int bytes;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(TAG, "Conntion established and now in Connected thread");
			
			while(true){
				try {
					bytes = inputStream.read(buffer);
					Log.e(TAG, "Message recieved!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					connectionLost();
				//	BluetoothService.this.start();
					break;
				}                
			}			
		}
		public void write(byte[] buffer){
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void write(int operation){
			try {
				outputStream.write(operation);
				if (D) Log.d(TAG, "write num to server"+ operation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void cancel(){
			try {
				btSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Closing socket while in connected thread");
				e.printStackTrace();
			}
		}
	}
	
}
