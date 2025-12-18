package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.configuration.ApprovalConfiguration;
import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CrudServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class CrudServiceTest {

    private final ApprovalConfiguration approvalConfiguration = getApprovalConfiguration();

    @Mock
    private CrudDataService<Review, Long> crudDataService;

    private CrudServiceImpl<Review, Long> crudService;

    @Mock
    private ReviewDataService reviewDataService;

    @Mock
    private UserDataService userDataService;

    @BeforeEach
    void beforeEach() {
        crudService = new CrudServiceImpl<Review, Long>(Review.class) {
            @Override
            protected CrudDataService<Review, Long> dataService() {
                return crudDataService;
            }
        };
    }

    @Test
    void testClearAllData() {
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
        Review review = TestFixtures.getReviewFixtures().getFirst();
        assertNotNull(review.getId());

        when(crudDataService.getById(review.getId()))
                .thenReturn(review);

        Review result = crudService.getById(review.getId());

        assertThat(result).isEqualTo(review);
        verify(crudDataService).getById(review.getId());
    }

    @Test
    void testGetDataByIdNotFound() {
        when(crudDataService.getById(102L))
                .thenThrow(new NotFoundException(Review.class, "id", "102"));

        assertThrows(NotFoundException.class,
                () -> crudService.getById(102L));

        verify(crudDataService).getById(102L);
    }

    @Test
    void testUpsertEntityIdNull() {
        Review review = TestFixtures.getReviewFixtures().getFirst().toBuilder()
                .id(null)
                .build();

        when(crudDataService.upsert(review)).thenReturn(review);

        Review result = crudService.upsert(review);

        assertThat(result).isEqualTo(review);
        verify(crudDataService).upsert(review);
        verify(crudDataService, never()).getById(any());
    }

    @Test
    void testUpsertEntityIdNotNull() {
        Review review = TestFixtures.getReviewFixtures().getFirst();
        assertNotNull(review.getId());

        when(crudDataService.getById(review.getId())).thenReturn(review);
        when(crudDataService.upsert(review)).thenReturn(review);

        Review result = crudService.upsert(review);

        assertThat(result).isEqualTo(review);
        verify(crudDataService).getById(review.getId());
        verify(crudDataService).upsert(review);
    }

    @Test
    void testUpsertUpdateEntityNotFound() {
        Review review = TestFixtures.getReviewFixtures().getFirst();
        assertNotNull(review.getId());

        when(crudDataService.getById(review.getId()))
                .thenThrow(new NotFoundException(Review.class, "id", review.getId().toString()));

        assertThrows(NotFoundException.class,
                () -> crudService.upsert(review));

        verify(crudDataService).getById(review.getId());
        verify(crudDataService, never()).upsert(any());
    }

    @Test
    void upsertDuplicateEntity() {
        Review review = TestFixtures.getReviewFixtures().getFirst();

        when(crudDataService.upsert(review))
                .thenThrow(new DuplicationException(Review.class, "duplicate", "2"));

        assertThrows(DuplicationException.class,
                () -> crudService.upsert(review));

        verify(crudDataService).upsert(review);
    }

    @Test
    void deleteEntity() {
        Long id = 1L;
        crudService.delete(id);
        verify(crudDataService).delete(id);
    }

    @Test
    void deleteEntityNotFound() {
        doThrow(new NotFoundException(Review.class, "id", "99"))
                .when(crudDataService).delete(102L);

        assertThrows(NotFoundException.class,
                () -> crudService.delete(102L));

        verify(crudDataService).delete(102L);
    }
}
