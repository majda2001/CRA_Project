package com.example.crabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CraConfigurationDto {
    private int windowStartDay;
    private int windowEndDay;
}
