package rhila.lib.http.client;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import quina.http.Header;
import quina.http.HttpCharset;
import quina.http.HttpConstants;
import quina.http.HttpSendChunkedData;
import quina.http.HttpSendHeader;
import quina.http.MediaType;
import quina.http.Method;
import quina.json.Json;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioSendFileData;
import quina.net.nio.tcp.NioSendInputStreamData;
import quina.net.nio.tcp.NioSendMemData;
import quina.util.Alphabet;
import quina.util.StringUtil;

/**
 * HttpClientOption.
 */
public class HttpClientOption {
	private Method method = Method.GET;
	private RedirectMode redirect = RedirectMode.follow;
	private HttpSendHeader headers = null;
	private HttpSendHeader fixHeaders = null;
	private NioSendData body = null;
	private String formData = null;
	private String referrer = null;
	private String mimeType = null;
	private String charset = null;
	private String bodyCharset = null;
	private boolean jsonFlag = false;
	private boolean fixFlag = false;

	/**
	 * オブジェクトを破棄.
	 */
	public void destroy() {
		if(body != null) {
			try {
				body.close();
			} catch(Exception e) {}
			body = null;
		}
		method = Method.GET;
		redirect = RedirectMode.follow;
		headers = null;
		fixHeaders = null;
		referrer = null;
		mimeType= null;
		charset = null;
		jsonFlag = false;
		fixFlag = false;
	}

	/**
	 * 情報を確定する.
	 */
	public void fix() {
		if(fixFlag) {
			return;
		}
		// MethodがGETやDELETEに対してBodyが設定されている場合は
		// エラー返却.
		if(body != null &&
			(Method.GET == method || Method.DELETE == method)) {
			throw new HttpClientException(
				"Body cannot be set when Method is " + method + ".");
		// MethodがPOSTに対してBodyが設定されてない場合は
		// エラー返却.
		} else if(body == null && Method.POST == method) {
			// ただしformDataが存在する場合はエラーにしない.
			if(formData == null || formData.isEmpty()) {
				throw new HttpClientException(
					"Body is not set for the method POST.");
			}
		}
		// ヘッダー情報が設定されていない場合.
		if(headers == null) {
			fixHeaders = new HttpSendHeader();
		} else {
			// fix後のヘッダを生成.
			fixHeaders = headers.copy();
		}
		// リダイレクトモードが設定されていない場合.
		if(redirect == null) {
			redirect = RedirectMode.follow;
		}
		// リファラーが設定されていない場合.
		if(referrer == null || referrer.isEmpty()) {
			referrer = null;
		// リファラーが設定されている場合.
		} else {
			fixHeaders.put("Referer", referrer);
		}
		String mime = null;
		String mimeCharset = null;
		// JSONモードでの送信の場合.
		if(jsonFlag) {
			mime = MediaType.JSON.getMimeType();
		// Formデータが存在する場合.
		} else if(formData != null && !formData.isEmpty()) {
			// メソッドがGETかDELETE以外の場合.
			if(Method.GET != method && Method.DELETE != method) {
				// Body変換してコンテンツタイプにフォームデータ設定.
				setBody(formData);
				mime = MediaType.FORM_DATA.getMimeType();
			}
		}
		// 現在設定されている文字コードを取得.
		if(charset != null && !charset.isEmpty()) {
			mimeCharset = charset;
		} else if(bodyCharset != null && !bodyCharset.isEmpty()) {
			mimeCharset = bodyCharset;
		}
		// コンテンツタイプが直接設定されている場合.
		String s = fixHeaders.get("Content-Type");
		if(s != null && !s.isEmpty()) {
			// charsetが定義されていて、直接設定されてる
			// Content-Typeにcharsetが設定されていない場合.
			if(mimeCharset != null && Alphabet.indexOf(s, "charset") == -1) {
				s += ";charset=" + mimeCharset;
				fixHeaders.put("Content-Type", s);
			}
		// コンテンツタイプが設定されていない場合.
		} else {
			// setMimeTypeで設定されてる場合.
			if(mimeType != null && !mimeType.isEmpty()) {
				s = mimeType;
			// formData や json設定で定義されてる場合.
			} else if(mime != null && !mime.isEmpty()) {
				s = mime;
			}
			// mimeTypeが存在する場合.
			if(s != null && !s.isEmpty()) {
				// charsetが設定されている場合.
				if(mimeCharset != null) {
					s += ";charset=" + charset;
				}
				fixHeaders.put("Content-Type", s);
			}
		}
		// bodyが存在する場合.
		if(body != null) {
			// Body情報がチャンク送信の場合.
			if(body instanceof HttpSendChunkedData) {
				// チャング送信条件をセット.
				fixHeaders.put("Transfer-Encoding", "chunked");
				fixHeaders.remove("Content-Length");
			// Body情報がチャンク送信でない場合.
			// コンテンツ長が設定されていない場合は設定する.
			} else if(!headers.containsKey("Content-Length")) {
				fixHeaders.put("Content-Length", "" + body.length());
			}
		}
		// fix完了.
		fixFlag = true;
	}

