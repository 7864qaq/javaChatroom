package Client;

import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/*
 * 客户端所用工具方法集
 */
public class ClientTools {

	/*
	 * JTextPane插入图片
	 */
	public static void insertIcon(File file, JTextPane textArea, StyledDocument doc) {
		textArea.setCaretPosition(doc.getLength());// 设置插入位置
		textArea.insertIcon(new ImageIcon(file.getPath())); // 插入图片
		try {
			doc.insertString(doc.getLength(), "\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 在线人员列表
	 */
	public static int position(String[] strs, String s, int num) {
		for (int i = 0; i < num; i++) {
			if (strs[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public static boolean judgeInput(String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '|' || c == '\\')
				return false;
		}
		return true;
	}

	public static boolean judgeInput(char[] text) {
		for (int i = 0; i < text.length; i++)
			if (text[i] == '|' || text[i] == '\\')
				return false;
		return true;
	}
}