import java.util.Iterator;

public class Fibonacci implements Iterable<Long>{

	@Override
	public Iterator<Long> iterator() {
		
		return new Iterator<Long>() {
			int count = 0;
			int num1 = 0;
			int num2 = 1;
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Long next() {
				if (count == 0) {
					count++;
					return (long)num2;
				}
				int num3 = num2 + num1;
				num1 = num2;
				num2 = num3;
				return (long) num3;
				
			}
			
		};

	}

}
