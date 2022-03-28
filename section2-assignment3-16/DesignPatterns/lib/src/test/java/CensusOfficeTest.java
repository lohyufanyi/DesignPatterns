import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class CensusOfficeTest {
	private Class<?> getClass(String name) {
		try {
			Package pkg  = getClass().getPackage();
			String  path = (pkg == null || pkg.getName().isEmpty()) ? "" : pkg.getName()+".";
			return Class.forName( path + name );
		} catch (ClassNotFoundException e) {
			fail( String.format( "Class '%s' doesn't exist", name ));
		}
		return null;
	}

	@Test
	public void testNoStaticNonPrivateFields() {
		Consumer<Class<?>> testFields = c -> Arrays.stream( c.getDeclaredFields() ).filter( f->!f.isSynthetic() ).forEach( f->{
			var mod  = f.getModifiers();
			var name = f.getName(); 
			Truth.assertWithMessage( String.format("field '%s' is not private", name )).that( Modifier.isPrivate( mod )).isTrue();
			Truth.assertWithMessage( String.format("field '%s' is static",      name )).that( Modifier.isStatic ( mod )).isFalse();
		});
		testFields.accept( getClass( "CensusOffice" ));
		testFields.accept( getClass( "LastCity" ));
		testFields.accept( getClass( "TopFiveCities" ));
	}
	@Test
	public void testCensusIsObservableAndListenersAreObservers() {
		Truth.assertThat( new CensusOffice( 2 )).isInstanceOf( Observable.class );
		Truth.assertThat( new LastCity()       ).isInstanceOf( Observer  .class );
		Truth.assertThat( new TopFiveCities()  ).isInstanceOf( Observer  .class );
	}
	@Test
	public void testNewCensus() {
		CensusOffice a = new CensusOffice( 1 );
		Truth.assertThat( a.getReported()  ).isNull();
		Truth.assertThat( a.hasObservers() ).isFalse();
	}
	@Test
	public void testNewOfficeNumberIsNegative() {
		for (int n : List.of( Integer.MIN_VALUE, -1, 0 )){
			Throwable t = assertThrows( IllegalArgumentException.class, () -> new CensusOffice( n ));
			Truth.assertThat( t.getMessage() ).isEqualTo( String.format("office number must greater that 0 [%d]", n ));
		}
	}
	
	private City virginiaBeach = new City("Virginia Beach","VA",447021);
	private City norfolk       = new City("Norfolk","VA",245782);
	private City chesapeake    = new City("Chesapeake","VA",228417);
	private City richmond      = new City("Richmond","VA",210309);
	private City newportNews   = new City("Newport News","VA",180726);
	private City alexandria    = new City("Alexandria","VA",146294);
	private City hampton       = new City("Hampton","VA",136836);
	private City roanoke       = new City("Roanoke","VA",97469);
	private City portsmouth    = new City("Portsmouth","VA",96470);
	private City suffolk       = new City("Suffolk","VA",85181);
	private City lynchburg     = new City("Lynchburg","VA",77113);
	private City harrisonburg  = new City("Harrisonburg","VA",50981);

	@Nested
	class TestCensusOffice {
		@Test
		public void testOneListenerOneSubject() {
			AtomicInteger                 counter = new AtomicInteger();
			AtomicReference<City>         city    = new AtomicReference<>();
			AtomicReference<CensusOffice> office  = new AtomicReference<>();
			
			CensusOffice a = new CensusOffice( 42 );
			Observer     b = observable -> {
				Truth.assertThat( observable ).isNotNull();
				Truth.assertThat( observable ).isInstanceOf( CensusOffice.class );

				office .set((CensusOffice) observable );
				city   .set( office.get().getReported() );
				counter.incrementAndGet();
			};
			Truth.assertThat( a.getNumber() ).isEqualTo( 42 );

			Truth.assertThat( a.hasObservers()   ).isFalse();
			Truth.assertThat( a.addObserver ( b )).isTrue();
			Truth.assertThat( a.addObserver ( b )).isFalse();
			Truth.assertThat( a.hasObservers()   ).isTrue();

			a.report( virginiaBeach );
			Truth.assertThat( counter.get() ).isEqualTo( 1 );
			Truth.assertThat( office .get() ).isEqualTo( a );
			Truth.assertThat( city   .get() ).isEqualTo( virginiaBeach );

			a.report( newportNews );
			Truth.assertThat( counter.get() ).isEqualTo( 2 );
			Truth.assertThat( office .get() ).isEqualTo( a );
			Truth.assertThat( city   .get() ).isEqualTo( newportNews );

			a.report    ( alexandria );
			Truth.assertThat( counter.get() ).isEqualTo( 3 );
			Truth.assertThat( office .get() ).isEqualTo( a );
			Truth.assertThat( city   .get() ).isEqualTo( alexandria );

			Truth.assertThat( a.hasObservers()     ).isTrue();
			Truth.assertThat( a.removeObserver( b )).isTrue();
			Truth.assertThat( a.hasObservers()     ).isFalse();
		}
		@Test
		public void testOneListenerSeveralSubjects() {
			AtomicInteger                 counter = new AtomicInteger();
			AtomicReference<City>         city    = new AtomicReference<>();
			AtomicReference<CensusOffice> office  = new AtomicReference<>();
			
			CensusOffice a1 = new CensusOffice(  2 );
			CensusOffice a2 = new CensusOffice( 11 );
			CensusOffice a3 = new CensusOffice(  5 );
			Observer     b  = observable -> {
				Truth.assertThat( observable ).isNotNull();
				Truth.assertThat( observable ).isInstanceOf( CensusOffice.class );

				office .set((CensusOffice) observable );
				city   .set( office.get().getReported() );
				counter.incrementAndGet();
			};
			Truth.assertThat( a1.getNumber() ).isEqualTo(  2 );
			Truth.assertThat( a2.getNumber() ).isEqualTo( 11 );
			Truth.assertThat( a3.getNumber() ).isEqualTo(  5 );

			Truth.assertThat( a1.hasObservers() ).isFalse();
			Truth.assertThat( a2.hasObservers() ).isFalse();
			Truth.assertThat( a3.hasObservers() ).isFalse();
			Truth.assertThat( a1.addObserver( b )).isTrue();
			Truth.assertThat( a1.hasObservers() ).isTrue();
			Truth.assertThat( a2.addObserver( b )).isTrue();
			Truth.assertThat( a2.hasObservers() ).isTrue();
			Truth.assertThat( a3.hasObservers() ).isFalse();

			a1.report( virginiaBeach );
			Truth.assertThat( counter.get() ).isEqualTo( 1 );
			Truth.assertThat( office .get() ).isEqualTo( a1 );
			Truth.assertThat( city   .get() ).isEqualTo( virginiaBeach );

			a2.report( newportNews );
			Truth.assertThat( counter.get() ).isEqualTo( 2 );
			Truth.assertThat( office .get() ).isEqualTo( a2 );
			Truth.assertThat( city   .get() ).isEqualTo( newportNews );

			a3.report( alexandria );
			Truth.assertThat( counter.get() ).isEqualTo( 2 );
			Truth.assertThat( office .get() ).isEqualTo( a2 );
			Truth.assertThat( city   .get() ).isEqualTo( newportNews );

			Truth.assertThat( a1.hasObservers()     ).isTrue();
			Truth.assertThat( a2.hasObservers()     ).isTrue();
			Truth.assertThat( a3.hasObservers()     ).isFalse();
			Truth.assertThat( a1.removeObserver( b )).isTrue();
			Truth.assertThat( a2.removeObserver( b )).isTrue();
			Truth.assertThat( a3.removeObserver( b )).isFalse();
			Truth.assertThat( a1.hasObservers()     ).isFalse();
			Truth.assertThat( a2.hasObservers()     ).isFalse();
			Truth.assertThat( a3.hasObservers()     ).isFalse();
		}
	}
	
	@Nested
	class TestLastCity {
		@Test
		public void testNewObserver() {
			AtomicInteger c = new AtomicInteger();
			LastCity      b = new LastCity() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c.incrementAndGet();
				}
			};
			Truth.assertThat( b.getLastCity()   ).isNull();
			Truth.assertThat( b.getLastOffice() ).isNull();
			Truth.assertThat( c.get()           ).isEqualTo( 0 );
		}
		@Test
		public void testOneListenerOneOffice() {
			CensusOffice  a = new CensusOffice ( 7 );

			AtomicInteger c = new AtomicInteger();
			LastCity      b = new LastCity() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c.incrementAndGet();
				}
			};
			Truth.assertThat( a.addObserver( b )).isTrue();

			a.report( norfolk );
			Truth.assertThat( c.get() ).isEqualTo( 1 );
			Truth.assertThat( b.getLastOffice() ).isEqualTo( a );
			Truth.assertThat( b.getLastCity() ).isEqualTo( norfolk );

			a.report( chesapeake );
			Truth.assertThat( c.get() ).isEqualTo( 2 );
			Truth.assertThat( b.getLastOffice() ).isEqualTo( a );
			Truth.assertThat( b.getLastCity() ).isEqualTo( chesapeake );

			a.report( richmond );
			Truth.assertThat( c.get() ).isEqualTo( 3 );
			Truth.assertThat( b.getLastOffice() ).isEqualTo( a );
			Truth.assertThat( b.getLastCity() ).isEqualTo( richmond );
		}
		@Test
		public void testOneListenerSeveralOffices() {
			CensusOffice  a1 = new CensusOffice ( 1 );
			CensusOffice  a2 = new CensusOffice ( 3 );
			CensusOffice  a3 = new CensusOffice ( 7 );

			AtomicInteger c  = new AtomicInteger();
			LastCity      b  = new LastCity() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c.incrementAndGet();
				}
			};
			Truth.assertThat( a1.addObserver( b )).isTrue();
			Truth.assertThat( a2.addObserver( b )).isTrue();
			Truth.assertThat( a3.addObserver( b )).isTrue();

			a1.report( norfolk );
			Truth.assertThat( c.get()           ).isEqualTo( 1 );
			Truth.assertThat( b.getLastOffice() ).isEqualTo( a1 );
			Truth.assertThat( b.getLastCity()   ).isEqualTo( norfolk );

			a2.report( chesapeake );
			Truth.assertThat( c.get()           ).isEqualTo( 2 );
			Truth.assertThat( b.getLastOffice() ).isEqualTo( a2 );
			Truth.assertThat( b.getLastCity()   ).isEqualTo( chesapeake );

			a3.report( richmond );
			Truth.assertThat( c.get()           ).isEqualTo( 3 );
			Truth.assertThat( b.getLastOffice() ).isEqualTo( a3 );
			Truth.assertThat( b.getLastCity()   ).isEqualTo( richmond );
		}
		@Test
		public void testSeveralListenersSeveralOffices() {
			CensusOffice  a1 = new CensusOffice( 1 );
			CensusOffice  a2 = new CensusOffice( 2 );
			CensusOffice  a3 = new CensusOffice( 3 );
			
			AtomicInteger c1 = new AtomicInteger();
			LastCity      b1 = new LastCity() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c1.incrementAndGet();
				}
			};
			AtomicInteger c2 = new AtomicInteger();
			LastCity      b2 = new LastCity() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c2.incrementAndGet();
				}
			};
			AtomicInteger c3 = new AtomicInteger();
			LastCity      b3 = new LastCity() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c3.incrementAndGet();
				}
			};
			Truth.assertThat( a1.addObserver( b1 )).isTrue();
			Truth.assertThat( a2.addObserver( b2 )).isTrue();
			Truth.assertThat( a3.addObserver( b1 )).isTrue();
			Truth.assertThat( a3.addObserver( b3 )).isTrue();

			a1.report( alexandria );
			Truth.assertThat( c1.get()           ).isEqualTo( 1 );
			Truth.assertThat( b1.getLastOffice() ).isEqualTo( a1 );
			Truth.assertThat( b1.getLastCity()   ).isEqualTo( alexandria );
			Truth.assertThat( c2.get()           ).isEqualTo( 0 );
			Truth.assertThat( b2.getLastOffice() ).isNull();
			Truth.assertThat( b2.getLastCity()   ).isNull();
			Truth.assertThat( c3.get()           ).isEqualTo( 0 );
			Truth.assertThat( b3.getLastOffice() ).isNull();
			Truth.assertThat( b3.getLastCity()   ).isNull();
			
			a2.report( hampton );
			Truth.assertThat( c1.get()           ).isEqualTo( 1 );
			Truth.assertThat( b1.getLastOffice() ).isEqualTo( a1 );
			Truth.assertThat( b1.getLastCity()   ).isEqualTo( alexandria );
			Truth.assertThat( c2.get()           ).isEqualTo( 1 );
			Truth.assertThat( b2.getLastOffice() ).isEqualTo( a2 );
			Truth.assertThat( b2.getLastCity()   ).isEqualTo( hampton );
			Truth.assertThat( c3.get()           ).isEqualTo( 0 );
			Truth.assertThat( b3.getLastOffice() ).isNull();
			Truth.assertThat( b3.getLastCity()   ).isNull();

			a3.report( roanoke );
			Truth.assertThat( c1.get()           ).isEqualTo( 2 );
			Truth.assertThat( b1.getLastOffice() ).isEqualTo( a3 );
			Truth.assertThat( b1.getLastCity()   ).isEqualTo( roanoke );
			Truth.assertThat( c2.get()           ).isEqualTo( 1 );
			Truth.assertThat( b2.getLastOffice() ).isEqualTo( a2 );
			Truth.assertThat( b2.getLastCity()   ).isEqualTo( hampton );
			Truth.assertThat( c3.get()           ).isEqualTo( 1 );
			Truth.assertThat( b3.getLastOffice() ).isEqualTo( a3 );
			Truth.assertThat( b3.getLastCity()   ).isEqualTo( roanoke );
		}
	}
	@Nested
	class TestTopFiveCities {
		@Test
		public void testNewListener() {
			AtomicInteger c = new AtomicInteger();
			TopFiveCities b = new TopFiveCities() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c.incrementAndGet();
				}
			};
			List<City> actual   = b.getTopFive();
			List<City> expected = List.of();
			Truth.assertThat( actual  ).containsExactlyElementsIn( expected );
			Truth.assertThat( c.get() ).isEqualTo( 0 );
		}
		@Test
		public void testOneListenerOneOffice() {
			CensusOffice  a1 = new CensusOffice( 1 );
			TopFiveCities b1 = new TopFiveCities();
			List<City>    actual;
			
			Truth.assertThat( a1.addObserver( b1 )).isTrue();
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).isEmpty();

			a1.report( harrisonburg );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( harrisonburg ));

			a1.report( suffolk ); 
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( suffolk, harrisonburg ));

			a1.report( portsmouth );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( portsmouth, suffolk, harrisonburg ) );

			a1.report( lynchburg );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( portsmouth, suffolk, lynchburg, harrisonburg ));

			a1.report( richmond );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( richmond, portsmouth, suffolk, lynchburg, harrisonburg ));

			a1.report( virginiaBeach );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( virginiaBeach, richmond, portsmouth, suffolk, lynchburg ));

			a1.report( norfolk );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( virginiaBeach, norfolk, richmond, portsmouth, suffolk ));

			a1.report( harrisonburg );
			actual = b1.getTopFive();
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactlyElementsIn( List.of( virginiaBeach, norfolk, richmond, portsmouth, suffolk ));
		}
		@Test
		public void testOneListenerSeveralOffices() {
			CensusOffice  a1 = new CensusOffice ( 1 );
			CensusOffice  a2 = new CensusOffice ( 3 );
			CensusOffice  a3 = new CensusOffice ( 7 );
			List<City>    actual;

			AtomicInteger c1 = new AtomicInteger();
			TopFiveCities b1 = new TopFiveCities() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c1.incrementAndGet();
				}
			};
			Truth.assertThat( a1.addObserver( b1 )).isTrue();
			Truth.assertThat( a2.addObserver( b1 )).isTrue();
			Truth.assertThat( a3.addObserver( b1 )).isTrue(); 
			a1.report( norfolk );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( norfolk ));
			Truth.assertThat( c1.get() ).isEqualTo( 1 );

			a2.report( richmond );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( norfolk, richmond ));
			Truth.assertThat( c1.get() ).isEqualTo( 2 );

			a3.report( chesapeake );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( norfolk, chesapeake, richmond ));
			Truth.assertThat( c1.get() ).isEqualTo( 3 );
		}
		@Test
		public void testSeveralListenersSeveralOffices() {
			CensusOffice  a1 = new CensusOffice( 1 );
			CensusOffice  a2 = new CensusOffice( 2 );
			CensusOffice  a3 = new CensusOffice( 3 );
			List<City>    actual;

			AtomicInteger c1 = new AtomicInteger();
			TopFiveCities b1 = new TopFiveCities() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c1.incrementAndGet();
				}
			};
			AtomicInteger c2 = new AtomicInteger();
			TopFiveCities b2 = new TopFiveCities() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c2.incrementAndGet();
				}
			};
			AtomicInteger c3 = new AtomicInteger();
			TopFiveCities b3 = new TopFiveCities() {
				@Override
				public void update(Observable observable) {
					super.update( observable );
					c3.incrementAndGet();
				}
			};
			Truth.assertThat( a1.addObserver( b1 )).isTrue();
			Truth.assertThat( a2.addObserver( b2 )).isTrue();
			Truth.assertThat( a3.addObserver( b1 )).isTrue();
			Truth.assertThat( a3.addObserver( b3 )).isTrue();

			a1.report( portsmouth );
			Truth.assertThat( c1.get() ).isEqualTo( 1 );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( portsmouth ));
			Truth.assertThat( c2.get() ).isEqualTo( 0 );
			actual = b2.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of() );
			Truth.assertThat( c3.get() ).isEqualTo( 0 );
			actual = b3.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of() );

			a2.report( suffolk );
			Truth.assertThat( c1.get() ).isEqualTo( 1 );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( portsmouth ));
			Truth.assertThat( c2.get() ).isEqualTo( 1 );
			actual = b2.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( suffolk ));
			Truth.assertThat( c3.get() ).isEqualTo( 0 );
			actual = b3.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of());

			a3.report( hampton );
			Truth.assertThat( c1.get() ).isEqualTo( 2 );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( hampton, portsmouth ));
			Truth.assertThat( c2.get() ).isEqualTo( 1 );
			actual = b2.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( suffolk ));
			Truth.assertThat( c3.get() ).isEqualTo( 1 );
			actual = b3.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( hampton ));

			a3.report( roanoke );
			Truth.assertThat( c1.get() ).isEqualTo( 3 );
			actual = b1.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( hampton, roanoke, portsmouth ));
			Truth.assertThat( c2.get() ).isEqualTo( 1 );
			actual = b2.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( suffolk ));
			Truth.assertThat( c3.get() ).isEqualTo( 2 );
			actual = b3.getTopFive();
			Truth.assertThat( actual   ).isNotNull();
			Truth.assertThat( actual   ).containsExactlyElementsIn( List.of( hampton, roanoke ));
		}
	}
}
