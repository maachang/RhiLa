package rhila.simulator;

import java.io.InputStream;
import java.net.Socket;

import rhila.RhilaException;
import rhila.lib.ByteArrayBuffer;
import rhila.lib.http.HttpReceiveChunked;
import rhila.lib.http.HttpRequest;
import rhila.lib.http.HttpResponse;

/**
 * [Thread実行]1つのHTTPサーバー処理を実行.
 */
final class HttpServerCall implements Runnable {
	private Socket socket = null;
	
	// コンストラクタ.
	public HttpServerCall(Socket socket) {
		this.socket = socket;
		setSocketOption(socket);
	}
	
	// Socket定義.
	private static final int LINGER = 0;
	private static final int SENDBUF = 65535;
	private static final int RECVBUF = 65535;
	private static final boolean TCP_NODELAY = false;
	private static final boolean KEEP_ALIVE = false;

	/** Httpソケットオプションをセット. **/
	private static final void setSocketOption(Socket soc) {
		try {
			soc.setReuseAddress(true);
			soc.setSoLinger(true, LINGER);
			soc.setSendBufferSize(SENDBUF);
			soc.setReceiveBufferSize(RECVBUF);
			soc.setKeepAlive(KEEP_ALIVE);
			soc.setTcpNoDelay(TCP_NODELAY);
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}

	
	@Override
	public void run() {
		// HttpRequestを受信する.
		HttpRequest req = readHttpRequest();
		
		
	}
	
	// socketのクローズ.
	private final void closeSocket() {
		try {
			socket.close();
		} catch(Exception e) {
		}
		socket = null;
	}
	
	// ReceiveBufferバケット長.
	private static final int RECEIVE_BUCKET_LENGTH = 1024;
	
	// HttpRequest情報を取得.
	private final HttpRequest readHttpRequest() {
		ByteArrayBuffer buf = new ByteArrayBuffer(RECEIVE_BUCKET_LENGTH);
		InputStream in = null;
		try {
			in = socket.getInputStream();
			// HttpHeaderを取得.
			String headerString = getRequestHeader(in, buf);
			// HttpRequestのヘッダを解析.
			HttpRequest req = getHttpRequest(headerString);
			headerString = null;
			// body情報が存在する場合はセットする.
			getHttpRequestBody(req, buf, in);
			buf = null;
			in.close();
			in = null;
			return req;
		} catch(RhilaException re) {
			closeSocket();
			throw re;
		} catch(Exception e) {
			closeSocket();
			throw new RhilaException(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {}
			}
		}
	}

	// ヘッダ終端.
	private static final byte[] END_HEADER = ("\r\n\r\n").getBytes();
	
	// HttpHeaderを取得.
	private final String getRequestHeader(InputStream in, ByteArrayBuffer buf)
		throws Exception {
		int len, off, heof;
		byte[] b = new byte[RECEIVE_BUCKET_LENGTH];
		off = 1;
		heof = -1;
		while((len = in.read(b)) == -1) {
			buf.write(b, 0, len);
			// ヘッダ終端が存在する場合.
			if((heof = buf.indexOf(END_HEADER, off - 1)) != -1) {
				break;
			}
			// 次のオフセット値をセット.
			off = buf.size();
		}
		// ヘッダのみの場合.
		if(heof == -1) {
			heof = buf.size();
		}
		b = new byte[heof];
		buf.read(b);
		return new String(b, 0, b.length - 4, "UTF8");
	}
	
	// requestヘッダを解析する.
	private final HttpRequest getHttpRequest(String header) {
		// {method} {url} HTTP/{version}{version}\r\n
		// {key}: {value}\r\n
		// ・・・・
		boolean next = true;
		int p, off;
		String v;
		off = 0;
		HttpRequest ret = null;
		while(next) {
			if((p = header.indexOf("\r\n", off)) == -1) {
				next = false;
				p = header.length();
			}
			v = header.substring(off, p);
			if(ret == null) {
				ret = new HttpRequest();
				// httpRequestのMethod,URL,Versionを取得.
				getRequestMethodUrlVersion(ret, v);
			} else {
				// httpHeaderをセット.
				getRequestOneHeader(ret, v);
			}
			off = p += 2;
		}
		return ret;
	}
	
	// httpRequestのMethod,URL,Versionを取得.
	private static final void getRequestMethodUrlVersion(
		HttpRequest req, String v) {
		// スペース区切り {method} {url} HTTP/{version}
		String[] list = v.split(" ");
		req.setMethod(list[0].toUpperCase());
		req.setURL(list[1]);
		req.setHttpVersion(list[2]);
	}
	
	// 1つのHttpHeaderを取得.
	private static final void getRequestOneHeader(
		HttpRequest req, String v) {
		int p = v.indexOf(":");
		if(p == -1) {
			return;
		}
		String key = v.substring(0, p).trim().toLowerCase();
		String value = v.substring(p + 1).trim();
		if("cookie".equals(key)) {
			req.getHeader().setCookie(false, value);
		} else {
			req.getHeader().setHeader(key, value);
		}
	}
	
	// HttpRequestのBody情報が存在する場合はセットする.
	private static final void getHttpRequestBody(
		HttpRequest req, ByteArrayBuffer buf, InputStream in)
		throws Exception {
		int len = (int)req.getHeader().getContentLength();
		// content-lengthが設定されている.
		if(len != -1) {
			readBodyToContentLength(req, buf, in, len);
		}
		// Transfer-Encoding: chunkedが存在する場合.
		else if("chunked".equals(
			req.getHeader().getHeader("transfer-encoding"))) {
			HttpReceiveChunked chunked =
				new HttpReceiveChunked(buf);
			readBodyToChunked(req, buf, in, chunked);
		// データが存在する限り取得.
		} else {
			readBodyToEof(req, buf, in);
		}
	}
	
	// content-lengthでBody情報を取得.
	private static final void readBodyToContentLength(
		HttpRequest req, ByteArrayBuffer buf, InputStream in,
		int contentLength)
		throws Exception {
		int len;
		byte[] b = new byte[RECEIVE_BUCKET_LENGTH];
		contentLength -= buf.size();
		while(contentLength > 0) {
			len = in.read(
				b, 0, contentLength > RECEIVE_BUCKET_LENGTH ?
					RECEIVE_BUCKET_LENGTH : contentLength);
			buf.write(b, 0, len);
			contentLength -= len;
		}
		req.setBody(buf.toByteArray());
	}
	
	// chunkedでBody情報を取得.
	private static final void readBodyToChunked(
		HttpRequest req, ByteArrayBuffer buf, InputStream in,
		HttpReceiveChunked chunked)
		throws Exception {
		int len;
		byte[] b = new byte[RECEIVE_BUCKET_LENGTH];
		while(!chunked.isEof()) {
			len = in.read(b);
			chunked.write(b, 0, len);
		}
		req.setBody(buf.toByteArray());
	}
	
	// eofまでBody情報を取得.
	private static final void readBodyToEof(
		HttpRequest req, ByteArrayBuffer buf, InputStream in)
		throws Exception {
		int len;
		byte[] b = new byte[RECEIVE_BUCKET_LENGTH];
		while((len = in.read()) != -1) {
			buf.write(b, 0, len);
		}
		req.setBody(buf.toByteArray());
	}
	
	private final void writeResponse(HttpResponse res)
		throws Exception {
		
		
	}
	
	
	
	
	

}
