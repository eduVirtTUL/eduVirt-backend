package pl.lodz.p.it.eduvirt.util.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    private int exp;
    private int iat;
    @JsonProperty("auth_time")
    private int authTime;
    private String jti;
    private String iss;
    private String aud;
    private String sub;
    private String typ;
    private String azp;
    @JsonProperty("session_state")
    private String sessionState;
    private String acr;
    @JsonProperty("realm_access")
    private RealmAccess realmAccess;
    @JsonProperty("resource_access")
    private ResourceAccess resourceAccess;
    private String scope;
    private String sid;
    @JsonProperty("email_verified")
    private boolean emailVerified;
    private String name;
    private List<String> groups = new ArrayList<>();
    @JsonProperty("preferred_username")
    private String preferredUsername;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    private String email;
}
