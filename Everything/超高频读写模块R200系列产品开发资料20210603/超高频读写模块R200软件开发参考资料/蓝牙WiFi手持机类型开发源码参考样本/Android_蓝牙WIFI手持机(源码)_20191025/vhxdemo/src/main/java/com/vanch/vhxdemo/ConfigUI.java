package com.vanch.vhxdemo;

import lab.sodino.language.util.Strings;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.iot.wantrue.lib.wlib;
import com.vanch.vhxdemo.helper.Utility;
import com.zma.vhxdemo.R;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import lib.AttendanceLib;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class ConfigUI extends Fragment {
	public static class LangChanged {

	}

	// Context context = getActivity();
	CheckBox autolinkBox, detectsoundBox, skipnameBox, singletagbBox,
			checkshocCheckBox;
	Spinner sSpinner, abSpinner, qSpinner;
	Spinner spinner_frequency;
	EditText timeouEditText;
	EditText EditTextip;
	EditText EditTextport;
	SeekBar powerSeekBar;
	TextView powerTextView, swTextView, hdTextView, verTextView;
	ConfigParam mParam;
	private RadioButton checkBoxLangEng, checkBoxLangCh;
	private RadioButton radioButtontypea, radioButtontypeb;
	CheckBox checkBoxCheckUpdate;
	CheckBox checkBoxCheckTest;
    Button btn_beepon;
    Button btn_beepclose;
    Button btn_poweronreadcard;
    Button btn_poweroffreadcard;
	Button btn_setfrequency;
	Button btn_getfrequency;
	Button btn_getpower;
	Button btn_setpower;

	// other widgets
	TextView textViewQueryTitle, textViewSession, textViewTimeout, textViewPower,
			textViewSoftVer, textViewHardVer;
	
	public static final long cmd_timeout = 500;
	
	public static final String key_autolink = "autolink";
	public static final String key_checksound = "check_sound";
	public static final String key_skipname = "skip_name";
	public static final String key_singletag = "single_tag";
	public static final String key_checkshock = "check_shock";
	public static final String key_session_s = "session_s";
	public static final String key_session_ab = "session_ab";
	public static final String key_session_q = "session_q";
	public static final String key_session_timeout = "session_timeout";
	public static final String key_power = "power";
	public static final String key_last_connect = "last_connect";
	public static final String key_check_update = "check_update";
	public static final String key_check_Test = "check_Test";
	public static final String key_check_IP = "check_ip";
	public static final String key_check_PORT = "check_port";
	private static final String TAG = "config";

	public ConfigUI() {
		// Required empty public constructor
	}

	public static ConfigUI me;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		me =this;
		View view = inflater.inflate(R.layout.config, null);
		//Utility.wq_UdpSendData("ConfigUI.java onCreateView!\n");

		autolinkBox = (CheckBox) view.findViewById(R.id.radio_autolink);
		detectsoundBox = (CheckBox) view.findViewById(R.id.radio_detectsound);
		skipnameBox = (CheckBox) view.findViewById(R.id.radio_skipname);
		singletagbBox = (CheckBox) view.findViewById(R.id.radio_singletag);
		singletagbBox.setVisibility(View.INVISIBLE);// 隐藏单标签,modify by martrin
													// 20131114

		checkshocCheckBox = (CheckBox) view.findViewById(R.id.radio_chechshock);

		sSpinner = (Spinner) view.findViewById(R.id.spinner_s);
		abSpinner = (Spinner) view.findViewById(R.id.spinner_ab);
		qSpinner = (Spinner) view.findViewById(R.id.spinner_q);
		spinner_frequency = (Spinner) view.findViewById(R.id.spinner_frequency);

		timeouEditText = (EditText) view.findViewById(R.id.edit_timeout);
		EditTextip = (EditText) view.findViewById(R.id.editTextip);
		// EditTextip.setText("192.168.0.71");//在后面有给值
		EditTextport = (EditText) view.findViewById(R.id.editTextport);
		// EditTextport.setText("8886");

		checkBoxLangCh = (RadioButton) view.findViewById(R.id.radio_ch);
		checkBoxLangCh.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Strings.setLanguage(getActivity(), Strings.LANGUAGE_CHINESE);
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
					editor.putString(MainActivity.lang_key, Strings.LANGUAGE_CHINESE);
					editor.commit();
					EventBus.getDefault().post(new LangChanged());
				}
			}
		});

		checkBoxLangEng = (RadioButton) view.findViewById(R.id.radio_eng);
		checkBoxLangEng.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Strings.setLanguage(getActivity(), Strings.LANGUAGE_ENGLISH);
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
					editor.putString(MainActivity.lang_key, Strings.LANGUAGE_ENGLISH);
					editor.commit();
					EventBus.getDefault().post(new LangChanged());
				}
			}
		});

		radioButtontypea = (RadioButton) view.findViewById(R.id.radioButtontypea);
		radioButtontypea.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Utility.iopt = 0;
					EventBus.getDefault().post(new LangChanged());
				}
			}
		});


		radioButtontypeb = (RadioButton) view.findViewById(R.id.radioButtontypeb);
		radioButtontypeb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Utility.iopt = 1;
					EventBus.getDefault().post(new LangChanged());
				}
			}
		});


		powerSeekBar = (SeekBar) view.findViewById(R.id.seekBar1);
		powerSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				powerTextView.setText("" + (progress-9 ));
			}
		});

		powerTextView = (TextView) view.findViewById(R.id.text_power);
		swTextView = (TextView) view.findViewById(R.id.text_sw_version);
		hdTextView = (TextView) view.findViewById(R.id.text_hd_version);
		verTextView = (TextView) view.findViewById(R.id.text_ver);
		verTextView.setText("V" + getVerStr());
		verTextView.setVisibility(View.INVISIBLE);

		// other widgets
		textViewQueryTitle = (TextView) view.findViewById(R.id.txt_queryparamtitle);
		textViewSession = (TextView) view.findViewById(R.id.txt_session);

		textViewTimeout = (TextView) view.findViewById(R.id.txt_timeout);
		textViewPower = (TextView) view.findViewById(R.id.text_txpower);
		textViewSoftVer = (TextView) view
				.findViewById(R.id.text_sofe_version_title);
		textViewHardVer = (TextView) view
				.findViewById(R.id.text_hd_version_title);
		
		checkshocCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigCheckshock(getActivity(), isChecked);
			}
		});
		
		skipnameBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//setConfigSkipsame(getActivity(), isChecked);
				setConfigChecksound(getActivity(), isChecked);
			}
		});
		
		detectsoundBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigChecksound(getActivity(), isChecked);
			}
		});
		
		singletagbBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//setConfigSingletag((getActivity(), isChecked);
				setConfigSingletag(getActivity(), isChecked);
			}
		});

		autolinkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigAutolink(getActivity(), isChecked);
			}
		});
		
		
		timeouEditText.addTextChangedListener(new TextWatcher() {
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
				if (s.length() == 0)
					return;
				selectionStart = timeouEditText.getSelectionStart();
                selectionEnd = timeouEditText.getSelectionEnd();
                
				if (!Utility.isNumber(s.toString())) {
					updateText(s);
					Toast.makeText(getActivity(), "Timeout  must be number...", Toast.LENGTH_LONG).show();
					return;
				}
				setConfigTimeout(getActivity(), Integer.valueOf(s.toString()));
			}
			
			private void updateText(Editable s) {
				s.delete(selectionStart-1, selectionEnd);
                int tempSelection = selectionStart;
                timeouEditText.setText(s);
                timeouEditText.setSelection(tempSelection);
			}
		});
		
		// ////////////////////////////////////////////////////////////////
		EditTextip.addTextChangedListener(new TextWatcher() {
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0)
					return;
				selectionStart = EditTextip.getSelectionStart();
				selectionEnd = EditTextip.getSelectionEnd();

				setConfigCheckIp(getActivity(), (s.toString()));
			}

			private void updateText(Editable s) {
				s.delete(selectionStart - 1, selectionEnd);
				int tempSelection = selectionStart;
				EditTextip.setText(s);
				EditTextip.setSelection(tempSelection);
			}
		});

		EditTextport.addTextChangedListener(new TextWatcher() {
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0)
					return;
				selectionStart = EditTextport.getSelectionStart();
				selectionEnd = EditTextport.getSelectionEnd();

				if (!Utility.isNumber(s.toString())) {
					updateText(s);
					Toast.makeText(getActivity(), "port  must be number...",
							Toast.LENGTH_LONG).show();
					return;
				}
				setConfigCheckPort(getActivity(), (s.toString()));
			}

			private void updateText(Editable s) {
				s.delete(selectionStart - 1, selectionEnd);
				int tempSelection = selectionStart;
				EditTextport.setText(s);
				EditTextport.setSelection(tempSelection);
			}
		});
		// /////////////////////////////////////////////////////////////////////////////

		checkBoxCheckUpdate = (CheckBox) view.findViewById(R.id.radio_check_update);
		checkBoxCheckUpdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigCheckUpdate(getActivity(), isChecked);
			}
		});
		
		// add by martrin 20131114
		checkBoxCheckTest = (CheckBox) view.findViewById(R.id.checkBoxTest);
		checkBoxCheckTest
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setConfigCheckTest(getActivity(), isChecked);
					}
				});


        btn_beepon = (Button) view.findViewById(R.id.btn_beepon);
        btn_beepon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
				//打开蜂鸣器指令：
				if (LinkUi.currentDevice != null) {
					ZMLibBeepOnCmd();
				}else{
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}
            }
        });

        btn_beepclose = (Button) view.findViewById(R.id.btn_beepclose);
        btn_beepclose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
				//关闭蜂鸣器指令：
				if (LinkUi.currentDevice != null) {
					ZMLibBeepCloseCmd();
				}else{
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}

            }
        });

        btn_poweronreadcard = (Button) view.findViewById(R.id.btn_poweronreadcard);
        btn_poweronreadcard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //打开上电读卡指令
                if (LinkUi.currentDevice != null) {
					//1--上电打开读卡，0--上电关闭读卡
					ZMLibPoweronoffReadCardCmd(1);
                }else{
                    Utility.WarningAlertDialg(getActivity(),
                            Strings.getString(R.string.msg_waring),
                            Strings.getString(R.string.msg_device_not_connect))
                            .show();
                }
            }
        });

        btn_poweroffreadcard = (Button) view.findViewById(R.id.btn_poweroffreadcard);
        btn_poweroffreadcard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //关闭上电读卡指令
                if (LinkUi.currentDevice != null) {
					//1--上电打开读卡，0--上电关闭读卡
					ZMLibPoweronoffReadCardCmd(0);
                }else{
                    Utility.WarningAlertDialg(getActivity(),
                            Strings.getString(R.string.msg_waring),
                            Strings.getString(R.string.msg_device_not_connect))
                            .show();
                }
            }
        });

		btn_getfrequency = (Button) view.findViewById(R.id.btn_getfrequency);
		btn_getfrequency.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//获取频率
				ZMLibTestCmd(0);
			}
		});

		btn_setfrequency = (Button) view.findViewById(R.id.btn_setfrequency);
		btn_setfrequency.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//设置频率
				int region = 0x01;
				region = (int)spinner_frequency.getSelectedItemId();
				if ( region == 0 ){
					region = 0x01;
				}else if ( region == 1 ){
					region = 0x04;
				}else if ( region == 2 ){
					region = 0x02;
				}else if ( region == 3 ){
					region = 0x03;
				}else if ( region == 4 ){
					region = 0x06;
				}
				if (LinkUi.currentDevice != null) {
					ZMLibSetRegionCmd(region);
				}else{
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}

			}
		});


		btn_getpower = (Button) view.findViewById(R.id.btn_getpower);
		btn_getpower.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//读取功率
				if (LinkUi.currentDevice != null) {
					ZMLibGetPowerCmd();
				}else{
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}

			}
		});

		btn_setpower = (Button) view.findViewById(R.id.btn_setpower);
		btn_setpower.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//设置功率
				int power = 0;
				String strpowr = powerTextView.getText().toString();
				power = Integer.parseInt(strpowr);
				//ZMLibSetPowerCmd(26);
				if (LinkUi.currentDevice != null) {
					ZMLibSetPowerCmd(power);
				}else{
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}


			}
		});

		byte [] vers = new byte[3];
		//AttendanceLib.GetLibVersion(vers);//这两个都可以，这个是调用G:\Javab\project\androidstudio\vhxdemo\vhxdemo\jni\AttendanceLib.c
		wlib.GetLibVersion(vers);//这个也可以 G:\Javab\project\UhfSDKdemo_Lock\jni\wantruelib.c
		updateLang();

		return view;
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0x01:

					break;
				case 0x10:
					Toast.makeText(getActivity(), "OK!", Toast.LENGTH_LONG).show();
					break;
				case 0x11:
					Toast.makeText(getActivity(), "Error!", Toast.LENGTH_LONG).show();
					break;

			}
		}
	};

	private void ZMLibBeepOnCmd(){
		try {
			//打开蜂鸣器指令： BB 00 99 00 01 01 9B 7E  设置成功返回：BB 01 99 00 01 00 9B 7E

			//关闭蜂鸣器指令： BB 00 99 00 01 00 9A 7E  设置成功返回：BB 01 99 00 01 00 9B 7E
			int inumber = 10; //这么条一传
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.ZMABeepOn();//打开蜂鸣器指令
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[1] == (byte) 0x01 && aa[2] == (byte) 0x99) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) aa[5] & 0xFF);
						if (itot==0){
							break;
						}
					}
				}
			}

			if (ik < im){
				myHandler.sendEmptyMessage(0x10);
			}else{
				myHandler.sendEmptyMessage(0x11);
			}



		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	private void ZMLibBeepCloseCmd(){
		try {
			//关闭蜂鸣器指令： BB 00 99 00 01 00 9A 7E  设置成功返回：BB 01 99 00 01 00 9B 7E
			int inumber = 10; //这么条一传
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.ZMABeepClose();//关闭蜂鸣器指令
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[1] == (byte) 0x01 && aa[2] == (byte) 0x99) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) aa[5] & 0xFF);
						if (itot==0){
							break;
						}
					}
				}
			}

			if (ik < im){
				myHandler.sendEmptyMessage(0x10);
			}else{
				myHandler.sendEmptyMessage(0x11);
			}



		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	//1--上电打开读卡，0--上电关闭读卡
	private void ZMLibPoweronoffReadCardCmd(int ioffon){
		try {
			int inumber = 10; //这么条一传
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				if (1==ioffon) {
					LinkUi.currentDevice.ZMAPowerOnReadCard();//打开上电读卡指令：BB 00 98 00 01 01 9A 7E 应答:BB 01 98 00 01 00 9A 7E
				}else{
					LinkUi.currentDevice.ZMAPowerOffReadCard();//关闭上电读卡指令：BB 00 98 00 01 00 99 7E 应答:BB 01 98 00 01 00 9A 7E
				}
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[1] == (byte) 0x01 && aa[2] == (byte) 0x98) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) aa[5] & 0xFF);
						if (itot==0){
							break;
						}
					}
				}
			}

			if (ik < im){
				myHandler.sendEmptyMessage(0x10);
			}else{
				myHandler.sendEmptyMessage(0x11);
			}



		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}


	//获取功率
	private void ZMLibGetPowerCmd(){
		try {
			//打开功率指令：BB 00 B7 00 00 B7 7E 	  成功返回：BB 01 B7 00 02 0A 28 EC 7E 	==>26dbm(0A28->2600->26dbm)
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.ZMAGetPower();//打开功率指令
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[1] == (byte) 0x01 && aa[2] == (byte) 0xB7) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) (aa[5] << 8) | (int) aa[6] & 0xFF);
						//itot = ((int) aa[5] & 0xFF);
						if (itot>0){
							//是对的,算出值
							itot = itot / 100;
							powerTextView.setText(""+itot);
							powerSeekBar.setProgress(itot+9);
							break;
						}
					}
				}
			}

			if (ik < im){
				myHandler.sendEmptyMessage(0x10);
			}else{
				myHandler.sendEmptyMessage(0x11);
			}



		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}


	//设置功率
	private void ZMLibSetPowerCmd(int power){
		try {
			//-9dBm
			//BB 00 B6 00 02 FC 7C 30 7E ==>
			//BB 01 B6 00 01 00 B8 7E
			//设置功率指令：BB 00 B6 00 02 0A 28 EA 7E 	 	  成功返回：BB 01 B6 00 01 00 B8 7E 	==>26dbm(0A28->2600->26dbm)
			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.ZMASetPower(power);//功率指令
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[1] == (byte) 0x01 && aa[2] == (byte) 0xB6) {
						//itot = aa[5] << 8 | aa[6];
						itot = ((int) aa[5] & 0xFF);
						if (itot==0){

							break;
						}
					}
				}
			}

			if (ik < im){
				myHandler.sendEmptyMessage(0x10);
			}else{
				myHandler.sendEmptyMessage(0x11);
			}



		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

    //设置区域
    private void ZMLibSetRegionCmd(int region){
        try {
            //BB 00 07 00 01 01 09 7E
            //BB 01 07 00 01 00 09 7E
            //不同国家地区代码如下表：
            //Region Parameter
            //中国 900MHz 01
            //中国 800MHz 04
            //美国 02
            //欧洲 03
            //韩国 06

            int itot = 0; //总数为记录数
            int icnt = 0; //序号从0开始
            int ik = 0, im = 0;
            int ixu = 0;
            im = 3;
            for (ik = 0; ik < im; ik++) {
                LinkUi.currentDevice.ZMASetRegion(region);//频率指令
                Thread.sleep(200);
                byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
                if (aa != null && aa.length > 0) {
                    //if (aa.length==9 && aa[2] == 0xCE){
                    if (aa[1] == (byte) 0x01 && aa[2] == (byte) 0x07) {
                        //itot = aa[5] << 8 | aa[6];
                        itot = ((int) aa[5] & 0xFF);
                        if (itot==0){

                            break;
                        }
                    }
                }
            }

            if (ik < im){
                myHandler.sendEmptyMessage(0x10);
            }else{
                myHandler.sendEmptyMessage(0x11);
            }



        } catch (IOException e1) {
            e1.printStackTrace();
            //Utility.wq_UdpSendDataHex(strData);
            //Log.d(TAG, "Recc:"+strData);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

	//测试
	private void ZMLibTestCmd(int region){
		try {


			int itot = 0; //总数为记录数
			int icnt = 0; //序号从0开始
			int ik = 0, im = 0;
			int ixu = 0;
			im = 3;
			for (ik = 0; ik < im; ik++) {
				LinkUi.currentDevice.ZMATestCmd(region);//频率指令
				Thread.sleep(200);
				byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
				if (aa != null && aa.length > 0) {
					//if (aa.length==9 && aa[2] == 0xCE){
					if (aa[1] == (byte) 0x4D && aa[2] == (byte) 0x44) {


							break;

					}
				}
			}

			if (ik < im){
				myHandler.sendEmptyMessage(0x10);
			}else{
				myHandler.sendEmptyMessage(0x11);
			}



		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	private String getPackageName() {
		return "com.vanch.vhxdemo";
	}

	private String getVerStr() {
		String verName = "1.x.x";
		try {
			verName = getActivity().getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}

		return verName;
	}

	@Override
	public void onResume() {
		EventBus.getDefault().register(this);
//		ConfigParam param = (ConfigParam) EventBus.getDefault().getStickyEvent(
//				ConfigParam.class);
//		if (param != null) {
//			Log.d(TAG, "get param:" + param);
//			this.mParam = param;
//		}
		refreshUI();
		super.onResume();
	}

	private void refreshUI() {
		autolinkBox.setChecked(getConfigAutolink(getActivity()));//自动连接
		detectsoundBox.setChecked(getConfigChecksound(getActivity()));//检测声音
		skipnameBox.setChecked(getConfigChecksound(getActivity())); //getConfigSkipsame(getActivity()));//服了相同
		singletagbBox.setChecked(getConfigSingletag(getActivity()));//检查震动
		checkshocCheckBox.setChecked(getConfigCheckshock(getActivity()));

		//			sSpinner.setSelection(mParam.getS());
		//			abSpinner.setSelection(mParam.getAb());
		//			qSpinner.setSelection(mParam.getQ());
		//
		int timeout = getConfigTimeout(getActivity());
		if (timeout == 0) {
			timeout = 1000;
			setConfigTimeout(getActivity(), timeout);
		}
		timeouEditText.setText("" + timeout);

		String strip = getConfigCheckIp(getActivity());
		EditTextip.setText(strip);
		String strport = getConfigCheckPort(getActivity());
		EditTextport.setText(strport);

		// powerSeekBar.setProgress(mParam.getPower() - 20);
		//powerSeekBar.setProgress(20);
		//
		//			swTextView.setText(mParam.getSwVersion());
		//			hdTextView.setText(mParam.getHwVersion());
			
			

		String lang = getConfigLang(getActivity());
		if (lang.equals(Strings.LANGUAGE_ENGLISH)) {
			checkBoxLangEng.setChecked(true);
		} else {
			checkBoxLangCh.setChecked(true);
		}

		// add by martrin 20131114
		checkBoxCheckTest.setChecked(getConfigCheckTest(getActivity()));

	}

	public void onEventMainThread(LangChanged e) {
		updateLang();
	}

	private void updateLang() {
		autolinkBox.setText(Strings.getString(R.string.autolink));
		detectsoundBox.setText(Strings.getString(R.string.detectsound));
		skipnameBox.setText(Strings.getString(R.string.detectsound)); //R.string.skipname));
		singletagbBox.setText(Strings.getString(R.string.singletag));
		checkshocCheckBox.setText(Strings.getString(R.string.checkshock));
		textViewQueryTitle.setText(Strings.getString(R.string.queryparam));
		textViewSession.setText(Strings.getString(R.string.session));
		textViewTimeout.setText(Strings.getString(R.string.timeout));
		textViewPower.setText(Strings.getString(R.string.txpower));
		textViewSoftVer.setText(Strings.getString(R.string.softversion_title));
		textViewHardVer.setText(Strings.getString(R.string.hdversion));
		checkBoxLangCh.setText(Strings.getString(R.string.radio_lang_chinese));
		checkBoxLangEng.setText(Strings.getString(R.string.radio_lang_eng));
		timeouEditText.setHint(Strings.getString(R.string.timeout));
		checkBoxCheckUpdate.setText(Strings.getString(R.string.radio_check_update));

        btn_beepon.setText(Strings.getString(R.string.beepon));
        btn_beepclose.setText(Strings.getString(R.string.beepclose));

        btn_poweronreadcard.setText(Strings.getString(R.string.poweronreadcard));
        btn_poweroffreadcard.setText(Strings.getString(R.string.poweroffreadcard));

		btn_getfrequency.setText(Strings.getString(R.string.getfrequency));
		btn_setfrequency.setText(Strings.getString(R.string.setfrequency));

		btn_setpower.setText(Strings.getString(R.string.setpower));
		btn_getpower.setText(Strings.getString(R.string.getpower));

		radioButtontypea.setText(Strings.getString(R.string.radioButtontypeastr));
		radioButtontypeb.setText(Strings.getString(R.string.radioButtontypebstr));

	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}
	
	public static void setConfigAutolink(Activity activity, boolean autolink) {
		setBoolean(activity, key_autolink, autolink);
	}
	
	public static boolean getConfigAutolink(Activity activity) {
		return getBoolean(activity, key_autolink, false); //true);
	}
	
	public static void setConfigChecksound(Activity activity, boolean checksound) {
		setBoolean(activity, key_checksound, checksound);
	}
	
	public static boolean getConfigChecksound(Activity activity) {
		return getBoolean(activity, key_checksound, false); //true);
	}
	
	public static void setConfigSkipsame(Activity activity, boolean skipsame) {
		setBoolean(activity, key_skipname, skipsame);
	}
	
	public static boolean getConfigSkipsame(Activity activity) {
		return getBoolean(activity, key_skipname, false); //true);
	}
	
	public static void setConfigSingletag(Activity activity, boolean singletag) {
		setBoolean(activity, key_singletag, singletag);
	}
	
	public static boolean getConfigSingletag(Activity activity) {
		return getBoolean(activity, key_singletag, false); //true);
	}
	
	public static void setConfigCheckshock(Activity activity, boolean checkshock) {
		setBoolean(activity, key_checkshock, checkshock);
	}
	
	public static boolean getConfigCheckshock(Activity activity) {
		return getBoolean(activity, key_checkshock, false); //true);
	}
	
	public static void setConfigSeesionS(Activity activity, int value) {
		setInt(activity, key_session_s, value);
	}
	
	public static int getConfigSessionS(Activity activity) {
		return getInt(activity, key_session_s, 0);
	}
	
	//
	public static void setConfigSeesionAb(Activity activity, int value) {
		setInt(activity, key_session_ab, value);
	}
	
	public static int getConfigSessionAb(Activity activity) {
		return getInt(activity, key_session_ab, 0);
	}
	
	public static void setConfigSeesionQ(Activity activity, int value) {
		setInt(activity, key_session_q, value);
	}
	
	public static int getConfigSessionQ(Activity activity) {
		return getInt(activity, key_session_q,0);
	}
	
	public static void setConfigTimeout(Activity activity, int value) {
		setInt(activity, key_session_timeout, value);
	}
	
	public static int getConfigTimeout(Activity activity) {
		int timeout = getInt(activity, key_session_timeout,1000);
		if (timeout == 0)
		{
			timeout = 3000;
			setConfigTimeout(activity, timeout);
		}
		return timeout;
	}
	
	public static void setConfigLang(Activity activity, String lang) {
		setString(activity, MainActivity.lang_key, lang);
	}
	
	public static String getConfigLang(Activity activity) {
		return getString(activity, MainActivity.lang_key, Strings.LANGUAGE_CHINESE); //Strings.LANGUAGE_ENGLISH);
	}
	
	public static void setConfigLastConnect(Activity activity, String mac) {
		setString(activity, key_last_connect, mac);
	}
	
	public static String getConfigLastConnect(Activity activity) {
		return getString(activity, key_last_connect, "");
	}
	
	public static void setConfigCheckUpdate(Activity activity, boolean check) {
		setBoolean(activity, key_check_update, check);
	}
	
	public static boolean getConfigCheckUpdate(Activity activity) {
		return getBoolean(activity, key_check_update, true);
	}
	
	public static void setConfigCheckTest(Activity activity, boolean check) {
		setBoolean(activity, key_check_Test, check);
	}

	public static boolean getConfigCheckTest(Activity activity) {
		return getBoolean(activity, key_check_Test, false); //true);
	}

	private static void setBoolean(Activity activity, String key, boolean value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	private static boolean getBoolean(Activity activity, String key, boolean def) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getBoolean(key, def);
	}
	
	private static void setInt(Activity activity, String key, int value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	private static int getInt(Activity activity, String key, int defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getInt(key, defValue);
	}
	
	public static void setConfigCheckIp(Activity activity, String strcheck) {
		setString(activity, key_check_IP, strcheck);
	}

	public static String getConfigCheckIp(Activity activity) {
		return getString(activity, key_check_IP, "192.168.0.71");
	}

	public static void setConfigCheckPort(Activity activity, String strcheck) {
		setString(activity, key_check_PORT, strcheck);
	}

	public static String getConfigCheckPort(Activity activity) {
		return getString(activity, key_check_PORT, "8887");
	}

	private static void setString(Activity activity, String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	private static String getString(Activity activity, String key, String defaultVal) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getString(key, defaultVal);
	}
}
