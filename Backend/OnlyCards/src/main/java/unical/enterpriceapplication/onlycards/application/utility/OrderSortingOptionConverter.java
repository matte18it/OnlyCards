package unical.enterpriceapplication.onlycards.application.utility;
import org.springframework.core.convert.converter.Converter;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.dto.OrderSortingOptions;

@Component
@Slf4j
public class OrderSortingOptionConverter implements Converter<String, OrderSortingOptions> {

    @Override
    public OrderSortingOptions convert(String value)  {
        log.info("Converting value: {}", value);
        return OrderSortingOptions.fromValue(value);
    }

}
