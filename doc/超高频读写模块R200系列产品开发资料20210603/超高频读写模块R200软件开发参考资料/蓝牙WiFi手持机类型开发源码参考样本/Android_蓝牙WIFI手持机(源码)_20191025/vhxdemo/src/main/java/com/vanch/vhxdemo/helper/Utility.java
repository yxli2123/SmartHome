package com.vanch.vhxdemo.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.vanch.vhxdemo.ConfigUI;

public class Utility {
	private static Activity activity;
	public static int iopt = 0; //0--bluereader 1--bluehandler
	public static int iNetorBTcomm = 0; //0--BT,1--NET,是蓝牙还是网络通信
	public static int iNetConnectd = 0; //0--没有连上,1--连上了。

	public Utility(Activity activity) {

		this.activity = activity;
	}
	
	public static void setActivity(Activity act) {
		activity = act;
	}

	public static byte[] convert2HexArray(String hexString) {
		int len = hexString.length() / 2;
		char[] chars = hexString.toCharArray();
		String[] hexes = new String[len];
		byte[] bytes = new byte[len];
		for (int i = 0, j = 0; j < len; i = i + 2, j++) {
			hexes[j] = "" + chars[i] + chars[i + 1];
			bytes[j] = (byte) Integer.parseInt(hexes[j], 16);
		}

		return bytes;
	}
	
