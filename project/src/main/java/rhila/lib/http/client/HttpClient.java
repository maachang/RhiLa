package rhila.lib.http.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import rhila.RhilaConstants;
import rhila.RhilaException;
import rhila.lib.BooleanUtil;
import rhila.lib.ByteArrayBuffer;
import rhila.lib.NumberUtil;
import rhila.lib.ObjectUtil;
import rhila.lib.http.HttpHeader;
import rhila.lib.http.HttpReceiveChunked;
import rhila.lib.http.HttpRequest;
import rhila.lib.http.HttpResponse;
import rhila.lib.http.HttpUtil;
import rhila.lib.http.MimeType;
import rhila.scriptable.AbstractRhinoCustomStatic;
import rhila.scriptable.BinaryScriptable;

/**
 * [同期]HttpClient.
 */
@SuppressWarnings("rawtypes")
public final class HttpClient extends AbstractRhinoCustomStatic {
	
    // lambda snapStart CRaC用.
    protected static final HttpClient LOAD_CRAC = new HttpClient();

	//
	// [memo]
	//
	// HTTPClientでは、以下のようにclient用のTLSバージョンを指定しないと
	// 接続を拒否されるものもあるようです.
    // ※RhilaSocketFactoryで設定してるので不要.
	//
	// -Djdk.tls.client.protocols=TLSv1.2
	//              or
	// System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
	//
    
	
	/*
	// testMain.
	public static final void main(String[] args) throws Exception {
		// オブジェクトの初期化.
		Global.getInstance();
		//System.setProperty("javax.net.debug", "all");
		//-Dhttps.protocols=TLSv1.2
		//-Djdk.tls.client.protocols=TLSv1.2
		//System.setProperty("https.protocols", "TLSv1.2");
		//System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
		String url;
		//url = "http://127.0.0.1:3333/";
		//url = "https://google.com/";
		//url = "https://www.google.com/";
		url = "https://raw.githubusercontent.com/maachang/testLFU/main/public/index.html";
		//url = "https://yahoo.co.jp/";
		//url = "https://ja.javascript.info/fetch-api";
		//url = "http://www.asyura2.com";
		// !!githubusercontentのアクセス
		// time: 125 msec(10回実行平均)
		// time: 300 msec(1回実行)
		int loopLen = 1;
		HttpRequest request = new HttpRequest();
		HttpResponse response = new HttpResponse();
		// １回目はSSL関連の初期化があるのではじめに一度だけ実行する.
		request.setURL(url);
		System.out.println("start");
		long time = System.currentTimeMillis();
		for(int i = 0; i < loopLen; i ++) {
			HttpClient.request(request, response);
		}
		System.out.println("time: " + ((System.currentTimeMillis() - time) / loopLen) + " msec");
		System.out.println("length: " + response.getHeader().getContentLength());
		System.out.println("bodyLen: " + response.getBody().size());
		System.out.println("gzip: " + response.getHeader().isGzip());
		//System.out.println(response.getHeader());
		System.out.println(response.getBody().isGzip());
	}
	*/

	protected HttpClient() {}
	
	// Maxリトライ(15回).
	private static int MAX_RETRY = 15;
	
	// Socketタイムアウト値(30秒).
	private static int TIMEOUT = 30000;
	
	// 最大レスポンス受信(6MByte).
	// aws lambdaの最大返却値が6mbyte.
	private static int MAX_RESPONSE_BODY = 0x0100000 * 6;
	
	// argsにbase64条件が存在するかチェック.
	private static final boolean isBase64Encoded(Map args) {
		Object value = args.get("isBase64Encoded");
		if(ObjectUtil.isNull(value)) {
			return false;
		}
		return BooleanUtil.parseBoolean(value);
	}
	
	// ContentTypeの条件を取得.
	private static final String getContentType(HttpHeader header) {
		String contentType = header.getContentType();
		if(ObjectUtil.isNull(contentType)) {
			return null;
		}
		int p = contentType.indexOf(";");
		if(p == -1) {
			return contentType.toLowerCase().trim();
		} else {
			return contentType.substring(0, p).trim().toLowerCase();
		}
	}
	
