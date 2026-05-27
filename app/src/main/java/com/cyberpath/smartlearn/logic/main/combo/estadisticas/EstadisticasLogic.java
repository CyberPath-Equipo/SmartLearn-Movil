package com.cyberpath.smartlearn.logic.main.combo.estadisticas;

import android.content.Context;

import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.model.estadisticas.DatoHistorico;
import com.cyberpath.smartlearn.data.model.estadisticas.InteresItem;
import com.cyberpath.smartlearn.data.model.estadisticas.ResumenEstadisticas;

import java.util.List;

public class EstadisticasLogic {

    private final ContenidoDAO contenidoDAO;

    public EstadisticasLogic(Context context) {
        this.contenidoDAO = new ContenidoDAO(context);
    }

    public ResumenEstadisticas obtenerResumen(int idUsuario) {
        return contenidoDAO.obtenerResumenEstadisticas(idUsuario);
    }

    public List<DatoHistorico> obtenerTiempoPorDia(int idUsuario, int dias) {
        return contenidoDAO.obtenerTiempoEstudioPorDia(idUsuario, dias);
    }

    public List<DatoHistorico> obtenerRendimientoHistorico(int idUsuario, int limite) {
        return contenidoDAO.obtenerRendimientoHistorico(idUsuario, limite);
    }

    public List<InteresItem> obtenerTopIntereses(int idUsuario, int limite) {
        return contenidoDAO.obtenerTopIntereses(idUsuario, limite);
    }
}

