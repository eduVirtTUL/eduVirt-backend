package pl.lodz.p.it.eduvirt.util.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RealmAccess {
    private List<String> roles = new ArrayList<>();
}
