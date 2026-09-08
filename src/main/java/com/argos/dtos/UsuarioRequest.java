package com.argos.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {
//    private Long id;
    private String cargo;
    private String email;
    private Long gestorId;
    private Long empresaId;

}