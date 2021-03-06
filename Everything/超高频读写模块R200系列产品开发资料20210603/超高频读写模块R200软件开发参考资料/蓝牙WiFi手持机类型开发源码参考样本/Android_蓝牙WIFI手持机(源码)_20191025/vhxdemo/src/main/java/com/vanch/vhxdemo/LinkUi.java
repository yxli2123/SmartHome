package com.vanch.vhxdemo;
//martrin  于sz 中文：马全水 shenzhen TELE:壹叁柒壹叁陆叁陆伍陆壹 编写
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lab.sodino.language.util.Strings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vanch.vhxdemo.helper.Utility;
import com.zma.vhxdemo.R;

import de.greenrobot.event.EventBus;

public class LinkUi extends Fragment {
	
	public static class FreshList {

	}

	public static class DoReadParam {

	}

	private static final String TAG = "link";
	Button btnScan;
	Button btnDisconnect;
	Button btnNetConnect;
	TextView txtIp;
	TextView txtPort;
	EditText editTextIp;
	EditText editPortIp;

	ProgressDialog progressDialog;
	ListView listView;
	BluetoothAdapter mBluetoothAdapter;
	boolean hasRequetBt = false;
	public static final int REQUEST_ENABLE_BT = 1;
	
	public static final String action_scan ="scan_click";
	public static final String action_disconnect ="disconnect_click";
	protected static final int CONNECTING = 1;
	protected static final int CONNECTING_OK = 2;
	protected static final int CONNECTING_FAILE = 3;
	protected static final int DISCONNECT = 4;
	
	//other widgets
	public static final String Device_check_IP = "Device_ip";
	public static final String Device_check_PORT = "Device_port";
	public static final String Device_check_Status = "Device_ConnectStatus";

	
	/**
	 * device found event
	 * @author liugang
	 *
	 */
	public static class BTDeviceFoundEvent {
		BluetoothDevice device;

		public BTDeviceFoundEvent(BluetoothDevice device) {
			this.device = device;
		}

		public BluetoothDevice getDevice() {
			return device;
		}

		public void setDevice(BluetoothDevice device) {
			this.device = device;
		}
		
	}
	
	ListAdapter adapter;
//	Set<BluetoothDevice> pairedDevices;
	//Set<BluetoothDevice> foundDevices;
	List<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
//	static BluetoothDevice currentDevice;
	static VH73Device currentDevice;

	@Override
	public void onStart() {
		super.onStart();
	}
	
	public static LinkUi me;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		me = this;
		View view = inflater.inflate(R.layout.link, null);
		//Utility.wq_UdpSendData("LinkUi.java onCreateView!\n");

		btnScan = (Button) view.findViewById(R.id.btn_scan);
		btnScan.setOnClickListener(scanClickListener);
		btnDisconnect = (Button) view.findViewById(R.id.btn_disconnect);
		btnDisconnect.setOnClickListener(disconnectClickListener);
		listView = (ListView) view.findViewById(R.id.device_list);

		btnNetConnect = (Button) view.findViewById(R.id.btn_connectdevice);
		btnNetConnect.setOnClickListener(DeviceConnectClickListener);
		txtIp = (TextView) view.findViewById(R.id.textViewip);
		txtPort = (TextView) view.findViewById(R.id.textViewport);
		editTextIp = (EditText) view.findViewById(R.id.editTextip);
		editPortIp = (EditText) view.findViewById(R.id.editTextport);
		editTextIp.setText("192.168.0.101");
		editPortIp.setText("8899");

		String strip = getConfigCheckIp(getActivity());
		editTextIp.setText(strip);
		String strport = getConfigCheckPort(getActivity());
		editPortIp.setText(strport);

		//progress
		progressDialog = new ProgressDialog(getActivity());
		//progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setIndeterminate(true);
		
		progressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mBluetoothAdapter != null) {
					mBluetoothAdapter.cancelDiscovery();
				}
			}
		});
		
		updateLang();
		
		return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        EventBus.getDefault().register(this);
        activateBtMonitor();
        initBluetooth();
        refreshList();
    }

	View.OnClickListener scanClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Log.i(TAG, "start scan");
			foundDevices.clear();
			refreshList();
			if (mBluetoothAdapter!=null) {
				mBluetoothAdapter.startDiscovery();
			}
		}
	};
	
	View.OnClickListener disconnectClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
//			Toast.makeText(getActivity(), "Not implement", Toast.LENGTH_LONG).show();
			disconnect();
		}
	};

	View.OnClickListener DeviceConnectClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
