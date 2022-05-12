/**
 * 
 */
package com.vanch.vhxdemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lab.sodino.language.util.Strings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vanch.vhxdemo.helper.Utility;
import com.zma.vhxdemo.R;

import de.greenrobot.event.EventBus;

/**
 * @author lgnhm_000
 */
public class AccessUI extends Fragment  {
	
	private static final String TAG = "access";
	protected static final int MAX_LEN_PASSWD = 8;

	Thread threadcmd = null; //块的读写，锁定，杀死全部用线程实现
	private boolean threadcmdflag = false;
	private int iopt = 0; //1--读，2--写，3--锁， 4--杀

	public static class MaskEvent {

	}

	public static class AccessEvent {

	}

	public static class StatusChangeEvent {

	}

	public static class EpcSelectedEvent {
		Epc epc;

		public EpcSelectedEvent(Epc epc) {
			this.epc = epc;
		}

		public Epc getEpc() {
			return epc;
		}

		@Override
		public String toString() {
			return "EpcSelectedEvent [epc=" + epc + "]";
		}
	}

	ImageView statusOnImageView, statusTxImageView, statusRxImageView;
	ImageView detectStatusImageView, statusImageView;
	Button accessButton, maskButton;
	RadioGroup radioGroup;
	static EditText dataEditText, passwdEditText, epcEditText, addrEditText,
			lenEditText;
	static Spinner epcSpinner;
	static CheckBox dataCheckBox;
	static Epc epcToBeAccess;
	
	RadioButton radio_read, radio_write,radio_lock,radio_kill;
	
	//other widgets
	TextView textViewPasswd,textViewData,textViewLocationTitle,textViewDetect,
	textViewStatus;

	Status on = Status.ON, tx = Status.BAD, rx = Status.BAD, detect = Status.BAD,
			status = Status.BAD;

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		EventBus.getDefault().register(this);
		
		if (LinkUi.currentDevice !=null && LinkUi.currentDevice.isConnected())
			setOn(Status.ON);
		else
			setOn(Status.BAD);
		
		EpcSelectedEvent epcSelectedEvent = (EpcSelectedEvent) EventBus
				.getDefault().getStickyEvent(EpcSelectedEvent.class);

		if (epcSelectedEvent != null) {
			Log.i(TAG, "epc selected " + epcSelectedEvent);
			epcToBeAccess = epcSelectedEvent.getEpc();
			EventBus.getDefault().removeStickyEvent(epcSelectedEvent);
			epcEditText.setText(epcToBeAccess.getWATag()); //getId());
		}
	}

	public static AccessUI me;
	
