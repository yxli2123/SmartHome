package com.vanch.vhxdemo;
//martrin  于sz 中文：马全水 shenzhen TELE:壹叁柒壹叁陆叁陆伍陆壹 编写
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lab.sodino.language.util.Strings;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vanch.vhxdemo.AccessUI.StatusChangeEvent;
import com.vanch.vhxdemo.helper.Utility;
import com.zma.vhxdemo.R;

import de.greenrobot.event.EventBus;
import lib.AttendanceLib;

public class InventoryUI extends Fragment implements OnItemLongClickListener {

	MediaPlayer findEpcSound;
	AudioManager audioManager;
	private Vibrator vibrator;
	long[] pattern = { 100, 400, 100, 400 }; // 停止 开启 停止 开启

	Thread thread = null;
	Thread threadGetdata = null;
	private boolean runFlag = false;
	private boolean startFlag = false;
	private boolean libgetdataflag = false;

	TextView txtxuhao; //一个是前面的序号个数 张数
	TextView txttotnum; //一个是次数的总数 次数
	TextView txttotcom; // 传输的个数
	int itxtxuhao = 0; //一个是前面的序号个数
	int itxttotnum = 0; //一个是次数的总数
	int itxttotcom = 0;
	int iwherebtn = 0; //0--时时盘存，1--单次盘存 2--取数据

	//private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	// 创建一个锁对象
	Lock lock = new ReentrantLock();
	/**
	 * inventory terminal event
	 * 
	 * @author liugang
	 * 
	 */
	public static class InventoryTerminal {

	}

	private static final String TAG = "inventory";

	public static class TimeoutEvent {
	}

	/**
	 * an epc discovered
	 * 
	 * @author liugang
	 */
	public static class EpcInventoryEvent {
	}

	/**
	 * inventory button clicked
	 * 
	 * @author liugang
	 * 
	 */
	public static class InventoryEvent {
	}

	public static class InventorySingle {
	}

	public static class InventoryGetData {
	}

	public static InventoryUI me;

	ListView listView;
	Button btnInventory, btnStop;
	Button btnInventory2,btnInventory3;
	Button beepon, beepclose;
	//TextView txtCount;
	ListAdapter adapter;
	//ArrayAdapter arryAdapter;
	List<Epc> epcs = new ArrayList<Epc>();
	List<Epc> datatmp = new ArrayList<Epc>();
	//private List<EPCInfo> datatmp = new ArrayList<EPCInfo>();
	//Map<String, Integer> epc2num = new ConcurrentHashMap<String, Integer>();
	//Map<String, Integer> epc2num = new HashMap<String, Integer>();
	ImageView statusOnImageView, statusTxImageView, statusRxImageView;
	Status on = Status.ON, tx = Status.BAD, rx = Status.BAD;

	ProgressDialog progressDialog;

	boolean stoped = false;
	int readCount = 0;

	boolean inventoring = false;

	InventoryThread inventoryThread;
	Timer timer;

	public InventoryUI() {
	}

	class InventoryThread extends Thread {
		int len, addr, mem;
		Strings mask;

		public InventoryThread(int len, int addr, int mem, Strings mask) {
			this.len = len;
			this.addr = addr;
			this.mem = mem;
			this.mask = mask;
		}

