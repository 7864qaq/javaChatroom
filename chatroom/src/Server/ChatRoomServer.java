package Server;

import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatRoomServer extends Thread {
	ServerFrame serverFrame = null;
	ServerSocket serverSocket = null; 

	public boolean bServerIsRunning = false;// 用来判断服务器是否已经启动
	private final int SERVER_PORT = 8888;// 定义服务器端口号

	public ChatRoomServer() {
		try {
			serverSocket = new ServerSocket(SERVER_PORT); // 启动服务
			bServerIsRunning = true;// 确认服务器已经启动

			serverFrame = new ServerFrame();// 创建一个服务器框架
			getServerIP(); // 得到并显示服务器端IP
			System.out.println("Server port is:" + SERVER_PORT);
			serverFrame.taLog.setText("服务器已经启动...");// 服务器端显示服务器已经启动
			while (true) {// 循环监听客户端的请求
				Socket socket = serverSocket.accept(); // 监听客户端的连接请求，并返回客户端socket
				new ServerProcess(socket, serverFrame); // 创建一个新线程来处理与该客户的通讯
			}
		} catch (BindException e) {
			System.out.println("端口使用中....");
			System.out.println("请关掉相关程序并重新运行服务器！");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("[ERROR] Cound not start server." + e);
		}

	
	}

	// 获取服务器的主机名和IP地址
	public void getServerIP() {
		try {
			InetAddress serverAddress = InetAddress.getLocalHost();// 得到主机名
			byte[] ipAddress = serverAddress.getAddress();// 得到主机地址

			serverFrame.txtServerName.setText(serverAddress.getHostName());// 显示主机名
			serverFrame.txtIP.setText(serverAddress.getHostAddress());// 显示主机地址
			serverFrame.txtPort.setText("8888");// 显示端口号

			System.out.println("Server IP is:" + (ipAddress[0] & 0xff) + "." + (ipAddress[1] & 0xff) + "."
					+ (ipAddress[2] & 0xff) + "." + (ipAddress[3] & 0xff));
		} catch (Exception e) {
			System.out.println("[ERROR] Cound not get Server IP." + e);
		}
	}

	public static void main(String[] args) {
		new ChatRoomServer();
	}

}