//			Toast.makeText(getActivity(), "Not implement", Toast.LENGTH_LONG).show();
			//发送:BB 00 03 00 01 00 04 7E
			//接收如下:
			//BB 01 03 00 10 00 4D 31 30 30 20 32 36 64 42 6D 20 56 31 2E 30 92 7E
			//		?????M100 26dBm V1.0?~
			//		BB 01 03 00 10 00 4D 31 30 30 20 32 36 64 42 6D 20 56 31 2E 30 92 7E ??????????????
            //int x = -900;
            //char y = 0;
			//char bbb[] = new char[2];
			//bbb[0] = (char)(int)(x >> 8 & 0xFF);
			//if (bbb[0] < 0){
			//	x  = (0x100+bbb[0]);
			//}
			//bbb[1] = (char)(x & 0xFF);
			//int a = bbb[0] & 0xffffffff;
			//int b = bbb[1] & 0xffffffff;
            Utility.iNetorBTcomm = 1; //0--BT,1--NET,是蓝牙还是网络通信
			String strip = editTextIp.getText().toString();
			setConfigCheckIp(getActivity(), strip);

			String strport = editPortIp.getText().toString();
			setConfigCheckPort(getActivity(), strport);

			if (currentDevice == null){
				currentDevice = new VH73Device(getActivity());
			}
			currentDevice.setDeviceIp(editTextIp.getText().toString());
			currentDevice.setDevicePort(editPortIp.getText().toString());

			if ( currentDevice.isConnected() ){
				disconnect();
				btnNetConnect.setText(Strings.getString(R.string.Connect));
				Utility.iNetConnectd = 0; //0--没有连上,1--连上了。
			}else{
				if ( currentDevice.connectnet() ) {
					Utility.iNetConnectd = 1; //0--没有连上,1--连上了。
					btnNetConnect.setText(Strings.getString(R.string.disconnect));
				}else{
					Utility.iNetConnectd = 0; //0--没有连上,1--连上了。
					btnNetConnect.setText(Strings.getString(R.string.Connect));
				}
			}

