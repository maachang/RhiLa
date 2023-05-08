package rhila.lib.http.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import rhila.RhilaException;
import rhila.lib.ByteArrayBuffer;
import rhila.lib.NumberUtil;
import rhila.lib.ObjectUtil;
import rhila.lib.http.HttpCookieValue;
import rhila.lib.http.HttpStatus;
import rhila.scriptable.BinaryScriptable;

/**
 * [同期]HttpClient.
 */
public final class HttpClient {
	
    // lambda snapStart CRaC用.
    protected static final HttpCookieValue LOAD_CRAC = new HttpCookieValue();

	//
	// [memo]
	//
	// HTTPClientでは、以下のようにclient用のTLSバージョンを指定しないと
	// 接続を拒否されるものもあるようです.
	//
	// -Djdk.tls.client.protocols=TLSv1.2
	//              or
	// System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
	//

	protected HttpClient() {
	}
	
	// Maxリトライ(15回).
	private static int MAX_RETRY = 15;
	
	// Socketタイムアウト値(30秒).
	private static int TIMEOUT = 30000;
	
	// HttpClient.
	public static final HttpResult request(
		String host, String path, Map<String, Object> options) {
		String accessUrl;
		HttpStatus state;
		String location;
		int cnt = 0;
		HttpResult ret = null;
		if(options == null) {
			options = new HashMap<String, Object>();
		}
		String url = createURL(host, path);
		while (true) {
			// MethodがGETで、FormDataが存在する場合はGETのパラメータ設定.
			accessUrl = appendUrlParams(url, options);
			// Httpアクセス.
			ret = accessHttp(accessUrl, options);
			// 処理結果のステータスを取得.
			state = ret.getStatus();
			// リダイレクト要求の場合.
			if(HttpStatus.MovedPermanently == state ||
				HttpStatus.MovedTemporarily == state ||
				HttpStatus.SeeOther == state ||
				HttpStatus.TemporaryRedirect == state ||
				HttpStatus.PermanentRedirect == state) {
				// リダイレクトが許可されていない場合.
				if(RedirectMode.error == option.getRedirect()) {
					throw new HttpClientException(
						"Redirection is not allowed.");
				}
				// locationを取得.
				location = ret.getHeader().get("location");
				// locationが存在しない場合.
				if(location == null || location.isEmpty()) {
					throw new HttpClientException(
						"Detects rogue redirects.");
				}
				// リダイレクト先のURLがフルパスじゃない場合.
				if(!location.startsWith("http://") &&
					!location.startsWith("https://")) {
					location = margeUrl(accessUrl, location);
				}
				// GETでリダイレクト必須のステータスの場合.
				if(HttpStatus.MovedPermanently == state ||
					HttpStatus.MovedTemporarily == state ||
					HttpStatus.SeeOther == state) {
					// GETメソッドでない場合はGETメソッドに変更する.
					if(Method.GET != option.getMethod()) {
						option.cancelFix();
						option.setMethod(Method.GET);
						option.fix();
					}
				}
				// リダイレクト先のURL設定.
				url = location;
				// 規定回数を超えるリダイレクトの場合.
				if (cnt ++ > MAX_RETRY) {
					throw new HttpClientException(
						"Retry limit exceeded.");
				}
				continue;
			}
			// 処理終了.
			break;
		}
		return ret;
	}

	// 接続処理.
	private static final HttpResult accessHttp(
		String url, Map<String, Object> options) {
		Socket socket = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			// URL解析.
			String[] urlArray = parseUrl(url);

			// リクエスト送信.
			socket = createSocket(urlArray);
			out = new BufferedOutputStream(socket.getOutputStream());

			// リクエスト送信.
			createRequest(urlArray, out, options);

			// レスポンス受信.
			in = new BufferedInputStream(socket.getInputStream());
			HttpResult ret = receiveHttp(url, in, options);

			out.close();
			out = null;
			in.close();
			in = null;
			socket.close();
			socket = null;
			return ret;
		} catch(Exception e) {
			// エラー発生の場合はbodyをクローズ.
			if (option != null) {
				if(option.getBody() != null) {
					try {
						option.getBody().close();
					} catch(Exception ee) {
					}
				}
			}
			// Internalエラー返却.
			throw new HttpClientException(500, e);
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
		
		return RhilaSocketFactory.create(
			"https".equals(urlArray[0]), urlArray[1],
			NumberUtil.parseInt(urlArray[2]),
			TIMEOUT);
	}

