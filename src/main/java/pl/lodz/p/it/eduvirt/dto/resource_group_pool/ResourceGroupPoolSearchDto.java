package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.dto.search.SearchDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceGroupPoolSearchDto {
    private int page;
    private int size;
    private List<SearchDto> searchDtoList;
    private String dataOption;
}
