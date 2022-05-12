/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */


package lib;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

/*
 * AttendanceLib类是JNI类，负责程序与硬件的通信
 */
public class AttendanceLib {

	private static final String TAG = "AttendanceLib";


	public static native String system(String strcmd ,String path);
	public static native int systemmy(String strcmd);
	public static native int setDevName(byte[] strDevName ,int isize);
	public static native int  Num(int x, int y);
	public static native int  IsProcessExit(String strNamePro);//返回1则进程存在,否则不存在
	public static native int DateA(byte[] buf);//取时间
	public static native int GetLocalIP(byte[] var0);

	public static native int openSerialPort(String devName, long baud, int dataBits, int stopBits);
	public static native int close(int fd);
	public static native int select(int fd, int sec, int usec);
	public static native int write(int fd, byte[] data, int len);
	public static native int read(int fd, byte[] buf, int len);


	public static native String ZMPacket(String lpSendBuf);

	public static native int ZMAinit(); //初始化
	public static native int ZMASetFormata(int iform); ////0--M0模块解析 1--车载控制板协议解析
	public static native int ZMAPushData(byte[] buf, int len); //往里面放数据
	public static native int ZMAGetData(byte[] buf, int len); //从里面取数据


	// so库的版本号
	public static native int GetLibVersion(byte[] buf);

	//连接读写器并设读写器的IP地址和端口，一次命令即可完成,最后再关闭读写器同时重启读写器
	//sSocket 为通信句柄 nTargetAddress 为读写器地址 nTargetPort 为读写器端口 如:192.168.0.199 1969
	//nHostAddress 为本机如果安桌为android地址 nHostPort 为本机地址 如:192.168.0.104 5001
	//nReaderAddress 为新的读写器地址 如:192.168.0.198
	//nReaderMask 为新的读写器地址 如:255.255.255.0
	//nReaderGateway 为新的读写器地址 如:192.168.0.1
	//nReaderPort 为新的读写器地址 如:1970
	//nNewHostAddress 为新的主机地址 如:192.168.0.198
	//nNewHostPort 为新的主机端口 如:1970
	public static native int ZMSetReaderIPAndHostIPAddr(int[] socket, byte[] nTargetAddress, int nTargetPort, byte[] nHostAddress, int nHostPort, byte[] nReaderAddress, byte[] nReaderMask, byte[] nReaderGateway, int nReaderPort, byte[] nNewHostAddress, int nNewHostPort);

	static {
		// System.loadLibrary("devapi");
		System.loadLibrary("Attendance");//hello-jni");// irdaSerialPort");
		//System.loadLibrary("AndroidMLib");
	}
	
}
