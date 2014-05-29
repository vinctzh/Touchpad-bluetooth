package com.beyare.dztrackpad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	// action flags for touch pad
	private static int ACTION_LEFT_BUTTON_CLICK = 0;
	private static int ACTION_LEFT_BUTTON_DOWN = 1;
	private static int ACTION_LEFT_BUTTON_UP = 2;
	
	private static int ACTION_RIGHT_BUTTON_CLICK = 3;
	private static int ACTION_RIGHT_BUTTON_DOWN = 4;
	private static int ACTION_RIGHT_BUTTON_UP = 5;
	
	private static int ACTION_MOUSE_MOVE = 6;
	private static int ACTION_SCROLL_MOVE = 7;
	
	private static int ACTION_TEST_MESSAGE = 8;
	
	private int move_x = 0;		// touch panel
	private int move_y = 0;
	
	private int flag_x = 0 ;
	private int flag_y = 0;
	
	private int diff_dist = 0;	// scroll panel
	private int diff_flag = 0;
		
	
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothSocket clientSocket;
	Button btn_connect;
	Button btn_left;
	Button btn_right;
	

	Button btn_test_message = null;
	Button btn_left_1 = null;
	Button btn_right_1= null;
	FrameLayout scrollArea = null;
	FrameLayout touchPanel = null;
	
	OutputStream outputStream = null;
	
	private final int REQUEST_ENABLE_BT = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		if (!bluetoothAdapter.isEnabled()){
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		} else {
			setupService();
		}
	}
	private void initTouchLayout(){
		btn_left_1 = (Button)findViewById(R.id.btn_left_1);
		btn_right_1= (Button)findViewById(R.id.btn_right_1);
		scrollArea = (FrameLayout)findViewById(R.id.flayout_scrollArea_1);
		touchPanel = (FrameLayout)findViewById(R.id.flayout_touchPanel_1);
		btn_test_message = (Button)findViewById(R.id.btn_testtxt);
						
		if (outputStream == null){
			try {
				outputStream = clientSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		btn_test_message.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				writeMessage(ACTION_TEST_MESSAGE);
			}
		});
		
		btn_left_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (outputStream == null){
//					Toast.makeText(MainActivity.this, "Output stream unavailable", Toast.LENGTH_LONG).show();
//				}
//				else{
//					try {
//						outputStream.write(ACTION_LEFT_BUTTON_CLICK);						
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					writeMessage(ACTION_LEFT_BUTTON_CLICK);
//				}
			}
		});
		
		btn_left_1.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				int action = -1;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					action = ACTION_LEFT_BUTTON_DOWN;
					break;
				case MotionEvent.ACTION_UP:
					action = ACTION_LEFT_BUTTON_UP;
					break;
				default:
					break;
				}
				
				if (action != -1)
					writeMessage(action);
				return true;
			}
		});
		
		btn_right_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				writeMessage(ACTION_RIGHT_BUTTON_CLICK);
			}
		});
		
		btn_right_1.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = -1;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					action = ACTION_RIGHT_BUTTON_DOWN;
					break;
				case MotionEvent.ACTION_UP:
					action = ACTION_RIGHT_BUTTON_UP;
					break;
				default:
					break;
				}
				
				if (action != -1)
					writeMessage(action);
				
				return true;
			}
		});
		
		touchPanel.setOnTouchListener(new OnTouchListener() {
			int lastX;
			int lastY;
			
			int sum_x;
			int sum_y ;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				 
				int action = ACTION_MOUSE_MOVE;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					//action = ACTION_MOUSE_MOVE;
					move_x = 0;
					move_y = 0;
					sum_x = 0;
					sum_y = 0;
					lastX = (int) event.getX();
					lastY = (int) event.getY();
					
					break;
				case MotionEvent.ACTION_MOVE:
				//	action = ACTION_MOUSE_MOVE;
					
					move_x = (int) event.getX() - lastX;
					move_y = (int) event.getY() - lastY;
					if (move_x < 0 ){
						flag_x = 1;
						move_x = -move_x;
						Log.i("Test", "move_x < 0");
					}else
						flag_x = 0;
						
					if (move_y < 0){
						flag_y = 1;
						move_y = -move_y;
					}else
						flag_y = 0;
					
					lastX = (int) event.getX();
					lastY = (int) event.getY();
					sum_x += move_x;
					sum_y += move_y;
					
					writeMessage(action);
					break;
				case MotionEvent.ACTION_UP:
					if(sum_x<2 && sum_y<2)
						writeMessage(ACTION_LEFT_BUTTON_CLICK);
					break;
				default:
					break;
				}
				return true;
			}
		});
		
		scrollArea.setOnTouchListener(new OnTouchListener() {
						
			int beginY;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				int action = ACTION_SCROLL_MOVE;
				switch (event.getAction()) {				
				case MotionEvent.ACTION_DOWN:	
					Log.e("Test", "Scroll Move Down");
					beginY = (int) event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					Log.e("Test", "Scroll Moved1");
					diff_dist = (int)event.getY() - beginY;
					if (diff_dist<0){
						diff_dist = -diff_dist;
						diff_flag = 1;
					}
					else
						diff_flag = 0;
					diff_dist = 1;
					Log.e("Test", "Scroll Moved2");
					writeMessage(action);
					break;
				case MotionEvent.ACTION_UP:
					Log.e("Test", "Scroll Move up");
					break;
	
				default:
					break;
				}
				return true;
			}
		});
		
		
	}
	private void setupService(){
		btn_connect = (Button)findViewById(R.id.btn_cnt_zjy);
		btn_left = (Button)findViewById(R.id.btn_left);
		btn_right= (Button)findViewById(R.id.btn_right);
		
		btn_connect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				UUID uuid = UUID.fromString("CAA6519E-F75E-2AC0-B7B8-F80348BC3FC3");
				Set<BluetoothDevice> deivces = bluetoothAdapter.getBondedDevices();
				
				Log.i("Test","onClick");
				for (BluetoothDevice device : deivces)
				{
					if (device.getName().equals("ZJY-PC")){
						Log.i("Test","onClick");
						try {
							clientSocket = device.createRfcommSocketToServiceRecord(uuid);
							clientSocket.connect();
							Log.i("Test","Connected with ZJY-PC");
							setContentView(R.layout.touchpad_1);
							
							initTouchLayout();
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("Test","Fail to connect with ZJY-PC");
							e.printStackTrace();
							Log.e("Test",e.toString());
						}
					}
				}				
				
			}
		});		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;	
	}
	
	private void writeMessage(int action){
		
		if (outputStream == null){
			Toast.makeText(MainActivity.this, "Output stream unavailable", Toast.LENGTH_LONG).show();
		}
		
		try {
			outputStream.write(action);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (action == ACTION_MOUSE_MOVE){
			try {
				Log.e("Test", "Send Scroll Move information.");
				outputStream.write(flag_x);
				outputStream.write(move_x);
				outputStream.write(flag_y);
				outputStream.write(move_y);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (action == ACTION_SCROLL_MOVE){
			try {
				outputStream.write(diff_flag);
				outputStream.write(diff_dist);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		if (action == ACTION_TEST_MESSAGE){
			
			String txt = "Hi, I am Vincent Zhang~";
			try {
				outputStream.write(txt.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	
	}
			
}