//	private InputFilter passwdFilter = new InputFilter() {
//
//		@Override
//		public CharSequence filter(CharSequence source, int start, int end,
//				Spanned dest, int dstart, int dend) {
//			return null;
//		}
//	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		me = this;
		View view = inflater.inflate(R.layout.access, null);
		//Utility.wq_UdpSendData("AccessUI.java onCreateView!\n");

		statusOnImageView = (ImageView) view.findViewById(R.id.status_on);
		statusTxImageView = (ImageView) view.findViewById(R.id.status_tx);
		statusRxImageView = (ImageView) view.findViewById(R.id.status_rx);
		detectStatusImageView = (ImageView) view.findViewById(R.id.status_detect);
		statusImageView = (ImageView) view.findViewById(R.id.ic_connect_status);

		accessButton = (Button) view.findViewById(R.id.btn_access);
		accessButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//EventBus.getDefault().post(new AccessEvent());
				if (LinkUi.currentDevice != null) {

					threadcmd = null;
					threadcmdflag = true;
					int id = radioGroup.getCheckedRadioButtonId(); ////1--读，2--写，3--锁， 4--杀
					if (id == radio_read.getId()) {
						iopt = 1;////1--读，2--写，3--锁， 4--杀
						dataEditText.setText("");
					} else if (id == radio_write.getId()) {
						iopt = 2;////1--读，2--写，3--锁， 4--杀
					} else if (id == radio_lock.getId()) {
						iopt = 3;////1--读，2--写，3--锁， 4--杀
					} else if (id == radio_kill.getId()) {
						iopt = 4;////1--读，2--写，3--锁， 4--杀
					}else{
						iopt = 0;
					}

					threadcmd = new LibBlockcmd();
					threadcmd.start();
					//LibGetDatacmd();


				} else {

					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}
			}
		});

		maskButton = (Button) view.findViewById(R.id.btn_mask);
		maskButton.setVisibility(View.GONE); //隐藏掉
		maskButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//EventBus.getDefault().post(new MaskEvent()); //modi martrin 20180315
			}
		});
		
		

		radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_op);

		dataEditText = (EditText) view.findViewById(R.id.edit_data);
		dataEditText.addTextChangedListener(new TextWatcher() {
			private int selectionStart ;
            private int selectionEnd ;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
				selectionStart = dataEditText.getSelectionStart();
                selectionEnd = dataEditText.getSelectionEnd();
                
            	if (s.length() == 0)
					return;
                
				if (!Utility.isHexString(s.toString())) {
					updateText(s);
					Toast.makeText(getActivity(), Strings.getString(R.string.msg_data_must_be_hex), 
							Toast.LENGTH_LONG).show();
					return;
				}
			}
			
			private void updateText(Editable s) {
				s.delete(selectionStart-1, selectionEnd);
                int tempSelection = selectionStart;
                dataEditText.setText(s);
                dataEditText.setSelection(tempSelection);
			}
		});
		
		passwdEditText = (EditText) view.findViewById(R.id.edit_passwd);
		passwdEditText.addTextChangedListener(new TextWatcher() {
			private int selectionStart ;
            private int selectionEnd ;
			private CharSequence temp;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				Log.i(TAG, "onTextChanged " + s + " "+start + " "+ count+ this);
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
//				Log.i(TAG, "beforeTextChanged " + s + " "+start + " "+ count + this);
				temp = s;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0)
					return;
				selectionStart = passwdEditText.getSelectionStart();
                selectionEnd = passwdEditText.getSelectionEnd();
                
				if (!Utility.isHexString(s.toString())) {
					updateText(s);
					Toast.makeText(getActivity(), Strings.getString(R.string.msg_passwd_must_be_hex), Toast.LENGTH_LONG).show();
					return;
				}
				
				if (s.length() > MAX_LEN_PASSWD) {
					updateText(s);
					return;
				}
			}
			
			private void updateText(Editable s) {
				s.delete(selectionStart-1, selectionEnd);
                int tempSelection = selectionStart;
                passwdEditText.setText(s);
                passwdEditText.setSelection(tempSelection);
			}
		});
		epcEditText = (EditText) view.findViewById(R.id.edit_epc);
		addrEditText = (EditText) view.findViewById(R.id.edit_start);
		addrEditText.setText("0");
		lenEditText = (EditText) view.findViewById(R.id.edit_data_len);
		lenEditText.setText("4");

		epcSpinner = (Spinner) view.findViewById(R.id.spinner_epc);
		epcSpinner.setSelection(3);
		dataCheckBox = (CheckBox) view.findViewById(R.id.checkBox_data);
		
		radio_kill = (RadioButton) view.findViewById(R.id.radio_kill);
		radio_read = (RadioButton) view.findViewById(R.id.radio_read);
		radio_lock = (RadioButton) view.findViewById(R.id.radio_lock);
		radio_write = (RadioButton) view.findViewById(R.id.radio_write);
		
		//other widgets
