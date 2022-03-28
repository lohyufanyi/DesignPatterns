
public abstract class UnaryOperator implements Term {
	private Term termOne;
	
	protected UnaryOperator(Term one) {
		if (one == null) {
			throw new IllegalArgumentException("Term cannot be null");
		}
		termOne = one;
	}
	
	public Term getTerm() {
		return termOne;
	}
}
