
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    public Usuario obterUsuario() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || !(autenticacao.getPrincipal() instanceof Usuario usuario)) {
            throw new UsuarioNaoAutenticadoException(
                    "Usuário não autenticado"
            );
        }

        return usuario;
    }
}
