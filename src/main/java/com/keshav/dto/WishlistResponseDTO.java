package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponseDTO {

    private List<WishlistItemResponseDTO> items = new ArrayList<>();
    private List<Long> productIds = new ArrayList<>();
    private int totalCount;
}
