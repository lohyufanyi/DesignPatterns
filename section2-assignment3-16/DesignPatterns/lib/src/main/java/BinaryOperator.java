
public abstract class BinaryOperator implements Term {
	private Term l;
	private Term r;
	
	protected BinaryOperator(Term left, Term right) {
		if (left == null) {
			throw new IllegalArgumentException("Left term cannot be null");
		}
		else if (right == null) {
			throw new IllegalArgumentException("Right term cannot be null");
		}
		l = left;
		r = right;
	}
	
	public Term getLeft() {
		return l;
	}
	
	public Term getRight() {
		return r;
	}
}
