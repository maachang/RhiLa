package rhila.lib.http.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import rhila.RhilaException;
import rhila.lib.ByteArrayBuffer;

/**
 * RhilaSocketFactory.
 */
public final class RhilaSocketFactory {
    // lambda snapStart CRaC用.
	public static final RhilaSocketFactory LOAD_CRAC =
		new RhilaSocketFactory();

	// cacertsファイルのパスワード.
	private static final char[] TRUST_PASS = "changeit".toCharArray();
	
	// factory生成フラグ.
	private static volatile boolean sslSocketFactoryFlag = false;
	
	// sslSocketFactory実態.
	private static SocketFactory sslSocketFactory = null;
	
	// InputStreamからBinaryを取得.
	private static final byte[] convertInputStreamToBinary(InputStream in)
		throws IOException {
		int len;
		byte[] b = new byte[1024];
		ByteArrayBuffer buf = new ByteArrayBuffer();
		while((len = in.read(b)) >= 0) {
			buf.write(b, 0, len);
		}
		return buf.toByteArray();
	}
	
	// JAVA_HOME以下のcacertsを取得.
	private static final byte[] getJavaHomeCacerts() {
		InputStream in = null;
		try {
			String javaHome = System.getenv("JAVA_HOME");
			// JAVA_HOME環境変数が存在する場合.
			if (javaHome != null && javaHome.length() != 0) {
				String fs = System.getProperty("file.separator");
				String changeitFile = new StringBuilder(javaHome)
						.append(fs).append("lib").append(fs)
						.append("security").append(fs).append("cacerts")
						.toString();
				in = new BufferedInputStream(new FileInputStream(changeitFile));
				return convertInputStreamToBinary(in);
			}
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
	
	// jar内のcacerts情報を取得.
	private static final byte[] getJarCacerts() {
		InputStream in = null;
		try {
			in = RhilaSocketFactory.class.getResourceAsStream("cacerts");
			return convertInputStreamToBinary(in);
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
	
	// HTTPS用のSocketFactoryを取得.
	private static final SSLSocketFactory createSSLSocketFactory(byte[] cacerts) {
		try {
			KeyStore t = KeyStore.getInstance("JKS");
			t.load(new ByteArrayInputStream(cacerts), TRUST_PASS);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(t);
			t = null;

			// SSL用Contextを生成する。
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			return context.getSocketFactory();
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// SSLSocketFactoryが初期化されたか取得.
	protected static final boolean isSSLSocketFactory() {
		return sslSocketFactoryFlag;
	}

	// SSLSocketFactoryを取得.
	protected static final SocketFactory getSSLSocketFactory() {
		try {
			if (!sslSocketFactoryFlag) {
				// jar内のcacertsを取得.
				// このオブジェクトと同じclassPath内のcacertsを取得.
				byte[] b = getJarCacerts();
				if(b == null) {
					// JAVA_HOMEのcacertsを取得.
					b = getJavaHomeCacerts();
				}
				if(b == null) {
					// cacertsが存在しない場合.
					throw new RhilaException("Failed to get cacerts.");
				}
				// SSLSocketFactoryを生成.
				sslSocketFactory = createSSLSocketFactory(b);
				sslSocketFactoryFlag = true;
			}
			return sslSocketFactory;
		} catch(RhilaException re) {
			throw re;
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}

	/** Socket基本オプション. **/
	private static final int LINGER = 0;
	private static final int SENDBUF = 8192;
	private static final int RECVBUF = 65535;
	private static final boolean TCP_NODELAY = false;
	private static final boolean KEEP_ALIVE = false;

	/** Httpソケットオプションをセット. **/
	private static final void setSocketOption(
		Socket soc, int timeout) throws Exception {
		soc.setReuseAddress(true);
		soc.setSoLinger(true, LINGER);
		soc.setSendBufferSize(SENDBUF);
		soc.setReceiveBufferSize(RECVBUF);
		soc.setKeepAlive(KEEP_ALIVE);
		soc.setTcpNoDelay(TCP_NODELAY);
		soc.setSoTimeout(timeout);
	}

	/** SSLSocket生成. **/
	private static final Socket createSSLSocket(
		String addr, int port, int timeout) {
		SSLSocket ret = null;
		try {
			SSLSocketFactory factory = (SSLSocketFactory)
				getSSLSocketFactory();
			ret = (SSLSocket) factory.createSocket();
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), timeout);
			ret.startHandshake();
		} catch (Exception e) {
			if (ret != null) {
				try {
					ret.close();
				} catch (Exception ee) {
				}
			}
			if(e instanceof RhilaException) {
				throw (RhilaException)e;
			}
			throw new RhilaException(e);
		}
		return ret;
	}

	/** Socket生成. **/
	private static final Socket createSocket(
		String addr, int port, int timeout) {
		Socket ret = new Socket();
		try {
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), timeout);
		} catch (Exception e) {
			try {
				ret.close();
			} catch (Exception ee) {
			}
			if(e instanceof RhilaException) {
				throw (RhilaException)e;
			}
			throw new RhilaException(e);
		}
		return ret;
	}
	
	/**
	 * Socket作成.
	 *
	 * @param ssl
	 *            [true]の場合、SSLで接続します.
	 * @param addr
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            対象のポート番号を設定します.
	 * @param timeout
	 *            通信タイムアウト値を設定します.
	 */
	public static final Socket create(
		boolean ssl, String addr, int port, int timeout) {
		try {
			if (ssl) {
				return createSSLSocket(addr, port, timeout);
			} else {
				return createSocket(addr, port, timeout);
			}
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// コンストラクタ.
	protected RhilaSocketFactory() {}
	
	// static呼び出し.
	static {
		// tls.clientプロトコルの設定.
		System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
		getSSLSocketFactory();
	}
}
