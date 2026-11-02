
import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

import { listarMinhasEmpresas } from "../services/empresaService";

const ContextoTrabalho = createContext(null);

export function ContextoTrabalhoProvider({ children }) {
  const [contexto, setContexto] = useState("pessoal");
  const [empresas, setEmpresas] = useState([]);
  const [empresaSelecionada, setEmpresaSelecionada] = useState(null);
  const [carregandoEmpresas, setCarregandoEmpresas] = useState(true);
  const [erroEmpresas, setErroEmpresas] = useState("");

  useEffect(() => {
    let ativo = true;

    async function carregarEmpresas() {
      try {
        const resultado = await listarMinhasEmpresas();

        if (!ativo) return;

        const empresasReais = Array.isArray(resultado)
          ? resultado
          : [];

        setEmpresas(empresasReais);

        setEmpresaSelecionada((selecionadaAtual) => {
          return (
            empresasReais.find(
              (empresa) =>
                Number(empresa.id) === Number(selecionadaAtual?.id)
            ) ??
            empresasReais[0] ??
            null
          );
        });

        setErroEmpresas("");
      } catch (error) {
        if (!ativo) return;

        setEmpresas([]);
        setEmpresaSelecionada(null);
        setErroEmpresas(
          error.message || "Não foi possível carregar as empresas."
        );
      } finally {
        if (ativo) {
          setCarregandoEmpresas(false);
        }
      }
    }

    carregarEmpresas();

    return () => {
      ativo = false;
    };
  }, []);

  function selecionarContexto(novoContexto) {
    if (novoContexto !== "pessoal" && novoContexto !== "empresa") {
      return;
    }

    setContexto(novoContexto);
  }

  function selecionarEmpresa(id) {
    const empresa = empresas.find(
      (item) => Number(item.id) === Number(id)
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
        carregandoEmpresas,
        erroEmpresas,
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
