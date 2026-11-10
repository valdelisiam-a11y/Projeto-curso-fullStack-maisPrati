
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

  // Recupera as empresas e restaura a última seleção do usuário.
  useEffect(() => {
    let ativo = true;

    setEmpresas([]);
    setEmpresaSelecionada(null);
    setContexto("pessoal");
    setMinhasEtapas([]);
    setCarregandoEtapas(false);
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

        const chave = `docflow-contexto-trabalho-${usuario.id}`;

        let selecaoSalva = null;

        try {
          selecaoSalva = JSON.parse(
            localStorage.getItem(chave)
          );
        } catch {
          localStorage.removeItem(chave);
        }

        const empresaSalva = empresasReais.find(
          (empresa) =>
            Number(empresa.id) ===
            Number(selecaoSalva?.empresaId)
        );

        const contextoSalvo = selecaoSalva?.contexto;

        if (
          contextoSalvo === "empresa" &&
          empresaSalva
        ) {
          setContexto("empresa");
          setEmpresaSelecionada(empresaSalva);
        } else {
          setContexto("pessoal");

          setEmpresaSelecionada(
            empresaSalva ?? empresasReais[0] ?? null
          );
        }
      } catch (error) {
        if (!ativo) return;

        setEmpresas([]);
        setEmpresaSelecionada(null);
        setContexto("pessoal");

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

  // Carrega somente as etapas da empresa selecionada.
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

  function salvarSelecao(novoContexto, empresaId) {
    if (!usuario?.id) return;

    const chave = `docflow-contexto-trabalho-${usuario.id}`;

    localStorage.setItem(
      chave,
      JSON.stringify({
        contexto: novoContexto,
        empresaId: empresaId ?? null,
      })
    );
  }

  function selecionarContexto(novoContexto) {
    if (
      novoContexto !== "pessoal" &&
      novoContexto !== "empresa"
    ) {
      return;
    }

    if (
      novoContexto === "empresa" &&
      !empresaSelecionada
    ) {
      return;
    }

    setContexto(novoContexto);

    salvarSelecao(
      novoContexto,
      empresaSelecionada?.id
    );
  }

  function selecionarEmpresa(id) {
    const empresa = empresas.find(
      (item) => Number(item.id) === Number(id)
    );

    if (empresa) {
      setEmpresaSelecionada(empresa);

      salvarSelecao(contexto, empresa.id);
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
