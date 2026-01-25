package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrsRouteRequest {

    private List<List<Double>> coordinates;

}