	public static String bytes2HexString(byte[] b, int count) {
		String ret = "";
		for (int i = 0; i < count; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase(Locale.getDefault());
		}
		return ret;
	}

	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase(Locale.getDefault());
		}
		return ret;
	}
	
	public static String bytes2HexStringWithSperator(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase(Locale.getDefault());
			if ((i+1) % 4 == 0 && (i +1) != b.length)
				ret += "-";
		}
		return ret;
	}
	
	public static AlertDialog WarningAlertDialg(Activity activity, String titile, String message) {
		return new AlertDialog.Builder(activity).setTitle(titile)
				.setMessage(message)
				.setPositiveButton("OK", null)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.create();
	}
	

	public static void showDialogInNonUIThread(final Activity activity, final String title, final String message) {
		new Thread() {
			public void run() {
				Looper.prepare();
				WarningAlertDialg(activity, title, message).show();
				Looper.loop();
			}
		}.start();
	}
	
	public static void showTostInNonUIThread(final Activity activity, final String message) {
		new Thread() {
			public void run() {
				Looper.prepare();
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
	}
	
  public static byte BYTE(int i) {
    return (byte) i;
  }
  
  /**
   * check whether the str is a hex str
   *
   * @param str str
   * @param bits bits
   * @return true or false
   */
  public static boolean isHexString(String str, int bits) {
      String patten = "[abcdefABCDEF0123456789]{" + bits + "}";
      if (str.matches(patten)) {
          return true;
      } else {
          return false;
      }
  }

  public static boolean isHexString(String str) {
      String patten = "[abcdefABCDEF0123456789]{1,}";
      if (str.matches(patten)) {
          return true;
      } else {
          return false;
      }
  }

  public static boolean isNumber(String str) {
      String patten = "[-]{0,1}[0123456789]{0,}";
      return str.matches(patten);
  }

	public static JSONObject get(String url){
		StringBuffer strJson = new StringBuffer();
		//HttpClient client = new DefaultHttpClient();
		//HttpGet get = new HttpGet(url);
		//HttpResponse response;
		InputStream is = null;
		try {
			URL urla = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urla.openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(3000);
			conn.connect();
			is = conn.getInputStream();
		} catch (Exception e) {
			return null;
		}

		//InputStream is = conn.getInputStream();

		try {
			//response = client.execute(get);
			//HttpEntity entity = response.getEntity();
			//InputStream is = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				strJson.append(line);
			}
		} catch (ClientProtocolException e) {
			return null;
		} catch (IllegalStateException e) {

			e.printStackTrace();
			return null;
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}

		if (strJson.length() <= 0) {
			Log.i("help", "get empty json file...");
			return null;
		}

		Log.i("help", "get json " + strJson.toString());

		JSONObject object = null;
		try {
			object = new JSONObject(strJson.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return object;
	}

	/**
	 * byte[]转变为16进制String字符, 每个字节2位, 不足补0
	 */
	public static String getStringByByteslen(byte[] bytes, int ilen) {
		String result = null;
		String hex = null;
		if (bytes != null && ilen > 0) {
			final StringBuilder stringBuilder = new StringBuilder(ilen);
			for (int i = 0; i < ilen; i++) {
				byte byteChar = bytes[i];
				hex = Integer.toHexString(byteChar & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				stringBuilder.append(hex.toUpperCase());
			}
			result = stringBuilder.toString();
		}
		return result;
	}

	/**
	 * byte[]转变为16进制String字符, 每个字节2位, 不足补0
	 */
	public static String getStringByBytes(byte[] bytes) {
		String result = null;
		String hex = null;
		if (bytes != null && bytes.length > 0) {
			final StringBuilder stringBuilder = new StringBuilder(bytes.length);
			for (byte byteChar : bytes) {
				hex = Integer.toHexString(byteChar & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				stringBuilder.append(hex.toUpperCase());
			}
			result = stringBuilder.toString();
		}
		return result;
	}

	/*******************************************************************************
	 * Function Name  : CheckSum
	 * Description    : 校验和算法
	 * Input          : buf:数据起始地址； packLen:数据长度；
	 * Output         : None
	 * Return         : 校验码
	 * Attention		   : None
	 *******************************************************************************/
	public static String CheckSum(String hexString)
	{
		int			i,j;
		byte		sum;

		int len = hexString.length() / 2;
		char[] chars = hexString.toCharArray();
		String[] hexes = new String[len];
		byte[] bytes = new byte[len];
		for (i = 0, j = 0; j < len; i = i + 2, j++) {
			hexes[j] = "" + chars[i] + chars[i + 1];
			bytes[j] = (byte) Integer.parseInt(hexes[j], 16);
		}

		sum = 0;
		for(i=0; i<len; i++)
			sum += bytes[i];

		String hex = Integer.toHexString(sum & 255);
		if (hex.length() == 1) {
			hex = "0" + hex;
		}
		return hex;
	}

	public static boolean wq_UdpSendData(String strData) {
		try {
			// 1. 创建一个DatagramSocket对象

			DatagramSocket socket = new DatagramSocket(8888);

			// 2. 创建一个 InetAddress ， 相当于是地址

			// InetAddress serverAddress =
			// InetAddress.getByName("想要发送到的那个IP地址");
			// InetAddress serverAddress =
			// InetAddress.getByName("192.168.0.71");// "192.168.0.71");
			String strIP = ConfigUI.getConfigCheckIp(activity);
			InetAddress serverAddress = InetAddress.getByName(strIP);

			// 3. 这是随意发送一个数据

			String str = "hello";
			str = strData;

			// 4. 转为byte类型

			byte data[] = str.getBytes();

			// 5. 创建一个DatagramPacket 对象，并指定要讲这个数据包发送到网络当中的哪个地址，以及端口号

			// DatagramPacket sendPacket = new DatagramPacket(data, data.length,
			// serverAddress, 8888);// 8888);
			String strPort = ConfigUI.getConfigCheckPort(activity);
			int iPort = Integer.valueOf(strPort);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length,
					serverAddress, iPort);// 8888);

			// 6. 调用DatagramSocket对象的send方法 发送数据

			socket.send(sendPacket);

			socket.close();

			return true;
		} catch (Exception e) {
			// Log.d("TAG", e.getMessage());
			e.printStackTrace();

		}

		return false;
	}


	public static boolean wq_UdpSendDataHex(String strData) {
		try {
			// 1. 创建一个DatagramSocket对象

			DatagramSocket socket = new DatagramSocket(8888);

			// 2. 创建一个 InetAddress ， 相当于是地址

			// InetAddress serverAddress =
			// InetAddress.getByName("想要发送到的那个IP地址");
			// InetAddress serverAddress =
			// InetAddress.getByName("192.168.0.71");// "192.168.0.71");
			String strIP = ConfigUI.getConfigCheckIp(activity);
			InetAddress serverAddress = InetAddress.getByName(strIP);

			// 3. 这是随意发送一个数据

			String str = "hello";
			str = strData;

			// 4. 转为byte类型

			//byte data[] = str.getBytes();
			byte data[] = Utility.convert2HexArray(str);

			// 5. 创建一个DatagramPacket 对象，并指定要讲这个数据包发送到网络当中的哪个地址，以及端口号

			// DatagramPacket sendPacket = new DatagramPacket(data, data.length,
			// serverAddress, 8888);// 8888);
			String strPort = ConfigUI.getConfigCheckPort(activity);
			int iPort = Integer.valueOf(strPort);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length,
					serverAddress, iPort);// 8888);

			// 6. 调用DatagramSocket对象的send方法 发送数据

			socket.send(sendPacket);

			socket.close();

			return true;
		} catch (Exception e) {
			// Log.d("TAG", e.getMessage());
			e.printStackTrace();

		}

		return false;
	}


}
