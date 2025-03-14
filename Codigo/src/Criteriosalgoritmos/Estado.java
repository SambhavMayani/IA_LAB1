package Criteriosalgoritmos;

import IA.Red.Sensor;
import IA.Red.Sensores;
import IA.Red.Centro;
import IA.Red.CentrosDatos;
import aima.search.csp.Assignment;

import java.util.ArrayList;
import java.util.Arrays;

public class Estado {
    public static Sensores sensores;
    public static CentrosDatos centrosDatos;
    public static double a, b;

    private AsignacionSensor asignacionSensores[];
    private double ocupacionCentros[];
    private int cantidadConexionesCentros[];
    private int cantidadConexionesSensores[];
    private int ocupacionSensores[];
    private double costo = 0;
    private double eficiencia = 0;

    public double getCosto() {
        return costo;
    }
    public double getEficiencia() {
        return eficiencia;
    }

    public double getHeuristica() {
        return a * costo - b * eficiencia;
    }

    public Estado(boolean greedy) {
        asignacionSensores = new AsignacionSensor[sensores.size()];
        ocupacionCentros = new double[centrosDatos.size()];
        cantidadConexionesCentros = new int[centrosDatos.size()];

        Arrays.fill(asignacionSensores, -1);

        if (greedy) generarSolucionGreedy();
        else generarSolucionIngenua();
    }

    public void clone(Estado e) {
        asignacionSensores = Arrays.copyOf(e.asignacionSensores, sensores.size());
        ocupacionCentros = Arrays.copyOf(e.ocupacionCentros, centrosDatos.size());
        this.costo = e.costo;
        this.eficiencia = e.eficiencia;
    }

    void generarSolucionIngenua() { //meter algo auqui random ns, lo de abajo est amal
        int j = 0;
        for (int i = 0; i < sensores.size(); i++) {
            ocupacionCentros[j] += sensores.get(i).getCapacidad();
            cantidadConexionesCentros[j] += 1;
            if (ocupacionCentros[j] > 150 || cantidadConexionesCentros[j] > 25) {
                cantidadConexionesCentros[j]--;
                ocupacionCentros[j] -= sensores.get(i).getCapacidad();
                j++;
            }
            asignacionSensores[i].setAssignacion(j);
        }
    }

    void generarSolucionGreedy() { //hacer la greedy lo de abajo eta mal

    }

    public boolean isGoal() {
        for (int i = 0; i < sensores.size(); i++) {
            if (cantidadConexionesSensores[i] > 3) return false;

        }
        for (int i = 0; i < centrosDatos.size(); i++) {
            if (cantidadConexionesCentros[i] > 25) return false;
        }



        return true;
    }
}

class AsignacionSensor {
    int assignacion;
    boolean conectaSensor;

    public AsignacionSensor() {
        assignacion = 0;
        conectaSensor = false;
    }

    public int getAssignacion() {
        return assignacion;
    }

    public void setAssignacion(int assignacion) {
        this.assignacion = assignacion;
    }

    public boolean getConectaSensor() {
        return conectaSensor;
    }

    public void setConectaSensor(boolean conectaSensor) {
        this.conectaSensor = conectaSensor;
    }
}