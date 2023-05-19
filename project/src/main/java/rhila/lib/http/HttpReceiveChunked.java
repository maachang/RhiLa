package rhila.lib.http;

import rhila.RhilaException;
import rhila.lib.ByteArrayBuffer;

/**
 * 受信Chunked用オブジェクト.
 */
public final class HttpReceiveChunked {
	// chunked format.
	//
	// (bodyLength[16進数])
	// ¥r¥n
	// (body)
	// ¥r¥n
	//
	// end of chunked.
	//
	// 0
	// ¥r¥n
	// ¥r¥n
	
	/** Httpの１つの終端を示す記号. **/
	private static final byte[] END_LINE = "\r\n".getBytes();
	
	/** Httpの１つの終端を示す記号の長さ. **/
	private static final int END_LINE_LENGTH = END_LINE.length;

	// Nio受信バッファ.
	private ByteArrayBuffer nioBuffer = null;

	// 受信完了フラグ.
	private boolean eof = false;

	// １つのチャンク受信長.
	private int chunkedBodyLength = -1;

	// １つのチャンク受信中データ長.
	private int chunkedBodyPosition = 0;

	// bodyLengthを取得するバイナリ長.
	private final byte[] lengthBuffer = new byte[16];

	/**
	 * コンストラクタ.
	 */
	public HttpReceiveChunked() {
		nioBuffer = new ByteArrayBuffer();
	}

	/**
	 * コンストラクタ.
	 * @param r 対象のNio受信バッファを設定します.
	 */
	public HttpReceiveChunked(ByteArrayBuffer r) {
		nioBuffer = r;
	}

	/**
	 * クリア処理.
	 */
	public void clear() {
		nioBuffer = null;
	}

	/**
	 * 受信データの書き込み処理.
	 * @param bin 対象のバイナリを設定します.
	 */
	public void write(byte[] bin) {
		nioBuffer.write(bin, 0, bin.length);
	}

	/**
	 * 受信データの書き込み処理.
	 * @param bin 対象のバイナリを設定します.
	 * @param off 対象のオフセット値を設定します.
	 * @param len 対象の長さを設定します.
	 */
	public void write(byte[] bin, int off, int len) {
		nioBuffer.write(bin, off, len);
	}

	/**
	 * 読み込み処理.
	 * @param buf 受信データを受け取るバッファを設定します.
	 * @return int -1が返却された場合、受信完了しています.
	 */
	public int read(byte[] buf) {
		return read(buf, 0, buf.length);
	}

	/**
	 * 読み込み処理.
	 * @param buf 受信データを受け取るバッファを設定します.
	 * @param len bufの長さを設定します.
	 * @return int -1が返却された場合、受信完了しています.
	 */
	public int read(byte[] buf, int len) {
		return read(buf, 0, len);
	}

	/**
	 * 読み込み処理.
	 * @param buf 受信データを受け取るバッファを設定します.
	 * @param off bufのオフセット値を設定します.
	 * @param len bufの長さを設定します.
	 * @return int -1が返却された場合、受信完了しています.
	 */
	public int read(byte[] buf, int off, int len) {
		// eofの場合は-1.
		if(eof) {
			return -1;
		}
		int n, p, remLen;
		int ret = 0;
		// 基本的にchunked単位の情報を受信して、指定バッファにデータをセットする.
		// 対象バッファが満タンになった場合はループを抜ける.
		while(len > off && nioBuffer.size() != 0) {
			// chunkedBodyLengthが取得できていない場合.
			if(chunkedBodyLength == -1) {
				// chunkedBodyLength + ¥r¥n を検索.
				p = nioBuffer.indexOf(END_LINE);
				if(p == -1) {
					// chunkedBodyLengthが読み切れていない場合.
					// ループを抜ける.
					break;
				// １つの塊が４バイトを超える長さの場合はエラーにする.
				} else if(p > lengthBuffer.length) {
					throw new RhilaException(
						"Impossible chunked-body Length length: " + p);
				}
				// 受信バッファから、chunkedBodyLength + ¥r¥nまでを読み込む.
				nioBuffer.read(lengthBuffer, 0, p);
				nioBuffer.skip(END_LINE_LENGTH);
				// chunkedBodyLength(16進数)を解析.
				n = toHex(lengthBuffer, 0, p);
				// chunkedBodyLengthが0の場合はchunkedの終端.
				if(n == 0) {
					// 終端を検出した場合はループを抜ける.
					eof = true;
					break;
				}
				// chunkedの開始.
				chunkedBodyPosition = 0;
				chunkedBodyLength = n;
			}
			// chunkedBodyの残り受信データ長を取得.
			remLen = chunkedBodyLength - chunkedBodyPosition;
			// 読み込みバッファの長さの方がchunkedBody残りデータ長より少ない
			if(remLen > nioBuffer.size()) {
				remLen = nioBuffer.size();
			}
			// 残りデータよりもバッファ格納データの方が小さい場合.
			if(len - off < remLen) {
				n = nioBuffer.read(buf, off, len - off);
			// 残りデータの方がバッファ格納データより大きい場合.
			} else {
				n = nioBuffer.read(buf, off, remLen);
			}
			off += n;
			ret += n;
			chunkedBodyPosition += n;
			// chunkedBodyの読み込みが終わった場合.
			if(chunkedBodyPosition == chunkedBodyLength) {
				// chunkedBodyの終端(¥r¥n)が存在するかチェック.
				p = nioBuffer.indexOf(END_LINE);
				if(p == -1) {
					break;
				} else if(p != 0) {
					throw new RhilaException(
						"The position of chunked-end is incorrect.");
				}
				// ¥r¥nを読み飛ばす.
				nioBuffer.skip(END_LINE_LENGTH);
				// 次のchunkedを取得.
				chunkedBodyLength = -1;
				chunkedBodyPosition = 0;
			}
		}
		return ret;
	}

	/**
	 * Chunkedが終端を迎えたかチェック.
	 * @return trueの場合、終端です.
	 */
	public boolean isEof() {
		return eof;
	}

	/** byte[] を 16進数値で変換. **/
	private static final int toHex(final byte[] b, int off, int len) {
		char c;
		int n;
		int ret = 0;
		for(int i = len - 1, j = 0; i >= off; i --, j += 4) {
			c = (char)(b[i] & 0x00ff);
			if (c >= '0' && c <= '9') {
				n = ((int) (c - '0') & 0x0000000f);
			} else if (c >= 'A' && c <= 'F') {
				n = ((int) (c - 'A') & 0x0000000f) + 10;
			} else if (c >= 'a' && c <= 'f') {
				n = ((int) (c - 'a') & 0x0000000f) + 10;
			} else {
				throw new RhilaException("Not a hexadecimal value: " +
					new String(b, off, len));
			}
			ret += n << j;
		}
		return ret;
	}
}
