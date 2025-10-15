package com.store.management.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponseDto(HttpStatus errorCode, String errorMessage, LocalDateTime errorTime) {
}
