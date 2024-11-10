package pl.lodz.p.it.eduvirt.util;

import lombok.NonNull;

import java.util.Collection;
import java.util.LinkedList;

public class PaginationUtil {

    public static <TYPE> Collection<TYPE> getPaginatedCollection(int pageNumber, int pageSize,
                                                                 @NonNull Collection<TYPE> collection) {
        Collection<TYPE> result = new LinkedList<>();
        int index = 0;
        for (TYPE element : collection) {
            if (index >= pageNumber * pageSize && index < (pageNumber + 1) * pageSize) {
                result.add(element);
            }
            index++;
        }

        return result;
    }
}
