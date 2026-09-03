package com.argos.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {
    private Long id;
    private String nome;
    private String cargo;
    private String email;
//
//    // Getters e Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getNome() { return nome; }
//    public void setNome(String nome) { this.nome = nome; }
//
//    public String getCargo() { return cargo; }
//    public void setCargo(String cargo) { this.cargo = cargo; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
}