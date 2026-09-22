package com.acolher.psicologo.autenticacao.servico;

import com.acolher.psicologo.autenticacao.dto.NovoUsuario;
import com.acolher.psicologo.shared.exception.EmailJaCadastradoException;
import com.acolher.psicologo.usuario.Usuario;
import com.acolher.psicologo.usuario.UsuarioRepository;
import com.acolher.psicologo.usuario.dto.DadosUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CadastroService {

    private final UsuarioRepository repository;
    private final PasswordEncoder codificadorDeSenha;
    private final VerificacaoDeEmailService verificacao;

    @Transactional
    public DadosUsuario cadastrar(NovoUsuario dados) {
        String email = normalizar(dados.email());

        if (repository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = repository.save(Usuario.novo(
                dados.nomeCompleto().trim(),
                email,
                codificadorDeSenha.encode(dados.senha()),
                dados.tipo(),
                dados.desejaAnonimato()));

        verificacao.enviarPara(usuario);

        return DadosUsuario.de(usuario);
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
