package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseSyncRequestDTO {
    private String idToken;
    private String name;
    private String email;
    private String guestSessionId;
}

