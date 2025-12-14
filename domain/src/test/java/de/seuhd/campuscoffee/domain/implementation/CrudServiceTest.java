package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.configuration.ApprovalConfiguration;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import de.seuhd.campuscoffee.domain.ports.data.ReviewDataService;
import de.seuhd.campuscoffee.domain.ports.data.UserDataService;
import de.seuhd.campuscoffee.domain.tests.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static de.seuhd.campuscoffee.domain.tests.TestFixtures.getApprovalConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;


/**
 * Unit and integration tests for the operations related to POS (Point of Sale).
 */
@ExtendWith(MockitoExtension.class)
public class CrudServiceTest {
    private final ApprovalConfiguration approvalConfiguration = getApprovalConfiguration();

    @Mock
    private CrudDataService<Review, Long> crudDataService;

    @Mock
    private CrudServiceImpl<Review, Long> crudService;

    @Mock
    private ReviewDataService reviewDataService;

    @Mock
    private UserDataService userDataService;

    @BeforeEach
    void beforeEach() {
        crudService = new CrudServiceImpl<Review, Long>(Review.class){
            @Override
            protected CrudDataService<Review,Long> dataService(){
                return crudDataService;
            }
        };
    }

    @Test
    void testClearAllData(){
        crudService.clear();
        verify(crudDataService).clear();
    }

    @Test
    void testGetAllData() {
        crudService.getAll();
        verify(crudDataService).getAll();
    }

    @Test
    void testGetDataById() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst();
        assertNotNull(review.getId());

        when(crudDataService.getById(review.getId()))
                .thenReturn(review);

        // when
        Review review_after = crudService.getById(review.getId());

        // then
        assertThat(review_after).isEqualTo(review);
        verify(crudDataService).getById(review.getId());

    }

    @Test
    void testUpsertEntityIdNull() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst().toBuilder()
                .id(null)
                .build();

        when(crudDataService.upsert(review)).thenReturn(review);

        // when
        Review review_after = crudService.upsert(review);

        // then
        assertThat(review_after).isEqualTo(review);
        verify(crudDataService).upsert(review);
    }

    @Test
    void testUpsertEntityIdNotNull() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst();
        assertNotNull(review.getId());

        when(crudDataService.getById(review.getId())).thenReturn(review);
        when(crudDataService.upsert(review)).thenReturn(review);

        // when
        Review review_after = crudService.upsert(review);

        // then
        assertThat(review_after).isEqualTo(review);
        verify(crudDataService).getById(review.getId());
        verify(crudDataService).upsert(review);
    }


    @Test
    void upsertDuplicateEntity() {
        // given
        Review review = TestFixtures.getReviewFixtures().getFirst();

        when(crudDataService.upsert(review)).thenThrow(new DuplicationException(Review.class,"duplicate","2"));

        assertThrows(DuplicationException.class, () -> crudService.upsert(review));
        verify(crudDataService).upsert(review);
    }

    @Test
    void deleteEntity() {
        // given
        Long id = 1L;

        // when
        crudService.delete(id);
        verify(crudDataService).delete(id);
    }


}

