import { Link } from 'react-router-dom';
import { House, SearchX } from 'lucide-react';
import './PaginaNaoEncontrada.css';

function PaginaNaoEncontrada() {
  return (
    <main className="pagina-nao-encontrada">
      <div className="pagina-nao-encontrada__conteudo">
        <div className="pagina-nao-encontrada__icone">
          <SearchX size={40} strokeWidth={1.5} />
        </div>

        <span className="pagina-nao-encontrada__codigo">Erro 404</span>

        <h1>Página não encontrada</h1>

        <p>
          A página que você tentou acessar não existe ou foi movida.
          Verifique o endereço ou volte para o início.
        </p>

        <Link to="/" className="pagina-nao-encontrada__botao">
          <House size={18} />
          Voltar ao início
        </Link>
      </div>
    </main>
  );
}

export default PaginaNaoEncontrada;