//		TextView textViewPasswd,textViewData,textViewLocationTitle,textViewDetect,
//		textViewStatus;
		textViewPasswd = (TextView) view.findViewById(R.id.txt_UsbSend);
		
		textViewData = (TextView) view.findViewById(R.id.txt_data_title);
		textViewLocationTitle = (TextView) view.findViewById(R.id.txt_location_title);
		textViewDetect = (TextView) view.findViewById(R.id.txt_detect);
		textViewStatus = (TextView) view.findViewById(R.id.txt_status);
		
		updateLang();

		return view;
	}

	private void freshStatus() {
		Map<ImageView, Status> map = new HashMap<ImageView, Status>();
		map.put(statusOnImageView, on);
		map.put(statusTxImageView, tx);
		map.put(statusRxImageView, rx);
		map.put(detectStatusImageView, detect);
		map.put(statusImageView, status);

		for (ImageView imageView : map.keySet()) {
			Status status = map.get(imageView);
			switch (status) {
			case ON:
				imageView.setImageResource(R.drawable.ic_on);
				break;
//			case OFF:
//				imageView.setImageResource(R.drawable.ic_off);
//				break;
			case BAD:
				imageView.setImageResource(R.drawable.ic_unnormal);
				break;
			case INCOMPLETE:
				imageView.setImageResource(R.drawable.ic_unnormal);
				break;
			default:
				break;
			}
		}
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0x01:{
					String strTemp;
					strTemp = msg.obj.toString();
					dataEditText.setText(strTemp);
				}
				break;
				case 0x02: {
					Utility.WarningAlertDialg(getActivity(),
							"Warning",
							"Write Error!")
							.show();
				}
				break;
				case 0x03: {
					Utility.WarningAlertDialg(getActivity(),
							"Infomation",
							"Write Success!")
							.show();
				}
				break;


			}
		}
	};


	public Status getDetect() {
		return detect;
	}

	public void setDetect(Status detect) {
		this.detect = detect;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	public Status getOn() {
		return on;
	}

	public void setOn(Status on) {
		this.on = on;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	public Status getTx() {
		return tx;
	}

	public void setTx(Status tx) {
		this.tx = tx;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	public Status getRx() {
		return rx;
	}

	public void setRx(Status rx) {
		this.rx = rx;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	/**
	 * when status changed
	 * 
	 * @param e
	 */
	public void onEventMainThread(StatusChangeEvent e) {
		freshStatus();
	}

	public void onEventBackgroundThread(AccessEvent e) {
		int id = radioGroup.getCheckedRadioButtonId();
		
		if (id == radio_read.getId()) {
			readData();
		} else if (id == radio_kill.getId()) {
			killEpc();
		} else if (id == radio_write.getId()) {
			writeData();
		} else {
			lockEpc();
		}
	}



	

	public void onEventBackgroundThread(MaskEvent e) {
		// TODO do mask
		if (LinkUi.currentDevice != null) {
			//				LinkUi.currentDevice.mask();
			Utility.showDialogInNonUIThread(getActivity(), "Warning", "未实现");
		} else {
			Utility.showDialogInNonUIThread(getActivity(), "Warning",
					"No devices connected!");
		}
	}
	
	public void onEventMainThread(ConfigUI.LangChanged e) {
		updateLang();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStop() {
		Log.i(TAG, "onStop");
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "onPause");
		EventBus.getDefault().unregister(this);
		super.onPause();
	}
	
	private void refreshSpinner() {
		String[] epcs = Strings.getStringArray(R.array.epcs);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity() , android.R.layout. simple_spinner_item , epcs );
		adapter.setDropDownViewResource(android.R.layout. simple_spinner_dropdown_item );
		epcSpinner.setAdapter(adapter);
	}
	
	private void updateLang() {
//		refreshSpinner();
		accessButton.setText(Strings.getString(R.string.btn_access));
		maskButton.setText(Strings.getString(R.string.mask));
		textViewPasswd.setText(Strings.getString(R.string.label_passwd));
		radio_read.setText(Strings.getString(R.string.read));
		radio_kill.setText(Strings.getString(R.string.kill));
		radio_lock.setText(Strings.getString(R.string.lock));
		radio_write.setText(Strings.getString(R.string.write));
		textViewData.setText(Strings.getString(R.string.str_data));
		dataEditText.setHint(Strings.getString(R.string.data_hint));
		textViewLocationTitle.setText(Strings.getString(R.string.title_access_location));
		textViewDetect.setText(Strings.getString(R.string.detect));
		textViewStatus.setText(Strings.getString(R.string.status));
	}

//	@Override
//	public void onCheckedChanged(RadioGroup group, int checkedId) {
//		switch(checkedId) {
//		case 0:
//			readData();
//			break;
//		case 1:
//			writeData();
//			break;
//		case 2:
//			lockEpc();
//			break;
//		case 4:
//			killEpc();
//			break;
//		default:
//			break;
//		}
//		
//	}

	private void killEpc() {
		if(!checkAccessKillEpc())
			return;
		
		String epc = epcToBeAccess.getWATag(); //getId();
		String passwd = passwdEditText.getText().toString();
		
		try {
			LinkUi.currentDevice.KillTag(epc, passwd);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_kill_succ));
			} else {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_kill_fail));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
//			e.printStackTrace();
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_kill_timeout));
		}
	}
	
	private boolean checkAccessKillEpc() {
		//if (epcToBeAccess == null || epcToBeAccess.getId().length() <= 0) {
		if (epcToBeAccess == null || epcToBeAccess.getWATag().length() <= 0) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_epc_must_larger_zero));
			return false;
		}
		
		if (passwdEditText.getText().length() < 8) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_passwd_len_must_eight));
			return false;
		}
		return true;
	}

	private void eraseBlock() {
		if (!checkAccessEraseEnable())
			return;
		
		String epc = epcToBeAccess.getWATag(); //getId();
		int mem = epcSpinner.getSelectedItemPosition();
		int addr = Integer.valueOf(addrEditText.getText().toString());
		int len = Integer.valueOf(lenEditText.getText().toString());
		
		try {
			LinkUi.currentDevice.EraseBlock(epc, mem, addr, len);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_erase_ok));
			} else {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_erase_fail));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
