package pl.lodz.p.it.eduvirt.util.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceAccess {
    private EduVirtDev eduVirtDev;
    private Account account;
}
