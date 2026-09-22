package com.acolher.psicologo.shared.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Ja existe uma conta com este e-mail");
    }
}
