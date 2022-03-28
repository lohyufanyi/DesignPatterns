
public class Times extends BinaryOperator{	
	
	public Times(Term left, Term right) {
		super(left, right);
	}
	
	@Override
	public int getValue() {
		return getLeft().getValue() * getRight().getValue();
	}

}
