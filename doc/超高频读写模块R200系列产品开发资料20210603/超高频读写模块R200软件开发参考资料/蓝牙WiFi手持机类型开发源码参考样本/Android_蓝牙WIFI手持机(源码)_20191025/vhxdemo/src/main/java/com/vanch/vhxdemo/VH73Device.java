package com.vanch.vhxdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.UUID;
import java.util.logging.Logger;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vanch.vhxdemo.helper.Utility;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.NoSubscriberEvent;

/**
 * 设备类，包含所有的命令操作
 * 
 * @author lgnhm_000
 */
public class VH73Device {
	public static class GetCommandResultSuccess {
		boolean success;

		public GetCommandResultSuccess(boolean b) {
			this.success = b;
		}

		public boolean isSuccess() {
			return success;
		}
	}

	public static class SendCommandSuccess {
		boolean success;

		public SendCommandSuccess(boolean b) {
			this.success = b;
		}

		public boolean isSuccess() {
			return success;
		}
	}

	public static class GetCmdResultEvent {
		byte[] ret;

		public GetCmdResultEvent(byte[] ret) {
			this.ret = ret;
		}

		public byte[] getRet() {
			return ret;
		}
	}

	public static class ReadTimerStop {

	}

	public static class ReadTimerStart {

	}

	private static final Logger LOG = Logger.getLogger(VH73Device.class
			.getName());

	private HandsetVersion handsetVersion;
	private HandsetParam handsetParam;

	private static final String TAG = "VH75";
	public static final String SerialPortServiceClass_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	// public VH73Device(BluetoothDevice device) {
	// this.bluetoothDevice = device;
	// }
	//
	// private Context context;

	private Activity activity;

	public VH73Device(Activity activity, BluetoothDevice device) {
		this.bluetoothDevice = device;
		this.activity = activity;
	}

	public VH73Device(Activity activity) {
		this.activity = activity;
	}

	private BluetoothDevice bluetoothDevice;

