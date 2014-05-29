package com.beyare.dztrackpad;

import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class TouchPadActivity extends Activity {

	// action flags for touch pad
	private final static int ACTION_LEFT_BUTTON_CLICK = 0;
	private final static int ACTION_LEFT_BUTTON_DOWN = 1;
	private final static int ACTION_LEFT_BUTTON_UP = 2;
		
	private final static int ACTION_RIGHT_BUTTON_CLICK = 3;
	private final static int ACTION_RIGHT_BUTTON_DOWN = 4;
	private final static int ACTION_RIGHT_BUTTON_UP = 5;
		
	private final static int ACTION_MOUSE_MOVE = 6;
	private final static int ACTION_SCROLL_MOVE = 7;
		
	private final static int ACTION_TEST_MESSAGE = 8;
		
	private int move_x = 0;		// touch panel
	private int move_y = 0;
		
	private int flag_x = 0 ;
	private int flag_y = 0;
		
	private int diff_dist = 0;	// scroll panel
	private int diff_flag = 0;
		
		
	private String device_name = null;

	private BluetoothService btService;
	private BluetoothAdapter bAdapter;
	
	Button btn_test_message = null;
	Button btn_left_1 = null;
	Button btn_right_1= null;
	FrameLayout scrollArea = null;
	FrameLayout touchPanel = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		btService = new BluetoothService(this, mHandler);
		bAdapter = BluetoothAdapter.getDefaultAdapter();
		Bundle bundle = getIntent().getExtras();
		setContentView(R.layout.touchpad_1);
		
		if (bundle != null){
			establishConnection(bundle.getString(SetupActivity.EXTRA_DEVICE_ADDRESS));
		}
		
		initTouchPadLayout();
		
	}
	
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK){
			btService.stop();
		}
		return super.onKeyUp(keyCode, event);
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		btService.stop();
	}



	private void establishConnection(String address){
		BluetoothDevice device = bAdapter.getRemoteDevice(address);
		btService.connecting(device);
	}
	
	
	private void writeMessage(int command){
		btService.write(command);
		
		switch (command) {
		case ACTION_MOUSE_MOVE:
			btService.write(flag_x);
			btService.write(move_x);
			btService.write(flag_y);
			btService.write(move_y);
			break;
		case ACTION_SCROLL_MOVE:
			btService.write(diff_flag);
			btService.write(diff_dist);
			break;
		case ACTION_TEST_MESSAGE:
			String txt = "Hi, I am Vincent Zhang~";
			btService.write(txt.getBytes());
		default:
			break;
		}
	}
	
	
	private final  void setStatus(String subTile){		
		TouchPadActivity.this.setTitle(subTile);
	}
	
	private  final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Log.i("Test", "msg"+msg.what);
			switch (msg.what) {
			
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTING:
					setStatus("connecting...");
					break;
				case BluetoothService.STATE_CONNECTED:
					setStatus("Connected to "+device_name);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					setStatus("Still not connected");
					break;
				case BluetoothService.STATE_CONNETING_FAILED:
					Log.e("Test", "Activity recieved connection failed information");
					btService.stop();
					Intent intent = new Intent();
					intent.setClass(TouchPadActivity.this, SetupActivity.class);
					startActivity(intent);
					break;
				case BluetoothService.STATE_CONNECTION_LOST:
					Log.e("Test", "Activity recieved connection lost information");
					btService.stop();
					Intent intent2 = new Intent();
					intent2.setClass(TouchPadActivity.this, SetupActivity.class);
					startActivity(intent2);
					break;
				default:
					break;
				}
				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:
				device_name = msg.getData().getString(BluetoothService.DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to "+device_name,Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST_INFO),
                        Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
	};
	
	private void initTouchPadLayout(){
		btn_left_1 = (Button)findViewById(R.id.btn_left_1);
		btn_right_1= (Button)findViewById(R.id.btn_right_1);
		scrollArea = (FrameLayout)findViewById(R.id.flayout_scrollArea_1);
		touchPanel = (FrameLayout)findViewById(R.id.flayout_touchPanel_1);
		btn_test_message = (Button)findViewById(R.id.btn_testtxt);
						
		
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
					
					diff_dist = (int)event.getY() - beginY;
					if (diff_dist<0){
						diff_dist = -diff_dist;
						diff_flag = 1;						
					}
					else{
						diff_flag = 0;
					}
					
					Log.i("Test", "moved "+diff_dist);
					if (diff_dist>25){
						diff_dist = 2;
						
						writeMessage(action);
					}
					
					break;
				case MotionEvent.ACTION_UP:
					Log.e("Test", "Scroll Move up");
					diff_dist = (int)event.getY() - beginY;
					Log.d("Test", " "+diff_dist);
					diff_dist = diff_dist>0 ? diff_dist : - diff_dist;
					
					if (diff_dist <10){
						
						int midY = v.getHeight()/2;
						Log.d("Test", "midY "+midY);
						Log.d("Test", "FinalY:"+(int)event.getY());
						diff_flag = midY < (int)event.getY() ? 0 : 1; 
						diff_dist = 2;
						writeMessage(action);
					}
					break;
	
				default:
					break;
				}
				return true;
			}
		});		
		
	}
	
}
