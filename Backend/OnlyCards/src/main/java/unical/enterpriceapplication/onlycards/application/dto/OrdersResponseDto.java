package unical.enterpriceapplication.onlycards.application.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class OrdersResponseDto {
    Page<OrdersDto> ordersDto;
    int totalNumber;
}
