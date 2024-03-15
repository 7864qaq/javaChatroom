package Client;

import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/*
 * �ͻ������ù��߷�����
 */
public class ClientTools {

	/*
	 * JTextPane����ͼƬ
	 */
	public static void insertIcon(File file, JTextPane textArea, StyledDocument doc) {
		textArea.setCaretPosition(doc.getLength());// ���ò���λ��
		textArea.insertIcon(new ImageIcon(file.getPath())); // ����ͼƬ
		try {
			doc.insertString(doc.getLength(), "\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * ������Ա�б�
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