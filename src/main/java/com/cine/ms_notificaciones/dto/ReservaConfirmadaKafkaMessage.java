package com.cine.ms_notificaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaConfirmadaKafkaMessage {

    private Long reservaId;
    private String email;
    private Long funcionId;
    private List<String> asientos;
    private Double total;
    private String fecha;

}
