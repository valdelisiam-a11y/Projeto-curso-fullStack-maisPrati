
import { createContext, useContext, useState } from "react";

const ContextoTrabalho = createContext(null);

const empresasMock = [
  { id: 1, nome: "Construtora Acácias" },
  { id: 2, nome: "Souza Serviços" },
];

export function ContextoTrabalhoProvider({ children }) {
  const [contexto, setContexto] = useState("pessoal");
  const [empresas] = useState(empresasMock);
  const [empresaSelecionada, setEmpresaSelecionada] = useState(
    empresasMock[0]
  );

  function selecionarContexto(novoContexto) {
    if (novoContexto !== "pessoal" && novoContexto !== "empresa") {
      return;
    }

    setContexto(novoContexto);
  }

  function selecionarEmpresa(id) {
    const empresa = empresas.find(
      (empresa) => empresa.id === Number(id)
    );

    if (empresa) {
      setEmpresaSelecionada(empresa);
    }
  }

  return (
    <ContextoTrabalho.Provider
      value={{
        contexto,
        selecionarContexto,
        empresas,
        empresaSelecionada,
        selecionarEmpresa,
      }}
    >
      {children}
    </ContextoTrabalho.Provider>
  );
}

export function useContextoTrabalho() {
  const context = useContext(ContextoTrabalho);

  if (!context) {
    throw new Error(
      "useContextoTrabalho deve ser usado dentro de ContextoTrabalhoProvider."
    );
  }

  return context;
}
