package rhila.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Process.env内容.
 */
public class ProcessEnv extends MapScriptable {
	
	// 条件としては、実行時のenv条件のMapをセットする.
	// これによってenv定義をプログラムで再定義できるようにする.
	// これにより、柔軟な形でのEnvの管理ができる.
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ProcessEnv() {
		super.map = new HashMap();
		Map<String, String> m = System.getenv();
		Iterator<Entry<String, String>> it = m.entrySet().iterator();
		Entry<String, String> e;
		while(it.hasNext()) {
			e = it.next();
			super.put(e.getKey(), null, e.getValue());
		}
	}
	
	@Override
	public String toString() {
		return "[env]";
	}
}
