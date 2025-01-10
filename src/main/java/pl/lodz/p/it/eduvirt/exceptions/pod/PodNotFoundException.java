package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class PodNotFoundException extends NotFoundException {

    public PodNotFoundException(String message) {
        super(message, I18n.POD_NOT_FOUND);
    }
}
