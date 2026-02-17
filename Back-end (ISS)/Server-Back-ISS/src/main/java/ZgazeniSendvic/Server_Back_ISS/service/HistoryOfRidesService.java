package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.exception.AccountNotFoundException;
import ZgazeniSendvic.Server_Back_ISS.exception.RideNotFoundException;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideDriverRatingRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideNoteRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class HistoryOfRidesService {

    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RideDriverRatingRepository rideDriverRatingRepository;
    @Autowired
    private RideNoteRepository rideNoteRepository;

    private static final Set<String> ADMIN_ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "startTime",
            "endTime",
            "status",
            "canceler",
            "price",
            "panic",
            "creationDate",
            "startLatitude",    // for sorting by beginning location
            "endLatitude"       // for sorting by ending location

    );

    private static final Set<String> USER_ALLOWED_SORT_FIELDS = Set.of(
            "startTime",
            "endTime",
            "creationDate",
            "startLatitude",    // for sorting by beginning location
            "endLatitude"       // for sorting by ending location
    );

    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public HistoryOfRidesDTO getHistoryOfRides(Long userId) {
        List<Ride> all = rideRepository.findAll();
        List<PastRideDTO> past = new ArrayList<>();
        for (Ride r : all) {
            if (r.getStatus().equals(RideStatus.FINISHED) && r.getDriver().getId() != null && r.getDriver().getId().equals(userId)) {
                String dep = r.getStartTime() == null ? null : r.getStartTime().toString();
                String arr = r.getEndTime() == null ? null : r.getEndTime().toString();
                double price =  r.getPrice();
                Location origin = null;
                Location dest = null;
//                if (r.getLocations().get(0) == null) {
//                    origin = new Location( 45.239576, 19.822779);
//                    dest = new Location(45.254582, 19.842490);
//                }else{
//                    origin = r.getLocations().get(0);
//                    dest = r.getLocations().get(r.getLocations().size() - 1);
//                }
                try{
                    origin = r.getLocations().get(0);
                    dest = r.getLocations().get(r.getLocations().size() - 1);
                }catch(IndexOutOfBoundsException e){
                    origin = new Location( 45.239576, 19.822779);
                    dest = new Location(45.254582, 19.842490);
                }
                PastRideDTO p = new PastRideDTO(r.getId(), origin, dest, dep, arr,
                        r.isPanic(), String.valueOf(r.isCanceled()), price);
                past.add(p);
            }
        }
        return new HistoryOfRidesDTO(past);
    }



    public Page<ARideRequestedDTO> getAllRidesOfAccount(Long accountId, Pageable pageable,
                                                        LocalDateTime fromDate, LocalDateTime toDate) {

        // Fetch account or throw if not found
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Validate sort fields
        validateSortFields(pageable, ADMIN_ALLOWED_SORT_FIELDS);
        System.out.println("=== DEBUGGING ===");
        System.out.println("fromDate parameter: " + fromDate);
        System.out.println("toDate parameter: " + toDate);


        Page<Ride> ridePage = rideRepository.findByAccountAndDateRange(account, fromDate, toDate, pageable);

        System.out.println("Number of results: " + ridePage.getTotalElements());
        ridePage.getContent().forEach(ride -> {
            System.out.println("Ride ID: " + ride.getId() +
                    ", creationDate: " + ride.getCreationDate());
        });

        // Convert Page<Ride> -> Page<ARideRequestedDTO> using mapper
        return ridesToARideRequestedDTOPage(ridePage);
    }

    public static void validateSortFields(Pageable pageable, Set<String> allowedFields) {
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                if (!allowedFields.contains(order.getProperty())) {
                    throw new IllegalArgumentException(
                            "Invalid sort field: " + order.getProperty() +
                                    ". Allowed fields are: " + allowedFields
                    );
                }
            }
        }
    }

    public Page<ARideRequestedDTO> ridesToARideRequestedDTOPage(Page<Ride> rides) {
        return rides.map(r -> {
            ARideRequestedDTO dto = new ARideRequestedDTO();

            dto.setRideID(r.getId());
            dto.setPanic(r.isPanic());
            dto.setDestinations(r.getLocations());

            // Safe checks for locations
            if (!r.getLocations().isEmpty()) {
                dto.setArrivingPoint(r.getLocations().get(0));
                dto.setEndingPoint(r.getLocations().get(r.getLocations().size() - 1));
            }

            dto.setBeginning(r.getStartTime());
            dto.setEnding(r.getEndTime());
            dto.setPrice(r.getPrice());
            dto.setStatus(r.getStatus());
            dto.setCreationTime(r.getCreationDate());

            // Handle possible null driver
            Account canceler = r.getCanceler();
            dto.setWhoCancelled(canceler != null ? canceler.getId() : null);

            return dto;
        });
    }


    public Page<ARideRequestedUserDTO> getAllRidesOfAccountUser(Pageable pageable,
                                                            LocalDateTime fromDate, LocalDateTime toDate){
        //gets account from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account account = userDetails.getAccount();

        // Throw error if account not found
        if (account == null) {
            throw new AccountNotFoundException(null);
        }

        // Validate sort fields before querying
        validateSortFields(pageable, USER_ALLOWED_SORT_FIELDS);

        // Call the existing admin function with the account ID
        Page<ARideRequestedDTO> adminDTOs = getAllRidesOfAccount(account.getId(), pageable, fromDate, toDate);

        // Convert Page<ARideRequestedDTO> to Page<ARideRequestedUserDTO>
        return adminDTOs.map(dto -> {
            ARideRequestedUserDTO userDTO = new ARideRequestedUserDTO();
            userDTO.setRideID(dto.getRideID());
            userDTO.setDestinations(dto.getDestinations());
            userDTO.setBeginning(dto.getBeginning());
            userDTO.setEnding(dto.getEnding());
            userDTO.setCreationTime(dto.getCreationTime());
            return userDTO;
        });
    }

    public ARideDetailsRequestedDTO getRideDetailsForAdmin(Long rideId) {
        // Fetch the ride by ID
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride with ID " + rideId + " not found"));

        // Find all relevant info
        List<RideDriverRating> ratings = rideDriverRatingRepository.findByRideId(rideId);
        List<RideNote> notes = rideNoteRepository.findByRideId(rideId);

       ARideDetailsRequestedDTO dto = new ARideDetailsRequestedDTO();
        dto.setPassengers(new ArrayList<>());
        dto.setRideDriverRatings(new ArrayList<>());
        dto.setRideNotes(new ArrayList<>());



        for(Account account: ride.getPassengers()){
            HORAccountDetailsDTO accountDTO = new HORAccountDetailsDTO();
            accountDTO.setAccountId(account.getId());
            accountDTO.setEmail(account.getEmail());
            accountDTO.setFirstName(account.getName());
            accountDTO.setLastName(account.getLastName());
            dto.getPassengers().add(accountDTO);
        }

        if(ride.getDriver() != null){
            HORAccountDetailsDTO driverDTO = new HORAccountDetailsDTO();
            driverDTO.setAccountId(ride.getDriver().getId());
            driverDTO.setEmail(ride.getDriver().getEmail());
            driverDTO.setFirstName(ride.getDriver().getName());
            driverDTO.setLastName(ride.getDriver().getLastName());
            dto.setDriver(driverDTO);
        }

        for(RideDriverRating rating : ratings){
            RideDriverRatingDTO ratingDTO = new RideDriverRatingDTO();
            ratingDTO.setRideId(ride.getId());
            ratingDTO.setDriverRating(rating.getDriverRating());
            ratingDTO.setComment(rating.getComment());
            ratingDTO.setVehicleRating(rating.getVehicleRating());
            ratingDTO.setUserId(rating.getUserId());
            dto.getRideDriverRatings().add(ratingDTO);
        }

        for(RideNote note : notes){
            ARideDetailsNoteDTO noteDTO = new ARideDetailsNoteDTO();
            noteDTO.setNoteId(note.getId());
            noteDTO.setUserId(note.getUserId());
            noteDTO.setNote(note.getNote());

            dto.getRideNotes().add(noteDTO);
            // WIll put userId here, despite the context


        }

        return dto;


    }

    public URideDetailsRequestedDTO getRideDetailsForUser(Long rideId){
        // Fetch the ride by ID
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride with ID " + rideId + " not found"));

        //based on security context, check if user is passenger, if not throw error
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account account = userDetails.getAccount();
        if(ride.getPassengers().stream().noneMatch(p -> p.getId().equals(account.getId()))){
            throw new RideNotFoundException("Ride with ID " + rideId + " not found for this user");
        }

        URideDetailsRequestedDTO dto = new URideDetailsRequestedDTO();

        ARideDetailsRequestedDTO adminDTO = getRideDetailsForAdmin(rideId);

        dto.setDriver(adminDTO.getDriver());
        dto.setRideDriverRatings(adminDTO.getRideDriverRatings());
        dto.setRideNotes(adminDTO.getRideNotes());

        return dto;

    }

}


