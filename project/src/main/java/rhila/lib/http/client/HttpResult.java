package rhila.lib.http.client;

import java.io.Closeable;
import java.io.InputStream;

import quina.http.Header;
import quina.http.HttpAnalysis;
import quina.http.HttpStatus;
import quina.json.Json;
import quina.net.nio.tcp.NioBuffer;

/**
 * HttpClient処理結果.
 */
public interface HttpResult extends Closeable {

	/**
	 * HTTPステータス取得.
	 *
	 * @return HttpStatus HTTPステータスが返却されます.
	 */
	public HttpStatus getStatus();

	/**
	 * メッセージを取得.
	 * @return String HTTPメッセージが返却サれます.
	 */
	public String getMessage();

	/**
	 * HTTPヘッダを取得.
	 *
	 * @param key
	 *            キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	public Header getHeader();

	/**
	 * 受信条件がGZIPか取得.
	 *
	 * @return boolean [true]の場合GZIP圧縮されています.
	 */
	public boolean isGzip();

	/**
	 * コンテンツタイプを取得.
	 *
	 * @return String コンテンツタイプが返却されます.
	 */
	public String getContentType();

	/**
	 * コンテンツ長を取得.
	 *
	 * @return long コンテンツ長が返却されます.
	 */
	public long getContentLength();

	/**
	 * Bodyに対する文字コードを取得.
	 * @return String 文字コードが返却されます.
	 */
	public String getCharset();

	/**
	 * レスポンスボディInputStreamを取得.
	 *
	 * @return InputStream レスポンスボディInputStreamが返却されます.
	 */
	public InputStream getInputStream();

	/**
	 * レスポンスボディバイナリを取得.
	 *
	 * @return byte[] レスポンスボディバイナリが返却されます.
	 */
	default byte[] getBody() {
		// inputStreamで受信している場合は、バイナリに展開.
		InputStream in = null;
		try {
			in = getInputStream();
			if (in != null) {
				int len;
				byte[] buf = new byte[1024];
				NioBuffer out = new NioBuffer();
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
				in.close();
				in = null;
				return out.toByteArray();
			}
		} catch (Exception e) {
			throw new HttpClientException(500, e);
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

	/**
	 * レスポンスボディを取得.
	 *
	 * @return String レスポンスボディが返却されます.
	 */
	default String getText() {
		final byte[] b = getBody();
		if (b != null) {
			try {
				String charset = HttpAnalysis.contentTypeToCharset(
					getContentType());
				return new String(b, charset);
			} catch (Exception e) {
				throw new HttpClientException(500, e);
			}
		}
		return null;
	}

	/**
	 * JSONオブジェクトを取得.
	 * @return
	 */
	default Object getJson() {
		return Json.decode(getText());
	}
}
