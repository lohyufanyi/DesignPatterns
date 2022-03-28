import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class TermTest {
	private static final Class<?> BINARY_OPERATOR = BinaryOperator.class;
	private static final Class<?> FACTORIAL       = Factorial     .class;
	private static final Class<?> NUMBER          = Number        .class;
	private static final Class<?> PLUS            = Plus          .class;
	private static final Class<?> TERM            = Term          .class;
	private static final Class<?> TIMES           = Times         .class;
	private static final Class<?> UNARY_OPERATOR  = UnaryOperator .class;
	private static final Class<?> OBJECT          = Object        .class;
	@Test
	public void testNoStaticNonPrivateFields() {
		Consumer<Class<?>> testFields = c -> Arrays.stream( c.getDeclaredFields() ).filter( f->!f.isSynthetic() ).forEach( f->{
			var mod  = f.getModifiers();
			var name = f.getName(); 
			Truth.assertWithMessage( String.format("field '%s' is not private", name )).that( Modifier.isPrivate( mod )).isTrue();
			Truth.assertWithMessage( String.format("field '%s' is static",      name )).that( Modifier.isStatic ( mod )).isFalse();
		});
		testFields.accept( BINARY_OPERATOR );
		testFields.accept( FACTORIAL );
		testFields.accept( NUMBER );
		testFields.accept( PLUS );
		testFields.accept( TERM );
		testFields.accept( TIMES );
		testFields.accept( UNARY_OPERATOR );
	}
	@Test
	public void testClassesHaveSuper() {
		BiConsumer<Class<?>,Class<?>> isSuperType = (a,b) ->
			Truth.assertWithMessage( String.format( "'%s' is not the supertype of '%s'", a.getSimpleName(), b.getSimpleName() ))
			     .that( a.isAssignableFrom( b )).isTrue();

		BiConsumer<Class<?>,Class<?>> isSuperClass = (a,b) ->
			Truth.assertWithMessage( String.format( "'%s' is not the superclass of '%s'", a.getSimpleName(), b.getSimpleName() ))
			     .that( b.getSuperclass() ).isEqualTo( a );

		Truth.assertThat( TERM.isInterface() ).isTrue();
		
		isSuperType .accept( TERM, BINARY_OPERATOR );
		isSuperType .accept( TERM, NUMBER );
		isSuperType .accept( TERM, UNARY_OPERATOR );

		isSuperClass.accept( OBJECT,          BINARY_OPERATOR );
		isSuperClass.accept( UNARY_OPERATOR,  FACTORIAL );
		isSuperClass.accept( OBJECT,          NUMBER );
		isSuperClass.accept( BINARY_OPERATOR, PLUS );
		isSuperClass.accept( BINARY_OPERATOR, TIMES );
		isSuperClass.accept( OBJECT,          UNARY_OPERATOR );
	}
	@Test
	public void testClassesHaveFields() {
		ObjIntConsumer<Class<?>> countFields = (a,b) -> {
			long count = Arrays.stream( a.getDeclaredFields() ).filter( f->!f.isSynthetic() ).collect( Collectors.counting() );
			Truth.assertWithMessage( String.format( "'%s' has unexpected number of fields", a.getSimpleName() ))
			     .that( count ).isEqualTo( b );
		};
		countFields.accept( BINARY_OPERATOR, 2 );
		countFields.accept( FACTORIAL,       0 );
		countFields.accept( NUMBER,          1 );
		countFields.accept( PLUS,            0 );
		countFields.accept( TERM,            0 );
		countFields.accept( TIMES,           0 );
		countFields.accept( UNARY_OPERATOR,  1 );
	}
	@Nested
	class TestPlus {
		@Test
		public void a() {
			Term  a = new Number( 47 );
			Term  b = new Number( -5 );
			Term  c = new Plus( a, b );
			int actual   = c.getValue(); // 47+(-5)
			int expected = 42;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void b() {
			Term  a = new Number(  7 );
			Term  b = new Number( 11 );
			Term  c = new Plus( a, b );
			int actual   = c.getValue(); // 7+11
			int expected = 18;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void c() {
			Term  a = new Number( -2 );
			Term  b = new Number( -1 );
			Term  c = new Plus( a, b );
			int actual   = c.getValue(); // (-2)+(-1)
			int expected = -3;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
	}
	@Nested
	class TestTimes {
		@Test
		public void a() {
			Term  a = new Number( -5 );
			Term  b = new Number(  2 );
			Term  c = new Times( a, b ); // (-5)*2 
			int actual   = c.getValue();
			int expected = -10;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void b() {
			Term  a = new Number(  7 );
			Term  b = new Number( 11 );
			Term  c = new Times( a, b );
			int actual   = c.getValue(); // 7*11
			int expected = 77;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void c() {
			Term  a = new Number( -2 );
			Term  b = new Number( -1 );
			Term  c = new Times( a, b );
			int actual   = c.getValue(); // (-2)*(-1)
			int expected = 2;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
	}
	@Nested
	class TestUnaryOperator {
		@Test
		public void exception() {
			Throwable t;
			t = assertThrows( IllegalArgumentException.class, ()->new UnaryOperator( null ) {
							@Override
							public int getValue() {
								return 0;
							}
			}); 
			Truth.assertThat( t.getMessage() ).isEqualTo( "Term cannot be null" );

			t = assertThrows( IllegalArgumentException.class, ()->new Factorial( null )); 
			Truth.assertThat( t.getMessage() ).isEqualTo( "Term cannot be null" );
		}
	}
	@Nested
	class TestBinaryOperator {
		@Test
		public void exceptionLeft() {
			Term      a = new Number(  7 );
			Throwable t;
			t = assertThrows( IllegalArgumentException.class, ()->new BinaryOperator( null, a ) {
				@Override
				public int getValue() {
					return 0;
				}
			}); 
			Truth.assertThat( t.getMessage() ).isEqualTo( "Left term cannot be null" );

			t = assertThrows( IllegalArgumentException.class, ()->new Times( null, a )); 
			Truth.assertThat( t.getMessage() ).isEqualTo( "Left term cannot be null" );
		}
		@Test
		public void exceptionRight() {
			Term      a = new Number( -1 );
			Throwable t;
			t = assertThrows( IllegalArgumentException.class, ()->new BinaryOperator( a, null ) {
				@Override
				public int getValue() {
					return 0;
				}
			}); 
			Truth.assertThat( t.getMessage() ).isEqualTo( "Right term cannot be null" );

			t = assertThrows( IllegalArgumentException.class, ()->new Times( a, null )); 
			Truth.assertThat( t.getMessage() ).isEqualTo( "Right term cannot be null" );
		}
	}
	@Nested
	class TestFactorial {
		@Test
		public void a() {
			Term  a = new Number( 6 );
			Term  b = new Factorial( a ); // 6!
			int actual   = b.getValue();
			int expected = 720;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void b() {
			Term  a = new Number( 2 );
			Term  b = new Factorial( a ); // 2!
			int actual   = b.getValue();
			int expected = 2;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void exception() {
			for (int n : List.of( -1, -42, Integer.MIN_VALUE )) {
				Term      a = new Number( n );
				Throwable t = assertThrows( IllegalArgumentException.class, ()->new Factorial( a ) ); // (negative)! 
				Truth.assertThat( t.getMessage() ).isEqualTo( "negative value: " + n );
			}
		}
	}
	@Nested
	class TestExpressions {
		@Test
		public void a() {
			Term  a = new Number(  6 );
			Term  b = new Number(  3 );
			Term  c = new Number(  4 );
			Term  e = new Plus ( new Times( a,b ), new Factorial( c )); // (6*3)+4! 
			int actual   = e.getValue();
			int expected = 42;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void b() {
			Term  a = new Number( 47 );
			Term  b = new Number( -5 );
			Term  c = new Number(  2 );
			Term  d = new Number( -3 );
			Term  e = new Number( 17 );
			Term  f = new Plus ( new Times( new Plus( a,b ),c ), new Times( d,e )); // (47+(-5))*2+(-3*17) 
			int actual   = f.getValue();
			int expected = 33;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		public void c() {
			Term  a = new Number(  2 );
			Term  b = new Number(  5 );
			Term  c = new Number(  3 );
			Term  d = new Number(  7 );
			Term  e = new Number( -1 );
			Term  f = new Times( new Plus( new Times( a, new Factorial( b )), new Times( new Factorial( c ), d )), e ); // ((2*(5!))+(3!*7))*(-1) 
			int actual   = f.getValue();
			int expected = -282;
			Truth.assertThat( actual ).isEqualTo( expected );
		}
	}
}