	/**
	 * Fix状態をキャンセル.
	 */
	public void cancelFix() {
		fixFlag = false;
	}

	/**
	 * Fixされているかチェック.
	 * @return boolean trueの場合Fixしています.
	 */
	public boolean isFix() {
		return fixFlag;
	}

	// Fixしている場合はエラー.
	private void checkFix() {
		if(fixFlag) {
			throw new HttpClientException("I have already fixed it.");
		}
	}

	/**
	 * HttpMethodを取得.
	 * @return
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * HttpMethodを設定.
	 * @param method
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setMethod(Method method) {
		checkFix();
		// メソッドが設定されてない場合.
		if(method == null) {
			// GETをセット.
			method = Method.GET;
		// 対応していないメソッドが設定された場合.
		} else if(Method.HEAD == method ||
			Method.OPTIONS == method ||
			Method.TRACE == method) {
			throw new HttpClientException(
				"The configured Http method is not supported: " +
				method);
		}
		// methodがGETの場合.
		if(Method.GET == method) {
			// 設定されているBodyを削除.
			if(this.body != null) {
				try {
					this.body.close();
				} catch(Exception e) {}
			}
			// ヘッダからBody関連の条件を削除.
			if(headers != null) {
				headers.remove("Content-Length");
				headers.remove("Transfer-Encoding");
			}
			bodyCharset = null;
		}
		this.method = method;
		return this;
	}

	/**
	 * HttpMethodを設定.
	 * @param method
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setMethod(String method) {
		checkFix();
		return setMethod(Method.get(method));
	}

	/**
	 * HttpHeaderを取得.
	 * @return
	 */
	public Header getHeaders() {
		if(fixFlag) {
			return fixHeaders;
		} else if(headers == null) {
			headers = new HttpSendHeader();
		}
		return headers;
	}

	/**
	 * HttpHeaderを設定.
	 * @param headers
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setHeaders(Object... headers) {
		checkFix();
		this.headers = new HttpSendHeader(headers);
		return this;
	}

	/**
	 * HttpHeaderを設定.
	 * @param headers
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setHeaders(Header headers) {
		checkFix();
		if(headers instanceof HttpSendHeader) {
			this.headers = (HttpSendHeader)headers;
		} else {
			this.headers = new HttpSendHeader(headers);
		}
		return this;
	}

	/**
	 * HttpHeaderを設定.
	 * @param headers
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setHeaders(Map<String, String> headers) {
		checkFix();
		this.headers = new HttpSendHeader(headers);
		return this;
	}

	/**
	 * MimeTypeを設定.
	 * @param mimeType
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setMimeType(String mimeType) {
		checkFix();
		this.mimeType = mimeType;
		return this;
	}

	/**
	 * MimeTypeを設定.
	 * @param mimeType
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setMimeType(MediaType mimeType) {
		checkFix();
		this.mimeType = mimeType.getMimeType();
		return this;
	}


	/**
	 * 文字コードを設定.
	 * @param charset
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setCharset(String charset) {
		checkFix();
		this.charset = charset;
		return this;
	}

	/**
	 * 文字コードを設定.
	 * @param charset
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setCharset(Charset charset) {
		checkFix();
		this.charset = charset.displayName();
		return this;
	}

	/**
	 * 文字コードを設定.
	 * @param charset
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setCharset(HttpCharset charset) {
		checkFix();
		this.charset = charset.getCharset();
		return this;
	}


	/**
	 * Body情報を取得.
	 * @return
	 */
	public NioSendData getBody() {
		return body;
	}

