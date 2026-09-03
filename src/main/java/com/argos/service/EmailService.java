package com.argos.service;

import com.argos.entity.Usuario;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private Usuario usuario;

    public void enviarEmail(Usuario usuario, String token) {
        // montar o email aqui com os dados do usuario
    }
}
