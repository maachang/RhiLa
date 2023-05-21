package rhila.scriptable;

import org.mozilla.javascript.Scriptable;

/**
 * staticで管理されてるFunctionから非staticなFunction生成するためのもの.
 * 
 * 何故これが必要かと言えば、snapStartだと 暖機的(CRaC)なJava実装 で static化してる
 * 内容をスナップショット化するそうなので、それを実現するための実装.
 */
public abstract class AbstractRhinoFunctionInstance extends AbstractRhinoFunction {
	
	// 新しいインスタンスを生成.
	public abstract Scriptable getInstance(Object... args);
}