	// contentTypeが設定されていない場合設定する.
	private static final void setDefaultContentType(
		HttpRequest req, String contentType, String charset) {
		if(ObjectUtil.isNull(req.getHeader().getContentType())) {
			if(charset != null) {
				req.getHeader().setContentType(contentType, charset);
			} else {
				req.getHeader().setContentType(contentType);
			}
		}
	}
	
	// argsからHttpRequestを生成.
	private static final HttpRequest _request(Object args) {
		// argsがHttpRequestの場合.
		if(args instanceof HttpRequest) {
			return (HttpRequest)args;
		}
		// 新しいHttpHeaderを生成.
		HttpRequest ret = new HttpRequest();
		// argsがMapの場合.
		if(args instanceof Map) {
			Object o;
			Map params = (Map)args;
			// headerが存在する場合.
			if(!ObjectUtil.isNull(o = params.get("headers"))) {
				HttpHeader rh = ret.getHeader();
				// ヘッダ設定がMapの場合.
				if(o instanceof Map) {
					rh.setHeaders((Map)o);
				// headerがHttpHeaderの場合.
				} else if(o instanceof HttpHeader) {
					rh.setHttpHeader((HttpHeader)o);
				}
			}
			// cookiesが存在する場合.
			if(!ObjectUtil.isNull(o = params.get("cookies"))) {
				HttpHeader rh = ret.getHeader();
				// ヘッダ設定がMapの場合.
				if(o instanceof Map) {
					rh.setCookies((Map)o);
				}
			}
			// queryStringおよびparamsが存在する場合.
			if(!ObjectUtil.isNull(o = params.get("queryString")) ||
				!ObjectUtil.isNull(o = params.get("query")) ||
				!ObjectUtil.isNull(o = params.get("params"))) {
				if(o instanceof String) {
					ret.setQueryString((String)o);
				} else if(o instanceof Map) {
					ret.setQueryStringToMap((Map)o);
				}
			}
		}
		return ret;
	}
	
	// bodyをセット.
	private static final void _body(HttpRequest req, Object args) {
		// argsがMapの場合.
		if(args instanceof Map) {
			Object o;
			Map params = (Map)args;
			// bodyが存在する場合.
			if(!ObjectUtil.isNull(o = params.get("body"))) {
				// binaryの場合.
				if(o instanceof byte[]) {
					req.setBody((byte[])o);
				// binaryの場合.
				} else if(o instanceof BinaryScriptable) {
					req.setBody((BinaryScriptable)o);
				// 文字列の場合.
				} else if(o instanceof String) {
					// base64設定条件を取得.
					boolean base64Body = isBase64Encoded(params);
					if(base64Body) {
						// base64の場合.
						req.setBodyToBase64((String)o);
					} else {
						// bodyをセット.
						req.setBody((String)o,
							req.getHeader().getContentType());
					}
				// mapの場合.
				} else if(o instanceof Map) {
					// bodyのContentTypeを取得.
					String contentType = getContentType(
						req.getHeader());
					// contentTypeが設定されている場合.
					if(contentType != null) {
						// formデータ.
						if(contentType.startsWith(MimeType.FORM_DATA)) {
							req.setBodyToForm(o);
						// jsonデータ.
						} else if(contentType.startsWith(MimeType.JSON)) {
							req.setBodyToJSON(o);
						}
					}
				}
			}
		}
	}

	// [GET]URLを指定してHttpClient実行.
	public static final HttpResponse requestGet(String url) {
		return requestGet(url, null);
	}
	
	// [GET]URLを指定してHttpClient実行.
	public static final HttpResponse requestGet(
		String url, Object options) {
		HttpRequest req = _request(options);
		req.setMethod("GET");
		req.setURL(url);
		return request(req);
	}
	
