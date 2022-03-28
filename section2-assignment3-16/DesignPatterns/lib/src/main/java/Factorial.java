
public class Factorial extends UnaryOperator{
	public Factorial(Term one) {
		super(one);
		if (one.getValue() < 0) {
			throw new IllegalArgumentException(String.format("negative value: %s", one.getValue()));
		}
	}
	
	@Override
	public int getValue() {
		int fin = 1;
		for (int i = getTerm().getValue(); i> 0; i--) {
			fin *= i;
		}
		return fin;
	}

}
