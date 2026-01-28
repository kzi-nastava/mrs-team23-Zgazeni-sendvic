package ZgazeniSendvic.Server_Back_ISS.service;
import ZgazeniSendvic.Server_Back_ISS.repository.VehiclePositionsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ZgazeniSendvic.Server_Back_ISS.dto.VehiclePositionDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.VehiclePositionsDTO;
import ZgazeniSendvic.Server_Back_ISS.model.VehiclePosition;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class VehiclePositionsService {
    private final VehiclePositionsRepository repository;
    public VehiclePositionsService(VehiclePositionsRepository repository) {
        this.repository = repository;
    }
    @Transactional(readOnly = true)
    public VehiclePositionsDTO getAllVehiclePositions() {
        List<VehiclePositionDTO> dtos = repository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new VehiclePositionsDTO(dtos);
    }
    @Transactional(readOnly = true)
    public VehiclePositionDTO findById(Long id) {
        VehiclePosition entity = findVehicleById(id);
        return toDto(entity);
    }
    private VehiclePosition findVehicleById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle position not found with id: " + id));
    }
    @Transactional
    public VehiclePositionDTO save(VehiclePositionDTO dto) {
        VehiclePosition entity = toEntity(dto);
        VehiclePosition saved = repository.save(entity);
        return toDto(saved);
    }
    private VehiclePositionDTO toDto(VehiclePosition e) {
        return new VehiclePositionDTO(e.getId(), e.getVehicleId(), e.getLatitude(), e.getLongitude(), e.getStatus());
    }
    private VehiclePosition toEntity(VehiclePositionDTO dto) {
        VehiclePosition e = new VehiclePosition();
        e.setId(dto.getId());
        e.setVehicleId(dto.getVehicleId());
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        e.setStatus(dto.getStatus());
        return e;
    }
}