		public void run() {
			//try {
				//LinkUi.currentDevice.listTagID(1, 0, 0);
				//Log.i(TAG, "start read!!");
				//LinkUi.currentDevice.getCmdResult();
				Log.i(TAG, "read ok!!");
			//} catch (IOException e) {
				//e.printStackTrace();
			//}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		me = this;

		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setCanceledOnTouchOutside(false);

		View view = inflater.inflate(R.layout.inventory, null);
		//Utility.wq_UdpSendData("InventoryUI.java onCreateView!\n");

		listView = (ListView) view.findViewById(R.id.list_rfid);

		statusOnImageView = (ImageView) view.findViewById(R.id.status_on);
		statusTxImageView = (ImageView) view.findViewById(R.id.status_tx);
		statusRxImageView = (ImageView) view.findViewById(R.id.status_rx);
		statusOnImageView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				readCount = 0;
				itxtxuhao = 0;
				itxttotnum = 0;
				itxttotcom = 0;
				txtxuhao.setText(""+itxtxuhao); //一个是前面的序号个数 张数
				txttotnum.setText(""+itxttotnum); //一个是次数的总数 次数
				txttotcom.setText(""+itxttotcom);
				epcs.clear();
				datatmp.clear();
				refreshList();
				return true;
			}

		});
		btnInventory2 = (Button) view.findViewById(R.id.btn_inventory2);
		btnInventory2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//try {
				{
//					if (LinkUi.currentDevice != null) {
//						iwherebtn = 1; //0--时时盘存，1--单次盘存 2--取数据
//						//EventBus.getDefault().post(new InventorySingle());
//						//BB01CE00020001D27E
//						//BB01FF000117187E
//						//BB01CE00020001D27E
//						//BB01FF000117187E
//
//						//BB01CE00020001D27E
//						//BB01CC
//						//0010
//						//00000C1234123412341234123412348E7E
//						//BB 02 22 00 11 D5 34 00 12 34 12 34 12 34 12 34 12 34 12 34 8F 5A CB 7E //加7
//						lock.lock();
//						AttendanceLib.ZMAinit();
//						lock.unlock();
//						runFlag = false;
//						startFlag = false;
//						thread = null;
//						Thread.sleep(100);
//						runFlag = true;
//						startFlag = true;
//						thread = new LibEpc();
//						thread.start();
//						LinkUi.currentDevice.listTagIDAndTid();//获取epc+tid
//						Thread.sleep(1000); //500);
//						byte aa[] = LinkUi.currentDevice.getCmdZMAResult();
//						if (aa != null && aa.length > 0) {
//							//readWriteLock.writeLock().lock(); // 获取写锁
//							lock.lock();
//							AttendanceLib.ZMAPushData(aa, aa.length); //往里面放数据
//							//readWriteLock.writeLock().unlock(); // 释放写锁
//							lock.unlock();
//
//							//ZMAGettDataa();
//						}
//					}
//

					if (LinkUi.currentDevice != null) {
						if (!inventoring) {
							inventoring = !inventoring;
							iwherebtn = 1; //0--时时盘存，1--单次盘存 2--取数据 后来1改为同时读epc+tid
							readCount = 0;
							itxtxuhao = 0;
							itxttotnum = 0;
							itxttotcom = 0;
							txtxuhao.setText("" + itxtxuhao); //一个是前面的序号个数 张数
							txttotnum.setText("" + itxttotnum); //一个是次数的总数 次数
							txttotcom.setText("" + itxttotcom);
							btnInventory2
									.setBackgroundResource(R.drawable.stop_btn_press);
							btnInventory2.setText(Strings.getString(R.string.stop));
							clearList();
							EventBus.getDefault().post(new InventoryEvent());
							setRx(Status.ON);

							btnInventory.setEnabled(false);
							btnInventory3.setEnabled(false);

							runFlag = true;
							startFlag = true;
							thread = new LibEpc();
							thread.start();
							// progressDialog.setMessage(Strings.getString(R.string.inventory)+"......");
							// progressDialog.show();
						} else {
							inventoring = !inventoring;
							runFlag = false;
							startFlag = false;
							thread = null;
							ZMAGettDataa();
							// setRx(Status.OFF);
							// btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
							// btnInventory.setText(Strings.getString(R.string.inventory));
							btnInventory.setEnabled(true);
							btnInventory3.setEnabled(true);
							progressDialog.setMessage(Strings
									.getString(R.string.msg_inventory_stoping));
							;
							progressDialog.show();
						}
					} else {
						Utility.WarningAlertDialg(getActivity(),
								Strings.getString(R.string.msg_waring),
								Strings.getString(R.string.msg_device_not_connect))
								.show();
					}

					//}catch(IOException e1){
					//	e1.printStackTrace();
					//}catch(InterruptedException e){
					//	e.printStackTrace();
					//}
				}
				}
		});

		btnInventory3 = (Button) view.findViewById(R.id.btn_inventory3);
		btnInventory3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				iwherebtn = 2; //0--时时盘存，1--单次盘存 2--取数据
				if (LinkUi.currentDevice != null) {
					readCount = 0;
					itxtxuhao = 0;
					itxttotnum = 0;
					itxttotcom = 0;
					txtxuhao.setText("" + itxtxuhao); //一个是前面的序号个数 张数
					txttotnum.setText("" + itxttotnum); //一个是次数的总数 次数
					txttotcom.setText("" + itxttotcom);
					epcs.clear();
					datatmp.clear();
					refreshList();

					btnInventory.setEnabled(false);
					btnInventory2.setEnabled(false);
					btnInventory3.setEnabled(false);

					//EventBus.getDefault().post(new InventoryGetData());
					threadGetdata = null;
					libgetdataflag = true;
					threadGetdata = new LibGetData();
					threadGetdata.start();
					//LibGetDatacmd();


				} else {
					btnInventory.setEnabled(true);
					btnInventory2.setEnabled(true);
					btnInventory3.setEnabled(true);
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}
			}
		});

		btnInventory = (Button) view.findViewById(R.id.btn_inventory);
		btnInventory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// This is a test!!!
				if (ConfigUI.getConfigCheckTest(getActivity())) {
					// progressDialog.setMessage(("test"));
					// progressDialog.show();
					//addEpcTest("1234");
					//addEpcTest("1235");
					//addEpcTest("1236");
					//addEpcTest("1237");
					addEpcs("1234");
					addEpcs("1235");
					addEpcs("1236");
					addEpcs("1237");

					refreshList();
					ZMADispDataa();
					return;
				}

				if (LinkUi.currentDevice != null) {
					if (!inventoring) {
						inventoring = !inventoring;
						iwherebtn = 0; //0--时时盘存，1--单次盘存 2--取数据
						readCount = 0;
						itxtxuhao = 0;
						itxttotnum = 0;
						itxttotcom = 0;
						txtxuhao.setText(""+itxtxuhao); //一个是前面的序号个数 张数
						txttotnum.setText(""+itxttotnum); //一个是次数的总数 次数
						txttotcom.setText(""+itxttotcom);
						btnInventory
								.setBackgroundResource(R.drawable.stop_btn_press);
						btnInventory.setText(Strings.getString(R.string.stop));
						clearList();
						EventBus.getDefault().post(new InventoryEvent());
						setRx(Status.ON);

						btnInventory2.setEnabled(false);
						btnInventory3.setEnabled(false);

						runFlag = true;
						startFlag = true;
						thread = new LibEpc();
						thread.start();
						// progressDialog.setMessage(Strings.getString(R.string.inventory)+"......");
						// progressDialog.show();
					} else {
						inventoring = !inventoring;
						runFlag = false;
						startFlag = false;
						thread = null;
						ZMAGettDataa();
						// setRx(Status.OFF);
						// btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
						// btnInventory.setText(Strings.getString(R.string.inventory));
						btnInventory2.setEnabled(true);
						btnInventory3.setEnabled(true);
						progressDialog.setMessage(Strings
								.getString(R.string.msg_inventory_stoping));
						;
						progressDialog.show();
					}
				} else {
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}
			}
		});

		beepon = (Button) view.findViewById(R.id.btn_beepon);
		beepon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ZMLibBeepOnCmd();
			}
		});

		beepclose = (Button) view.findViewById(R.id.btn_beepclose);
		beepclose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ZMLibBeepCloseCmd();
			}
		});

		txtxuhao = (TextView) view.findViewById(R.id.txtxuhao); //一个是前面的序号个数 张数
		txttotnum = (TextView) view.findViewById(R.id.txttotnum); //一个是次数的总数 次数
		txttotcom = (TextView) view.findViewById(R.id.txttotcom); // 传输的个数

		listView.setOnItemLongClickListener(this);

		updateLang();

		findEpcSound = new MediaPlayer();
		// mediaPlayer01.start();
		audioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);

		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);



		// vibrator.vibrate(pattern,-1); //重复两次上面的pattern 如果只想震动一次，index设为-1
		int xx = AttendanceLib.ZMAinit(); //初始化

		return view;
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0x01:
					//dataa.clear();//要先清空data中的数据，避免把list中的数据重复放入data中。

					//dataa.addAll(datatmp);//这样做，list中的数据就放入到data中，之后list在后台线程中改变，但data不会改变，这时，你再
					//adapter.notifyDataSetChanged();
					refreshList();
					//mListView.smoothScrollToPosition(0);//移动到首部
					listView.setSelection(listView.getBottom());
					//ListView.smoothScrollToPosition(mListView.getCount() - 1);//移动到尾部
					//ListView.setSelection(ListView.getBottom())
					ZMADispDataa();

					break;

				case 0x02:
					btnInventory.setEnabled(true);
					btnInventory2.setEnabled(true);
					btnInventory3.setEnabled(true);
					break;
				case 0x03:

					break;
				case 0x04:

					break;
				case 0x05:

					break;
				case 0x06:

					break;
				case 0x07:

					break;
				case 0x08:

					break;
				case 0x09:
					refreshList();
					ZMADispDataa();
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

	private void ZMAGettDataa(){
		while (true) {
			boolean bflag = false;
			String strTemp;
			int iret = 0;
			int inum = 0;
			byte[] dat = new byte[100];
			int icnt = dat.length;
			//读 EPC+TID
			//发:BB 00 9A 00 03 22 FF FF BD 7E
			//收:BB 01 39 00 17 0A 24 00 11 11 00 00 00 02 11 22 E2 80 11 0C 20 00 33 46 35 A8 00 00 CB 7E

			//readWriteLock.readLock().lock(); // 获取读锁
			lock.lock();
			iret = AttendanceLib.ZMAGetData(dat, icnt);
			if (iret > 0) {
				if (0x22 == (int)dat[2] || 0x39 == (int)dat[2] || 0x9A == (int)dat[2] ) {
					icnt = dat[3] << 8 | dat[4];
					inum = icnt + 7;

					if ( iwherebtn == 0 ){ //0--时时盘存，1--单次盘存 2--取数据
						//发盘存命令
						icnt -= 5;
						if ( 0x22 == (int)dat[2] ){
							bflag = true;
						}
					}
					else if ( iwherebtn == 1){
						//epc+tid
						//epc:11 11 00 00 00 02 11 22
						//tid:E2 80 11 0C 20 00 33 46 35 A8 00 00
						icnt -= 3;
						if ( 0x39 == (int)dat[2] || 0x9A == (int)dat[2] ){
							bflag = true;
						}
					}
					if (bflag) {
						//if (icnt>0) {
						if ((icnt > 0) && (inum == iret) && (dat[iret - 1] == 0x7E)) {
							byte[] epc = new byte[icnt];
							for (iret = 0; iret < icnt; iret++) {
								epc[iret] = dat[iret + 8];
							}
							strTemp = Utility.getStringByByteslen(epc, icnt);
							Log.d(TAG, "Datt:" + strTemp);
							if (strTemp.equalsIgnoreCase("000000000000000000000000")) {
								iret = 0;
							}
							addEpcs(strTemp);
						}
					}
				}
				//readWriteLock.readLock().unlock(); // 释放读锁
				lock.unlock();
			}
			else{
				//readWriteLock.readLock().unlock(); // 释放读锁
				lock.unlock();
				return;
			}

		}
	}

	private void ZMADispDataa(){
		txtxuhao.setText("总次数:"+itxttotnum); //itxtxuhao); //一个是前面的序号个数 张数
		txttotnum.setText("总序号:"+itxtxuhao); //itxttotnum); //一个是次数的总数 次数
		txttotcom.setText(""+itxttotcom);
	}

	class LibEpc extends Thread {
		@Override
		public void run() {
			super.run();
//            String []strtmpp = new String[2];
//            strtmpp[0] = "7C127782";
//            strtmpp[1] = "F60C8AE8";
//            boolean bbf = true;
//            int kf = 0;
//            runFlag = true;
//            startFlag = true;
			while (runFlag) {
				if (startFlag) {
					ZMAGettDataa();
				}
			}
		}
	}

	private void LibGetDatacmd(){
		try {
			//取数据，先取张数总数，然后5或10个取
			//BB01CE00020001D27E
			//BB01CE00020001D27E
			//BB01CE00020001D27E
			//BB01CC
			//0010
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
			if (itot > 0) {
				if (itot % inumber == 0) {
					im = itot / inumber;
				} else {
					im = itot / inumber + 1;
				}
			}

			for (ik = 0; ik < im; ik++) {
				if (ixu == 250){
					Thread.sleep(20);
				}
				LinkUi.currentDevice.listTagIDCon(ixu, inumber);
				Thread.sleep(200);
				byte bb[] = LinkUi.currentDevice.getCmdZMAResult();
				//BB01CC001000000C1234123412341234123412348E7E =>16+7=23
				//正确的是:BB01CC000F00000C1234123412341234123412348E7E
				//000F
				//00000C123412341234123412341234 序号（2个字节）+长度（1个字节）+卡号（12个字节）
				if (bb != null && bb.length > 0) {
					String strData;
					int i = 5;
					int itmpxuhao = 0;
					byte btag[];
					itot = ((int) (bb[3] << 8) | (int) bb[4] & 0xFF);
					if (itot > 0) {
						while (true) {
							if (i >= bb.length - 7) {
								break;
							}

							if (bb[i+2]<0){
								itmpxuhao = 0;//非法数据直接退出
								break;
							}
							itmpxuhao = bb[i] << 8 | bb[i + 1];
							btag = new byte[bb[i + 2]];//标签号
							System.arraycopy(bb, i + 3, btag, 0, bb[i + 2]);
							i += bb[i + 2] + 3;

							strData = Utility.bytes2HexString(btag, btag.length);
							//Log.d(TAG, "Data:" + strData);
							addEpcs(strData);
						}
					}
				}

				ixu += inumber;
			}

			//读完了发个消息让三个按钮可用
			myHandler.sendEmptyMessage(0x02);

		} catch (IOException e1) {
			e1.printStackTrace();
			//Utility.wq_UdpSendDataHex(strData);
			//Log.d(TAG, "Recc:"+strData);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	class LibGetData extends Thread {
		@Override
		public void run() {
			super.run();

			if (libgetdataflag) {
				LibGetDatacmd();
			}
		}
	}

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


	private void playFindEpcSound() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

		try {
			findEpcSound.setDataSource(getActivity(), alert);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			findEpcSound.setAudioStreamType(AudioManager.STREAM_ALARM);
			findEpcSound.setLooping(false);
			try {
				findEpcSound.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			findEpcSound.start();
		}
	}

	private void shock() {
		vibrator.vibrate(pattern, -1);
	}

	private void freshStatus() {
		Map<ImageView, Status> map = new HashMap<ImageView, Status>();
		map.put(statusOnImageView, on);
		map.put(statusTxImageView, tx);
		map.put(statusRxImageView, rx);

		for (ImageView imageView : map.keySet()) {
			Status status = map.get(imageView);
			switch (status) {
			case ON:
				imageView.setImageResource(R.drawable.ic_on);
				break;
			// case OFF:
			// imageView.setImageResource(R.drawable.ic_off);
			// break;
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
	 * clear id list
	 */
	private void clearList() {
		//if (epc2num != null && epc2num.size() > 0) {
		//	epc2num.clear();
		//	refreshList();
		//}
		if (epcs != null && epcs.size() > 0) {
			epcs.clear();
			datatmp.clear();
			refreshList();
		}
	}

	private void onInventoryOri(){
		// 1.因为要跟新的VH75的一致，所以要加0B命令
		// 设置手机进入读写器模式，即模块电源打开，1--打开，0--关闭
		int i = 0;
		// while (i < 2) {
		try {
			//LinkUi.currentDevice.SetReaderMode((byte) 1);
			byte[] res = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(res)) {
				// TODO show error message
				// if (i > ) {
				inventoring = false;
				EventBus.getDefault().post(new InventoryTerminal());
				return;
				// }
				// i++;
				// continue;
				// } else {
				// break;
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) { // timeout
			Log.i(TAG, "Timeout!!@");
		}

		// }
		//

		while (inventoring) {
			// if (inventoring) {//this is a test!!!
			long lnow = android.os.SystemClock.uptimeMillis(); // 起始时间

			doInventory();

			while (true) {
				long lnew = android.os.SystemClock.uptimeMillis(); // 结束时间
				if (lnew - lnow > 500) {
					break;
				}
			}
		}
		EventBus.getDefault().post(new InventoryTerminal());

		// 片断code开始 try { // 1.因为要跟新的VH75的一致，所以要加0B命令 //
		// 设置手机进入读写器模式，即模块电源打开，1--打开，0--关闭
		try {
			//LinkUi.currentDevice.SetReaderMode((byte) 1);
			LinkUi.currentDevice.SetReaderMode((byte) 1);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) { // TODO show error message //
				Log.i(TAG, "SetReaderMode Fail!"); // return;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) { // timeout
			Log.i(TAG, "Timeout!!@");
		}
		// 片断code结束

	}

	private void onInventoryZMA(){
		int i = 0;
		try {
			lock.lock();
			AttendanceLib.ZMAinit(); //初始化
			if (Utility.iNetorBTcomm == 0) { //0--BT,1--NET,是蓝牙还是网络通信
				while(true) {
					byte[] data = LinkUi.currentDevice.getCmdZMAResult();
					if ( data == null ) break;
				}
			}
			lock.unlock();

			if ( iwherebtn == 0 ){ //0--时时盘存，1--单次盘存 2--取数据
				LinkUi.currentDevice.listTagIDMult();//发盘存命令
			}
			else if ( iwherebtn == 1){
				LinkUi.currentDevice.listTagIDAndTid();//epc+tid
			}



			while (inventoring) {
				// if (inventoring) {//this is a test!!!
				long lnow = android.os.SystemClock.uptimeMillis(); // 起始时间
				Thread.sleep(1);
				doInventoryZMA();

				if (Utility.iNetorBTcomm == 2) { //0--BT,1--NET,是蓝牙还是网络通信
					while (true) {
						long lnew = android.os.SystemClock.uptimeMillis(); // 结束时间
						if (lnew - lnow > 500) {
							break;
						}
					}
				}
			}
			EventBus.getDefault().post(new InventoryTerminal());

			LinkUi.currentDevice.listTagIDStop();//退出盘存命令
			if (Utility.iNetorBTcomm == 0) { //0--BT,1--NET,是蓝牙还是网络通信
				while(true) {
					byte[] data = LinkUi.currentDevice.getCmdZMAResult();
					if ( data == null ) break;
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 响应inventory按钮
	 *
	 * @param e
	 */
	public void onEventBackgroundThread(InventoryEvent e) {
		//onInventoryOri(); //这是原来的手持机的
		onInventoryZMA(); //新的手持机，如苍蝇拍
	}

	public void onEventBackgroundThread(InventorySingle e) {

	}


	public void onEventBackgroundThread(InventoryGetData e) {


	}

	private void doInventory() {
		try {

			LinkUi.currentDevice.listTagID(1, 0, 0);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) {
				// TODO show error message
				return;
			}
			VH73Device.ListTagIDResult listTagIDResult = VH73Device
					.parseListTagIDResult(ret);
			addEpc(listTagIDResult);
			EventBus.getDefault().post(new EpcInventoryEvent());

			// read the left id
			int left = listTagIDResult.totalSize - 8;
			while (left > 0) {
				if (left >= 8) {
					LinkUi.currentDevice.getListTagID(8, 8);
					left -= 8;
				} else {
					LinkUi.currentDevice.getListTagID(8, left);
					left = 0;
				}
				byte[] retLeft = LinkUi.currentDevice
						.getCmdResultWithTimeout(3000);
				if (!VH73Device.checkSucc(retLeft)) {
					Utility.showTostInNonUIThread(getActivity(),
							Strings.getString(R.string.msg_command_fail));
					continue;
				}
				VH73Device.ListTagIDResult listTagIDResultLeft = VH73Device
						.parseGetListTagIDResult(retLeft);
				addEpc(listTagIDResultLeft);
				EventBus.getDefault().post(new EpcInventoryEvent());
			}
			// EventBus.getDefault().post(new InventoryTerminal());
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) { // timeout
			// e1.printStackTrace();
			// EventBus.getDefault().post(new TimeoutEvent());
			Log.i(TAG, "Timeout!!!");
		}
	}

	private void doInventoryZMA() {
		//try {

			byte[] data = LinkUi.currentDevice.getCmdZMAResult();
			if (data !=null && data.length>0) {
				//readWriteLock.writeLock().lock(); // 获取写锁
				lock.lock();
				AttendanceLib.ZMAPushData(data, data.length); //往里面放数据
				//readWriteLock.writeLock().unlock(); // 释放写锁
				lock.unlock();
			}
			//ZMAGettDataa();
			//addEpc(listTagIDResult);
			EventBus.getDefault().post(new EpcInventoryEvent());

		//}
	}

	public void onEventMainThread(TimeoutEvent e) {
		progressDialog.dismiss();
		Utility.showDialogInNonUIThread(getActivity(),
				Strings.getString(R.string.msg_waring),
				Strings.getString(R.string.msg_timeout));
		inventoring = false;
		setRx(Status.BAD);
		if ( iwherebtn == 0 ){ //0--时时盘存，1--单次盘存 2--取数据
			btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
			btnInventory.setText(Strings.getString(R.string.inventory));
		}
		else if ( iwherebtn == 1){
			btnInventory2.setBackgroundResource(R.drawable.inventory_btn_press);
			btnInventory2.setText(Strings.getString(R.string.Single));
		}

		EventBus.getDefault().post(new InventoryTerminal());
	}

	/**
	 * inventory 结束
	 * 
	 * @param e
	 */
	public void onEventMainThread(InventoryTerminal e) {
		progressDialog.dismiss();
		inventoring = false;
		setRx(Status.BAD);
		if ( iwherebtn == 0 ){ //0--时时盘存，1--单次盘存 2--取数据
			btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
			btnInventory.setText(Strings.getString(R.string.inventory));
		}
		else if ( iwherebtn == 1){
			btnInventory2.setBackgroundResource(R.drawable.inventory_btn_press);
			btnInventory2.setText(Strings.getString(R.string.Single));
		}
	}

	private void addEpc(VH73Device.ListTagIDResult list) {
		ArrayList<byte[]> epcs = list.epcs;
		for (byte[] bs : epcs) {
			String string = Utility.bytes2HexString(bs);
//			if (!ConfigUI.getConfigSkipsame(getActivity())) {
//				if (epc2num.containsKey(string)) {
//					epc2num.put(string, epc2num.get(string) + 1);
//				} else {
//					epc2num.put(string, 1);
//				}
//			} else {
//				epc2num.put(string, 1);
//			}
			// readCount++;
			// 改为下面表格有多少行，则为多少行显示,add by martrin 20131114
			//readCount = epc2num.size();
		}
	}

//	private void addEpcTest(String strEpc) {
//		int inum = epc2num.size();
//		if (epc2num.containsKey(strEpc)) {
//			epc2num.put(strEpc, epc2num.get(strEpc) + 1);
//		} else {
//			epc2num.put(strEpc, 0); //1); ////1);
//		}
//		readCount = epc2num.size();
//	}

	private boolean addEpcs(String strEpc) {
		/*先判断是否重复*/
		String strTempp = "";
		String strTempp16 = ""; //16进制
		String strTempa = strEpc;
		String strTempb = "";
		int iret = 0;
		int len = datatmp.size(); //epcs.size();

		int ret = 0;
		int i = 0;

		//读 EPC+TID
		//发:BB 00 9A 00 03 22 FF FF BD 7E
		//收:BB 01 39 00 17 0A 24 00 11 11 00 00 00 02 11 22 E2 80 11 0C 20 00 33 46 35 A8 00 00 CB 7E
		if ( iwherebtn == 0 ){ //0--时时盘存，1--单次盘存 2--取数据
			//发盘存命令
		}
		else if ( iwherebtn == 1){
			//epc+tid
			//epc:11 11 00 00 00 02 11 22
			//tid:E2 80 11 0C 20 00 33 46 35 A8 00 00
			if (strEpc.length()>24) {
				strTempa = strEpc.substring(0, strEpc.length() - 24);
				strTempb = strEpc.substring(strEpc.length() - 24, strEpc.length());
			}else{
				return false;
			}
		}

		itxttotnum++; //一个是次数的总数
		for (i = 0; i < len; i++) {
			if (datatmp.get(i).getWATag().equalsIgnoreCase(strTempa)) { //判断编号是不是界面的LIST中
				//ret = Integer.parseInt(epcs.get(i).getWACiShu());
				ret = Integer.parseInt(datatmp.get(i).getWACiShu());
				ret++;
				datatmp.get(i).setWACiShu(""+ret);
				myHandler.sendEmptyMessage(0x09);
				return false;
			}
		}
		//
		//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		//Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		//String strttime = formatter.format(curDate);

		Epc epcInfo = new Epc();
		epcInfo.setCount(33);
		epcInfo.setId("11");
		epcInfo.setixuhao(itxtxuhao+1);
		epcInfo.setWATag(strTempa);
		epcInfo.setWATID(strTempb);
		epcInfo.setWACiShu("1");

		datatmp.add(epcInfo);
		Message msg = new Message();
		msg.what = 0x01;
		//msg.arg1 = (int) epcInfo.getId().longValue(); //0; //i;
		//msg.obj = epcInfo.getWABianHao().toString(); //0; //i;
		itxtxuhao++; //一个是前面的序号个数
		myHandler.sendMessage(msg);

		return true;
	}

	/**
	 * when inventory an epc, refresh the list
	 * 
	 * @param e
	 */
	public void onEventMainThread(EpcInventoryEvent e) {
		refreshList();
		//txtCount.setText("" + readCount);
		//
		if (ConfigUI.getConfigCheckshock(getActivity()))
			shock();

		if (ConfigUI.getConfigChecksound(getActivity()))
			playFindEpcSound();
	}

	/**
	 * 状态改变
	 * 
	 * @param e
	 */
	public void onEventMainThread(StatusChangeEvent e) {
		freshStatus();
	}

	public void onEventMainThread(ConfigUI.LangChanged e) {
		updateLang();
	}

	/**
	 * 刷新列表
	 */
	private void refreshList() {
		epcs.clear();
		epcs.addAll(datatmp);
		adapter = new IdListAdaptor();
		//adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);
		// listView.scrollTo(0, adapter.getCount());
		listView.setSelection(listView.getAdapter().getCount() - 1);
	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume");
		EventBus.getDefault().register(this);
		refreshList();
		if (LinkUi.currentDevice != null && LinkUi.currentDevice.isConnected())
			setOn(Status.ON);
		else
			setOn(Status.BAD);
		runFlag = false;
		startFlag = false;
		thread = null;
		super.onResume();
	}

	private class IdListAdaptor extends BaseAdapter {

		@Override
		public int getCount() {
			return epcs.size(); //epc2num.size();
		}

		@Override
		//public Object getItem(int position) {
		public Epc getItem(int position) {
			//String[] ids = new String[epc2num.size()];
			//epc2num.keySet().toArray(ids);
			//return ids[position];
			return epcs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.inventory_item_list, null);
			TextView rfidTextView = (TextView) view.findViewById(R.id.txt_rfid);
			TextView countTextView = (TextView) view
					.findViewById(R.id.txt_count_item);


			//String id = (String) getItem(position);
			Epc epcInfo = getItem(position);
			//int count = epcs.get(position); //epc2num.get(id);
			int count = epcInfo.getCount();
			if ( iwherebtn == 0 ) { //0--时时盘存，1--单次盘存 2--取数据
				rfidTextView.setText(epcInfo.getWATag());
			}
			else if ( iwherebtn == 1 ) {
				rfidTextView.setText("EPC:" + epcInfo.getWATag() + ",TID:" + epcInfo.getWATID());
			}
			countTextView.setText(epcInfo.getWACiShu()+" 序号:"+epcInfo.getixuhao()); //"" + count);

			//txt_xuhao.setText(epcInfo.getixuhao());

			TextView textViewNoTitle = (TextView) view
					.findViewById(R.id.txt_no_title);
			textViewNoTitle.setText(Strings.getString(R.string.count_lable));//数量 改为 次数
			return view;
		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		findEpcSound.stop();
		findEpcSound.release();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		//String[] ids = new String[epc2num.size()];
		//epc2num.keySet().toArray(ids);
		//Epc epc = new Epc(ids[position], epc2num.get(ids[position]));
		//EventBus.getDefault().postSticky(new AccessUI.EpcSelectedEvent(epc));
		//Log.i(TAG, "epc selected " + epc);
		Epc epc = epcs.get(position);
		EventBus.getDefault().postSticky(new AccessUI.EpcSelectedEvent(epc));
		return true;
	}

	private void updateLang() {
		String strtext;
		btnInventory.setText(Strings.getString(R.string.inventory));
		strtext = btnInventory.getText().toString();
		btnInventory2.setText(Strings.getString(R.string.Single));
		btnInventory3.setText(Strings.getString(R.string.Get));
		if (Utility.iopt == 0) {
			btnInventory3.setVisibility(View.GONE);
		}else{
			btnInventory3.setVisibility(View.VISIBLE);
		}
		beepon.setText(Strings.getString(R.string.Beep));
		beepclose.setText(Strings.getString(R.string.BeepClose));
		refreshList();
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
		} else {
			setTx(Status.BAD);
		}
	}

	/**
	 * tx blink
	 * 
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
}