	// [POST]URLを指定してHttpClient実行.
	public static final HttpResponse requestPost(
		String url, Object options) {
		HttpRequest req = _request(options);
		req.setMethod("POST");
		req.setURL(url);
		// form条件が存在しない場合はセット.
		setDefaultContentType(req, MimeType.FORM_DATA, null);
		_body(req, options);
		return request(req);
	}
	
	// [JSON]URLを指定してHttpClient実行.
	public static final HttpResponse requestJSON(
		String url, Object options) {
		HttpRequest req = _request(options);
		req.setMethod("POST");
		req.setURL(url);
		// json強制指定.
		req.getHeader().setContentType(MimeType.JSON);
		_body(req, options);
		return request(req);
	}
	
	// HttpRequestを指定してHttpClient実行.
	public static final HttpResponse request(HttpRequest request) {
		return request(request, new HttpResponse());
	}
	
	// HttpClient実行.
	public static final HttpResponse request(
		HttpRequest request, HttpResponse response) {
		int redirectCount = 0;
		String url = request.getFullURL();
		String srcURL = url;
		while (true) {
			// Http / httpsアクセス.
			sendRequest(request, response);
			// リダイレクトの場合.
			if(response.isRedirect()) {
				// 規定回数を超えるリダイレクトの場合.
				if (redirectCount ++ > MAX_RETRY) {
					throw new RhilaException(
						"Redirect limit exceeded: " + srcURL);
				}
				// リダイレクトが許可されていない場合.
				if(response.getRedirect() == null) {
					throw new RhilaException(
						"Detects rogue redirects.");
				}
				// requestのURL再設定.
				request.setQueryString(null); // queryStringは削除.
				request.setURL(response.getRedirect()); // redirectURLをセット.
				// responseをクリア.
				response.reset();
			} else {
				break;
			}
		}
		return response;
	}

