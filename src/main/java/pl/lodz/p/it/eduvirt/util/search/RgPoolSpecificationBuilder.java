package pl.lodz.p.it.eduvirt.util.search;

import org.springframework.data.jpa.domain.Specification;
import pl.lodz.p.it.eduvirt.dto.search.SearchDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;

import java.util.ArrayList;
import java.util.List;

public class RgPoolSpecificationBuilder {
    private final List<SearchDto> params;

    public RgPoolSpecificationBuilder() {
        this.params = new ArrayList<>();
    }

    public final RgPoolSpecificationBuilder with(String key, String operation, Object value) {
        this.params.add(new SearchDto(key, operation, value));
        return this;
    }

    public final RgPoolSpecificationBuilder with(SearchDto search) {
        this.params.add(search);
        return this;
    }

    public Specification<ResourceGroupPool> build() {
        if (params.isEmpty()) {
            return null;
        }

        Specification<ResourceGroupPool> result = new ResourceGroupPoolSpecification(params.get(0));
        for (var param : params) {
            result = SearchOperation.getDataOption(param.getDataOption()) == SearchOperation.ALL ?
                    Specification.where(result).and(new ResourceGroupPoolSpecification(param)) :
                    Specification.where(result).or(new ResourceGroupPoolSpecification(param));
        }

        return result;
    }
}
