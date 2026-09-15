package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.CadastroUsuarioDTO;
import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.docflow.backend.repository.EmpresaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroUsuarioDTO dto) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException("E-mail já cadastrado.");
        }
        Empresa empresa = new Empresa();

        empresa.setNome("Empresa de " + dto.getNome());
        empresa.setDataCadastro(LocalDateTime.now());
        empresa.setAtivo(true);

        empresa = empresaRepository.save(empresa);

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setDataCadastro(LocalDateTime.now());
        usuario.setAtivo(true);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        return usuarioRepository.save(usuario);
    }
}