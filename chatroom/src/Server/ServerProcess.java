package Server;

/*
 * 类名：ServerProcess
 * 服务端线程
 * 描述：接收到客户端socket发来的信息后进行解析、处理、转发。
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

class ServerProcess extends Thread {
	private Socket socket = null;// 定义客户端套接字

	private BufferedReader in;// 定义输入流
	private PrintWriter out;// 定义输出流

	@SuppressWarnings("rawtypes")
	private static Vector onlineUser = new Vector(10, 5);// 初始化一个可增长数组
	@SuppressWarnings("rawtypes")
	private static Vector socketUser = new Vector(10, 5);// 初始化一个可增长数组

	private String strReceive, strKey;// 定义接受到的信息，和命令
	private StringTokenizer st;// 具有标记的字符串

	private ServerFrame sFrame = null;// 定义一个框架



	// 构造函数
	public ServerProcess(Socket client, ServerFrame frame) throws IOException {
		socket = client;// 接受客户端信息
		sFrame = frame;// 接受框架信息

		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); // 客户端接收
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);// 客户端输出
		this.start();
	}
	public boolean select(String Acc,String inp) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user","myuser","123456");
		String sql="select "+Acc+" from info";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet re = ps.executeQuery();
		
		while(re.next()) {
			if(inp.equals(re.getString(Acc))) {
				conn.close();
				ps.close();
				return true;
			}
		}
		conn.close();
		ps.close();
		return false;
	}
	public boolean selectyw(String Acc,String Pas) throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user","myuser","123456");
		String sql="select * from info";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet re = ps.executeQuery();
		while(re.next()) {
			re.getString("Account");
			re.getString("Password");
			if(re.getString("Account").equals(Acc)&re.getString("Password").equals(Pas)) {
				conn.close();
				ps.close();
				return true;
			}
		}
		conn.close();
		ps.close();
		return false;
	}
	public void insert(String Acc,String Pas) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user","myuser","123456");
		String sql="insert info values('"+Acc+"','"+Pas+"')";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		conn.close();
		ps.close();
		//SQLIntegrityConstraintViolationException
	}
	// run函数,线程跑起来之后会调用run函数
	public void run() {
		try {
			while (true) {
				strReceive = in.readLine();// 从服务器端接收一条信息后拆分、解析，并执行相应操作
				st = new StringTokenizer(strReceive, "|");// 用‘|’拆分数据
				strKey = st.nextToken();// 根据命令执行各种操作
				if (strKey.equals("login")) {
					login();// 调用登录函数
				} else if (strKey.equals("talk")) {
					talk();
				} else if (strKey.equals("init")) {
					freshClientsOnline();
				} else if (strKey.equals("reg")) {
					register();
				} else if (strKey.equals("expression")) {
					sendExpression();
				} 

			}
		} catch (IOException e) { // 用户关闭客户端造成此异常，关闭该用户套接字。
			String leaveUser = closeSocket();
			log("用户 " + leaveUser + " 已经退出。");
			try {
				freshClientsOnline();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("[SYSTEM] " + leaveUser + " leave chatroom!");
			sendAll("talk|[系统]" + leaveUser + "离开了聊天室");
		}
	}

	// 判断是否有该注册用户
	private boolean isExistUser(String name) {
		try {
			boolean flag=select("Acconut",name);
			if(flag==true)
			{
				return true;
			}
		} catch (ClassNotFoundException e) {
			System.out.println("[ERROR] User File has not exist!" );
			out.println("warning|读写文件时出错!");
		} catch (SQLException e) {
			System.out.println("[ERROR] User File has not exist!" );
			out.println("warning|读写文件时出错!");
		}
		return false;// 没有找到就返回false
	}

	// 判断用户的用户名密码是否正确
	private boolean isUserLogin(String name, String password) {
		try {
				boolean flag=selectyw(name,password);
				if(flag==true)
				{
					return true;
				}
			} catch (ClassNotFoundException e) {
				System.out.println("[ERROR] User File has not exist!" );
				out.println("warning|读写文件时出错!");
			} catch (SQLException e) {
				System.out.println("[ERROR] User File has not exist!" );
				out.println("warning|读写文件时出错!");
				
			}
		
		return false;
	}

	// 用户注册
	private void register() throws IOException {
		String name = st.nextToken(); // 得到用户名称
		String password = st.nextToken().trim();// 得到用户密码

		if (isExistUser(name)) {// 判断用户是否已经注册了
			System.out.println("[ERROR] " + name + " Register fail!");
			out.println("warning|该用户已存在，请改名!");
		} else {
			try {
				insert(name,password);
			} catch (ClassNotFoundException e) {
				System.out.println("[ERROR] User File has not exist!" );
				out.println("warning|读写文件时出错!");
			} catch (SQLException e) {
				System.out.println("[ERROR] User File has not exist!" );
				out.println("warning|读写文件时出错!");
			}
			log("用户 " + name + " 注册成功。");
			System.out.println("用户 " + name + " 注册成功。");
			userLoginSuccess(name); // 自动登陆聊天室
		}
	}

	// 用户登陆(从登陆框直接登陆)
	private void login() throws IOException {
		String name = st.nextToken(); // 得到用户名称
		String password = st.nextToken().trim();// 得到用户密码
		boolean succeed = false;// 用来判断是否登录成功的

		log("用户 " + name + " 正在登陆..." + "\n" + "密码 : " + password);
		System.out.println("[USER LOGIN] " + name + ":" + password + ":" + socket);

		for (int i = 0; i < onlineUser.size(); i++) {// 匹配在线用户列表，判断是否已经登录
			if (onlineUser.elementAt(i).equals(name)) {
				System.out.println("[ERROR] " + name + " is logined!");
				out.println("warning|" + name + "已经登陆聊天室");
				return;
			}
		}
		if (isUserLogin(name, password)) { // 判断用户名和密码
			userLoginSuccess(name);
			succeed = true;
		}
		if (!succeed) {
			out.println("warning|" + name + "登陆失败，请检查您的输入!");
			log("用户 " + name + " 登陆失败！");
			System.out.println("[SYSTEM] " + name + " login fail!");
		}
	}

	// 用户登陆成功
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void userLoginSuccess(String name) throws IOException {
		Date t = new Date();
		out.println("login|succeed");
		sendAll("online|" + name);// 向所有用户发送登录成功

		onlineUser.addElement(name);// 在线列表添加该用户名字
		socketUser.addElement(socket);// 连接套接字列表添加该用户名字

		log("用户 " + name + " 登录成功， " + "\n登录时间:" + t.toLocaleString());

		freshClientsOnline();// 刷新客户端在线名单
		sendAll("talk|[系统]欢迎" + name + "来到聊天室");// 服务器发送消息客户上线信息
		System.out.println("[SYSTEM] " + name + " login succeed!");// 在日志框中输出登录信息
	}

	// 聊天信息处理
	private void talk() throws IOException {
		String strTalkInfo = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);// 记录在日志中
		Socket socketSend;
		PrintWriter outSend;

		// 得到当前时间
		String strTime = getTime();

		log("用户 " + strSender + " 对  " + strReceiver + " 说: " + strTalkInfo);// 记录在日志中

		if (strReceiver.equals("All")) {// 判断是否是群聊
			sendAll("talk|" + strSender + " " + strTime + " : " + strTalkInfo);
		}else {
				for (int i = 0; i < onlineUser.size(); i++) {
					if (strReceiver.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println("talk|[私聊 TO" + " me]" + strSender+" " + strTime + "：" + strTalkInfo);
					}else  if (strSender.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println("talk|[私聊 TO" + strReceiver+"]"+strSender + " " + strTime + "：" + strTalkInfo);
					}
				
			}
		}
	}

	// 获取时间
	@SuppressWarnings("deprecation")
	private String getTime() {
		Date date = new Date();
		String strTime = "(" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + ")";
		strTime = "(" + date.toLocaleString() + ")";
		return strTime;
	}

	// 发送表情
	private void sendExpression() throws IOException {
		String strExpression = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[SendExpression_" + strReceiver + "] " + strExpression);
		Socket socketSend;
		PrintWriter outSend;
		new Date();

		// 得到当前时间
		String strTime = getTime();

		log("用户 " + strSender + " 对  " + strReceiver + " 说: " + strExpression);

		if (strReceiver.equals("All")) {
			sendAll("expression|" + strSender + " " + strTime + " : " + "|" + strExpression + "|");
		} else {
			if (strSender.equals(strReceiver)) {
				out.println("talk|[系统]不可以自言自语！");
			} else {
				for (int i = 0; i < onlineUser.size(); i++) {
					if (strReceiver.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println(
								"expression|[私聊]" + strSender + " " + strTime + "：" + "|" + strExpression + "|");
					} else if (strSender.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println(
								"expression|[私聊]" + strReceiver + " " + strTime + "：" + "|" + strExpression + "|");
					}
				}
			}
		}
	}

	// 在线用户列表
	@SuppressWarnings("unchecked")
	private void freshClientsOnline() throws IOException {
		String strOnline = "online";
		String[] userList = new String[20];
		String useName = null;

		for (int i = 0; i < onlineUser.size(); i++) {
			strOnline += "|" + onlineUser.elementAt(i);
			useName = (String) onlineUser.elementAt(i);
			userList[i] = useName;
		}

		sFrame.txtNumber.setText("" + onlineUser.size());
		sFrame.lstUser.setListData(userList);
		System.out.println(strOnline);
		out.println(strOnline);
	}

	// 信息群发
	private void sendAll(String strSend) {
		Socket socketSend;
		PrintWriter outSend;
		try {
			for (int i = 0; i < socketUser.size(); i++) {
				socketSend = (Socket) socketUser.elementAt(i);
				outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())),
						true);
				outSend.println(strSend);
			}
		} catch (IOException e) {
			System.out.println("[ERROR] send all fail!");
		}
	}

	public void log(String log) {
		String newlog = sFrame.taLog.getText() + "\n" + log;
		sFrame.taLog.setText(newlog);
	}

	private String closeSocket() {
		String strUser = "";
		for (int i = 0; i < socketUser.size(); i++) {
			if (socket.equals((Socket) socketUser.elementAt(i))) {
				strUser = onlineUser.elementAt(i).toString();
				socketUser.removeElementAt(i);
				onlineUser.removeElementAt(i);
				try {
					freshClientsOnline();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sendAll("remove|" + strUser);
			}
		}
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("[ERROR] " + e);
		}

		return strUser;
	}
}