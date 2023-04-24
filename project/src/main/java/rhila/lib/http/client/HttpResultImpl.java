package rhila.lib.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import quina.http.Header;
import quina.http.HttpAnalysis;
import quina.http.HttpStatus;
import quina.net.nio.tcp.NioRecvBody;
import quina.util.Alphabet;

/**
 * HttpClient処理結果オブジェクト.
 */
class HttpResultImpl implements HttpResult {
	protected Header header = null;
	protected HttpStatus status = null;
	protected String message = null;
	protected String contentType = null;
	protected NioRecvBody recvBody = null;

	/**
	 * コンストラクタ.
	 * @param status
	 * @param message
	 * @param header
	 */
	protected HttpResultImpl(
		int status, String message, Header header) {
		this.status = HttpStatus.getHttpStatus(status);
		this.message = message;
		this.header = header;
	}

	/**
	 * クローズ処理.
	 */
	@Override
	public void close() throws IOException {
		NioRecvBody bf = recvBody;
		recvBody = null;
		if (bf != null) {
			try {
				bf.close();
			} catch(Exception e) {}
			bf = null;
		}
		header = null;
		status = null;
		message = null;
	}

	/**
	 * HTTPステータス取得.
	 *
	 * @return HttpStatus HTTPステータスが返却されます.
	 */
	@Override
	public HttpStatus getStatus() {
		return status;
	}

	/**
	 * メッセージを取得.
	 * @return String HTTPメッセージが返却サれます.
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * HTTPヘッダを取得.
	 *
	 * @param key
	 *            キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	@Override
	public Header getHeader() {
		return header;
	}

	/**
	 * NioRecvBodyを設定.
	 * @param body
	 */
	protected void setNioRecvBody(NioRecvBody body) {
		this.recvBody = body;
	}

	/**
	 * コンテンツタイプを取得.
	 *
	 * @return String コンテンツタイプが返却されます.
	 */
	@Override
	public String getContentType() {
		if (contentType == null) {
			try {
				contentType = header.get("content-type");
			} catch (Exception e) {
				contentType = null;
			}
		}
		return contentType;
	}

	/**
	 * 受信条件がGZIPか取得.
	 *
	 * @return boolean [true]の場合GZIP圧縮されています.
	 */
	@Override
	public boolean isGzip() {
		final String value = header.get("content-encoding");
		if (value != null && Alphabet.indexOf(value, "gzip") != -1) {
			return true;
		}
		return false;
	}

	/**
	 * コンテンツ長を取得.
	 *
	 * @return long コンテンツ長が返却されます.
	 */
	@Override
	public long getContentLength() {
		Long len = header.getLong("content-length");
		if(len == null) {
			len = recvBody.getLength();
		}
		return len;
	}

	/**
	 * Bodyに対する文字コードを取得.
	 * @return String 文字コードが返却されます.
	 */
	@Override
	public String getCharset() {
		return HttpAnalysis.contentTypeToCharset(getContentType());
	}

	/**
	 * レスポンスボディInputStreamを取得.
	 *
	 * @return InputStream レスポンスボディInputStreamが返却されます.
	 */
	@Override
	public InputStream getInputStream() {
		if (recvBody != null) {
			InputStream ret = null;
			try {
				// gzip圧縮されている場合.
				if (isGzip()) {
					ret = new GZIPInputStream(recvBody.getInputStream());
				} else {
					ret = recvBody.getInputStream();
				}
				recvBody = null;
				return ret;
			} catch (Exception e) {
				throw new HttpClientException(500, e);
			}
		}
		return null;
	}
}