	public BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}

	private InputStream inStream;
	private OutputStream outStream;
	private BluetoothSocket socket;
	private boolean isConnected;

	public HandsetParam getHandsetParam() {
		return handsetParam;
	}

	public void setHandsetParam(HandsetParam handsetParam) {
		this.handsetParam = handsetParam;
	}

	/**
	 * merger 2 byte[]
	 * 
	 * @param first
	 * @param second
	 * @return the merged byte[]
	 */
	private static byte[] mergeBytes(byte[] first, byte[] second) {
		byte[] ret = new byte[first.length + second.length];
		System.arraycopy(first, 0, ret, 0, first.length);
		System.arraycopy(second, 0, ret, first.length, second.length);
		return ret;
	}

	/**
	 * 解析获取记录结果
	 * 
	 * @param data
	 * @return 返回的记录列表
	 */
	public static LinkedList<RecordData> parseGetRecordResult(byte[] data) {
		LinkedList<RecordData> list = new LinkedList<RecordData>();
		int num = data[3];

		if (num == 0) {
			return list;
		}

		byte[] dataSegment = new byte[250];
		// Arrays.copyOfRange(dataSegment, 4, data.length-1);
		System.arraycopy(data, 4, dataSegment, 0, 250);

		for (int i = 0; i < num; i++) {
			// int index = 0;
			RecordData recordData = new RecordData();
			Date date = new Date();
			date.setYear(Utility.BYTE(dataSegment[0] + 2000 - 1900));
			date.setMonth(Utility.BYTE(dataSegment[1]) - 1);
			date.setDate(Utility.BYTE(dataSegment[2]));
			date.setHours(Utility.BYTE(dataSegment[3]));
			date.setMinutes(Utility.BYTE(dataSegment[4]));
			date.setSeconds(Utility.BYTE(dataSegment[5]));
			recordData.time = date;

			byte tagtype = dataSegment[6];
			recordData.tagtype = tagtype;
			// epc
			byte epcLen = dataSegment[7];
			recordData.epcLen = epcLen;

			byte[] epc = new byte[16];
			// Arrays.copyOfRange(epc, 8, 23);
			System.arraycopy(dataSegment, 8, epc, 0, 16);
			recordData.epc = epc;

			if (tagtype == 0x04) { // 6C
				byte tidLen = dataSegment[24];
				byte[] tid = new byte[8];
				// Arrays.copyOfRange(tid, 25, 32);
				System.arraycopy(dataSegment, 25, tid, 0, 8);

				byte userLen = dataSegment[33];
				byte[] user = new byte[249 - 34 + 1];
				// Arrays.copyOfRange(user, 34, 250);
				System.arraycopy(dataSegment, 34, user, 0, 216);

				recordData.tid = tid;
				recordData.tidLen = tidLen;
				recordData.user = user;
				recordData.userLen = userLen;
			} else {
				byte userLen = dataSegment[24];
				byte[] user = new byte[249 - 25 + 1];
				// Arrays.copyOfRange(user, 25, 250);
				System.arraycopy(dataSegment, 25, user, 0, 225);

				recordData.user = user;
				recordData.userLen = userLen;
			}
			//
			list.add(recordData);
		}

		return list;
	}

	/**
	 * 解析获取版本号结果
	 * 
	 * @param data
	 * @return
	 */
	public static HandsetVersion parseGetVersionResult(byte[] data) {

		HandsetVersion version = new HandsetVersion();
		version.hdVer1 = data[3];
		version.hdVer2 = data[4];
		version.swVer1 = data[5];
		version.swVer2 = data[6];

		return version;
	}

	/**
	 * 解析获取名单结果
	 * 
	 * @param data
	 * @return
	 */
	public static List<String> parseGetLabelIDResult(byte[] data) {
		LinkedList<String> result = new LinkedList<String>();

		int count = data[3];
		int index = 4;

		for (int i = 0; i < count; i++) {
			int len = data[index];
			byte[] id = new byte[len * 2];
			for (int j = 0; j < len * 2; j++) {
				id[j] = data[index + 1 + j];
			}

			result.add(Utility.bytes2HexString(id));
			index += 17;
		}

		return result;
	}

	/**
	 * 解析获取设备基本参数结果
	 * 
	 * @param data
	 * @return
	 */
	public static HandsetParam parseReadparamResult(byte[] data) {
		HandsetParam param = new HandsetParam();
		int index = 3;

		param.TagType = data[index++];
		param.Alarm = data[index++];
		param.OutputMode = data[index++];
		param.USBBaudRate = data[index++];
		param.Reserve5 = data[index++];
		param.Min_Frequence = data[index++];
		param.Max_Frequence = data[index++];
		param.Power = data[index++];
		param.RFhrdVer1 = data[index++];
		param.RFhrdVer2 = data[index++];
		param.RFSoftVer1 = data[index++];
		param.RFSoftVer2 = data[index++];
		param.ISTID = data[index++];
		param.TIDAddr = data[index++];
		param.TIDLen = data[index++];
		param.ISUSER = data[index++];
		param.USERAddr = data[index++];
		param.USERLen = data[index++];

		param.Reserve19 = data[index++];
		param.Reserve20 = data[index++];
		param.Reserve21 = data[index++];
		param.Reserve22 = data[index++];
		param.Reserve23 = data[index++];
		param.Reserve24 = data[index++];
		param.Reserve25 = data[index++];
		param.Reserve26 = data[index++];
		param.Reserve27 = data[index++];
		param.Reserve28 = data[index++];
		param.Reserve29 = data[index++];
		param.Reserve30 = data[index++];
		param.Reserve31 = data[index++];
		param.Reserve32 = data[index];
		return param;
	}

	/**
	 * 解析获取ID结果
	 * 
	 * @param data
	 * @return
	 */
	public static String parserGetHandsetIDResult(byte[] data) {
		byte[] dataSegment = getDataSegment(data);
		StringBuffer buffer = new StringBuffer();
		for (byte b : dataSegment) {
			if (b != 0) {
				buffer.append((char) b);
			}
		}
		return buffer.toString();
	}

	/**
	 * 获取结果中数据部分
	 * 
	 * @param data
	 * @return
	 */
	private static byte[] getDataSegment(byte[] data) {
		int len = data[1];
		byte[] dataSegment = new byte[len - 2];
		System.arraycopy(data, 3, dataSegment, 0, len - 2);
		LOG.info("data segment is " + Utility.bytes2HexString(dataSegment));
		return dataSegment;
	}

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

	private String DeviceIp = "192.168.0.101";
	private String DevicePort = "8899";
	private Selector selector = null;
	private DatagramSocket socketnet = null;
	private DatagramChannel channel = null;
	private int ilocalport = 5099;

	public void setDeviceIp(String strip) {
		this.DeviceIp = strip;
	}
	public void setDevicePort(String strport) {
		this.DevicePort = strport;
	}

	/**
	 * 连接到设备
	 * 
	 * @throws PortInUseException
	 * @throws IOException
	 * @throws TooManyListenersException
	 * @throws UnsupportedCommOperationException
	 */
	public boolean connectnet() {
		boolean bbflag = false;
		BluetoothSocket tmpSocket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		if (Utility.iNetorBTcomm == 1){ //0--BT,1--NET,是蓝牙还是网络通信
			selector = null;
			socket = null;
			try {
				channel = DatagramChannel.open();
				socketnet = channel.socket();
				channel.configureBlocking(false);
				String strport = DevicePort;
				int iport = Integer.parseInt(strport);
				if ( !socketnet.isBound() ) {
					socketnet.bind(new InetSocketAddress(iport)); //5057));
				}

				selector = Selector.open();
				channel.register(selector, SelectionKey.OP_READ);

				byte[] chTemp = new byte[] {(byte)0xBB,0x00,0x03,0x00,0x01,0x00,0x04,0x7E};

				ByteBuffer bb = ByteBuffer.allocate (chTemp.length);
				bb.put (chTemp);
				bb.flip ();

				String strip = DeviceIp;
				String[] all=strip.split("\\.");
				//byte[] bs = new byte[] { (byte) 192, (byte) 168, 0, 101 }; //{ (byte) 192, (byte) 168, 0, 71 };
				byte [] bs = new byte[4];
				bs[0] = (byte)Integer.parseInt(all[0]);
				bs[1] = (byte)Integer.parseInt(all[1]);
				bs[2] = (byte)Integer.parseInt(all[2]);
				bs[3] = (byte)Integer.parseInt(all[3]);
				InetAddress address=InetAddress.getByAddress(bs);
				SocketAddress sa = new InetSocketAddress(address, iport); //8899);



				channel.send(bb, sa);


			} catch (Exception e) {
				e.printStackTrace();
			}
			bbflag = false;
			setConnected(false);
			ByteBuffer byteBuffer = ByteBuffer.allocate(65535);

			if (true) {
				try {
					int iRecvLen = 0;
					byte[] chTemp = new byte[4096];
					int eventsCount = selector.select(5000);
					if (eventsCount > 0) {
						Set selectedKeys = selector.selectedKeys();
						Iterator iterator = selectedKeys.iterator();
						while (iterator.hasNext()) {
							SelectionKey sk = (SelectionKey) iterator.next();
							iterator.remove();
							if (sk.isReadable()) {

								DatagramChannel datagramChannel = (DatagramChannel) sk
										.channel();
								byteBuffer.clear();
								SocketAddress sa = datagramChannel.receive(byteBuffer);
								byteBuffer.flip();


								iRecvLen = byteBuffer.limit();
								// 测试：通过将收到的ByteBuffer首先通过缺省的编码解码成CharBuffer 再输出马储油平台
								CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
								Bcd2AscEx(chTemp, byteBuffer.array(), iRecvLen*2);
								//System.out.println("receive message:"+ charBuffer.toString());
								String s = new String(chTemp, "UTF-8");
								System.out.println("receive message["+ iRecvLen +"]: "+ s.trim());

								byteBuffer.clear();
								bbflag = true;
								setConnected(true);


								//要发送就下面打开
								//datagramChannel.send(bb, sa);
								//datagramChannel.write(buffer);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}


			}

		}else{

		try {
			tmpSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID
					.fromString(SerialPortServiceClass_UUID));
			tmpSocket.connect();
			tmpIn = tmpSocket.getInputStream();
			tmpOut = tmpSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "connect to " + bluetoothDevice.getName() + " failed!");
			return false;
		}

		socket = tmpSocket;
		inStream = tmpIn;
		outStream = tmpOut;
		setConnected(true);
		}
		// new Thread() {
		// public void run() {
		// while(true) {
		// try {
		// byte[] ret = getCmdResult();
		// EventBus.getDefault().post(new GetCmdResultEvent(ret));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }.start();

		return bbflag;
	}

	/**
	 * 连接到设备
	 *
	 * @throws PortInUseException
	 * @throws IOException
	 * @throws TooManyListenersException
	 * @throws UnsupportedCommOperationException
	 */
	public boolean connect() {
		boolean bbflag = false;
		BluetoothSocket tmpSocket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;



			try {
				tmpSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID
						.fromString(SerialPortServiceClass_UUID));
				tmpSocket.connect();
				tmpIn = tmpSocket.getInputStream();
				tmpOut = tmpSocket.getOutputStream();
				bbflag = true;
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "connect to " + bluetoothDevice.getName() + " failed!");
				return false;
			}

			socket = tmpSocket;
			inStream = tmpIn;
			outStream = tmpOut;
			setConnected(true);

		// new Thread() {
		// public void run() {
		// while(true) {
		// try {
		// byte[] ret = getCmdResult();
		// EventBus.getDefault().post(new GetCmdResultEvent(ret));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }.start();

		return bbflag;
	}

	/**
	 * 断开设备
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (Utility.iNetorBTcomm == 1) { //0--BT,1--NET,是蓝牙还是网络通信
			{
				socketnet.disconnect();
				socketnet.close();
				socketnet = null;
				setConnected(false);
			}

		} else {
			try {
				inStream.close();
				outStream.close();
				socket.close();
				setConnected(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public String getAddress() {
		return bluetoothDevice.getAddress();
	}

	/**
	 * generate command byte[] for sending to device
	 * 
	 * @param cmd
	 *            command code
	 * @param param
	 *            command param, null is there is no param
	 */
	private byte[] generateCMD(CommandCode cmd, byte[] param) {
		int len = 1 + 1; // cmd + checksum
		if (param != null) {
			len = len + param.length; // param
		}

		ByteBuffer buffer = ByteBuffer.allocate(len + 2);
		buffer.put((byte) Head.SEND.getCode()).put((byte) len)
				.put((byte) cmd.getCode());
		if (param != null) {
			buffer.put(param);
		}

		byte checksum = crc(buffer.array(), len + 1);
		buffer.put(checksum);

		LOG.info("cmd=[" + Utility.bytes2HexString(buffer.array()) + "]");

		return buffer.array();
	}

	private byte[] genTimeParam(Date date) {
		byte[] param = new byte[6];
		param[0] = Utility.BYTE(date.getYear() + 1900 - 2000);
		param[1] = Utility.BYTE(date.getMonth() + 1);
		param[2] = Utility.BYTE(date.getDate());
		param[3] = Utility.BYTE(date.getHours());
		param[4] = Utility.BYTE(date.getMinutes());
		param[5] = Utility.BYTE(date.getSeconds());

		return param;
	}

	private byte crc(byte[] data, int len) {
		byte checksum = 0;
		for (int i = 0; i < len; i++) {
			checksum += data[i];
		}
		checksum = Utility.BYTE(~checksum);
		checksum = Utility.BYTE(checksum + 1);

		return Utility.BYTE(checksum);
	}

	/**
	 * 发送命令
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	public void sendCommand(byte[] cmd) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.SSS");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String Standby = formatter.format(curDate);

			// String strData =
			// YY+"-"+MM+"-"+DD+"-"+HH+"-"+NN+"-"+SS+"-"+MI+"--recv:" +
			// Utility.bytes2HexString(bytesTmp);
			String strData = "[" + Standby + "]" + "---Send:" + "("
					+ cmd.length + ")" + Utility.bytes2HexString(cmd);
			//wq_UdpSendData(strData + "\r\n");
			Utility.wq_UdpSendDataHex(Utility.bytes2HexString(cmd));
            Log.d(TAG, "Send:" + strData);
			if (Utility.iNetorBTcomm == 1) { //0--BT,1--NET,是蓝牙还是网络通信
				ByteBuffer bb = ByteBuffer.allocate (cmd.length);
				bb.put (cmd);
				bb.flip ();

				String strip = DeviceIp;
				String[] all=strip.split("\\.");
				//byte[] bs = new byte[] { (byte) 192, (byte) 168, 0, 101 }; //{ (byte) 192, (byte) 168, 0, 71 };
				byte [] bs = new byte[4];
				bs[0] = (byte)Integer.parseInt(all[0]);
				bs[1] = (byte)Integer.parseInt(all[1]);
				bs[2] = (byte)Integer.parseInt(all[2]);
				bs[3] = (byte)Integer.parseInt(all[3]);
				InetAddress address=InetAddress.getByAddress(bs);
				int iport = Integer.parseInt(DevicePort);
				SocketAddress sa = new InetSocketAddress(address, iport); //8899);

				byte[] buffer = new byte[65537];
				DatagramPacket receivePacket = new DatagramPacket(buffer,buffer.length);
				//socketnet.receive(receivePacket); //放在这里有问题
				int iRecvLen = 0;
				ByteBuffer buf = ByteBuffer.allocate(65537);
				buf.clear();
				channel.receive(buf);
				buf.flip();
				iRecvLen = buf.limit();
				while ( iRecvLen > 0 ){
					buf.clear();
					channel.receive(buf);
					buf.flip();
					iRecvLen = buf.limit();
				}


				channel.send(bb, sa);
			}else {
				outStream.write(cmd);
			}
			EventBus.getDefault().post(new SendCommandSuccess(true));
		} catch (IOException e1) {
			LOG.info("Write cmdError");
			e1.printStackTrace();
		}

	}

	private byte[] genGetVersionCmd() {
		return generateCMD(CommandCode.GetVersion, null);
	}

	/**
	 * 发送获取版本命令
	 * 
	 * @throws IOException
	 */
	public void getVersion() throws IOException {
		sendCommand(genGetVersionCmd());
	}

	private byte[] genAddLabelIDCmd(List<byte[]> ids) {
		byte[] param = new byte[1];
		param[0] = (byte) ids.size(); // len

		for (int i = 0; i < ids.size(); i++) {
			byte[] id = ids.get(i);
			param = mergeBytes(param, new byte[] { (byte) (id.length / 2) });
			param = mergeBytes(param, id);
		}

		return generateCMD(CommandCode.AddLableID, param);
	}

	/**
	 * 添加名单
	 * 
	 * @param ids
	 * @throws IOException
	 */
	public void addLable(String id) throws IOException {
		byte[] param = new byte[18];
		int len = id.length() / 2;
		param[0] = 0x01;
		param[1] = (byte) (len / 2);

		for (int i = 0; i < len; i++) {
			param[2 + i] = (byte) (0xff & Integer.parseInt(
					id.substring(i * 2, i * 2 + 2), 16));
		}
		sendCommand(generateCMD(CommandCode.AddLableID, param));

		// sendCommand(genAddLabelIDCmd(ids));
	}

	private byte[] genDelLabelIDCmd(List<byte[]> ids) {
		byte[] param = new byte[1];
		param[0] = (byte) ids.size(); // len

		for (int i = 0; i < ids.size(); i++) {
			byte[] id = ids.get(i);
			param = mergeBytes(param, new byte[] { (byte) (id.length / 2) });
			param = mergeBytes(param, id);
		}

		return generateCMD(CommandCode.DelLableID, param);
	}

	/**
	 * 删除名单
	 * 
	 * @param ids
	 * @throws IOException
	 */
	public void deleteLabel() throws IOException {
		// sendCommand(genDelLabelIDCmd(ids));
		sendCommand(generateCMD(CommandCode.DelLableID, null));
	}

	/**
	 * 生成获取名单指令数据
	 * 
	 * @param addr
	 *            起始地址
	 * @param len
	 *            名单长度
	 * @return 指令数据
	 */
	private byte[] genGetLabelIDCmd(int addr, int len) {
		byte[] param = new byte[3];
		param[0] = (byte) (addr >> 8);
		param[1] = (byte) (addr & 0xFF);
		param[2] = (byte) len;
		return generateCMD(CommandCode.GetLableID, param);
	}

	/**
	 * 获取名单
	 * 
	 * @param addr
	 * @param len
	 * @throws IOException
	 */
	public void getLabelID(int addr, int len) throws IOException {
		sendCommand(genGetLabelIDCmd(addr, len));
	}

	/**
	 * 保存名单
	 * 
	 * @throws IOException
	 */
	public void saveLabel() throws IOException {
		sendCommand(generateCMD(CommandCode.SaveLabelToSDCard, null));
	}

	/**
	 * 生成读取设备参数指令数据
	 * 
	 * @return 指令数据
	 */
	private byte[] genReadparamCmd() {
		return generateCMD(CommandCode.ReadHandsetParam, null);
	}

	/**
	 * 读取设备参数
	 * 
	 * @throws IOException
	 */
	public void readParam() throws IOException {
		sendCommand(genReadparamCmd());
	}

	private byte[] genWriteParam(HandsetParam param) {
		return generateCMD(CommandCode.WriteHanderParam, param.toBytes());
	}

	/**
	 * 设置设备参数
	 * 
	 * @param param
	 * @throws IOException
	 */
	public void writeParam(HandsetParam param) throws IOException {
		sendCommand(genWriteParam(param));
	}

	private byte[] genWriteFactoryParam(HandsetParam param) {
		return generateCMD(CommandCode.WriteFactoryParam, param.toBytes());
	}

	public void writeFactoryParam(HandsetParam param) throws IOException {
		sendCommand(genWriteFactoryParam(param));
	}

	/**
	 * 生成设置过滤器指令数据
	 * 
	 * @param addr
	 *            掩码地址， 0 - 95
	 * @param len
	 *            掩码长度， 0 - 96
	 * @param mask
	 *            M个字节，输入M的字符串
	 * @return 指令数据
	 */
	private byte[] genSetReportFilterCmd(int addr, int len, byte[] mask) {

		byte[] param;
		if (len == 0) {
			param = new byte[5];
		} else {
			int i, m;

			if (len % 8 == 0) {
				m = len / 8;
			} else {
				m = len / 8 + 1;
			}

			param = new byte[m + 4];
			param[0] = (byte) (addr >> 8);
			param[1] = (byte) addr;
			param[2] = (byte) (len >> 8);
			param[3] = (byte) len;

			for (i = 0; i < m; i++) {
				param[4 + i] = mask[i];
			}
		}

		return generateCMD(CommandCode.SetReportFilter, param);
	}

	/**
	 * 设置过滤
	 * 
	 * @param addr
	 * @param len
	 * @param mask
	 * @throws IOException
	 */
	public void setReportFilter(int addr, int len, byte[] mask)
			throws IOException {
		sendCommand(genSetReportFilterCmd(addr, len, mask));
	}

	/**
	 * 获取过滤
	 * 
	 * @throws IOException
	 */
	public void getReportFilter() throws IOException {
		sendCommand(generateCMD(CommandCode.GetReportFilter, null));
	}

	// public byte[] genSetFactoryParam(HandsetParam param) {
	// throw new UnsupportedOperationException();
	// }

	/**
	 * 生成恢复出厂设置指令数据
	 * 
	 * @return 指令数据
	 */
	private byte[] genResetFactoryparam() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 生成设置时间指令数据
	 * 
	 * @param date
	 *            时间
	 * @return 指令数据
	 */
	private byte[] genSetTimeCmd(Date date) {
		return generateCMD(CommandCode.SetReaderTime, genTimeParam(date));
	}

	/**
	 * 设置时间
	 * 
	 * @param date
	 * @throws IOException
	 */
	public void setTime(Date date) throws IOException {
		sendCommand(genSetTimeCmd(date));
	}

	/**
	 * 生成获取设备时间指令数据
	 * 
	 * @return 指令数据
	 */
	private byte[] genGetTimeCmd() {
		return generateCMD(CommandCode.GetReaderTime, null);
	}

	/**
	 * 获取时间命令
	 * 
	 * @throws IOException
	 */
	public void getTime() throws IOException {
		sendCommand(genGetTimeCmd());
	}

	/**
	 * 解析获取时间命令结果
	 * 
	 * @param data
	 * @return
	 */
	public static Date parseGetTimeResult(byte[] data) {
		Date date = new Date();
		date.setYear(Utility.BYTE(data[3] + 2000 - 1900));
		date.setMonth(Utility.BYTE(data[4]) - 1);
		date.setDate(Utility.BYTE(data[5]));
		date.setHours(Utility.BYTE(data[6]));
		date.setMinutes(Utility.BYTE(data[7]));
		date.setSeconds(Utility.BYTE(data[8]));

		return date;
	}

	/**
	 * 
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @return 指令数据
	 */
	private byte[] genGetRecordCmd(Date start, Date end) {
		ByteBuffer buffer = ByteBuffer.allocate(6 + 6);
		buffer.put(genTimeParam(start));
		buffer.put(genTimeParam(end));

		return generateCMD(CommandCode.GetRecord, buffer.array());
	}

	/**
	 * 下载记录
	 * 
	 * @param start
	 * @param end
	 * @throws IOException
	 */
	public void getRecord(Date start, Date end) throws IOException {
		sendCommand(genGetRecordCmd(start, end));
	}

	private byte[] genGetHansetIDCmd() {
		return generateCMD(CommandCode.GetHandsetID, null);
	}

	/**
	 * 获取设备ID
	 * 
	 * @throws IOException
	 */
	public void getHandsetID() throws IOException {
		sendCommand(genGetHansetIDCmd());
	}

	/**
	 * 设置设备ID
	 * 
	 * @param id
	 * @throws IOException
	 */
	public void setHandsetID(String id) throws IOException {
		sendCommand(genSetHansetID(id));
	}

	private byte[] genSetHansetID(String id) {
		byte[] bidBs = id.getBytes();
		byte[] param = new byte[10];
		System.arraycopy(bidBs, 0, param, 0, bidBs.length);
		return generateCMD(CommandCode.SetHandsetID, param);
	}

	private byte[] genDelRecordCmd() {
		return generateCMD(CommandCode.DeleteAllRecord, null);
	}

	/**
	 * 删除设备记录
	 * 
	 * @throws IOException
	 */
	public void delRecord() throws IOException {
		sendCommand(genDelRecordCmd());
	}

	private byte[] genSetBTNameCmd(String name) {
		byte[] bidBs = name.getBytes();
		return generateCMD(CommandCode.SetBluetoothName, bidBs);
	}

	/**
	 * 设置蓝牙名称
	 * 
	 * @param name
	 * @throws IOException
	 */
	public void setBtName(String name) throws IOException {
		sendCommand(genSetBTNameCmd(name));
	}

	private byte[] genGetBTNameCmd() {
		return generateCMD(CommandCode.GetBluetoothName, null);
	}

	/**
	 * 获取蓝牙名称
	 * 
	 * @throws IOException
	 */
	public void getBTName() throws IOException {
		sendCommand(generateCMD(CommandCode.GetBluetoothName, null));
	}

	/**
	 * 解析获取蓝牙名字结果
	 * 
	 * @param data
	 * @return
	 */
	public static String parseGetBTNameResult(byte[] data) {
		byte[] dataSegment = getDataSegment(data);
		StringBuffer buffer = new StringBuffer();
		for (byte b : dataSegment) {
			if (b != 0) {
				buffer.append((char) b);
			}
		}
		return buffer.toString();
	}

	/**
	 * 设置蓝牙波特率
	 * 
	 * @param baud
	 * @throws IOException
	 */
	public void setBtBaudRate(byte baud) throws IOException {
		sendCommand(generateCMD(CommandCode.SetBtBaudRate, new byte[] { baud }));
	}

	/**
	 * 获取蓝牙波特率
	 * 
	 * @throws IOException
	 */
	public void getBtBaudRate() throws IOException {
		sendCommand(generateCMD(CommandCode.GetBtBaudRate, null));
	}

	/**
	 * 设置手机进入读写器模式，即模块电源打开，1--打开，0--关闭
	 * 
	 * @param nMode
	 * @throws IOException
	 */
	public void SetReaderMode(byte nMode) throws IOException {
		sendCommand(generateCMD(CommandCode.SetReaderMode, new byte[] { nMode }));
		//发送停止盘存命令
		//String strcmd = "BB00280000287E";
		//byte bcmd[] = Utility.convert2HexArray(strcmd);
		//sendCommand(bcmd);
	}

	/**
	 * 读取一个字节数据
	 * 
	 * @return
	 * @throws IOException
	 */
	public int read() throws IOException {
		// EventBus.getDefault().post(new ReadTimerStart());
		int i = inStream.read();
		// EventBus.getDefault().post(new ReadTimerStop());
		return i;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public int read(byte[] buffer) throws IOException {
		return inStream.read(buffer);
	}

	/**
	 * 打印错误信息
	 * 
	 * @param errCode
	 * @return
	 */
	public static String errMessage(int errCode) {
		String errorString = null;
		switch (errCode) {
		case (byte) 0x10:
			errorString = "invalid command";
			break;
		case (byte) 0x11:
			errorString = "Param error";
			break;
		case (byte) 0x12:
			errorString = "checksum error";
			break;
		case (byte) 0x20:
			errorString = "command executing error";
			break;
		default:
			errorString = "unkown error";
			break;
		}

		return errorString;
	}

	private void processGetReportFileter(byte[] data) {
		int addr = data[3] * 0x100 + data[4];
		int len = data[5] * 0x100 + data[6];
		int l = 0;

		// jTextField_mask_len.setText(len + "");
		// jTextField_mask_start_addr.setText(addr + "");

		if (len % 8 == 0) {
			l = len / 8;
		} else {
			l = len / 8 + 1;
		}

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < l; i++) {
			buffer.append(String.format("%02X", data[i + 7]));
		}
		// jTextField_mask.setText(buffer.toString());
	}

	/**
	 * 
	 * @return
	 */
	public HandsetVersion getHandsetVersion() {
		return handsetVersion;
	}

	/**
	 * 
	 * @param handsetVersion
	 */
	public void setHandsetVersion(HandsetVersion handsetVersion) {
		this.handsetVersion = handsetVersion;
	}

	/**
	 * 一条标签记录
	 */
	public static class RecordData {
		/**
		 * id号
		 */
		public byte[] id;
		/**
		 * 记录时间
		 */
		public Date time;
		public byte[] user;
		public byte[] tid;
		public byte tagtype;
		public byte tidLen;
		public byte userLen;
		public byte[] epc;
		public byte epcLen;

		@Override
		public String toString() {
			return time.toLocaleString() + " " + Utility.bytes2HexString(id);
		}
	}

	public ConfigParam readConfigParam() throws IOException {
		// TODO read config
		// ========== test ==============
		ConfigParam param = new ConfigParam();
		param.setAutolink(true);
		param.setPower(25);
		param.setSwVersion("1.0.0");
		param.setHwVersion("2.0.0");
		return param;
	}

	public void access() throws IOException {
		// TODO do access
	}

	// public void inventory(int mem, int addr, int len) throws IOException {
	// // listTagID(1, 0, 0);
	// // getCmdResult();
	// }
	//设置是否上电读卡：（新加功能）
	//关闭上电读卡指令：BB 00 98 00 01 00 99 7E
	//打开上电读卡指令：BB 00 98 00 01 01 9A 7E

	public void ZMAPowerOnReadCard() throws IOException {
		//打开上电读卡指令：BB 00 98 00 01 01 9A 7E
		String strcmd = "BB00980001019A7E";
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}

	public void ZMAPowerOffReadCard() throws IOException {
		//关闭上电读卡指令：BB 00 98 00 01 00 99 7E
		String strcmd = "BB0098000100997E";
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}


	public void ZMABeepOn() throws IOException {
		//打开蜂鸣器指令： BB 00 99 00 01 01 9B 7E  设置成功返回：BB 01 99 00 01 00 9B 7E
		String strcmd = "BB00990001019B7E";
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}

	public void ZMABeepClose() throws IOException {
		//关闭蜂鸣器指令： BB 00 99 00 01 00 9A 7E  设置成功返回：BB 01 99 00 01 00 9B 7E
		String strcmd = "BB00990001009A7E";
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}

	public void ZMAGetPower() throws IOException {
		//打开功率指令：BB 00 B7 00 00 B7 7E 	  成功返回：BB 01 B7 00 02 0A 28 EC 7E 	==>26dbm(0A28->2600->26dbm)
		String strcmd = "BB00B70000B77E";
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}
    public void ZMASetPower(int power) throws IOException {
        //设置功率指令：BB 00 B6 00 02 0A 28 EA 7E 	 	  成功返回：BB 01 B6 00 01 00 B8 7E 	==>26dbm(0A28->2600->26dbm)
        int ipower = power * 100;
        char []b = new char[2];
        b[0] = (char) (ipower >> 8 & 0xFF);
        b[1] = (char) (ipower & 0xFF);
        String strhead = "BB";
        String strendd = "7E";
        String strcmd = "00B60002";
        String strhao = Integer.toHexString(b[0]&0xff);
        if (strhao.length() == 1){
            strhao = "0" + strhao;
        }else {
            //strhao = strhao;
        }

        String strhao1 = Integer.toHexString(b[1]&0xff);
        if (strhao1.length() == 1){
            strhao1 = "0" + strhao1;
        }else {
            //strhao = strhao;
        }

        strcmd = strcmd + strhao + strhao1;
        String strcheck = Utility.CheckSum(strcmd); //

        strcmd = strhead + strcmd + strcheck+ strendd;

        byte cmd[] = Utility.convert2HexArray(strcmd);

        sendCommand(cmd);

    }

	public void ZMASetRegion(int region) throws IOException {
		//设置国家指令 BB 00 07 00 01 06 0E 7E 
		int iregion = region;
		char []b = new char[1];
		b[0] = (char) (iregion & 0xFF);
		String strhead = "BB";
		String strendd = "7E";
		String strcmd = "00070001";
		String strhao = Integer.toHexString(b[0]&0xff);
		if (strhao.length() == 1){
			strhao = "0" + strhao;
		}else {
			//strhao = strhao;
		}


		strcmd = strcmd + strhao;
		String strcheck = Utility.CheckSum(strcmd); //

		strcmd = strhead + strcmd + strcheck+ strendd;

//		【2018-07-31 15:44:25:092】MK+MD9=1-1
//
//		【2018-07-31 15:44:25:378】+MD9
//				00001::900002600152406,18/07/30 19:57:10,,F1,82
//
//		【2018-07-31 15:44:25:694】MK+MD9=1-1
//
//		【2018-07-31 15:44:25:986】+MD9
//				00001::900002600152406,18/07/30 19:57:10,,F1,82
//
//		【2018-07-31 15:44:26:137】MK+MD9=1-1
//
//		【2018-07-31 15:44:26:434】+MD9
//		00001::900002600152406,18/07/30 19:57:10,,F1,82
		//strcmd = "4D4B2B4D44393D312D310D0A"; //"MK+MD9=1-1\r\n";

		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}

	public void ZMATestCmd(int region) throws IOException {
		//设置国家指令
		int iregion = region;
		String strcmd;

//		【2018-07-31 15:44:25:092】MK+MD9=1-1
//
//		【2018-07-31 15:44:25:378】+MD9
//				00001::900002600152406,18/07/30 19:57:10,,F1,82
//
//		【2018-07-31 15:44:25:694】MK+MD9=1-1
//
//		【2018-07-31 15:44:25:986】+MD9
//				00001::900002600152406,18/07/30 19:57:10,,F1,82
//
//		【2018-07-31 15:44:26:137】MK+MD9=1-1
//
//		【2018-07-31 15:44:26:434】+MD9
//		00001::900002600152406,18/07/30 19:57:10,,F1,82
		strcmd = "4D4B2B4D44393D312D310D0A"; //"MK+MD9=1-1\r\n";

		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}


	public void listTagID(int mem, int addr, int len) throws IOException {
		byte[] cmd = genListTagIDCmd(mem, addr, len, new byte[] {});
		//发送盘存命令
		//String strcmd = "BB00270003222710837E";
		//byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);
	}

	public void listTagIDAndTid() throws IOException {
		//读 EPC+TID
		//发:BB 00 9A 00 03 22 FF FF BD 7E
		//收:BB 01 39 00 17 0A 24 00 11 11 00 00 00 02 11 22 E2 80 11 0C 20 00 33 46 35 A8 00 00 CB 7E

		//BB 01 --开始
		//39    --cmd
		//00 17 --0x0017(33)
		//0A --
		//24 00 11 11 00 00 00 02 11 22
		//E2 80 11 0C 20 00 33 46 35 A8 00 00 CB
		//7E --结束

		//BB 01 39 00 1B 0E 31 C1 36 39 31 32 33 38 38 39 31 32 33 66 E2 00 34 12 01 3E FD 00 03 E1 7D C3 87 7E

		//BB 01
		//39
		//00 1B (1D)
		//0E 31 C1 36 39 31 32 33 38 38 39 31 32 33 66 E2 00 34 12 01 3E FD 00 03 E1 7D C3 87 7E
		//获取epc+tid
		String strcmd = "BB009A000322FFFFBD7E";//获取epc+tid
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}

	public void listTagIDSingle() throws IOException {
		//获取发单次
		String strcmd = "BB00220000227E";//获取单次:
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

	}

	public void listTagIDTotal() throws IOException {
		//发送总数
		String strcmd = "BB00CE0000CE7E";//获取卡号总数:
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);
		//BB	01	CE	00	2	卡号长度（2个字节）		7E
		//BB01CE00020001D27E ==>只有1个标签
	}

	public void listTagIDMult() throws IOException {
		//发送盘存命令
		//String strcmd = "BB00270003222710837E"; //4分钟左右就完事
		String strcmd = "BB0027000322FFFF4A7E "; //30分钟左右就完事
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);
	}

	public void listTagIDStop() throws IOException {
		//发送停止盘存命令
		String strcmd = "BB00280000287E";
		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);
	}


	public void listTagIDCon(int ixuhao, int inum) throws IOException {
		//发送取卡号
//		获取卡号:
//		Header	Type	Command	PL(MLB	PL(LSB)	Parameter	Len	Checksum	End
//		BB	00	CC	00	03	卡起始序号
//		（2个字节）	获取的长度N
//		1个字节	校验	7E
		//String strcmd = "BB00CC0003000001D07E";//起始序号从0开始（2byte），长度1字节
		String strhead = "BB";
		String strendd = "7E";
		String strcmd = "00CC0003";
		String strhao = Integer.toHexString(ixuhao);
		if (strhao.length() == 1){
			strhao = "000" + strhao;
		}else if (strhao.length() == 2) {
			strhao = "00" + strhao;
		}else if (strhao.length() == 3) {
			strhao = "0" + strhao;
		}

		String strnum = Integer.toHexString(inum);
		if (strnum.length() == 1){
			strnum = "0" + strnum;
		}

		strcmd = strcmd + strhao + strnum;
		String strcheck = Utility.CheckSum(strcmd); //

		strcmd = strhead + strcmd + strcheck+ strendd;

		byte cmd[] = Utility.convert2HexArray(strcmd);

		sendCommand(cmd);

		//下面是一个卡号
		////BB01CE00020001D27E
		//BB01CC
		//0010
		//00000C1234123412341234123412348E7E
		//卡号为:序号（2个字节）+长度（1个字节）+卡号（12个字节）
		//123412341234123412341234
	}

	private byte[] genListTagIDCmd(int mem, int addr, int len, byte[] mask) {

		byte[] param;
		if (len == 0) {
			param = new byte[4];
			param[0] = (byte) mem;
		} else {
			int i, m;

			if (len % 8 == 0) {
				m = len / 8;
			} else {
				m = len / 8 + 1;
			}

			param = new byte[m + 4];
			param[0] = (byte) mem;
			param[1] = (byte) (addr >> 8);
			param[2] = (byte) (addr & 0xFF);
			param[3] = (byte) len;

			for (i = 0; i < m; i++) {
				param[4 + i] = mask[i];
			}
		}

		return generateCMD(CommandCode.listTag, param);
	}

	public void getListTagID(int no, int l) throws IOException {
		byte[] param = new byte[2];
		param[0] = (byte) no;
		param[1] = (byte) l;
		byte[] cmd = generateCMD(CommandCode.getIdList, param);
		sendCommand(cmd);
	}

	/**
	 * getListTagID command result
	 * 
	 * @param ret
	 * @return
	 */
	public static ListTagIDResult parseGetListTagIDResult(byte[] ret) {
		byte[] data = getDataSegment(ret);
		int len = data[0];

		int index = 1;
		ArrayList<byte[]> epcs = new ArrayList<byte[]>();
		for (int i = 0; i < len; i++) {
			int ecpLen = data[index];
			byte[] epc = new byte[ecpLen * 2];
			System.arraycopy(data, index + 1, epc, 0, ecpLen * 2);
			epcs.add(epc);
			index = index + (ecpLen * 2) + 1;
			Log.i(TAG, "get epc " + Utility.bytes2HexString(epc) + " " + index
					+ " " + ecpLen);
		}

		return new ListTagIDResult(len, epcs);
	}

	/**
	 * read command result data
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] getCmdResult() throws IOException {
		int head = read();
		int len = read();
		ByteBuffer bb = ByteBuffer.allocate(2 + len);
		bb.put((byte) head).put((byte) len);
		for (int i = 0; i < len; i++) {
			bb.put((byte) read());
		}

		Log.d(TAG, "get result " + Utility.bytes2HexString(bb.array()));
		EventBus.getDefault().post(new GetCommandResultSuccess(true));
		return bb.array();
	}

	/**
	 * 超时读取结果
	 * 
	 * @param maxTimeout
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	// public byte[] getCmdResultWithTimeout(long maxTimeout)
	// throws IOException, InterruptedException, TimeoutException {
	// long interval = 30;
	// int available = 0;
	// long timeout = 0;
	//
	// while((available = inStream.available()) == 0 && timeout <
	// ConfigUI.getConfigTimeout(activity)) {
	// timeout += interval;
	// Thread.sleep(interval);
	// }
	//
	// if( available <=0 && timeout > 0) {
	// throw new TimeoutException();
	// }
	//
	// Log.i(TAG, "available " + available+" timeout " + timeout);
	//
	// int head = inStream.read();
	// //skip data from click button on the reader
	// if ( Head.RECEIVE_OK.getCode() != (byte)head &&
	// Head.RECEIVE_FAIL.getCode() != (byte)head ) {
	// Log.w(TAG, "Get other data, skip 250 bytes [head] " + head + " " +
	// (byte)head);
	// long start = System.currentTimeMillis();
	// for (int i = 0; i< 249; i++) {
	// inStream.read();
	// }
	// long cost = System.currentTimeMillis() - start;
	// timeout+=cost;
	//
	// while((available = inStream.available()) == 0 && timeout <
	// ConfigUI.getConfigTimeout(activity)) {
	// timeout += 100;
	// Thread.sleep(100);
	// }
	//
	// if( available <=0 ) {
	// throw new TimeoutException();
	// }
	// }
	//
	// if ( Head.RECEIVE_OK.getCode() != (byte)head &&
	// Head.RECEIVE_FAIL.getCode() != (byte)head ) {
	// head = inStream.read();
	// }
	//
	// int len = read();
	// ByteBuffer bb = Byte.allocate(2 + len);
	// bb.put((byte) head).put((byte) len);
	// for (int i = 0; i < len; i++) {
	// bb.put((byte) read());
	// }
	//
	// Log.d(TAG, "get result " + Utility.bytes2HexString(bb.array()) +" " +
	// VH73Device.getError(bb.array()));
	// EventBus.getDefault().post(new GetCommandResultSuccess(true));
	// return bb.array();
	// }

	public byte[] getCmdZMAResult() {
		int available = 0;
		String strData;
		//String strData1;
		byte[] buffer = new byte[4097];
		if (Utility.iNetorBTcomm == 1) { //0--BT,1--NET,是蓝牙还是网络通信
			ByteBuffer byteBuffer = ByteBuffer.allocate(4097); //65535);
			if (true) {
				try {
					int iRecvLen = 0;
					byte[] chTemp = new byte[4096];
					int eventsCount = selector.select(5000);
					if (eventsCount > 0) {
						Set selectedKeys = selector.selectedKeys();
						Iterator iterator = selectedKeys.iterator();
						while (iterator.hasNext()) {
							SelectionKey sk = (SelectionKey) iterator.next();
							iterator.remove();
							if (sk.isReadable()) {

								DatagramChannel datagramChannel = (DatagramChannel) sk
										.channel();
								byteBuffer.clear();
								SocketAddress sa = datagramChannel.receive(byteBuffer);
								byteBuffer.flip();


								iRecvLen = byteBuffer.limit();
								// 测试：通过将收到的ByteBuffer首先通过缺省的编码解码成CharBuffer 再输出马储油平台
								CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
								//Bcd2AscEx(chTemp, byteBuffer.array(), iRecvLen*2);
								//System.out.println("receive message:"+ charBuffer.toString());
								//String s = new String(chTemp, "UTF-8");
								//System.out.println("receive message["+ iRecvLen +"]: "+ s.trim());
								buffer = byteBuffer.array();
								strData = Utility.bytes2HexString(buffer, iRecvLen);
								//strData1 = Utility.getStringByBytes(buffer);
								Utility.wq_UdpSendDataHex(strData);
								Log.d(TAG, "Recc:" + strData);
								byteBuffer.clear();
								//byte[] bret = new byte[iRecvLen];
								chTemp = new byte[iRecvLen];
								System.arraycopy(buffer, 0, chTemp, 0, iRecvLen);
								//return  bret;

								//要发送就下面打开
								//datagramChannel.send(bb, sa);
								//datagramChannel.write(buffer);
							}
						}
						return  chTemp;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}


			}
		}else {
			try {
				available = inStream.available();
				if (available > 0) {
					buffer = new byte[available];
					inStream.read(buffer, 0, available);
					strData = Utility.bytes2HexString(buffer, available);
					//strData1 = Utility.getStringByBytes(buffer);
					Utility.wq_UdpSendDataHex(strData);
					Log.d(TAG, "Recc:" + strData);
					return buffer;
				}

			} catch (IOException e1) {
				e1.printStackTrace();
				//Utility.wq_UdpSendDataHex(strData);
				//Log.d(TAG, "Recc:"+strData);
			}
		}

		return null;
	}

	public byte[] getCmdResultWithTimeout(long maxTimeout) throws IOException,
			InterruptedException, TimeoutException {
		long interval = 30;
		int available = 0;
		long timeout = 0;
		long timev = ConfigUI.getConfigTimeout(activity);
		byte[] buffer = new byte[1024];
		byte[] bufTmp = new byte[5];
		int bytes = 0;
		int k = 0;
		int j = 0;
		int i = 0;
		long start = System.currentTimeMillis();
		String strData = "";

		// 下面这是JAVA中的条件编译，与C++还是有点不同的，C++中则为#IFDEF这种形式
		final boolean bComplile = false;// true为以前的算法，false 为后来的算法

		if (bComplile) {
			while (true) {
				if ((available = inStream.available()) == 0 && timeout < timev) {
					Thread.sleep(interval);
					timeout += interval;
					continue;
				}

				if (available <= 0 && timeout >= timev)
					throw new TimeoutException();

				int head = inStream.read();
				// skip data from click button on the reader
				if (Head.RECEIVE_OK.getCode() != (byte) head
						&& Head.RECEIVE_FAIL.getCode() != (byte) head) {
					Log.w(TAG, "Get other data, skip 250 bytes [head] " + head
							+ " " + (byte) head);

					for (i = 0; i < 249; i++) {
						if ((available = inStream.available()) == 0
								&& (System.currentTimeMillis() - start) < timeout) {
							Thread.sleep(interval);
							continue;
						}
						inStream.read();
					}
					// long cost = System.currentTimeMillis() - start;
					// timeout += cost;
					// continue;
				}

				int len = read();
				ByteBuffer bb = ByteBuffer.allocate(2 + len);
				bb.put((byte) head).put((byte) len);
				for (i = 0; i < len; i++) {
					bb.put((byte) read());
				}

				Log.d(TAG, "get result " + Utility.bytes2HexString(bb.array())
						+ " " + VH73Device.getError(bb.array()));
				EventBus.getDefault().post(new GetCommandResultSuccess(true));
				return bb.array();
			}
		} else {
			// Calendar CD = Calendar.getInstance();
			// int YY = CD.get(Calendar.YEAR);
			// int MM = CD.get(Calendar.MONTH) + 1;
			// int DD = CD.get(Calendar.DATE);
			// int HH = CD.get(Calendar.HOUR);
			// int NN = CD.get(Calendar.MINUTE);
			// int SS = CD.get(Calendar.SECOND);
			// int MI = CD.get(Calendar.MILLISECOND);
			// System.out.println();

			// 1.在超时时间内读出两字节,如果超时了直接退出
			// 2.判断第一个字节是不是F0或F4，如果不是则退出
			// 3.根据第二个字节长度，则读出后面的这么多字节，如：F403EE0219,如果超时则退出
			// 4.分析数据，或返回读出BYTE数组
			long lnow = android.os.SystemClock.uptimeMillis(); // 起始时间
			long lnew = android.os.SystemClock.uptimeMillis(); // 结束时间
			// if(true)
			{
				// 1.在超时时间内读出两字节,如果超时了直接退出
				int count = 0;
				int readCount = 0;
				byte[] bufferTmp1 = new byte[5];
				byte[] bufferTmp2 = new byte[32];

				// <1>先读5个
				for (j = 0; j < 5;) {
					lnew = android.os.SystemClock.uptimeMillis();
					if (lnew - lnow > timev) {
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String Standby = formatter.format(curDate);

						strData = "[" + Standby + "]"
								+ "---Timout!!!-->recOri:" + "(" + j
								+ ")" + Utility.bytes2HexString(bufferTmp1);
						wq_UdpSendData(strData + "\r\n");
						Log.d(TAG, strData);

						throw new TimeoutException();

					}
					available = inStream.available();
					if (available > 0) {
						inStream.read(bufferTmp1, j, 1);
						j++;
					} else {
						continue;
					}
				}

				// 如果大于或等于以上则有可能读到了？
				if (j >= 5) {
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss.SSS");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String Standby = formatter.format(curDate);

					strData = "[" + Standby + "]" + "---recx:" + "(" + j + ")"
							+ Utility.bytes2HexString(bufferTmp1);
					wq_UdpSendData(strData + "\r\n");
					Log.d(TAG, strData);
				}

				// /////////////////////////////////////////////////////////////////////
				// 下面这是判跳250个字节
				// <2>.判断第一个字节是不是F0或F4，如果不是则退出
				strData = "Head Check1!!!";
				wq_UdpSendData(strData + "\r\n");

				int head = bufferTmp1[0];
				// skip data from click button on the reader
				if (Head.RECEIVE_OK.getCode() != (byte) head
						&& Head.RECEIVE_FAIL.getCode() != (byte) head) {
					Log.w(TAG, "Get other data, skip 250 bytes [head] " + head
							+ " " + (byte) head);
					start = System.currentTimeMillis();
					for (i = 0; i < 245; i++) {
						lnew = android.os.SystemClock.uptimeMillis();
						if (lnew - lnow > timev) {
							break;
						}
						// inStream.read();
						available = inStream.available();
						if (available > 0) {
							inStream.read(buffer, i, 1);
						} else {
							continue;
						}

					}

					strData = "Get Error!!!";
					wq_UdpSendData(strData + "\r\n");
					buffer = new byte[5];
					buffer[0] = (byte) 0xF4;
					buffer[1] = (byte) 0x03;
					buffer[2] = (byte) 0xEE;
					buffer[3] = (byte) 0x02;
					buffer[4] = (byte) 0x19;
					return buffer;

				}
				// //////////////////////////////////////////////////////////////////////

				// <3>.根据第二个字节长度，则读出后面的这么多字节，如：F403EE0219,如果超时则退出

				// int len = read();
				bufferTmp2 = new byte[1];
				strData = "Head Check2!!!";
				wq_UdpSendData(strData + "\r\n");
				int iRevc = 5;// 是已心声了的
				int len = bufferTmp1[1];
				if (Head.RECEIVE_OK.getCode() == (byte) head
						|| Head.RECEIVE_FAIL.getCode() == (byte) head) {
					count = 2 + len - iRevc;
					if (count > 0) {
						bufferTmp2 = new byte[count];
					}
					i = 0;
					while (i < count) {
						lnew = android.os.SystemClock.uptimeMillis();
						if (lnew - lnow > timev) {
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss.SSS");
							Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
							String Standby = formatter.format(curDate);

							strData = "[" + Standby + "]"
									+ "---Timout$$$-->recOri:" + "(" + j + ")"
									+ Utility.bytes2HexString(bufferTmp2);
							wq_UdpSendData(strData + "\r\n");
							Log.d(TAG, strData);

							throw new TimeoutException();
							// break;
						}
						available = inStream.available();
						if (available > 0) {
							inStream.read(bufferTmp2, i, 1);
							i++;
						} else {
							continue;
						}

					}

					if (count > 0) {
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String Standby = formatter.format(curDate);

						strData = "[" + Standby + "]" + "---recy:" + "(" + i
								+ ")"
								+ Utility.bytes2HexString(bufferTmp2);
						wq_UdpSendData(strData + "\r\n");
					}

					// 合并字节
					buffer = new byte[5 + bufferTmp2.length];
					int kk = 0;
					int ii = 0;
					for (ii = 0; ii < 5; ii++) {
						buffer[ii] = bufferTmp1[kk];
						kk = kk + 1;
					}
					kk = 0;
					for (; ii < 5 + bufferTmp2.length; ii++) {
						buffer[ii] = bufferTmp2[kk];
						kk = kk + 1;
					}

					ByteBuffer bb = ByteBuffer.allocate(2 + len);
					bb.put((byte) head).put((byte) len);
					for (i = 0; i < len; i++) {
						// bb.put((byte) read());
						bb.put((byte) buffer[i + 2]);
					}

					strData = "get result "
							+ Utility.bytes2HexString(bb.array()) + " "
							+ VH73Device.getError(bb.array());
					wq_UdpSendData(strData + "\r\n");
					Log.d(TAG, strData);
					EventBus.getDefault().post(
							new GetCommandResultSuccess(true));
					return bb.array();
				}

				strData = "Get Error!!!";
				wq_UdpSendData(strData + "\r\n");
				buffer = new byte[5];
				buffer[0] = (byte) 0xF4;
				buffer[1] = (byte) 0x03;
				buffer[2] = (byte) 0xEE;
				buffer[3] = (byte) 0x02;
				buffer[4] = (byte) 0x19;
				return buffer;
			}
		}

	}

	public static boolean wq_UdpSendData(String strData) {
		try {
			// 1. 创建一个DatagramSocket对象

			DatagramSocket socket = new DatagramSocket(8888);

			// 2. 创建一个 InetAddress ， 相当于是地址

			// InetAddress serverAddress =
			// InetAddress.getByName("想要发送到的那个IP地址");
			InetAddress serverAddress = InetAddress.getByName("192.168.0.71");

			// 3. 这是随意发送一个数据

			String str = "hello";
			str = strData;

			// 4. 转为byte类型

			byte data[] = str.getBytes();

			// 5. 创建一个DatagramPacket 对象，并指定要讲这个数据包发送到网络当中的哪个地址，以及端口号

			DatagramPacket sendPacket = new DatagramPacket(data, data.length,
					serverAddress, 8888);

			// 6. 调用DatagramSocket对象的send方法 发送数据

			socket.send(sendPacket);

			socket.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();

		}

		return false;
	}

	public static boolean checkSucc(byte[] data) {
		if (data[0] == Head.RECEIVE_FAIL.getCode())// 0xF4
			return false;
		return true;
	}

	public static String getError(byte[] data) {
		if (!checkSucc(data)) {
			return errMessage(data[4]);
		}

		return "Success";
	}

	public static ListTagIDResult parseListTagIDResult(byte[] ret) {
		byte[] data = getDataSegment(ret);
		int size = data[0];
		int len = 0;
		if (size > 8) {
			len = 8;
		} else {
			len = size;
		}

		int index = 1;
		ArrayList<byte[]> epcs = new ArrayList<byte[]>();
		for (int i = 0; i < len; i++) {
			int ecpLen = data[index];
			byte[] epc = new byte[ecpLen * 2];
			System.arraycopy(data, index + 1, epc, 0, ecpLen * 2);
			epcs.add(epc);
			index = index + (ecpLen * 2) + 1;
			Log.i(TAG, "get epc " + Utility.bytes2HexString(epc) + " " + index
					+ " " + ecpLen);
		}

		return new ListTagIDResult(size, epcs);
	}

	public static class ListTagIDResult {
		public int totalSize;
		public ArrayList<byte[]> epcs;

		public ListTagIDResult(int totalSize, ArrayList<byte[]> epcs) {
			this.totalSize = totalSize;
			this.epcs = epcs;
		}

	}

	public void ReadWordBlock(String epc, int mem, int addr, int lenData,
			String passwd) throws IOException {
		int epcLen = epc.length() / 2;
		int paramSize = 1 + epcLen + 1 + 1 + 1 + 4;
		byte[] param = new byte[paramSize];

		byte[] bepc = Utility.convert2HexArray(epc);
		param[0] = (byte) (epcLen / 2);
		System.arraycopy(bepc, 0, param, 1, epcLen); // epc

		param[1 + epcLen] = (byte) mem;
		param[1 + epcLen + 1] = (byte) addr;
		param[1 + epcLen + 2] = (byte) lenData;

		byte[] bpasswd = Utility.convert2HexArray(passwd);
		param[4 + epcLen] = bpasswd[0];
		param[4 + epcLen + 1] = bpasswd[1];
		param[4 + epcLen + 2] = bpasswd[2];
		param[4 + epcLen + 3] = bpasswd[3];

		sendCommand(generateCMD(CommandCode.readWordBlock, param));
	}

	public static byte[] parseReadWordBlockResult(byte[] ret) {
		return getDataSegment(ret);
	}

	public void WriteWordBlock(String epc, int mem, int addr, String data,
			String passwd) throws IOException {
		// L EPC mem addr len data AccessPassword
		// 1 12 1 1 1 len 4
		int epcLen = epc.length() / 2;
		int dataLen = data.length() / 2;
		int paramSize = 1 + epcLen + 1 + 1 + 1 + dataLen + 4;
		byte[] param = new byte[paramSize];

		byte[] bepc = Utility.convert2HexArray(epc);
		param[0] = (byte) (epcLen / 2);
		System.arraycopy(bepc, 0, param, 1, epcLen); // epc

		param[1 + epcLen] = (byte) mem;
		param[1 + epcLen + 1] = (byte) addr;

		byte[] bData = Utility.convert2HexArray(data);
		param[1 + epcLen + 2] = (byte) (dataLen / 2);
		System.arraycopy(bData, 0, param, 1 + epcLen + 2 + 1, dataLen);

		byte[] bpasswd = Utility.convert2HexArray(passwd);
		param[4 + epcLen + dataLen] = bpasswd[0];
		param[4 + epcLen + dataLen + 1] = bpasswd[1];
		param[4 + epcLen + dataLen + 2] = bpasswd[2];
		param[4 + epcLen + dataLen + 3] = bpasswd[3];

		sendCommand(generateCMD(CommandCode.writeWordBlock, param));
	}

	public void EraseBlock(String epc, int mem, int addr, int len)
			throws IOException {
		int lenEpc = epc.length() / 2; // 12
		int paramSize = 1 + lenEpc + 1 + 1 + 1; // 16
		byte[] param = new byte[paramSize];

		param[0] = (byte) (lenEpc / 2); // 0
		System.arraycopy(Utility.convert2HexArray(epc), 0, param, 1, lenEpc); // 1-13

		param[1 + lenEpc] = (byte) mem; // 14
		param[2 + lenEpc] = (byte) len; // 15

		sendCommand(generateCMD(CommandCode.eraseBlock, param));
	}

	public void KillTag(String epc, String passwd) throws IOException {
		int len = epc.length() / 2; // 12
		int lenPasswd = passwd.length() / 2; // 4
		int paramSize = 1 + len + lenPasswd; // 17

		byte[] param = new byte[paramSize];
		param[0] = (byte) (len / 2); // 0

		System.arraycopy(Utility.convert2HexArray(epc), 0, param, 1, len); // 1
																			// -
																			// 12
		System.arraycopy(Utility.convert2HexArray(passwd), 0, param, 1 + len,
				lenPasswd); // 13-16

		sendCommand(generateCMD(CommandCode.killTag, param));
	}

	public void SetLock(String epc, int mem, int lock, String passwd)
			throws IOException {
		int len = epc.length() / 2; // 12
		int lenPasswd = passwd.length() / 2; // 4
		// L EPC mem Lock AccessPassword
		int paramSize = 1 + len + 1 + 1 + lenPasswd; // 19
		byte[] param = new byte[paramSize];

		// epc
		param[0] = (byte) (len / 2); // 0
		System.arraycopy(Utility.convert2HexArray(epc), 0, param, 1, len); // 1-12

		// mem
		param[1 + len] = (byte) mem; // 13
		// lock
		param[2 + len] = (byte) lock; // 14
		// passwd
		System.arraycopy(Utility.convert2HexArray(passwd), 0, param, 3 + len,
				lenPasswd); // 15-18

		sendCommand(generateCMD(CommandCode.setLock, param));

	}

	public void BlockLock(String epc, String passwd) throws IOException {
		int len = epc.length() / 2; // 12
		int lenPasswd = passwd.length() / 2; // 4
		int paramSize = 1 + len + lenPasswd; // 17

		byte[] param = new byte[paramSize];
		param[0] = (byte) (len / 2); // 0

		System.arraycopy(Utility.convert2HexArray(epc), 0, param, 1, len); // 1
																			// -
																			// 12
		System.arraycopy(Utility.convert2HexArray(passwd), 0, param, 1 + len,
				lenPasswd); // 13-16

		sendCommand(generateCMD(CommandCode.blockLock, param));
	}


	/*
	4.5.设置 Select参数指令
	 */
	public void ZM_SelectTag(String strTag) {
		//select 选择标签 BB 00 0C 00 0D 01 00 00 00 20 60 00 C2 ED C8 AB CB AE 35 7E
		//BB 01 0C 00 01 00 0E 7E
		byte data[] = new byte[1024];
		String strcmd = strTag; //
		byte cmd[] = Utility.convert2HexArray(strcmd);

		int k = 0;
		data[k++] = (byte)0xBB;
		//帧类型 Type: 0x00
		//指令代码 Command: 0x0C
		data[k++] = (byte)0x00;
		data[k++] = (byte)0x0C;

		//指令参数长度 PL 0x0013
		data[k++] = (byte)0x00;
		data[k++] = (byte)0x00;

		//SelParam: 0x01 (Target: 3’b000, Action: 3’b000, MemBank: 2’b01)
		data[k++] = (byte)0x01;
		//Ptr: 0x00000020(以 bit 为单位，非 word) 从 EPC 存储位开始
		data[k++] = (byte)0x00;
		data[k++] = (byte)0x00;
		data[k++] = (byte)0x00;
		data[k++] = (byte)0x20;
		//Mask长度 MaskLen: 0x60(6 个 word，96bits)
		int len = strTag.length()/2;
		if (len==6){
			data[k++] = (byte)0x60;
		} else if (len == 5) {
			data[k++] = (byte)0x50;
		} else if (len == 4) {
			data[k++] = (byte)0x40;
		} else if (len == 3) {
			data[k++] = (byte)0x30;
		} else if (len == 2) {
			data[k++] = (byte)0x20;
		} else if (len == 1) {
			data[k++] = (byte)0x10;
		} else if (len == 7) {
			data[k++] = (byte)0x70;
		} else if (len == 8) {
			data[k++] = (byte)0x80;
		} else if (len == 9) {
			data[k++] = (byte)0x90;
		} else if (len == 10) {
			data[k++] = (byte)0xA0;
		}else{
			data[k++] = (byte)0x60;
		}
		//是否 Truncate: 0x00(0x00 是 Disable truncation，0x80 是 Enable truncation)
		data[k++] = (byte)0x00;
		System.arraycopy(cmd, 0, data, k,
				cmd.length);
		k += cmd.length;

		data[k++] = (byte)0x00;
		data[k++] = (byte)0x7E;

		data[3] = (byte)((int)((k-7)>>8) & 0xFF);
		data[4] = (byte)(k-7);
		byte bcmd = 0x00;
		int n = ((int) (data[3] << 8) | (int) data[4] & 0xFF) + 7 - 3;
		for (int i = 0; i < n; i++){
			bcmd += data[i+1];
		}
		data[k-2] = (byte)bcmd;

		byte []datacmd = new byte[k];
		System.arraycopy(data, 0, datacmd, 0,
				k);
		String strData = Utility.bytes2HexString(datacmd);
		//wq_UdpSendData(strData + "\r\n");
		Utility.wq_UdpSendDataHex(strData);

		sendCommand(datacmd);

	}

	/*
	4.7.读标签数据存储区
	 */
	public void ZM_ReadWordBlock(String epc, int mem, int addr, int len,
							  String passwd) {
		//BB
		//帧类型 Type: 0x00
		//指令代码 Command: 0x39
		//指令参数长度 PL 0x0009
		//Access Password: 0x0000FFFF
		//标签数据存储区 MemBank: 0x03(User 区)
		//读标签数据区地址偏移 SA: 0x0000
		//读标签数据区地址长度 DL: 0x0002
		//校验位 Checksum: 0x45
		//7E
		//总长度:09+7=0x10;//16个字节的指令长度
		byte[] cmd = new byte[16];
		int k = 0;
		cmd[k++] = (byte)0xBB;
		cmd[k++] = (byte)0x00;
		cmd[k++] = (byte)0x39;
		cmd[k++] = (byte)0x00;
		cmd[k++] = (byte)0x09;

		String strpasswd = passwd;//密码
		byte bpass[] = Utility.convert2HexArray(strpasswd);
		cmd[k++] = bpass[0];
		cmd[k++] = bpass[1];
		cmd[k++] = bpass[2];
		cmd[k++] = bpass[3];

		//标签数据存储区 MemBank: 0x03(User 区)
		cmd[k++] = (byte)mem;
		//读标签数据区地址偏移 SA: 0x0000
		cmd[k++] = (byte)((int)(addr>>8) & 0xFF);
		cmd[k++] = (byte)addr;
		//读标签数据区地址长度 DL: 0x0002
		cmd[k++] = (byte)((int)(len>>8) & 0xFF);
		cmd[k++] = (byte)len;

		//校验位 Checksum: 0x45
		byte bcmd = 0x00;
		int n = ((int) (cmd[3] << 8) | (int) cmd[4] & 0xFF) + 7 - 3;
		for (int i = 0; i < n; i++){
			bcmd += cmd[i+1];
		}
		cmd[k++] = (byte)bcmd;
		cmd[k++] = (byte)0x7E;

		String strData = Utility.bytes2HexString(cmd);
		Utility.wq_UdpSendDataHex(strData);

		sendCommand(cmd);
	}


	//BB 00 49 00 11 00 00 00 00 03 00 00 00 04 00 00 00 00 00 00 00 00 61 7E  ==>24字节
	//BB 01 49 00 10 0E 30 00 E2 00 40 74 84 04 00 18 15 00 7D C3 00 23 7E  ==>返回
	/*
	4.7.写标签数据存储区
	 */
	public void ZM_WriteWordBlock(String epcnew, int mem, int addr, int len,
								 String passwd) {
		//send:BB 00 49 00 25 00 00 00 00 01 00 00 00 0E 1A DF 70 70 70 00 89 88 99 95 31 10 00 62 20 90 00 00 00 00 00 00 00 00 00 00 00 00 58 7E
		//49 命令字 长度:0025 Access Password:00 00 00 00 写epc:0x01 从00 00 开始写 写00 0E那么长 数据:1A DF 70 70 70 00 89 88 99 95 31 10 00 62 20 90 00 00 00 00 00 00 00 00 00 00 00 00
		//58为校验 结束:7E
		//recv:BB 01 49 00 20 1E 70 70 70 00 89 88 99 95 31 10 00 62 20 90 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6A 7E
		//==>写入字节数X为PC位？如写入12 34 56 78 =>4*4=16 PC:10 00
		//BB
		//帧类型 Type: 0x00
		//指令代码 Command: 0x49
		//指令参数长度 PL 0x0011
		//Access Password: 0x00000000
		//签数据存储区 MemBank: 0x03(User 区) 0x01为epc
		//标签数据区地址偏移 SA: 0x0000
		//标签数据区地址长度 DL: 0x0004
		//写入数据:00 00 00 00 00 00 00 00 //8byte 4word
		//校验位 Checksum: 0x61
		//7E
		//总长度:0x11+7=0x10;//24个字节的指令长度
		byte[] cmd = new byte[196];
		int k = 0;
		cmd[k++] = (byte)0xBB;
		cmd[k++] = (byte)0x00;
		cmd[k++] = (byte)0x49;
		cmd[k++] = (byte)0x00;
		cmd[k++] = (byte)0x00;

		String strpasswd = passwd;//密码
		byte bpass[] = Utility.convert2HexArray(strpasswd);
		cmd[k++] = bpass[0];
		cmd[k++] = bpass[1];
		cmd[k++] = bpass[2];
		cmd[k++] = bpass[3];

		//标签数据存储区 MemBank: 0x03(User 区)
		cmd[k++] = (byte)mem;
		int iilen = len;
		if (addr >= 2) {
			//读标签数据区地址偏移 SA: 0x0000
			//cmd[k++] = (byte)((int)(addr>>8) & 0xFF);
			//cmd[k++] = (byte)addr;
			//【这里的偏移地址一定为1,一定要写PC位
			cmd[k++] = 0x00;
			cmd[k++] = 0x01;
			//读标签数据区地址长度 DL: 0x0002 假如从2开始写，写3字为6字节，所以pC：6*4=20，14 00 ，epc还是6字节
			len = addr-2 + len+1;
			cmd[k++] = (byte) ((int) (len >> 8) & 0xFF);
			cmd[k++] = (byte) len;
			//BB 00 49 00 0D 00 00 00 00 01 00 01 00 02 08 00 33 33 C8 7E
			//BB 01 49 00 06 04 08 00 33 33 00 C2 7E
		}else{
			cmd[k++] = (byte)((int)(addr>>8) & 0xFF);
			cmd[k++] = (byte)addr;
			cmd[k++] = (byte) ((int) (len >> 8) & 0xFF);
			cmd[k++] = (byte) len;
		}

		String newepc = epcnew;//新epc
		byte bnewepc[] = Utility.convert2HexArray(newepc);
		if (addr >= 2) {
			len = iilen * 2 * 4;
			cmd[k++] = (byte) len;
			cmd[k++] = (byte) 0x00;
			for (int ic = 0; ic < bnewepc.length; ic++) {
				cmd[k++] = bnewepc[ic];

			}
		}else {
			for (int ic = 0; ic < bnewepc.length; ic++) {
				cmd[k++] = bnewepc[ic];

			}
		}

		cmd[4] = (byte)(k-5);
		//校验位 Checksum: 0x45
		byte bcmd = 0x00;
		int n = ((int) (cmd[3] << 8) | (int) cmd[4] & 0xFF) + 7 - 3;
		for (int i = 0; i < n; i++){
			bcmd += cmd[i+1];
		}
		cmd[k++] = (byte)bcmd;
		cmd[k++] = (byte)0x7E;
		byte[] ccmd = new byte[k];
		for (int i = 0; i < k; i++){
			ccmd[i] = cmd[i];
		}

		String strData = Utility.bytes2HexString(ccmd);
		Utility.wq_UdpSendDataHex(strData);

		byte[] cmdd = new byte[k];
		for (int i = 0; i < k; i++){
			cmdd[i] = cmd[i];
		}

		sendCommand(cmdd);
	}




}
