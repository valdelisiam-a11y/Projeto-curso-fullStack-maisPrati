
import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

import { useAuth } from "./useAuth";
import { listarMinhasEmpresas } from "../services/empresaService";
import { listarMinhasEtapas } from "../services/trabalhoService";

const ContextoTrabalho = createContext(null);

export function ContextoTrabalhoProvider({ children }) {
  const { usuario } = useAuth();

  const [contexto, setContexto] = useState("pessoal");
  const [empresas, setEmpresas] = useState([]);
  const [empresaSelecionada, setEmpresaSelecionada] = useState(null);
  const [carregandoEmpresas, setCarregandoEmpresas] = useState(true);
  const [erroEmpresas, setErroEmpresas] = useState("");
  const [minhasEtapas, setMinhasEtapas] = useState([]);
  const [carregandoEtapas, setCarregandoEtapas] = useState(false);

  useEffect(() => {
    let ativo = true;

    // Limpa os dados da sessão anterior.
    setEmpresas([]);
    setEmpresaSelecionada(null);
    setContexto("pessoal");
    setErroEmpresas("");

    async function carregarEmpresas() {
      if (!usuario?.id) {
        setCarregandoEmpresas(false);
        return;
      }

      setCarregandoEmpresas(true);

      try {
        const resultado = await listarMinhasEmpresas();

        if (!ativo) return;

        const empresasReais = Array.isArray(resultado)
          ? resultado
          : [];

        setEmpresas(empresasReais);
        setEmpresaSelecionada(empresasReais[0] ?? null);
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
  }, [usuario?.id]);

  useEffect(() => {
    let ativo = true;

    async function carregarMinhasEtapas() {
      if (
        !usuario?.id ||
        contexto !== "empresa" ||
        !empresaSelecionada?.id
      ) {
        setMinhasEtapas([]);
        setCarregandoEtapas(false);
        return;
      }

      try {
        setCarregandoEtapas(true);

        const resultado = await listarMinhasEtapas(
          empresaSelecionada.id
        );

        if (ativo) {
          setMinhasEtapas(
            Array.isArray(resultado) ? resultado : []
          );
        }
      } catch (error) {
        if (ativo) {
          setMinhasEtapas([]);
          console.error("Erro ao carregar minhas etapas:", error);
        }
      } finally {
        if (ativo) {
          setCarregandoEtapas(false);
        }
      }
    }

  carregarMinhasEtapas();

  return () => {
    ativo = false;
  };
}, [usuario?.id, contexto, empresaSelecionada?.id]);

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
        minhasEtapas,
        carregandoEtapas,
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
