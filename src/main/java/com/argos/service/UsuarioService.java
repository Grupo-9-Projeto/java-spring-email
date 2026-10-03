package com.argos.service;

import com.argos.dtos.UsuarioRequest;
import com.argos.entity.Usuario;
import com.argos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    public Usuario saveUser(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void criarUsuario(UsuarioRequest usuarioRequest) {
        Usuario usuario = new Usuario();

        String token = gerarTokenAleatorio();

        usuario.setCargo("gerente");
        usuario.setEmail(usuarioRequest.getEmail());

        usuario.setFk_empresa_fornecedora(1L);
        usuario.setGestor_id(1L);

        usuario.setToken(token);

        usuarioRepository.save(usuario);
        emailService.enviarEmail(usuario, token);
    }

    public String gerarTokenAleatorio() {
        StringBuilder token = new StringBuilder();
        Random random = new Random();
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < 16; i++) {
            Integer index = random.nextInt(caracteres.length());
            token.append(caracteres.charAt(index));
        }

        return token.toString();
    }
}
