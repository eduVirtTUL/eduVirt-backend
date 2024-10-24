package pl.lodz.p.it.eduvirt.util;

import org.ovirt.engine.sdk4.services.SystemService;

public interface ConnectionWrapper {

    SystemService systemService();

    boolean isLink(Object object);

    <TYPE> TYPE followLink(TYPE object);

    String authenticate();

    boolean validate();

    void renewConnection();
}
