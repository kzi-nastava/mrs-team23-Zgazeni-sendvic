package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.Date;
import java.util.Map;

public class ARidesRequestDTO {
    private Date filterBeginning;
    private Date filterEnding;
    private Map<String, Integer> sortParams;

    public ARidesRequestDTO(){}
    public ARidesRequestDTO(Date filterBeginning,
                            Date filterEnding, Map<String, Integer> sortParams) {

        this.filterBeginning = filterBeginning;
        this.filterEnding = filterEnding;
        this.sortParams = sortParams;
    }


    public Date getFilterBeginning() {
        return filterBeginning;
    }

    public void setFilterBeginning(Date filterBeginning) {
        this.filterBeginning = filterBeginning;
    }

    public Date getFilterEnding() {
        return filterEnding;
    }

    public void setFilterEnding(Date filterEnding) {
        this.filterEnding = filterEnding;
    }

    public Map<String, Integer> getSortParams() {
        return sortParams;
    }

    public void setSortParams(Map<String, Integer> sortParams) {
        this.sortParams = sortParams;
    }
}
