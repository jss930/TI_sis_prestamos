package com.biblioteca.reservations.domain.policy;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.shared.domain.BusinessRuleException;

public final class PoliticaElegibilidadSolicitud {
    private PoliticaElegibilidadSolicitud() { }
    public static void validar(Usuario operador, Usuario alumno, CatalogoItem item, LugarUso lugar,
                               boolean sancion, boolean vencido, long activos, PoliticaPrestamo politica) {
        if (!alumno.puedeSolicitarPrestamo()) rechazar("USUARIO_NO_HABILITADO", "La cuenta no esta habilitada");
        if (operador.id().equals(alumno.id())) rechazar("AUTOPRESTAMO_NO_PERMITIDO", "No puedes aprobar tu propia solicitud");
        if (!item.activo()) rechazar("ITEM_INACTIVO", "El bien esta fuera de circulacion");
        if (sancion) rechazar("SANCION_ACTIVA", "El alumno tiene una sancion activa");
        if (vencido) rechazar("PRESTAMO_VENCIDO_PENDIENTE", "El alumno tiene prestamos vencidos");
        if (activos >= politica.maxPrestamosSimultaneos(alumno.perfil())) rechazar("LIMITE_PRESTAMOS", "El alumno alcanzo el limite de prestamos y apartados");
        if (lugar == LugarUso.DOMICILIO && !politica.permiteDomicilio(alumno.perfil(), item.tipo()))
            rechazar("DOMICILIO_NO_PERMITIDO", "Este bien no se permite a domicilio para el perfil del alumno");
    }
    private static void rechazar(String codigo, String mensaje) { throw new BusinessRuleException(codigo, mensaje); }
}