	/**
	 * Body情報が存在するかチェック.
	 * @return
	 */
	public boolean isBody() {
		return body != null;
	}

	/**
	 * FormData情報を取得.
	 * @return
	 */
	public String getFormData() {
		return formData;
	}

	/**
	 * FormData情報が存在するかチェック.
	 * @return
	 */
	public boolean isFormData() {
		return formData != null && !formData.isEmpty();
	}

	// 現在のBody情報が存在する場合はクローズする.
	private void nowBodyByClose() {
		if(this.body != null) {
			try {
				this.body.close();
			} catch(Exception e) {}
		}
		this.body = null;
		this.formData = null;
		this.jsonFlag = false;
		this.bodyCharset = null;
	}

	// Body用の文字コードを取得.
	private String getBodyCharset() {
		String charset = this.charset;
		if(charset == null || charset.isEmpty()) {
			if(bodyCharset != null && !bodyCharset.isEmpty()) {
				charset = bodyCharset;
			} else {
				charset = HttpConstants.getCharset();
			}
		}
		return charset;
	}

	/**
	 * フォームデータを設定.
	 * @param formData フォームデータを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpClientOption setFormData(Map formData) {
		if(formData == null || formData.size() <= 0) {
			throw new HttpClientException("Form data is not set.");
		}
		checkFix();
		Entry e;
		Object v;
		String k;
		int cnt = 0;
		String charset = getBodyCharset();
		StringBuilder buf = new StringBuilder();
		Iterator<Entry> it = formData.entrySet().iterator();
		while (it.hasNext()) {
			e = it.next();
			k = e.getKey().toString();
			v = e.getValue();
			if (cnt ++ != 0)
				buf.append("&");
			buf.append(k).append("=");
			if (v != null && ((String) (v = v.toString())).length() > 0) {
				buf.append(StringUtil.urlEncode((String) v, charset));
			}
		}
		nowBodyByClose();
		this.formData = buf.toString();
		this.bodyCharset = charset;
		return this;
	}

	/**
	 * フォームデータを設定.
	 * @param args フォームデータを設定します.
	 *             [0] key, [1] value ... のように定義します.
	 *             また args[0] = string でargs.length == 1 で
	 *             定義した場合、直接文字列でFormData設定できます.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setFormData(Object... args) {
		if(args == null || args.length == 0) {
			throw new HttpClientException("Form data is not set.");
		}
		checkFix();
		if(args.length == 1 && args[0] instanceof String) {
			String data = (String)args[0];
			if(data == null || data.isEmpty()) {
				throw new HttpClientException("Form data is not set.");
			}
			this.formData = data;
			this.bodyCharset = getBodyCharset();
			return this;
		}
		Object v;
		int len = args.length;
		String charset = getBodyCharset();
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < len; i += 2) {
			if (i != 0) {
				buf.append("&");
			}
			buf.append(args[i]).append("=");
			v = args[i + 1];
			if (v != null && ((String) (v = v.toString())).length() > 0) {
				buf.append(StringUtil.urlEncode((String) v, charset));
			}
		}
		nowBodyByClose();
		this.formData = buf.toString();
		this.bodyCharset = charset;
		return this;
	}

	/**
	 * Body情報を設定.
	 * @param body バイナリ情報を設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(byte[] body) {
		if(body == null || body.length == 0) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		nowBodyByClose();
		this.body = new NioSendMemData(body);
		this.bodyCharset = null;
		return this;
	}

	/**
	 * Body情報を設定.
	 * @param body 文字列を設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(String body) {
		if(body == null || body.isEmpty()) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		String charset = getBodyCharset();
		NioSendData ns = null;
		try {
			ns = new NioSendMemData(body.getBytes(charset));
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		nowBodyByClose();
		this.body = ns;
		this.bodyCharset = charset;
		return this;
	}

	/**
	 * Body情報にJson情報をセット.
	 * @param json 送信するオブジェクトを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setJson(Object json) {
		if(json == null) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		String charset = getBodyCharset();
		String body = Json.encode(json);
		json = null;
		NioSendData ns = null;
		try {
			ns = new NioSendMemData(body.getBytes(charset));
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		nowBodyByClose();
		this.body = ns;
		this.bodyCharset = charset;
		this.jsonFlag = true;
		return this;
	}

	/**
	 * Body情報を設定.
	 * この処理ではチャング送信でBodyを送信します.
	 * @param body InputStreamを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(InputStream body) {
		if(body == null) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		try {
			nowBodyByClose();
			this.body = new HttpSendChunkedData(body);
			this.bodyCharset = null;
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		return this;
	}

	/**
	 * Body情報を設定.
	 * @param body InputStreamを設定します.
	 * @param length Bodyの長さを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(InputStream body, long length) {
		if(body == null) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		nowBodyByClose();
		this.body = new NioSendInputStreamData(body, length);
		this.bodyCharset = null;
		return this;
	}

	/**
	 * ファイル送信用のBody情報を設定.
	 * @param name ファイル名を設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setFile(String name) {
		if(name == null || name.isEmpty()) {
			throw new HttpClientException("The file name to be sent is not set.");
		}
		checkFix();
		NioSendData ns = null;
		try {
			ns = new NioSendFileData(name);
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		nowBodyByClose();
		this.body = ns;
		this.bodyCharset = null;
		return this;
	}

	/**
	 * 設定されたMimeTypeを取得.
	 * @return String MimeTypeが返却されます.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * 設定された文字コードを取得.
	 * @return String 文字コードが返却されます.
	 *                nullの場合は設定されていません.
	 */
	public String getCharset() {
		// 現在設定されている文字コードを取得.
		if(charset != null && !charset.isEmpty()) {
			return charset;
		} else if(bodyCharset != null && !bodyCharset.isEmpty()) {
			return bodyCharset;
		}
		return null;
	}

	/**
	 * リファラーを取得.
	 * @return String リファラーURLを設定します.
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * リファラーを設定.
	 * @param referrer リファラーを設定します.
	 *                 [null]及び空文字を設定した場合はリファラーは設定されません.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setReferrer(String referrer) {
		checkFix();
		if(referrer == null || referrer.isEmpty()) {
			this.referrer = null;
		} else {
			this.referrer = referrer;
		}
		return this;
	}

	/**
	 * リダイレクトモードを取得.
	 * @return RedirectMode リダイレクトモードが返却されます.
	 */
	public RedirectMode getRedirect() {
		return redirect;
	}

	/**
	 * リダイレクトモードを設定.
	 * @param redirect リダイレクトモードを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setRedirect(RedirectMode redirect) {
		checkFix();
		this.redirect = redirect;
		return this;
	}

	/**
	 * リダイレクトモードを設定.
	 * @param redirect リダイレクトモードを文字列で設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setRedirect(String redirect) {
		checkFix();
		this.redirect = RedirectMode.get(redirect);
		return this;
	}
}
