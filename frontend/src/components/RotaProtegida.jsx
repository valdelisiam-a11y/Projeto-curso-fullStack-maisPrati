
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";

function RotaProtegida() {
    const { autenticado } = useAuth();
    const location = useLocation();

    if (!autenticado) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: location }}
            />
        );
    }

    return <Outlet />;
}

export default RotaProtegida;
