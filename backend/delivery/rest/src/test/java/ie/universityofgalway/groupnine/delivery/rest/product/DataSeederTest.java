package ie.universityofgalway.groupnine.delivery.rest.product;

// Import testing libraries.
import ie.universityofgalway.groupnine.infrastructure.product.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.ProductJpaRepository;
import org.junit.jupiter.api.Test; // The basic annotation to mark a method as a test.
import org.junit.jupiter.api.extension.ExtendWith; // Used to add extensions to JUnit 5.
import org.mockito.ArgumentCaptor; // A tool to "capture" arguments passed to mock methods.
import org.mockito.InjectMocks; // Injects mock fields into the object being tested.
import org.mockito.Mock; // Creates a mock (fake) object for a dependency.
import org.mockito.junit.jupiter.MockitoExtension; // Integrates Mockito with JUnit 5.

import java.util.List;

// AssertJ provides "fluent" assertions (e.g., assertThat(x).is...()), which are easy to read.
import static org.assertj.core.api.Assertions.assertThat; 
// Static imports for Mockito methods (like when(), verify()) to make tests cleaner.
import static org.mockito.Mockito.*;

/**
 * This annotation tells JUnit 5 to initialize Mockito.
 * It's what enables the @Mock and @InjectMocks annotations.
 */
@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    /**
     * @Mock creates a "mock" or "fake" implementation of the ProductJpaRepository.
     * We do this because we want to test the DataSeeder *in isolation*,
     * without needing to connect to a real database. We can control
     * exactly how this mock object behaves.
     */
    @Mock
    private ProductJpaRepository productRepository;

    /**
     * @InjectMocks creates an actual instance of the DataSeeder class.
     * It then finds any fields in DataSeeder that match the @Mock objects
     * (like our 'productRepository') and "injects" the mock into it.
     * This means our 'dataSeeder' object will use the fake repository.
     */
    @InjectMocks
    private DataSeeder dataSeeder;

    /**
     * @Test marks this method as a unit test that JUnit should run.
     * The test name clearly describes the scenario: what happens when
     * the run() method is called and the database is empty.
     */
    @Test
    void run_whenDatabaseIsEmpty_shouldSeedData() throws Exception {
        // ARRANGE ------------------------------------------------------------------
        // This is the "setup" phase of the test.

        // We "stub" the productRepository's count() method.
        // This tells Mockito: "WHEN the count() method is called,
        // THEN return the value 0L (Long)."
        when(productRepository.count()).thenReturn(0L);

        // An ArgumentCaptor is a tool to "catch" the arguments that are
        // passed to a mock's method. We want to catch the List<ProductEntity>
        // that gets passed to the saveAll() method, so we can check if it's correct.
        ArgumentCaptor<List<ProductEntity>> captor = ArgumentCaptor.forClass(List.class);

        // ACT ----------------------------------------------------------------------
        // This is the "execution" phase. We call the actual method we want to test.
        dataSeeder.run(); // The DataSeeder's run() method is called.

        // ASSERT -------------------------------------------------------------------
        // This is the "verification" phase. We check if everything behaved as expected.

        // We verify that the saveAll() method on our mock repository was
        // called exactly 1 time.
        // The captor.capture() inside tells Mockito to "save" the argument
        // that was passed to saveAll() so we can inspect it.
        verify(productRepository, times(1)).saveAll(captor.capture());

        // Now, we get the list that was "captured" from the saveAll() call.
        List<ProductEntity> savedProducts = captor.getValue();

        // Finally, we use AssertJ to make claims about the captured list.
        // We assert that the list is not null and has exactly 3 items.
        assertThat(savedProducts).hasSize(3);
        // We can even check the content of the items to be extra sure.
        assertThat(savedProducts.get(0).getName()).isEqualTo("Classic Espresso");
        assertThat(savedProducts.get(1).getName()).isEqualTo("Creamy Latte");
        assertThat(savedProducts.get(2).getName()).isEqualTo("Butter Croissant");
    }

    /**
     * This test covers the "else" branch of the if-statement in DataSeeder.
     * It checks what happens if the database is *not* empty.
     */
    @Test
    void run_whenDatabaseIsNotEmpty_shouldSkipSeeding() throws Exception {
        // ARRANGE ------------------------------------------------------------------
        // We stub the count() method again, but this time we return a
        // non-zero number (5L) to simulate a database that already has data.
        when(productRepository.count()).thenReturn(5L);

        // ACT ----------------------------------------------------------------------
        // We execute the same method as before.
        dataSeeder.run();

        // ASSERT -------------------------------------------------------------------
        // We verify that the saveAll() method was NEVER called.
        // This confirms that our 'if (productRepository.count() == 0)' check
        // worked correctly and skipped the data seeding.
        // 'anyList()' is a matcher that means "any list at all".
        verify(productRepository, never()).saveAll(anyList());
    }
}
