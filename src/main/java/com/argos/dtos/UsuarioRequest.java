package com.argos.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {
//    private Long id;
    private String email;
    private String cargo;
    private Long gestor_id;
}