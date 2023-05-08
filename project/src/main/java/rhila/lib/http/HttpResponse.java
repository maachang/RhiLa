package rhila.lib.http;

import java.util.Date;

import org.mozilla.javascript.Scriptable;

import rhila.RhilaConstants;
import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.DateUtil;
import rhila.lib.http.HttpHeader.HttpHeaderScriptable;
import rhila.lib.http.HttpStatus.HttpStatusScriptable;

/**
 * HttpResponse.
 */
public class HttpResponse extends AbstractHttpIoCommon<HttpResponse> {
    // lambda snapStart CRaC用.
    protected static final HttpRequest LOAD_CRAC = new HttpRequest();

	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
	
	
	
	// httpStatus.
	private HttpStatusScriptable status = null;
	
	// HttpHeader.
	private HttpHeaderScriptable header = null;
	
	// コンストラクタ.
	public HttpResponse() {
		staticFlag = false;
	}
	
	// コンストラクタ.
	protected HttpResponse(boolean staticFlag) {
		this.staticFlag = staticFlag;
	}
	
	// HttpStatusを取得.
	public HttpStatus getStatus() {
		if(status == null) {
			status = HttpStatusScriptable.newInstance(
				new HttpStatus());
		}
		return status.getSrc();
	}
	
	// redirect条件が設定されている場合.
	public boolean isRedirect() {
		if(header == null || status == null) {
			return false;
		}
		if(header.getSrc().getHeader("location") == null) {
			return false;
		}
		return true;
	}
	
	// redirectURLを取得.
	public String getRedirectURL() {
		return getHeader().getHeader("location");
	}
	
	// redirect設定.
	public HttpResponse setRedirect(int status, String url) {
		if(!(status == 301 && status == 302 && status == 303 &&
			status == 307 && status == 308)) {
			throw new RhilaException(
				"Not a redirect target Status: " + status);
		}
		getStatus().setStatus(status);
		getHeader().setHeader("location", url);
		return this;
	}

	@Override
	public String getName() {
		return "HttpResponse";
	}
	
	@Override
	public String toString() {
		if(staticFlag) {
			return "[HttpResponse]";
		}
		StringBuilder buf = new StringBuilder();
		getHeaderToString(buf);
		return buf.toString();
	}
	
	// HTTPヘッダの文字変換.
	public void getHeaderToString(StringBuilder out) {
		int state = 200;
		String message = "OK";
		if(status != null) {
			state = status.src.getStatus();
			message = status.src.getMessage();
		}
		// HTTP/2.0 200 OK
		out.append(HTTP_VERSION).append(httpVersion).append(" ")
			.append(state).append(" ").append(message).append("\r\n");
		HttpHeader header = getHeader();
		// [header]Dateがセットされていない場合.
		this.setDefaultHeader("date", DateUtil.toRfc822(false, new Date()));
		// [header]Serverがセットされていない場合.
		this.setDefaultHeader("server", RhilaConstants.SERVER_NAME);
		// [header]connectionがセットされていない場合.
		this.setDefaultHeader("connection", "close");
		
		// ヘッダ出力.
		header.toString(true, out);
	}

	

	
	
	
}
