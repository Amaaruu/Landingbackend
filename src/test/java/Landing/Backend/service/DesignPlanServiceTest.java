package Landing.Backend.service;

import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.repository.DesignPlanRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DesignPlanService — CRUD de planes")
class DesignPlanServiceTest {

    @Mock
    private DesignPlanRepository planRepository;

    @InjectMocks
    private DesignPlanService designPlanService;

    private DesignPlan basicPlan;

    @BeforeEach
    void setUp() {
        basicPlan = new DesignPlan();
        basicPlan.setPlanId(1);
        basicPlan.setName("Básico");
        basicPlan.setDescription("Plan de entrada");
        basicPlan.setPrice(BigDecimal.valueOf(29.99));
        basicPlan.setActive(true);
    }

    @Test
    @DisplayName("getAllDesignPlans() retorna lista de planes")
    void shouldReturnAllPlans() {
        when(planRepository.findAll()).thenReturn(List.of(basicPlan));
        List<DesignPlan> result = designPlanService.getAllDesignPlans();

        assertThat(result).hasSize(1)
                .first()
                .extracting(DesignPlan::getName)
                .isEqualTo("Básico");
    }

    @Test
    @DisplayName("saveDesignPlan() persiste y retorna el plan guardado")
    void shouldSavePlan() {
        when(planRepository.save(basicPlan)).thenReturn(basicPlan);
        DesignPlan result = designPlanService.saveDesignPlan(basicPlan);
        assertThat(result.getPlanId()).isEqualTo(1);
        verify(planRepository).save(basicPlan);
    }

    @Test
    @DisplayName("updatePlan() actualiza nombre, descripción y precio correctamente")
    void shouldUpdatePlanFields() {
        DesignPlan updatedData = new DesignPlan();
        updatedData.setName("Premium");
        updatedData.setDescription("Plan avanzado");
        updatedData.setPrice(BigDecimal.valueOf(99.99));

        when(planRepository.findById(1)).thenReturn(Optional.of(basicPlan));
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DesignPlan result = designPlanService.updatePlan(1, updatedData);

        assertThat(result.getName()).isEqualTo("Premium");
        assertThat(result.getDescription()).isEqualTo("Plan avanzado");
        assertThat(result.getPrice()).isEqualByComparingTo("99.99");
    }

    @Test
    @DisplayName("updatePlan() lanza ResourceNotFoundException para ID inexistente")
    void shouldThrowWhenPlanToUpdateNotFound() {
        when(planRepository.findById(999)).thenReturn(Optional.empty());

        DesignPlan data = new DesignPlan();
        data.setName("Cualquier nombre");
        data.setDescription("desc");
        data.setPrice(BigDecimal.ONE);

        assertThatThrownBy(() -> designPlanService.updatePlan(999, data))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deletePlan() llama a repository.delete() con la entidad correcta")
    void shouldCallDeleteOnRepository() {
        when(planRepository.findById(1)).thenReturn(Optional.of(basicPlan));
        designPlanService.deletePlan(1);
        verify(planRepository).delete(basicPlan);
    }

    @Test
    @DisplayName("deletePlan() lanza ResourceNotFoundException si el plan no existe")
    void shouldThrowWhenDeletingNonExistentPlan() {
        when(planRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> designPlanService.deletePlan(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getPlanById() retorna Optional con el plan cuando existe")
    void shouldReturnPlanById() {
        when(planRepository.findById(1)).thenReturn(Optional.of(basicPlan));

        Optional<DesignPlan> result = designPlanService.getPlanById(1);

        assertThat(result).isPresent()
                .get()
                .extracting(DesignPlan::getName)
                .isEqualTo("Básico");
    }
}