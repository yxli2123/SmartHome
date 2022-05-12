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

package com.iot.wantrue.lib;

public class wlib {


	private static final String TAG = "LOK";

	/*text*/
	public static native int testaa(int[] data);
	/*验证函数*/
	/**
	 * @param bufsnd 16进制字符串
	 * @param isndsize 字节数组大小
	 * @param bufrcv 返回值
	 * @param ircvsize 返回值长度
	 * */
	public static native int testab(byte[] bufsnd,int[] isndsize, byte[] bufrcv, int[] ircvsize);

	//int [] iaa = new int[1];
	//isoc = wlib.testaa(iaa);
	
	
	// so库的版本号
	public static native int GetLibVersion(byte[] buf);

	//测试用
	public static native int Num(int a, int b);

	//连接读写器
	public static native int NetConnectScanner(int[] socket, byte[] nTargetAddress, int nTargetPort, byte[] nHostAddress, int nHostPort);
		
	//断开连接
	public static native int NetDisconnectScannerEx(int socket);
		
	// 通用通信模块 icmd为命令字
	/**
	 * @param fd 连接成功后返回的句柄值
	 * @param bufsnd 发送包的命令及效验码
	 * @param isndsize bufsnd的有效长度
	 * @param bufrcv 接收包的命令及效验码
	 * @param ircvsize bufrcv的有效长度
	 * @param bufret 发送和接收处理的数据
	 * @param iretsize bufret的有效长度
	 * @param icmd 命令码
	 * @param itimeout 超时时间
	 * */
	public static native int socketpacket(int fd, byte[] bufsnd,
			int[] isndsize, byte[] bufrcv, int[] ircvsize, byte[] bufret,
			int[] iretsize, int icmd, int itimeout);


	//连接读写器并设读写器的IP地址和端口，一次命令即可完成,最后再关闭读写器重启读写器
	//sSocket 为通信句柄 nTargetAddress 为读写器地址 nTargetPort 为读写器端口 如:192.168.0.199 1969
	//nHostAddress 为本机如果安桌为android地址 nHostPort 为本机地址 如:192.168.0.104 5001
	//nReaderAddress 为新的读写器地址 如:192.168.0.198
	//nReaderMask 为新的读写器地址 如:255.255.255.0
	//nReaderGateway 为新的读写器地址 如:192.168.0.1
	//nReaderPort 为新的读写器端口 如:1970
	public static native int ZMSetReaderAddr(int[] socket, byte[] nTargetAddress, int nTargetPort, byte[] nHostAddress, int nHostPort, byte[] nReaderAddress, byte[] nReaderMask, byte[] nReaderGateway, int nReaderPort);

	//连接读写器并设读写器的IP地址和端口，一次命令即可完成,最后再关闭读写器同时重启读写器
	//sSocket 为通信句柄 nTargetAddress 为读写器地址 nTargetPort 为读写器端口 如:192.168.0.199 1969
	//nHostAddress 为本机如果安桌为android地址 nHostPort 为本机地址 如:192.168.0.104 5001
	//nNewHostAddress 为新的主机地址 如:192.168.0.198
	//nNewHostPort 为新的主机端口 如:1970
	public static native int ZMHostAddr(int[] socket, byte[] nTargetAddress, int nTargetPort, byte[] nHostAddress, int nHostPort, byte[] nNewHostAddress, int nNewHostPort);

	//连接读写器并设读写器的IP地址和端口，一次命令即可完成,最后再关闭读写器同时开启自动模式
	//sSocket 为通信句柄 nTargetAddress 为读写器地址 nTargetPort 为读写器端口 如:192.168.0.199 1969
	//nHostAddress 为本机如果安桌为android地址 nHostPort 为本机地址 如:192.168.0.104 5001
	//nMode 为模式 0--停止 1--开启自动
	public static native int ZMAutoMode(int[] socket, byte[] nTargetAddress, int nTargetPort, byte[] nHostAddress, int nHostPort, int nMode);

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
		System.loadLibrary("AndroidMLib");
		System.loadLibrary("devapi");
		System.loadLibrary("uhf");
	}
}
