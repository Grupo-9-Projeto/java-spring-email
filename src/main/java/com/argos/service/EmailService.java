package com.argos.service;

import com.argos.entity.Usuario;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Service
    public class EmailService {
        private Usuario usuario;
        private String host = "smtp.gmail.com";
        final String user = "argos.techn@gmail.com";
        final String password = "edmfguhzlwdqsqku";


        public void enviarEmail(Usuario usuario, String token) {
            // montar o email aqui com os dados do usuario

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });

            try {
                MimeMessage message = new MimeMessage(session);
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//                helper.setTo();

                helper.setFrom(new InternetAddress(user, "Argos"));
                helper.setTo(usuario.getEmail());
                helper.setSubject("Código de verificação de cadastro - Argos");
                helper.setText(emailTemplate(usuario.getToken()), true);

//                message.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(usuario.getEmail()));
//                message.setSubject("Código de verificação de cadastro - Argos");

                Transport.send(message);
                System.out.println("Sucesso!");
//
//                Transport.send(message);
//                System.out.println("Email sent successfully!");
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public String emailTemplate(String token) {
            String template = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #f4f6f9;
                    margin: 0;
                    padding: 0;
                }

                .email-wrapper {
                    max-width: 600px;
                    margin: 40px auto;
                    background-color: #ffffff;
                    border-radius: 12px;
                    overflow: hidden;
                    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
                }

                .email-header {
                    background: linear-gradient(
                        180deg,
                        rgba(40, 32, 77, 1) 34%,
                        rgba(53, 38, 130, 1) 65%,
                        rgba(182, 197, 227, 0.42) 100%
                    );
                    padding: 28px 24px;
                    text-align: center;
                }

                .email-header img {
                    max-height: 60px;
                    width: auto;
                    display: block;
                    margin: 0 auto;
                }

                .email-body {
                    padding: 36px 28px;
                    color: #374151;
                    line-height: 1.6;
                }

                .email-body h2 {
                    color: #111827;
                    font-size: 20px;
                    margin-top: 0;
                    margin-bottom: 16px;
                }

                .token-box {
                    background-color: #f8fafc;
                    border: 2px dashed #28204D;
                    border-radius: 10px;
                    padding: 20px;
                    text-align: center;
                    margin: 28px 0;
                }

                .token-label {
                    font-size: 12px;
                    text-transform: uppercase;
                    letter-spacing: 1.5px;
                    color: #6b7280;
                    margin-bottom: 8px;
                    font-weight: 600;
                }

                .token-text {
                    font-size: 32px;
                    font-weight: 700;
                    color: #28204D;
                    letter-spacing: 6px;
                    margin: 0;
                }

                .email-footer {
                    background-color: #f9fafb;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #9ca3af;
                    border-top: 1px solid #e5e7eb;
                }
            </style>
        </head>

        <body>
            <div class="email-wrapper">

                <div class="email-header">
                    <img
                        src="https://raw.githubusercontent.com/Grupo-9-Projeto/site-institucional/main/src/assets/logos/Argos-logo_branco.png"
                        alt="Logo Argos"
                    >
                </div>

                <div class="email-body">
                    <h2>Olá!</h2>

                    <p>
                        Um administrador cadastrou o seu e-mail no
                        <strong>Sistema Argos</strong>.
                        Para realizar o seu cadastro e liberar o seu acesso
                        à plataforma, utilize o token de verificação abaixo:
                    </p>

                    <div class="token-box">
                        <div class="token-label">
                            Token de verificação
                        </div>

                        <p class="token-text">
                            ${tokenAcesso}
                        </p>
                    </div>

                    <p>
                        Basta copiar este código e colá-lo na tela de cadastro
                        do sistema.
                    </p>

                    <p style="font-size: 13px; color: #6b7280; margin-top: 24px;">
                        Se você não solicitou este acesso, pode ignorar esta
                        mensagem com segurança.
                    </p>
                </div>

                <div class="email-footer">
                    <p>&copy; 2026 Sistema Argos. Todos os direitos reservados.</p>
                </div>

            </div>
        </body>
        </html>
        """;

            template = template.replace("${tokenAcesso}", token);
            return template;
        }
    }

