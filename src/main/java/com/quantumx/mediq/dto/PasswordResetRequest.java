package com.quantumx.mediq.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String username;
    private String email;
}
