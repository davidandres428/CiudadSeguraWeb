package com.ciudadsegura.dto;

import com.ciudadsegura.model.enums.TipoPqrs;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PqrsRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 254, message = "El correo electrónico no puede superar 254 caracteres")
        String email,

        @NotNull(message = "El tipo de solicitud es obligatorio")
        TipoPqrs tipo,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 160, message = "El asunto no puede superar 160 caracteres")
        String asunto,

        @NotBlank(message = "El detalle es obligatorio")
        @Size(max = 4000, message = "El detalle no puede superar 4000 caracteres")
        String detalle
) {
}
