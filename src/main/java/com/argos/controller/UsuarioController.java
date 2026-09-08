package com.argos.controller;

import com.argos.dtos.UsuarioRequest;
import com.argos.entity.Usuario;
import com.argos.service.UsuarioService;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/criar-usuario")
    public void criarUsuario(@RequestBody UsuarioRequest request) {
        usuarioService.criarUsuario(request);
    }

    // criar usuario
    //
}