	// 1つのアクセス処理.
	private static final void sendRequest(
		HttpRequest request, HttpResponse response) {
		Socket socket = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			// Socket作成用のURL解析結果を生成.
			String[] urlArray = HttpUtil.parseUrl(request.getURL());

			// リクエスト送信.
			socket = createSocket(urlArray);
			// Socket書き込みを生成.
			out = new BufferedOutputStream(socket.getOutputStream());

			// リクエスト実行.
			executeRequest(request, urlArray, out);

			// レスポンス実行.
			in = new BufferedInputStream(socket.getInputStream());
			executeReceive(request, response, in);

			out.close();
			out = null;
			in.close();
			in = null;
			socket.close();
			socket = null;
		} catch(Exception e) {
			// Internalエラー返却.
			throw new RhilaException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception ee) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception ee) {
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception ee) {
				}
			}
		}
	}


	// ソケット生成.
	private static final Socket createSocket(String[] urlArray)
		throws IOException {
		// Socket or SSLSocketを作成.
		return RhilaSocketFactory.create(
			"https".equals(urlArray[0]), urlArray[1],
			NumberUtil.parseInt(urlArray[2]),
			TIMEOUT);
	}

	// httpHeaderをセット.
	private static final void appendHeader(
		StringBuilder buf, String key, String value) {
		buf.append(key).append(":").append(value)
			.append("\r\n");
	}

	// HTTPリクエストを作成.
	private static final void executeRequest(
		HttpRequest request, String[] urlArray, OutputStream out)
		throws IOException {
		
		HttpHeader header = request.getHeader();
		String method = request.getMethod();
		
		// 先頭条件を設定.
		StringBuilder buf = new StringBuilder();
		buf.append(method).append(" ")
			.append(request.getFullURL());
		buf.append(" HTTP/1.1\r\n");
		
		// 強制的にセットするヘッダをセット.
		if (("http".equals(urlArray[0]) && !"80".equals(urlArray[2]))
				|| ("https".equals(urlArray[0]) && !"443".equals(urlArray[2]))) {
			appendHeader(buf, "host", urlArray[1] + ":" + urlArray[2]);
		} else {
			appendHeader(buf, "host", urlArray[1]);
		}
		// 矯正書き込みヘッダ系.
		appendHeader(buf, "accept", "*/*");
		appendHeader(buf, "accept-encoding", "gzip,deflate");
		appendHeader(buf, "connection", "close");
		
		// ユーザ指定がない場合セット.
		if (header == null || !header.isHeader("User-Agent")) {
			appendHeader(buf, "user-agent", RhilaConstants.SERVER_NAME);
		}
		
		// ユーザ定義ヘッダを設定.
		int hlen;
		Object[] hKeys = header != null ? header.getHeaderKeys() : null;
		if (hKeys != null && (hlen = hKeys.length) > 0) {
			String k, v;
			for(int i = 0; i < hlen; i ++) {
				k = (String)hKeys[i];
				// 登録できない内容は処理しない.
				if ("host".equals(k) || "connection".equals(k) ||
					"accept".equals(k) || "accept-encoding".equals(k) ||
					"content-length".equals(k)) {
					continue;
				}
				v = header.getHeader(k);
				// ユーザー定義のヘッダを出力.
				appendHeader(buf, k, v);
			}
		}
		
		// bodyが存在する場合.
		BinaryScriptable body = request.getBody();
		if(body != null) {
			// contentLengthをセット.
			appendHeader(buf,
				"content-length", String.valueOf(body.size()));
		}
		
		// cookie設定.
		if(header != null && header.getCookieSize() > 0) {
			header.toStringByCookie(false, buf);
		}
		
		// ヘッダ終端をセット.
		buf.append("\r\n");
			
		// header and body出力.
		try {
			// ヘッダ出力.
			out.write(buf.toString().getBytes("UTF8"));
			buf = null;
			
			// bodyが存在する場合.
			if(body != null) {
				// body書き込み.
				out.write(body.getRaw());
			}
			
			out.flush();
			out = null;
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
			}
		}
	}

	// ヘッダ区切り文字.
	private static final byte[] CFLF = ("\r\n").getBytes();

	// ヘッダ終端.
	private static final byte[] END_HEADER = ("\r\n\r\n").getBytes();
	
	// ReceiveBufferバケット長.
	private static final int RECEIVE_BUCKET_LENGTH = 1024;
	
	// データ受信.
	private static final void executeReceive(
		HttpRequest request, HttpResponse response, InputStream in)
		throws IOException {
		int len, p, pp, ppp;
		final byte[] binary = new byte[RECEIVE_BUCKET_LENGTH];
		ByteArrayBuffer recvBuf = new ByteArrayBuffer(RECEIVE_BUCKET_LENGTH);
		HttpReceiveChunked recvChunked = null;
		long contentLength = 0L;
		byte[] b = null;
		boolean loadEndHeaderFlag = false;
		int status = -1;
		String message = "";
		ByteArrayBuffer body = null;
		try {
			while ((len = in.read(binary)) != -1) {
				// ヘッダの読み込みが完了していない場合.
				if (!loadEndHeaderFlag) {
					// 受信ヘッダバッファに出力.
					recvBuf.write(binary, 0, len);
					// ステータス取得が行われていない.
					if (status == -1) {
						// ステータスが格納されている１行目の情報を取得.
						if ((p = recvBuf.indexOf(CFLF)) != -1) {
							// HTTP/1.1 {Status} {MESSAGE}\r\n
							b = RECEIVE_BUCKET_LENGTH > p + 2 ?
								binary : new byte[p + 2];
							recvBuf.read(b, 0, p + 2);
							String top = new String(b, 0, p + 2, "UTF8");
							b = null;
							pp = top.indexOf(" ");
							if(pp == -1) {
								ppp = -1;
							} else {
								ppp = top.indexOf(" ", pp + 1);
							}
							if(pp == -1|| ppp == -1) {
								status = 200;
								message = "OK";
							} else {
								status = NumberUtil.parseInt(
									top.substring(pp + 1, ppp));
								message = top.substring(ppp + 1).trim();
							}
						} else {
							continue;
						}
					}
					// ヘッダ終端を検知した場合.
					if ((p = recvBuf.indexOf(END_HEADER)) != -1) {
						b = new byte[p + 2];
						recvBuf.read(b);
						recvBuf.skip(2);
						
						// responseにヘッダ反映.
						HttpHeader header = parseResponseHeader(response.getHeader(), b);
						b = null;
						
						// responseにステータス反映.
						response.getStatus().setStatus(status, message);
						
						// ヘッダ読み込み完了.
						loadEndHeaderFlag = true;
						
						// content-lengthを取得.
						String value = header.getHeader("content-length");
						// content-lengthが存在する場合.
						if (NumberUtil.isNumeric(value)) {
							contentLength = NumberUtil.parseLong(value);
						// content-lengthが存在しない場合.
						} else {
							// chunkedでBody取得かをチェック.
							value = header.getHeader("transfer-encoding");
							if (value != null && "chunked".equals(value.toLowerCase())) {
								// chunkedで取得.
								contentLength = -1L;
							}
						}
						// ContentLengthが存在する場合.
						if(contentLength > 0L) {
							// Bodyの受信最大容量を超えてる場合、エラー返却.
							if(contentLength > MAX_RESPONSE_BODY) {
								throw new RhilaException(
									"Body length exceeds maximum capacity: " +
									contentLength + "byte");
							}
							// 受信Bodyに現在の残りデータを設定.
							body = new ByteArrayBuffer();
							if(recvBuf.size() > 0) {
								body.write(recvBuf.toByteArray());
							}
							recvBuf.close();
							recvBuf = null;
							continue;
						// チャンク受信の場合.
						} else if(contentLength == -1) {
							recvChunked = new HttpReceiveChunked(recvBuf);
							body = new ByteArrayBuffer();
							// 今回分のデータ読み込み.
							while((len = recvChunked.read(binary)) > 0) {
								body.write(binary, 0, len);
							}
							recvBuf = null;
							continue;
						}
					}
				}
				// body受信.
				if(body != null) {
					// チャンク受信の場合.
					if(recvChunked != null) {
						recvChunked.write(binary, 0, len);
						// 今回分のデータ読み込み.
						while((len = recvChunked.read(binary)) > 0) {
							body.write(binary, 0, len);
							// Bodyの受信最大容量を超えてる場合、エラー返却.
							if(body.size() > MAX_RESPONSE_BODY) {
								throw new RhilaException(
									"Body's chunked receive length exceeds maximum capacity: " +
									contentLength + "byte");
							}
						}
					// 通常受信の場合.
					} else {
						body.write(binary, 0, len);
					}
				}
			}
			// body受信ができていない場合.
			if(!loadEndHeaderFlag) {
				throw new RhilaException(
					"The connection has been lost.");
			// bodyが存在しない場合.
			} else if(body == null || body.size() == 0L) {
				// 終了.
				return;
			}
			response.setBody(body.toByteArray());
		} finally {
			if (recvBuf != null) {
				try {
					recvBuf.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	// ヘッダKeyと要素の区切り.
	private static final byte SEPARATOR_KEY_VALUE = (byte)':';
	
	// responseのhttpHeaderを解析.
	private static final HttpHeader parseResponseHeader(
		HttpHeader out, final byte[] headerBin) {
		/**
		 * バイナリの設定範囲は、以下のように行う必要があります.
		 * --------------------------------------------
		 * Location: https://www.google.com/\r\n
		 * Content-Type: text/html; charset=UTF-8\r\n
		 * Date: Mon, 12 Apr 2021 08:02:34 GMT\r\n
		 * Expires: Wed, 12 May 2021 08:02:34 GMT\r\n
		 * Cache-Control: public, max-age=2592000\r\n
		 * Server: gws\r\n
		 * Content-Length: 220\r\n
		 * Connection: close\r\n
		 * --------------------------------------------
		 * key: value[\r\n]まで設定することでヘッダ行が認識されます.
		 */
		try {
			byte b;
			int i, p, s, e;
			int len = headerBin.length - 1;
			String key = null;
			String value = null;
			for(i = 0, p = 0, s = -1, e = -1; i < len; i ++) {
				b = headerBin[i];
				// ヘッダ要素の区切り情報がある場合.
				if(b == CFLF[0]) {
					// ¥r¥nの区切り情報の場合.
					if(headerBin[i + 1] == CFLF[1]) {
						// ヘッダキーを取得.
						if(s != -1) {
							// keyを取得.
							key = new String(headerBin, s, e - s, "UTF8").trim()
								.toLowerCase();
							// valueを取得.
							value = new String(headerBin, p, i - p, "UTF8").trim();
							
							// headerがsetCookieの場合.
							if("set-cookie".equals(key)) {
								// cookie設定(set-cookie).
								out.setCookie(true, value);
							} else {
								// header設定.
								out.setHeader(key, value);
							}
						}
						i ++;
						// 次のヘッダ要素開始条件をセット.
						p = i + 1;
						s = e = -1;
						continue;
					}
				}
				// キー情報のチェック.
				if(s == -1) {
					// keyとvalueの区切りの場合.
					if(b == SEPARATOR_KEY_VALUE) {
						s = p;
						e = i;
						p = i + 1;
					}
				}
			}
			return out;
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// [js]対応メソッド.
	private static final String[] FUNCTION_NAME_LIST = new String[] {
		"get",
		"json",
		"post",
		"request",
	};

	@Override
	public String getName() {
		return "HttpClient";
	}

	@Override
	protected String[] getFunctionNames() {
		return FUNCTION_NAME_LIST;
	}

	@Override
	protected Object callFunction(int type, Object[] args) {
		switch(type) {
		case 0: // get
			return _get(args);
		case 1: // json
			return _json(args);
		case 2: // post
			return _post(args);
		case 3: // request
			return _request(args);
		}
		// プログラムの不具合以外にここに来ることは無い.
		throw new RhilaException(
			"An unspecified error (type: " + type + ") occurred");
	}
	
	// 引数がない場合はエラー.
	private static final void checkArgs(Object[] args) {
		if(args == null || args.length <= 0) {
			throw new RhilaException("argument does not exist");
		}
	}
	
	// urlチェック.
	private static final void checkURL(Object[] args) {
		if(!(args[0] instanceof String)) {
			throw new RhilaException(
				"URL is not set with character specification");
		}
	}
	
	// 第二引数が無い場合エラーとする.
	private static final void checkOption(Object[] args) {
		if(args.length == 1) {
			throw new RhilaException("option is not set.");
		}
	}
	
	protected Object _get(Object[] args) {
		checkArgs(args);
		checkURL(args);
		if(args.length >= 2) {
			return requestGet((String)args[0], args[1]);
		}
		return requestGet((String)args[0]);
	}
	
	protected Object _json(Object[] args) {
		checkArgs(args);
		checkURL(args);
		checkOption(args);
		return requestJSON((String)args[0], args[1]);
	}
	
	protected Object _post(Object[] args) {
		checkArgs(args);
		checkURL(args);
		checkOption(args);
		return requestPost((String)args[0], args[1]);
	}
	
	protected Object _request(Object[] args) {
		checkArgs(args);
		if(!(args[0] instanceof HttpRequest)) {
			throw new RhilaException(
				"First argument is not HttpRequest.");
		}
		if(args.length >= 2) {
			if(!(args[1] instanceof HttpResponse)) {
				throw new RhilaException(
					"Second argument is not HttpResponse.");
			}
			return request(
				(HttpRequest)args[0],
				(HttpResponse)args[1]);
		}
		return request((HttpRequest)args[0]);
	}
}
