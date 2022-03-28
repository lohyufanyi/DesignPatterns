import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class FibonacciTest {
	private static final Class<?>     ITERABLE            = Fibonacci.class;
	private static final List<String> ITERABLE_INTERFACES = List.of( "java.lang.Iterable<java.lang.Long>" );

	@Test
	public void testInterfaceAndFields() {		
		Consumer<Class<?>> noArraysOrCollections = c -> 
		    Arrays.stream( c.getDeclaredFields() ).filter(f->!f.isSynthetic()).forEach( f -> {
			    var collection = "Field '%s' can't be a Java Collection";
			    var array      = "Field '%s' can't be a Java array";
			    var type       = f.getType();
			    var name       = f.getName();
			    Truth.assertWithMessage(String.format( collection, name )).that( Collection .class.isAssignableFrom( type )).isFalse();
			    Truth.assertWithMessage(String.format( collection, name )).that( Dictionary .class.isAssignableFrom( type )).isFalse();
			    Truth.assertWithMessage(String.format( collection, name )).that( AbstractMap.class.isAssignableFrom( type )).isFalse();
			    Truth.assertWithMessage(String.format( array,      name )).that( type.isArray()                            ).isFalse();
		});
		Consumer<Class<?>> arePrivateNonStatic = c ->
		    Arrays.stream( c.getDeclaredFields() ).filter( f -> !f.isSynthetic() ).forEach( f -> {				
		    	var name = f.getName();
		    	var mods = f.getModifiers();
		    	Truth.assertWithMessage(String.format( "Field '%s' should be private", name )).that( Modifier.isPrivate( mods ));
		    	Truth.assertWithMessage(String.format( "Field '%s' can't be static",   name )).that( Modifier.isStatic ( mods ));
		});
		BiConsumer<Class<?>,Class<?>> hasSuperClass = (c,s) ->
		    Truth.assertWithMessage(String.format( "'%s' should subclass from '%s'", c.getSimpleName(), s.getSimpleName() ))
			     .that( c.getSuperclass() ).isEqualTo( s );
		BiConsumer<Class<?>,List<String>> hasInterfaces = (c,i) -> 
		    Truth.assertWithMessage(String.format("'%s' should have interfaces %s", c.getName(), i ))
		         .that( Arrays.stream( ITERABLE.getGenericInterfaces() ).map( Type::getTypeName ).collect( Collectors.toList() ))
		         .containsExactlyElementsIn( i );
		BiConsumer<Class<?>,Class<?>> isInstanceOf = (c,s) -> 
	        Truth.assertWithMessage(String.format("'%s' should be an instance of '%s'", c.getName(), s.getName() ))
	             .that( c ).isAssignableTo( s );
		
		noArraysOrCollections.accept( ITERABLE );
		arePrivateNonStatic  .accept( ITERABLE );

		var iterator = new Fibonacci().iterator();
		Truth.assertThat( iterator ).isNotNull();
		noArraysOrCollections.accept( iterator.getClass() );
		arePrivateNonStatic  .accept( iterator.getClass() );
		// super-class & interfaces
		hasSuperClass.accept( ITERABLE, Object.class );
		hasInterfaces.accept( ITERABLE, ITERABLE_INTERFACES );

		hasSuperClass.accept( iterator.getClass(), Object  .class );
	    isInstanceOf .accept( iterator.getClass(), Iterator.class );
	}

	@Test
	public void testUsingNextOnly() {
		Iterable<Long> iterable = new Fibonacci();
		Iterator<Long> iterator = iterable.iterator();
		for (long expected : new long[]{ 1, 1, 2, 3, 5 }) {
			long actual = iterator.next();
			Truth.assertThat( actual ).isEqualTo( expected );
		}
	}
	@Test
	public void testHasNextDoesntAdvance() {
		Iterable<Long> iterable = new Fibonacci();
		Iterator<Long> iterator = iterable.iterator();
		for (long expected : new long[]{ 1, 1, 2, 3, 5 }) {
			Truth.assertThat( iterator.hasNext() ).isTrue();
			Truth.assertThat( iterator.hasNext() ).isTrue();
			Truth.assertThat( iterator.hasNext() ).isTrue();
			long actual = iterator.next();
			Truth.assertThat( actual ).isEqualTo( expected );
		}
	}

	@Test
	public void testFibonacciFrom1to5() {
		int     n    = 0;
		boolean done = false;
		for (long actual : new Fibonacci()) {
			long expected = -1;
			switch (++n) {
			case 1 : expected = 1; break;
			case 2 : expected = 1; break;
			case 3 : expected = 2; break;
			case 4 : expected = 3; break;
			case 5 : expected = 5; break;
			default: done = true;
			}
			if (done) {
				break;
			}
			Truth.assertWithMessage(String.format( "fibonacci(%d)", n ))
			     .that( actual ).isEqualTo( expected );
		}
	}
	@Test
	public void testFibonacciFrom6to12() {
		int     n    = 0;
		boolean done = false;
		for (long actual : new Fibonacci()) {
			if (++n > 5) {
				long expected = -1;
				switch (n) {
				case  6 : expected =   8; break;
				case  7 : expected =  13; break;
				case  8 : expected =  21; break;
				case  9 : expected =  34; break;
				case 10 : expected =  55; break;
				case 11 : expected =  89; break;
				case 12 : expected = 144; break;
				default : done = true;
				}
				if (done) {
					break;
				}
				Truth.assertWithMessage(String.format( "fibonacci(%d)", n ))
			         .that( actual ).isEqualTo( expected );
			}
		}
	}
	@Test
	public void testFibonacci42() {
		assertTimeout( Duration.ofSeconds(10), () -> {
			int n = 0;
			for (long actual : new Fibonacci()) {
				if (++n == 42) {
					long expected = 267914296l;
					Truth.assertWithMessage(String.format( "fibonacci(%d)", n ))
			             .that( actual ).isEqualTo( expected );
					break;
				}
			}
		});
	}
	@Test
	public void testTwoConcurrentIterables() {
		Fibonacci one      = new Fibonacci();
		Fibonacci two      = new Fibonacci();
		long[]    expected = new long[] { 1,1,2,3,5,8,13,21,34,55,89,144 };
		int       oneIndex = 0;
		int       twoIndex = 0;
		int       counter  = 0;
		for (long oneActual : one) {
			Truth.assertThat( oneActual ).isEqualTo( expected[ oneIndex ] );
			if (++oneIndex % 3 == 0) { 
				for (long twoActual: two) {
					Truth.assertThat( twoActual ).isEqualTo( expected[ twoIndex ] );
					if (++twoIndex % 4 == 0) {
						break;
					}
				}
			}
			if (++counter == 3) {
				break;
			}
		}
	}
	@Test
	public void testSeveralConcurrentIterators() {
		Fibonacci      fibonacci = new Fibonacci();
		Iterator<Long> one       = fibonacci.iterator();
		Iterator<Long> two       = fibonacci.iterator();
		Iterator<Long> three     = fibonacci.iterator();
		for (Long expected : new long[]{ 1,1,2,3,5,8,13,21,34,55,89,144 }) {
			Truth.assertThat( one  .next() ).isEqualTo( expected );
			Truth.assertThat( two  .next() ).isEqualTo( expected );
			Truth.assertThat( three.next() ).isEqualTo( expected );
		}
	}
	
}
