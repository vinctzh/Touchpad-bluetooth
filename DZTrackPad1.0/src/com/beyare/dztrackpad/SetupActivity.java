package com.beyare.dztrackpad;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SetupActivity extends Activity {
	 
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	private final static int REQUEST_ENABLE_BT = 0;
	
	// Member fields
	private BluetoothAdapter bAdapter = null;
	private ToggleButton btSwitch = null;
	private Button scanNew = null;
	
	private ArrayAdapter<String> devicesArrayAdapter = null;
	private ListView devicesList = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		
		setContentView(R.layout.setting_activity);
		bAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
		initSettingLayout();
	}	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}

	@Override
	protected void onDestroy() { 
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (bAdapter != null) {
			bAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK){
				initialDevicesList();
			}else{
				btSwitch.setChecked(false);
				Toast.makeText(this, "Fail to open bluetooth!", Toast.LENGTH_LONG).show();
			}
			break;

		default:
			break;
		}
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			bAdapter.cancelDiscovery();
			
			// Get the device MAC address
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			
			
			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.setClass(SetupActivity.this,TouchPadActivity.class);
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			// Set the result and finish this activity
			SetupActivity.this.startActivity(intent);
			
			// Destroy this activity
//			onDestroy();
			//SetupActivity.this.finish();
		}
	};
	
	private void initSettingLayout(){
		
		scanNew = (Button)findViewById(R.id.btn_scan);
		
		devicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		// find listview and set up the adapter
		devicesList = (ListView) findViewById(R.id.lv_deviceslist);
		devicesList.setAdapter(devicesArrayAdapter);
		devicesList.setOnItemClickListener(mDeviceClickListener);
		
		initialDevicesList();
		
		btSwitch = (ToggleButton) findViewById(R.id.bluetooth_switch);	
		//if (bAdapter.enable())
		btSwitch.setChecked(bAdapter.isEnabled());
		
		
		
		btSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (!isChecked){
					bAdapter.cancelDiscovery();
					bAdapter.disable();
					devicesArrayAdapter.clear();
				}else{
					Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(intent, REQUEST_ENABLE_BT);
				}
			}
		});		
		
		
		scanNew.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (bAdapter.isEnabled())
					doDiscovery();
				else
					Toast.makeText(SetupActivity.this, "Bluetooth is disabled ! ", Toast.LENGTH_LONG).show();
				
			}
		});
	}
	
	private void initialDevicesList(){
		if (bAdapter.isEnabled()){
			Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				findViewById(R.id.lv_deviceslist).setVisibility(View.VISIBLE);				
				for (BluetoothDevice device : pairedDevices){
					devicesArrayAdapter.addAll(device.getName() + "\n" + device.getAddress());					
				}
			}
		}
	}
	
    private void doDiscovery() {
        
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("正在扫描新蓝牙设备...");

        // Turn on sub-title for new devices
        scanNew.setClickable(false);

        // If we're already discovering, stop it
        if (bAdapter.isDiscovering()) {
        	bAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bAdapter.startDiscovery();
    }
		
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			
			if (BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String deviceInfo = device.getName() + '\n' + device.getAddress();
				
				//Boolean isNew = true;
				for (int i=0;i < devicesArrayAdapter.getCount();i++)
				{
					if (devicesArrayAdapter.getItem(i).equals(deviceInfo)){
						//isNew = false;
						return;
					}
				}
				
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					devicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
				
			}
			
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.app_name);
				
				scanNew.setClickable(true);
			}
		}
	};
}
