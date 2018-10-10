package ipaneltv.toolkit;

public class ASSERT {
	public static final void assertTrue(boolean b) {
		if (!b)
			throw new RuntimeException("assert failed");
	}
}
