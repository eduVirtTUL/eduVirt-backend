package pl.lodz.p.it.eduvirt.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {
    private String filterKey;
    private Object value;
    private String operation;
    private String dataOption;

    public SearchDto(String filterKey, String operation, Object value) {
        super();
        this.filterKey = filterKey;
        this.value = value;
        this.operation = operation;
    }
}