//			e.printStackTrace();
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_erase_timeout));
		}
	}

	private boolean checkAccessEraseEnable() {
		//if (epcToBeAccess == null || epcToBeAccess.getId().length() <= 0) {
		if (epcToBeAccess == null || epcToBeAccess.getWATag().length() <= 0) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_epc_must_larger_zero));
			return false;
		}
		
		if (passwdEditText.getText().length() < 8) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_passwd_len_must_eight));
			return false;
		}
		
		if (addrEditText.getText().length() <= 0 || !Utility.isHexString(addrEditText.getText().toString()) ) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_addr_must_be_hex));
			return false;
		}
		
		if (lenEditText.getText().length() <= 0|| !Utility.isHexString(lenEditText.getText().toString())) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_len_must_be_hex));
			return false;
		}
		
		return true;
	}

	private void lockEpc() {
		if (!checkAccessLock())
			return;
		String epc = epcToBeAccess.getWATag(); //getId();
		String passwd = passwdEditText.getText().toString();
		
		try {
			LinkUi.currentDevice.BlockLock(epc, passwd);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_blocklock_succ));
			} else {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_blocklock_fail));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
//			e.printStackTrace();
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_blocklock_timeout));
		}
	}

	private boolean checkAccessLock() {
		//if (epcToBeAccess == null || epcToBeAccess.getId().length() <= 0) {
		if (epcToBeAccess == null || epcToBeAccess.getWATag().length() <= 0) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_epc_must_larger_zero));
			return false;
		}
		
		if (passwdEditText.getText().length() < 8) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_passwd_len_must_eight));
			return false;
		}
		return true;
	}

	private void writeData() {
		if (!checkAccessWriteEnable())
			return;
		
		int mem,addr,lenData;
		String passwd = "00000000";
		String data;
		
		mem = epcSpinner.getSelectedItemPosition();
		
		//len
		if (addrEditText.getText().length() > 0 && Utility.isHexString(addrEditText.getText().toString()))
			addr = Integer.valueOf(addrEditText.getText().toString());
		else
			addr = 0;
		
		if (passwdEditText.getText().length() != 0) {
			passwd = passwdEditText.getText().toString();
		}
		
		data = dataEditText.getText().toString();
		
		try {
			//LinkUi.currentDevice.WriteWordBlock(epcToBeAccess.getId(), mem, addr, data, passwd);
			LinkUi.currentDevice.WriteWordBlock(epcToBeAccess.getWATag(), mem, addr, data, passwd);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_wirtewordblock_succ));
			} else {
				Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_wirtewordblock_fail));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
