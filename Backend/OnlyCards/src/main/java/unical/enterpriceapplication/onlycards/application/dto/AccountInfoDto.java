package unical.enterpriceapplication.onlycards.application.dto;

import java.util.UUID;
import java.util.List;

import lombok.Data;

@Data
public class AccountInfoDto {
    private UUID id;
    private List<RoleDto> roles;


}
