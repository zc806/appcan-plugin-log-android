package org.zywx.wbpalmstar.plugin.uexlog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import android.content.Context;
import android.widget.Toast;

public class EUExLog extends EUExBase {

	public static final String tag = "uexLog_";

	public static final String F_FILE_NAME = "widgetOne.log";

	private static final int logServerPort = 30050;

	private String m_logServerIp;

	private DatagramSocket m_udp;

	private Context m_context;

	public EUExLog(Context context, EBrowserView inParent) {
		super(context, inParent);
		m_context = context;
	}

	public void writeLog(String[] parm) {
		String inLog = parm[0];
		String widgetPath = null;
		if (mBrwView.getCurrentWidget() != null) {
			widgetPath = mBrwView.getCurrentWidget().m_widgetPath;
		}
		if (widgetPath != null) {
			File file = new File(widgetPath + F_FILE_NAME);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			BufferedWriter bWriter = null;
			try {
				bWriter = new BufferedWriter(new FileWriter(file, true));
				bWriter.write(inLog + "\n");
				bWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bWriter != null) {
					try {
						bWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				bWriter = null;
			}
		}
	}

	private void createUDP() {
		try {
			if (m_udp == null) {
				m_udp = new DatagramSocket();
				m_udp.setBroadcast(true);
				m_logServerIp = mBrwView.getCurrentWidget().m_logServerIp;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void sendLog(String[] parm) {
		String inLog = parm[0];
		createUDP();
		if (m_udp == null || inLog == null || inLog.length() == 0) {
			return;
		}

		byte[] data = inLog.getBytes();
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(m_logServerIp);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length,
					inetAddress, logServerPort);
			m_udp.send(sendPacket);
		} catch (UnknownHostException e) {
			closeUDP();
			e.printStackTrace();
		} catch (IOException e) {
			closeUDP();
			e.printStackTrace();
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"plugin_log_error_no_permisson_INTERNET"),
					Toast.LENGTH_SHORT).show();
		}

	}

	private void closeUDP() {
		if (m_udp != null) {
			m_udp.close();
			m_udp = null;
		}
	}

	@Override
	protected boolean clean() {
		closeUDP();
		return true;
	}
}