//			e.printStackTrace();
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_wirtewordblock_timeout));
		}
	}

	private boolean checkAccessWriteEnable() {
		//if (epcToBeAccess == null || epcToBeAccess.getId().length() <= 0) {
		if (epcToBeAccess == null || epcToBeAccess.getWATag().length() <= 0) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_epc_must_larger_zero));
			return false;
		}
		
		if (dataEditText.getText().length() <= 0) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_data_must_be_hex));
			return false;
		}

		return true;
	}

	//ReadWordBlock
	private void readData() {
		
		if (!checkAccessReadEnable()) {
			return;
		}
		int mem,addr,lenData;
		String passwd = "00000000";
		
		//if (epcToBeAccess==null || epcToBeAccess.getId().length() <= 0)
		if (epcToBeAccess==null || epcToBeAccess.getWATag().length() <= 0)
			return;
		mem = epcSpinner.getSelectedItemPosition();
		
		//len
		if (addrEditText.getText().length() > 0 && Utility.isHexString(addrEditText.getText().toString()))
			addr = Integer.valueOf(addrEditText.getText().toString());
		else
			addr = 0;
		// addr
		if (lenEditText.getText().length() > 0 && Utility.isHexString(lenEditText.getText().toString()))
			lenData = Integer.valueOf(lenEditText.getText().toString());
		else 
			lenData = 0;
		
		if (passwdEditText.getText().length() != 0) {
			passwd = passwdEditText.getText().toString();
		}
		
		try {
			//LinkUi.currentDevice.ReadWordBlock(epcToBeAccess.getId(), mem, addr, lenData, passwd);
			LinkUi.currentDevice.ReadWordBlock(epcToBeAccess.getWATag(), mem, addr, lenData, passwd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//read
		try {
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_ReadWordBlock_title) + Utility.bytes2HexString(ret));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
//			e.printStackTrace();
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_ReadWordBlock_timeout));
		}
	}
	
	/**
	 * check data length ......
	 * @return
	 */
	private boolean checkAccessReadEnable() {
		//if (epcToBeAccess == null || epcToBeAccess.getId().length() <= 0) {
		if (epcToBeAccess == null || epcToBeAccess.getWATag().length() <= 0) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_epc_must_larger_zero));
			return false;
		}
		
		if (passwdEditText.getText().length() < 8) {
			Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_passwd_len_must_eight));
			return false;
		}
		
		return true;
	}
	
	
	public void onEventMainThread(VH73Device.GetCommandResultSuccess e) {
		if (e.isSuccess())
			setRxWithBlink(ConfigUI.cmd_timeout, Status.ON, Status.BAD);
		else
			setRx(Status.BAD);
	}
	
	public void onEventMainThread(VH73Device.SendCommandSuccess e) {
		if (e.isSuccess()) {
			setTxWithBlink(ConfigUI.cmd_timeout, Status.ON, Status.BAD);
		}
		else {
			setTx(Status.BAD);
		}
	}
	
	/**
	 * tx blink 
	 * @param timeout
	 * @param from
	 * @param to
	 */
	private void setTxWithBlink(final long timeout, Status from, final Status to) {
		setTx(from);
		new Thread() {
			public void run() {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				new Thread() {
					public void run() {
						Looper.prepare();
						setTx(to);
					}
				}.start();
			}
		}.start();
	}
	
	private void setRxWithBlink(final long timeout, Status from, final Status to) {
		setRx(from);
		new Thread() {
			public void run() {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				new Thread() {
					public void run() {
						Looper.prepare();
						setRx(to);
					}
				}.start();
			}
		}.start();
	}

	//块读
	private void ZMBlockRead(){
		try {
			//select 选择标签 BB 00 0C 00 0D 01 00 00 00 20 60 00 C2 ED C8 AB CB AE 35 7E
			//BB 01 0C 00 01 00 0E 7E
			int ival = 0; //
			boolean bflag = true; //false;
			int ik = 0;
            int ilen = 0;
            int iret = 0;
            byte[] epc = new byte[64];

			byte[] dat = new byte[256];

//			for (ik = 0; ik < 3; ik++) {
//				LinkUi.currentDevice.ZM_SelectTag(epcEditText.getText().toString()); //"C2EDC8ABCBAE");
//				Thread.sleep(200);
//				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
//				if (aa != null && aa.length > 0) {
//					//if (aa.length==9 && aa[2] == 0xCE){
//					if (aa[2] == (byte) 0x0C) {
//						//itot = aa[5] << 8 | aa[6];
//						ival = ((int) (aa[5]  & 0xFF));
//						if (ival == 0) {
//							bflag = true;
//							break;
//						}
//					}
//				}
//			}

			int mem = epcSpinner.getSelectedItemPosition();
			int addr = Integer.valueOf(addrEditText.getText().toString());
			int len = Integer.valueOf(lenEditText.getText().toString());
			String passwd = passwdEditText.getText().toString();

			if (bflag){
				bflag = false;
				for (ik = 0; ik < 3; ik++) {
					//3--user 0--是ADDR 8--指长度 "00000000"为密码
					//LinkUi.currentDevice.ZM_ReadWordBlock(epcEditText.getText().toString(), 3, 0, 8, "00000000"); //"C2EDC8ABCBAE");
					LinkUi.currentDevice.ZM_ReadWordBlock(epcEditText.getText().toString(), mem, addr, len, passwd); //"C2EDC8ABCBAE");
					Thread.sleep(500);
					byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
					if (aa != null && aa.length > 0) {
						//if (aa.length==9 && aa[2] == 0xCE){
						if (aa[2] == (byte) 0x39) {
								bflag = true;
                                ilen = aa[5]-2;
                             if (ilen >0){
                                 String strTemp;
                                 for (iret = 0; iret < ilen; iret++) {
                                     epc[iret] = aa[iret + 8];
                                 }
                                 ik = iret;
								 for (iret = 0; iret < 2*len; iret++) {
									 epc[iret] = aa[iret + 8+ik];
								 }
                                 strTemp = Utility.getStringByByteslen(epc, 2*len);
                                 Log.d(TAG, "Datt:" + strTemp);
                                 //dataEditText.setText(strTemp);
								 Message msg = new Message();
								 msg.what = 0x01;
								 //msg.arg1 = (int) epcInfo.getId().longValue(); //0; //i;
								 msg.obj = strTemp.toString(); //0; //i;

								 myHandler.sendMessage(msg);
                             }
								break;
						}else{
							bflag = false;
						}

					}
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}


	//块写
	private void ZMBlockWrite(){
		try {

			//select 选择标签 BB 00 0C 00 0D 01 00 00 00 20 60 00 C2 ED C8 AB CB AE 35 7E
			//BB 01 0C 00 01 00 0E 7E
			//==>
			//send:BB 00 0C 00 23 01 00 00 00 20 E0 00 88 88 99 95 31 10 00 62 20 90 38 99 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 92 7E
			//=>命令字:0C 长度:0023 (参数的长度) 01 =>SelParam: 0x01 (Target: 3’b000, Action: 3’b000, MemBank: 2’b01)
			//00 00 00 20 =>Ptr: 0x00000020(以 bit 为单位，非 word) 从 EPC 存储位开始
			//E0 =>Mask长度 MaskLen: 0x60(6 个 word，96bits) 00=>是否 Truncate: 0x00(0x00 是 Disable truncation，0x80 是 Enable truncation
			//epc:88 88 99 95 31 10 00 62 20 90 38 99 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
			//check:92 结尾:7E
			//recv:BB 01 0C 00 01 00 0E 7E
			int ival = 0; //
			boolean bflag = true; //false;
			int ik = 0;

//			for (ik = 0; ik < 3; ik++) {
//				LinkUi.currentDevice.ZM_SelectTag(epcEditText.getText().toString()); //"C2EDC8ABCBAE");
//				Thread.sleep(200);
//				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
//				if (aa != null && aa.length > 0) {
//					//if (aa.length==9 && aa[2] == 0xCE){
//					if (aa[2] == (byte) 0x0C) {
//						//itot = aa[5] << 8 | aa[6];
//						ival = ((int) (aa[5]  & 0xFF));
//						if (ival == 0) {
//							bflag = true;
//							break;
//						}
//					}
//				}
//			}

			int mem = epcSpinner.getSelectedItemPosition();
			int addr = Integer.valueOf(addrEditText.getText().toString());
			int len = Integer.valueOf(lenEditText.getText().toString());
			String passwd = passwdEditText.getText().toString();
			String data;
			data = dataEditText.getText().toString();
			int id = data.length();
			if ( id == len*4){
				for (int i = 0; i < id; i++){
					String stra;
					String regex;
					stra = data.substring(i,i+1).toLowerCase();
					regex = "^[a-z0-9A-Z]+$";//其他需要，直接修改正则表达式就好
					if ( stra.matches(regex) ){
						//ok
					}else{
						Message msg = new Message();
						msg.what = 0x02;
						//msg.arg1 = (int) epcInfo.getId().longValue(); //0; //i;
						msg.obj = "";
						//写入错误
						myHandler.sendMessage(msg);
						break;
					}
				}
			}else{
				Message msg = new Message();
				msg.what = 0x02;
				//msg.arg1 = (int) epcInfo.getId().longValue(); //0; //i;
				msg.obj = "";
				//写入错误
				myHandler.sendMessage(msg);
			}

			//send:BB 00 49 00 25 00 00 00 00 01 00 00 00 0E 1A DF 70 70 70 00 89 88 99 95 31 10 00 62 20 90 00 00 00 00 00 00 00 00 00 00 00 00 58 7E
			//49 命令字 长度:0025 Access Password:00 00 00 00 写epc:0x01 从00 00 开始写 写00 0E那么长 数据:1A DF 70 70 70 00 89 88 99 95 31 10 00 62 20 90 00 00 00 00 00 00 00 00 00 00 00 00
			//58为校验 结束:7E
			//recv:BB 01 49 00 20 1E 70 70 70 00 89 88 99 95 31 10 00 62 20 90 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6A 7E
			if (bflag){
				bflag = false;
				for (ik = 0; ik < 3; ik++) {
					//3--user 0--是ADDR 8--指长度 "00000000"为密码
					LinkUi.currentDevice.ZM_WriteWordBlock(data, mem, addr, len, passwd); //"C2EDC8ABCBAE");
					Thread.sleep(500);
					byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
					if (aa != null && aa.length > 0) {
						//if (aa.length==9 && aa[2] == 0xCE){
						if (aa[2] == (byte) 0x49) {
							ival = ((int) (aa[4]  & 0xFF));
							if (aa[ival+5-1]==0x00){
								bflag = true;

							}else {
								bflag = false;
							}
							break;
						}else{
							bflag = false;
						}

					}
				}

				if ( bflag ){
					Message msg = new Message();
					msg.what = 0x03;
					//msg.arg1 = (int) epcInfo.getId().longValue(); //0; //i;
					msg.obj = "";
					//写入成功
					myHandler.sendMessage(msg);
				}else{
					Message msg = new Message();
					msg.what = 0x02;
					//msg.arg1 = (int) epcInfo.getId().longValue(); //0; //i;
					msg.obj = "";
					//写入失败
					myHandler.sendMessage(msg);
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	//块锁
	private void ZMBlocklock(){
		try {

			//00000C1234123412341234123412348E7E
			//BB 02 22 00 11 D5 34 00 12 34 12 34 12 34 12 34 12 34 12 34 8F 5A CB 7E //加7
			int inumber = 10; //这么条一传
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.listTagIDTotal();//获取总数
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[2] == (byte) 0xCE) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) (aa[5] << 8) | (int) aa[6] & 0xFF);
						if (itot>0){
							break;
						}
					}
				}
			}


		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	//块杀死
	private void ZMBlockkill(){
		try {

			//00000C1234123412341234123412348E7E
			//BB 02 22 00 11 D5 34 00 12 34 12 34 12 34 12 34 12 34 12 34 8F 5A CB 7E //加7
			int inumber = 10; //这么条一传
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.listTagIDTotal();//获取总数
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[2] == (byte) 0xCE) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) (aa[5] << 8) | (int) aa[6] & 0xFF);
						if (itot>0){
							break;
						}
					}
				}
			}


		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	class LibBlockcmd extends Thread {
		@Override
		public void run() {
			super.run();

			if (threadcmdflag) {
				//private int iopt = 0; //1--读，2--写，3--锁， 4--杀
				switch (iopt)
				{
					case 1: //1--读，
					{
						ZMBlockRead();
					}
					break;
					case 2: //2--写，
					{
						ZMBlockWrite();
					}
					break;
					case 3: //3--锁，
					{
						ZMBlocklock();
					}
					break;
					case 4: //4--杀，
					{
						ZMBlockkill();
					}
					break;
				}

			}
		}
	}


}
