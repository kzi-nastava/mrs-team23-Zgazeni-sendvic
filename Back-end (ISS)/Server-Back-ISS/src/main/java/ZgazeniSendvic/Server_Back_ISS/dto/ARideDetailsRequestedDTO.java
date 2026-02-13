package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ARideDetailsRequestedDTO {

    private List<HORAccountDetailsDTO> passengers;
    private HORAccountDetailsDTO driver;
    private List<ARideDetailsNoteDTO> rideNotes;
    private List<RideDriverRatingDTO> rideDriverRatings;

}
