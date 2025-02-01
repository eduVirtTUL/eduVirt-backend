package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;
import pl.lodz.p.it.eduvirt.exceptions.VlansRangeConflictingRangeException;
import pl.lodz.p.it.eduvirt.exceptions.VlansRangeInvalidDefinitionException;
import pl.lodz.p.it.eduvirt.exceptions.VlansRangeNotFoundException;
import pl.lodz.p.it.eduvirt.repository.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.service.impl.VlansRangeServiceImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class VlansRangeServiceTest {

    @Mock
    private VlansRangeRepository vlansRangeRepository;

    @InjectMocks
    private VlansRangeServiceImpl vlansRangeService;

    @Test
    void Given_VlansRangesExist_When_GetAll_Then_ReturnAllVlansRanges() {
        VlansRange vlansRange1 = new VlansRange(10, 20);
        VlansRange vlansRange2 = new VlansRange(30, 40);

        List<VlansRange> vlansRangesUnsorted = List.of(
                vlansRange2,
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangesUnsorted);

        List<VlansRange> vlansRangesSorted = List.of(
                vlansRange1,
                vlansRange2
        );

        when(vlansRangeRepository.findAll(any(Sort.class)))
                .thenReturn(vlansRangesSorted);

        var result1 = vlansRangeService.getVlansRanges(false);
        assertEquals(vlansRangesUnsorted, result1);
        assertEquals(2, result1.size());
        assertEquals(vlansRange2, result1.getFirst());

        var result2 = vlansRangeService.getVlansRanges(true);
        assertEquals(vlansRangesSorted, result2);
        assertEquals(2, result2.size());
        assertEquals(vlansRange1, result2.getFirst());
    }

    @Test
    void Given_VlansRangesNotExist_When_GetAll_Then_ReturnEmptyList() {
        List<VlansRange> vlansRangesUnsorted = List.of();

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangesUnsorted);

        List<VlansRange> vlansRangesSorted = List.of();

        when(vlansRangeRepository.findAll(any(Sort.class)))
                .thenReturn(vlansRangesSorted);

        var result1 = vlansRangeService.getVlansRanges(false);
        assertEquals(vlansRangesUnsorted, result1);
        assertTrue(result1.isEmpty());

        var result2 = vlansRangeService.getVlansRanges(true);
        assertEquals(vlansRangesSorted, result2);
        assertTrue(result2.isEmpty());
    }

    @Test
    void Given_VlansRangeExists_When_GetById_Then_ReturnVlansRange() {
        UUID id = UUID.randomUUID();

        VlansRange vlansRange = new VlansRange();
        setEntityId(vlansRange, id);

        when(vlansRangeRepository.findById(id))
                .thenReturn(Optional.of(vlansRange));

        assertEquals(vlansRange, vlansRangeService.getVlansRange(id));
    }

    @Test
    void Given_NoVlansRangeExists_When_GetById_Then_ThrowException() {
        UUID id = UUID.randomUUID();

        VlansRange vlansRange = new VlansRange();
        setEntityId(vlansRange, id);

        when(vlansRangeRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(VlansRangeNotFoundException.class, () -> vlansRangeService.getVlansRange(id));
    }

    @Test
    void Given_VlansRangeExists_When_Remove_Then_Success() {
        when(vlansRangeRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(new VlansRange()));

        doNothing().when(vlansRangeRepository).deleteById(any(UUID.class));

        vlansRangeService.removeVlansRange(UUID.randomUUID());

        verify(vlansRangeRepository, times(1)).deleteById(any(UUID.class));
    }

    @Test
    void Given_NoVlansRangeExists_When_Remove_Then_ThrowException() {
        when(vlansRangeRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(VlansRangeNotFoundException.class, () -> vlansRangeService.removeVlansRange(UUID.randomUUID()));

        verify(vlansRangeRepository, times(0)).deleteById(any(UUID.class));
    }

    @Test
    void Given_ValidDataAndListEmpty_When_AddVlansRange_Then_Success() {
        VlansRange newVlansRange = new VlansRange(10, 20);

        when(vlansRangeService.getVlansRanges(false))
                .thenReturn(List.of());

        when(vlansRangeRepository.saveAndFlush(newVlansRange))
                .thenReturn(newVlansRange);

        assertEquals(newVlansRange, vlansRangeService.addVlansRange(newVlansRange));
    }

    @Test
    void Given_ValidDataAndListNotEmpty_When_AddVlansRange_Then_Success() {
        VlansRange newVlansRange = new VlansRange(10, 20);

        VlansRange vlansRange1 = new VlansRange(0, 5);
        VlansRange vlansRange2 = new VlansRange(100, 200);

        when(vlansRangeService.getVlansRanges(false))
                .thenReturn(List.of(vlansRange1, vlansRange2));

        when(vlansRangeRepository.saveAndFlush(newVlansRange))
                .thenReturn(newVlansRange);

        assertEquals(newVlansRange, vlansRangeService.addVlansRange(newVlansRange));
    }

    @Test
    void Given_InvalidData_When_AddVlansRange_Then_ThrowException() {
        // Case 1
        assertThrows(VlansRangeInvalidDefinitionException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(-10, 20)));

        // Case 2
        assertThrows(VlansRangeInvalidDefinitionException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(null, 20)));

        // Case 3
        assertThrows(VlansRangeInvalidDefinitionException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(10, null)));

        // Case 4
        assertThrows(VlansRangeInvalidDefinitionException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(10, -20)));

        // Case 5
        assertThrows(VlansRangeInvalidDefinitionException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(20, 10)));
    }

    @Test
    void Given_ConflictingOtherRangeData_When_AddVlansRange_Then_ThrowException() {
        VlansRange vlansRange1 = new VlansRange(10, 20);
        VlansRange vlansRange2 = new VlansRange(100, 200);

        when(vlansRangeService.getVlansRanges(false))
                .thenReturn(List.of(vlansRange1, vlansRange2));

        // Case 1
        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(5, 10)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(5, 15)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(5, 20)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(5, 25)));

        // Case 2
        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(10, 15)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(10, 20)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(10, 25)));

        // Case 3
        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(15, 18)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(15, 20)));

        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(15, 25)));

        // Case 4
        assertThrows(VlansRangeConflictingRangeException.class,
                () -> vlansRangeService.addVlansRange(new VlansRange(20, 25)));
    }

    /* Utils */

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}
