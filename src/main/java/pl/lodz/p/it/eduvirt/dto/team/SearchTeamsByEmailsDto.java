package pl.lodz.p.it.eduvirt.dto.team;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Value;

import java.util.List;

@Value
public class SearchTeamsByEmailsDto {
    @NotEmpty(message = "teams.validation.emails.empty")
    List<String> emailPrefixes;
    
    @Pattern(regexp = "^(ASC|DESC)$", message = "teams.validation.sort.invalid")
    String sort = "ASC";
}