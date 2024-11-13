package unical.enterpriceapplication.onlycards.application.dto;

import java.util.UUID;

import com.google.firebase.database.annotations.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductIdDto {
    @NotNull
    private UUID id;
}