//			Selector selector = null;
//			DatagramSocket socket = null;
//			try {
//				DatagramChannel channel = DatagramChannel.open();
//				socket = channel.socket();
//				channel.configureBlocking(false);
//				String strport = editPortIp.getText().toString();
//				int iport = Integer.parseInt(strport);
//				if ( !socket.isBound() ) {
//					socket.bind(new InetSocketAddress(iport)); //5057));
//				}
//
//				selector = Selector.open();
//				channel.register(selector, SelectionKey.OP_READ);
//
//				byte[] chTemp = new byte[] {(byte)0xBB,0x00,0x03,0x00,0x01,0x00,0x04,0x7E};
//
//				ByteBuffer bb = ByteBuffer.allocate (chTemp.length);
//				bb.put (chTemp);
//				bb.flip ();
//
//				String strip = editTextIp.getText().toString();
//				String[] all=strip.split("\\.");
//				//byte[] bs = new byte[] { (byte) 192, (byte) 168, 0, 101 }; //{ (byte) 192, (byte) 168, 0, 71 };
//				byte [] bs = new byte[4];
//				bs[0] = (byte)Integer.parseInt(all[0]);
//				bs[1] = (byte)Integer.parseInt(all[1]);
//				bs[2] = (byte)Integer.parseInt(all[2]);
//				bs[3] = (byte)Integer.parseInt(all[3]);
//				InetAddress address=InetAddress.getByAddress(bs);
//				SocketAddress sa = new InetSocketAddress(address, iport); //8899);
//
//				channel.send(bb, sa);
//
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//
//			ByteBuffer byteBuffer = ByteBuffer.allocate(65535);
//
//			//while (true) {
//			if (true) {
//				try {
//					int iRecvLen = 0;
//					byte[] chTemp = new byte[4096];
//					int eventsCount = selector.select(5000);
//					if (eventsCount > 0) {
//						Set selectedKeys = selector.selectedKeys();
//						Iterator iterator = selectedKeys.iterator();
//						while (iterator.hasNext()) {
//							SelectionKey sk = (SelectionKey) iterator.next();
//							iterator.remove();
//							if (sk.isReadable()) {
//
//								DatagramChannel datagramChannel = (DatagramChannel) sk
//										.channel();
//								byteBuffer.clear();
//								SocketAddress sa = datagramChannel.receive(byteBuffer);
//								byteBuffer.flip();
//
//
//								iRecvLen = byteBuffer.limit();
//								// 测试：通过将收到的ByteBuffer首先通过缺省的编码解码成CharBuffer 再输出马储油平台
//								CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
//								Bcd2AscEx(chTemp, byteBuffer.array(), iRecvLen*2);
//								//System.out.println("receive message:"+ charBuffer.toString());
//								String s = new String(chTemp, "UTF-8");
//								System.out.println("receive message["+ iRecvLen +"]: "+ s.trim());
//
//								byteBuffer.clear();
//
//
//
//								//要发送就下面打开
//								//datagramChannel.send(bb, sa);
//								//datagramChannel.write(buffer);
//							}
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//
//			}
//
//			socket.disconnect();
//			socket.close();

		}
	};

	public static void Bcd2AscEx(byte asc[], byte bcd[], int len)
	{
		int	i, j;
		int k;

		j = (len + len%2) / 2;
		k = 3*j;
		for(i=0; i<j; i++)
		{
			asc[3*i]	= (byte)((bcd[i] >> 4) & 0x0f);
			asc[3*i+1]	= (byte)(bcd[i] & 0x0f);
			asc[3*i+2]	= 0x20;
		}
		for(i=0; i<k; i++)
		{
			if ( (i+1) % 3 == 0 )
			{
				continue;
			}
			if( asc[i] > 0x09)
			{
				asc[i]	= (byte)(0x41 + asc[i] - 0x0a);
			}
			else
			{
				asc[i]	+= 0x30;
			}
		}

		asc[k] = 0;

	}

	// char转byte
	private byte[] getBytes (char[] chars) {
		Charset cs = Charset.forName ("UTF-8");
		CharBuffer cb = CharBuffer.allocate (chars.length);
		cb.put (chars);
		cb.flip ();
		ByteBuffer bb = cs.encode (cb);

		return bb.array();

	}

	// byte转char
	private char[] getChars (byte[] bytes) {
		Charset cs = Charset.forName ("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate (bytes.length);
		bb.put (bytes);
		bb.flip ();
		CharBuffer cb = cs.decode (bb);

		return cb.array();
	}

	/**
	 * init bluetooth adaptor
	 */
	private void initBluetooth() {
		//check support
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			bluetoothNotSupport();
			return;
		}

		//check enable
		if (!mBluetoothAdapter.isEnabled() && !hasRequetBt) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			hasRequetBt = true;
			return;
		}
	}
	
	private void bluetoothNotSupport() {
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).
				setTitle("WARNING")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(Strings.getString(R.string.msg_bt_not_support))
				.setPositiveButton(Strings.getString(R.string.msg_ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//getActivity().finish();
					}
				}).create();
		dialog.show();
	}
	
	//Querying paired devices
	private void queryPairedDevices() {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.i(TAG, "found paired device " + device.getName() + " "+device.toString());
				foundDevices.add(device);
				EventBus.getDefault().post(new BTDeviceFoundEvent(device));
				String lastDeviceMac = ConfigUI.getConfigLastConnect(getActivity());
				Log.d(TAG, "last device " + lastDeviceMac);
				//last connected device
				if(lastDeviceMac.equals(device.getAddress()) && currentDevice==null) {
					Log.d(TAG, "will connect to last device " + lastDeviceMac);
					currentDevice = new VH73Device(getActivity(), device);
					new Thread() {
						public void run() {
							if (currentDevice.connect()) {
								ConfigUI.setConfigLastConnect(getActivity(), currentDevice.getAddress());
							} else {
								Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_fail_to_connect) + currentDevice.getAddress());
								currentDevice = null;
							}
							EventBus.getDefault().post(new FreshList());
						}
					}.start();
				}
			}
		}
	}
	
	public void onEventMainThread(FreshList e) {
		refreshList();
	}
	
	private void activateBtMonitor() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		
		//bond
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);  
//		filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);  
//		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);  
		
		getActivity().registerReceiver(mReceiver, filter);
	}

	private void deactivateBtMonitor() {
		getActivity().unregisterReceiver(mReceiver);
	}
	
	//Discovering devices
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				deviceFound(device);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				discoveryStarted();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				discoveryEnded();
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                switch (device.getBondState()) {  
                case BluetoothDevice.BOND_BONDING:  
					Log.d(TAG, "正在配对......");
                    progressDialog.setMessage(Strings.getString(R.string.msg_paring));
                    progressDialog.show();
                    break;  
                case BluetoothDevice.BOND_BONDED:  
					Log.d("BlueToothTestActivity", "完成配对");
                    progressDialog.setMessage(Strings.getString(R.string.msg_paired));
                    progressDialog.show();
					connect(device);// 连接设备
                    break;  
                case BluetoothDevice.BOND_NONE:  
					Log.d(TAG, "取消配对");
                default:  
                    break;  
                }  
			}
		}
		

		private void discoveryEnded() {
			Log.i(TAG, "finish discovery");
			progressDialog.setMessage(Strings.getString(R.string.msg_scan_over));
			progressDialog.dismiss();
			refreshList();
		}

		private void discoveryStarted() {
			Log.i(TAG, "start discovery");
			progressDialog.setMessage(Strings.getString(R.string.msg_scaning));
			progressDialog.show();
			foundDevices = new ArrayList<BluetoothDevice>();
			queryPairedDevices();
		}
		
		private void deviceFound(BluetoothDevice device) {
			if (!hasFoundDevice(device)) {
				Log.i(TAG, "Device " + device.getName() +" found " + device.toString());
				foundDevices.add(device);
				EventBus.getDefault().post(new BTDeviceFoundEvent(device));
			}
		}
	};
	
	public void onEventMainThread(BTDeviceFoundEvent e) {
		refreshList();
	}
	
	public void onEventMainThread(ConfigUI.LangChanged e) {
		updateLang();
	}
	
	private void connect(final BluetoothDevice device) {
        Utility.iNetorBTcomm = 0; //0--BT,1--NET,是蓝牙还是网络通信
		handle.sendEmptyMessage(CONNECTING);
		new Thread() {
		
			public void run() {
				VH73Device vh75Device = new VH73Device(getActivity(), device);
				boolean succ = vh75Device.connect();
				if (succ) {
					handle.sendEmptyMessage(CONNECTING_OK);
					currentDevice = vh75Device;
					EventBus.getDefault().post(new DoReadParam());
					ConfigUI.setConfigLastConnect(getActivity(), currentDevice.getAddress());
				} else {
					handle.sendEmptyMessage(CONNECTING_FAILE);
				}
			}
		}.start();
	}
	
	/**
	 * read param
	 * @param e
	 */
	public void onEventBackgroundThread(DoReadParam e) {
		try {
			ConfigParam param = currentDevice.readConfigParam();
			Log.d(TAG, "read param:"+param);
			EventBus.getDefault().postSticky(param);
		} catch (IOException e1) {
			e1.printStackTrace();
			Utility.showDialogInNonUIThread(getActivity(), Strings.getString(R.string.msg_waring), 
					Strings.getString(R.string.msg_read_config_fail));
			disconnect();
		}
	}
	
	private boolean hasFoundDevice(BluetoothDevice device) {
		for (BluetoothDevice dev : foundDevices) {
			if(device.getAddress().equals(dev.getAddress()))
				return true;
		}
		return false;
	}
	
	private boolean hasDeviceConnected(BluetoothDevice device) {
		if (Utility.iNetorBTcomm==0) {
			if (currentDevice != null
					&& currentDevice.getAddress().equals(device.getAddress())
					&& currentDevice.isConnected()) {
				return true;
			}
		}
		return false;
	}
	
	private void refreshList() {
		Log.i(TAG, "refreshList");
		if (foundDevices == null)
			return;
		
		if (foundDevices.size() <= 0) {
			listView.setAdapter((new ArrayAdapter<String>(getActivity(),
	                android.R.layout.simple_list_item_1, new String[]{"Not Found"})));
		} else {
			adapter = new DeviceListAdaptor();
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(itemClickListener);
		}
	}
	
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (foundDevices.size() > 0) {
				Boolean returnValue = false;  
				BluetoothDevice btDev = foundDevices.get(position);
				if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {  
					// 利用反射方法调用BluetoothDevice.createBond(BluetoothDevice
					// remoteDevice);
					Method createBondMethod;
					try {
						Class[] par = {};
						createBondMethod = BluetoothDevice.class.getMethod("createBond", par);
						returnValue = (Boolean) createBondMethod.invoke(btDev);  
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
						//Toast.makeText(getActivity(), "NoSuchMethodException", Toast.LENGTH_LONG).show();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						//Toast.makeText(getActivity(), "IllegalArgumentException", Toast.LENGTH_LONG).show();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						//Toast.makeText(getActivity(), "IllegalAccessException", Toast.LENGTH_LONG).show();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						//Toast.makeText(getActivity(), "InvocationTargetException", Toast.LENGTH_LONG).show();
					}
					Log.d(TAG, "开始配对");
				} else if (currentDevice==null){
					//connect
					connect(btDev);
				} else {
					Toast.makeText(getActivity(), Strings.getString(R.string.msg_already_connect), Toast.LENGTH_LONG).show();
				}
			}
		}
	};
	
	private class DeviceListAdaptor extends BaseAdapter {

		@Override
		public int getCount() {
			return foundDevices.size();
		}

		@Override
		public Object getItem(int position) {
			return foundDevices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BluetoothDevice device = foundDevices.get(position);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.device_list_item, null);
			TextView nameTextView = (TextView) view.findViewById(R.id.txt_device_name);
			nameTextView.setText(device.getName());
			TextView macTextView = (TextView) view.findViewById(R.id.txt_device_mac);
			macTextView.setText(device.getAddress());
			
			ImageView statusImageView = (ImageView) view.findViewById(R.id.ic_connect_status);
			
			TextView connectTextView = (TextView) view.findViewById(R.id.txt_connect_status);
			if (hasDeviceConnected(device)) {
				//connectTextView.setText("Connected");
                Utility.iNetorBTcomm = 0; //0--BT,1--NET,是蓝牙还是网络通信
				connectTextView.setText(Strings.getString(R.string.connected));
				connectTextView.setTextColor(Color.GREEN);
				statusImageView.setImageResource(R.drawable.ic_bluetooth_connected);
			} else {
				//connectTextView.setText("Disconnectd");
				connectTextView.setText(Strings.getString(R.string.notconnected));
				connectTextView.setTextColor(Color.RED);
				statusImageView.setImageResource(R.drawable.ic_bluetooth);
			}
			return view;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult");
		switch(requestCode) {
			case REQUEST_ENABLE_BT:  //enable bluetooth
			{
				if (resultCode == Activity.RESULT_CANCELED) { 
					AlertDialog dialog = new AlertDialog.Builder(getActivity()).
							setTitle(Strings.getString(R.string.msg_waring))
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage(Strings.getString(R.string.msg_bt_not_enable))
							.setPositiveButton(Strings.getString(R.string.msg_ok), new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//getActivity().finish();
								}
							}).create();
					dialog.show();
				} else {
					hasRequetBt = false;
				}
				break;
			}

			default:
				break;
		}
	}
	
	private Handler handle = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			case CONNECTING:
				progressDialog.setMessage(Strings.getString(R.string.msg_connecting));
				progressDialog.show();
				break;
			case CONNECTING_OK:
				progressDialog.setMessage(Strings.getString(R.string.msg_connected));
				progressDialog.show();
				progressDialog.dismiss();
				refreshList();
				break;
			case CONNECTING_FAILE:
				//progressDialog.setMessage("Connecting failed...");
				//progressDialog.show();
				progressDialog.dismiss();
				Utility.WarningAlertDialg(getActivity(), "", Strings.getString(R.string.msg_connect_fail)).show();
				break;
			case DISCONNECT:
				break;
			}
		}	
	};
	
	@Override
	public void onStop() {
		Log.i(TAG, "onStop");
		EventBus.getDefault().unregister(this);
		deactivateBtMonitor();
		super.onStop();
	}
	
	@Override
	public void onPause() {
		Log.i(TAG, "onPause");
		EventBus.getDefault().unregister(this);
		super.onPause();
	}
	
	protected void freshConnectStatus() {
		assert(currentDevice!=null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disconnect();
	}

	private void disconnect() {
		if (currentDevice!=null && currentDevice.isConnected()) {
			try {
				currentDevice.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			currentDevice = null;
			refreshList();
		}
	}
	
	private void updateLang() {
		btnDisconnect.setText(Strings.getString(R.string.disconnect));
		btnScan.setText(Strings.getString(R.string.scan));

		//boolean bFlag = getConfigNetConnectStatus(getActivity());//Utility.iNetConnectd = 0; //0--没有连上,1--连上了。

		if (Utility.iNetConnectd==0){
			btnNetConnect.setText(Strings.getString(R.string.Connect));
		}else{
			btnNetConnect.setText(Strings.getString(R.string.DisConnect));
		}

		txtIp.setText(Strings.getString(R.string.deviceAddr));
		txtPort.setText(Strings.getString(R.string.devicePort));

		refreshList();
	}


	public static void setConfigCheckIp(Activity activity, String strcheck) {
		setString(activity, Device_check_IP, strcheck);
	}

	public static String getConfigCheckIp(Activity activity) {
		return getString(activity, Device_check_IP, "192.168.0.101");
	}

	public static void setConfigCheckPort(Activity activity, String strcheck) {
		setString(activity, Device_check_PORT, strcheck);
	}

	public static String getConfigCheckPort(Activity activity) {
		return getString(activity, Device_check_PORT, "8899");
	}

	private static void setString(Activity activity, String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private static String getString(Activity activity, String key, String defaultVal) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getString(key, defaultVal);
	}




}