	// 文字コードを取得.
	private static final String getOptionCharset(HttpClientOption option) {
		String charset = option.getCharset();
		if(charset == null) {
			charset = HttpConstants.getCharset();
		}
		return charset;
	}

	// HTTPリクエストを作成.
	private static final void createRequest(
		String[] urlArray, OutputStream out, Map<String, Object> options)
		throws IOException {
		Header header = option.getHeaders();
		Method method = option.getMethod();
		String url = urlArray[3];
		String charset = getOptionCharset(option);
		// 先頭条件を設定.
		StringBuilder buf = new StringBuilder();
		buf.append(method).append(" ")
			.append(url);
		// GETでフォームデータが存在する場合.
		if(Method.GET == method && option.isFormData()) {
			// パラメーターとしてセット.
			if(url.indexOf("?") == -1) {
				buf.append("?");
			} else {
				buf.append("&");
			}
			buf.append(option.getFormData());
		}
		buf.append(" HTTP/1.1\r\n");
		// 基本ヘッダ生成.
		buf.append("Host:").append(urlArray[1]);
		if (("http".equals(urlArray[0]) && !"80".equals(urlArray[2]))
				|| ("https".equals(urlArray[0]) && !"443".equals(urlArray[2]))) {
			buf.append(":").append(urlArray[2]);
		}
		buf.append("\r\n");
		if (header == null || !header.containsKey("User-Agent")) {
			buf.append("User-Agent:").append(HttpConstants.getServerName())
				.append("\r\n");
		}
		buf.append("Accept-Encoding:gzip,deflate\r\n");
		buf.append("Connection:close\r\n");
		// ユーザ定義ヘッダを設定.
		int hlen;
		if (header != null && (hlen = header.size()) > 0) {
			boolean contentType = false;
			String k, v;
			for(int i = 0; i < hlen; i ++) {
				k = header.getKey(i);
				v = header.getValue(i);
				// 登録できない内容を削除.
				if (k == null ||
					Alphabet.eqArray(k, "host", "connection") != -1
				) {
					continue;
				// コンテンツタイプが設定されている場合.
				} else if(Alphabet.eq(k, "content-type")) {
					contentType = true;
				}
				// ユーザー定義のヘッダを出力.
				buf.append(k).append(":").append(v).append("\r\n");
			}
			// コンテンツタイプが設定されてなくて、Body情報がファイル送信の場合.
			if(!contentType && option.getBody() != null &&
				option.getBody() instanceof NioSendFileData) {
				// 拡張子からmimeTypeを取得する.
				String name = ((NioSendFileData)option.getBody())
					.getFileName();
				String mime = MimeTypes.getInstance()
					.getFileNameToMimeType(name);
				if(mime != null) {
					buf.append("Content-Type:").append(mime);
				}
				buf.append("\r\n");
			}
		}
		// ヘッダ終端.
		buf.append("\r\n");
		// binary 変換.
		String s = buf.toString();
		buf = null;
		try {
			// ヘッダ出力.
			out.write(s.getBytes(charset));
			s = null;
			// body情報が存在する場合.
			NioSendData body = option.getBody();
			if(body != null) {
				try {
					// body送信.
					int len;
					final byte[] b = new byte[1024];
					final ByteBuffer bbuf = ByteBuffer.wrap(b);
					// ByteBufferでWrapして取得.
					while((len = body.read(bbuf)) != -1) {
						out.write(b, 0, len);
					}
					body.close();
					body = null;
				} finally {
					if(body != null) {
						try {
							body.close();
						} catch(Exception e) {}
					}
				}
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

	// データ受信.
	@SuppressWarnings("resource")
	private static final HttpResult receiveHttp(
		String url, InputStream in, HttpClientOption option)
		throws IOException {
		int len, p;
		boolean memFlg = true;
		final long maxBodyLength = HttpConstants
			.getMaxRecvMemoryBodyLength();
		final int binaryLength = NioConstants.getBufferSize();
		final byte[] binary = new byte[binaryLength];
		ByteArrayBuffer recvBuf = new ByteArrayBuffer(NioConstants.getBufferSize());
		HttpReceiveChunked recvChunked = null;
		long contentLength = 0L;
		byte[] b = null;
		int status = -1;
		String message = "";
		HttpResultImpl result = null;
		NioRecvBody body = null;
		try {
			while ((len = in.read(binary)) != -1) {
				// データ生成が行われていない場合.
				if (result == null) {
					// 受信ヘッダバッファに出力.
					recvBuf.write(binary, 0, len);
					// ステータス取得が行われていない.
					if (status == -1) {
						// ステータスが格納されている１行目の情報を取得.
						if ((p = recvBuf.indexOf(CFLF)) != -1) {
							// HTTP/1.1 {Status} {MESSAGE}\r\n
							int pp, ppp;
							b = binaryLength > p + 2 ?
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
					// ヘッダ終端が存在.
					if ((p = recvBuf.indexOf(END_HEADER)) != -1) {
						b = new byte[p + 2];
						recvBuf.read(b);
						recvBuf.skip(2);
						Header header = new HttpReceiveHeader(b);
						b = null;
						result = new HttpResultImpl(status, message, header);
						// content-length.
						String value = header.get("content-length");
						if (NumberUtil.isNumeric(value)) {
							contentLength = NumberUtil.parseLong(value);
						}
						// chunked.
						else {
							value = header.get("transfer-encoding");
							if (Alphabet.eq("chunked", value)) {
								contentLength = -1L;
							}
						}
						// ContentLengthが存在する場合.
						if(contentLength > 0L) {
							// 一定以上のデータ長の場合は、一時ファイルで受け取る.
							if(contentLength > maxBodyLength) {
								body = new NioRecvFileBody();
								// 受信Bodyに現在の残りデータを設定.
								body.write(recvBuf.toByteArray());
								recvBuf.close();
							} else {
								body = new NioRecvMemBody(recvBuf);
							}
							recvBuf = null;
							continue;
						// チャンク受信の場合.
						} else if(contentLength == -1) {
							recvChunked = new HttpReceiveChunked(recvBuf);
							body = new NioRecvMemBody();
							// 今回分のデータ読み込み.
							while((len = recvChunked.read(binary)) > 0) {
								body.write(binary, 0, len);
								// データ長が一定以上を超えた場合.
								if(memFlg && body.getLength() > maxBodyLength) {
									// 一時ファイル処理に切り替える.
									NioRecvFileBody d = new NioRecvFileBody();
									d.write(binary, (NioRecvMemBody)body);
									body.close();
									body = d;
									memFlg = false;
								}
							}
							recvBuf = null;
							continue;
						}
					}
				}
				// チャンク受信の場合.
				if(body != null) {
					if(recvChunked != null) {
						recvChunked.write(binary, 0, len);
						// 今回分のデータ読み込み.
						while((len = recvChunked.read(binary)) > 0) {
							body.write(binary, 0, len);
							// データ長が一定以上を超えた場合.
							if(memFlg &&
								body.getLength() > maxBodyLength) {
								// 一時ファイル処理に切り替える.
								NioRecvFileBody d = new NioRecvFileBody();
								d.write(binary, (NioRecvMemBody)body);
								body.close();
								body = d;
								memFlg = false;
							}
						}
					// 通常受信の場合.
					} else {
						body.write(binary, 0, len);
					}
				}
			}
			// resultが取得できてない場合.
			if(result == null) {
				throw new HttpClientException("The connection has been lost.");
			// resultが存在する場合、recvBodyをセット.
			} else if(body == null || body.getLength() == 0L) {
				return result;
			}
			result.setNioRecvBody(body);
			return result;
		} finally {
			if (recvBuf != null) {
				try {
					recvBuf.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/*
	public static final void main(String[] args) throws Exception {
		//System.setProperty("javax.net.debug", "all");
		//-Dhttps.protocols=TLSv1.2
		//-Djdk.tls.client.protocols=TLSv1.2
		System.setProperty("https.protocols", "TLSv1.2");
		System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
		String url;
		url = "http://127.0.0.1:3333/";
		//url = "https://google.com/";
		//url = "https://yahoo.co.jp/";
		//url = "https://ja.javascript.info/fetch-api";
		//url = "http://www.asyura2.com";
		int loopLen = 1;
		HttpResult res = null;
		byte[] bin = null;
		// １回目はSSL関連の初期化があるのではじめに一度だけ実行する.
		res = HttpClient.get(url, null);
		res.close();
		System.out.println("start");
		long time = System.currentTimeMillis();
		for(int i = 0; i < loopLen; i ++) {
			res = HttpClient.get(url, null);
			bin = res.getBody();
			System.out.println("bin: " + bin.length);
		}
		System.out.println("time: " + ((System.currentTimeMillis() - time) / loopLen) + " msec");
		System.out.println("gzip: " + res.isGzip());
		//System.out.println(res.getHeader());
		//System.out.println(new String(bin, res.getCharset()));
	}
	*/
}
