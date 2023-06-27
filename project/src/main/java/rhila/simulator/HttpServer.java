package rhila.simulator;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;

import rhila.RhilaException;

/**
 * シミュレーション用のHTTPサーバー.
 */
public class HttpServer {
	// serverSocket.
	private ServerSocket server;
	
	// serverSocket生成.
	private final ServerSocket createServerSocket(
		int port, int backLog, String bindAddr) {
		if(backLog <= 0) {
			backLog = 50;
		}
		ServerSocket ret = null;
		try {
			// socket作成.
			if(bindAddr != null) {
				ret = new ServerSocket(
					port, backLog, InetAddress.getByName(bindAddr));
			} else {
				ret = new ServerSocket(port, backLog);
			}
			// アドレスを再利用します.
			ret.setReuseAddress(true);
		} catch(Exception e) {
			if(ret != null) {
				try {
					ret.close();
				} catch(Exception ee) {}
			}
			throw new RhilaException(e);
		}
		return ret;
	}
	
}
