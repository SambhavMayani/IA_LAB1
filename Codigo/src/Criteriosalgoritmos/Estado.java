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
    private double ocupacionSensores[];
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
        ocupacionSensores = new double[sensores.size()];
        cantidadConexionesSensores = new int[sensores.size()];
        ocupacionCentros = new double[centrosDatos.size()];
        cantidadConexionesCentros = new int[centrosDatos.size()];
        for (int i = 0; i < ocupacionSensores.length; i++) {
            ocupacionSensores[i] = sensores.get(i).getCapacidad();
        }

        Arrays.fill(asignacionSensores, -1);

        if (greedy) generarSolucionGreedy();
        else generarSolucionIngenua();
    }

    public void clone(Estado e) {
        asignacionSensores = Arrays.copyOf(e.asignacionSensores, sensores.size());
        ocupacionCentros = Arrays.copyOf(e.ocupacionCentros, centrosDatos.size());
        cantidadConexionesCentros = Arrays.copyOf(e.cantidadConexionesCentros, centrosDatos.size());
        cantidadConexionesSensores = Arrays.copyOf(e.cantidadConexionesSensores, sensores.size());
        ocupacionSensores = Arrays.copyOf(e.ocupacionSensores, sensores.size());
        this.costo = e.costo;
        this.eficiencia = e.eficiencia;
    }

    void generarSolucionIngenua() { //meter algo auqui random ns, lo de abajo est amal
        int j = 0;
        for (int i = 0; i < sensores.size(); i++) {
            ConectarA(i,j, false);
            if (ocupacionCentros[j] > 150 || cantidadConexionesCentros[j] > 25) {
                Desconectar(i);
                j++;
            }
            ConectarA(i,j,false);
        }
    }
    //conecta i a j, el booleano indica si j es sensor o centro
    private void ConectarA(int i, int j, boolean sensor) {
        Desconectar(i);
        if(!sensor) {
            ocupacionCentros[j] += Math.max(ocupacionSensores[i],sensores.get(i).getCapacidad());
            cantidadConexionesCentros[j] += 1;
        }
        else {
            ocupacionSensores[j]+= Math.max(ocupacionSensores[i],sensores.get(i).getCapacidad());
            cantidadConexionesSensores[j] += 1;
        }
        asignacionSensores[i].setAssignacion(j);
        asignacionSensores[i].setConectaSensor(sensor);
    }
    //desconecta lo que tenga conectado i
    private void Desconectar(int i) {
        boolean sensor = asignacionSensores[i].getConectaSensor();
        int j = asignacionSensores[i].getAssignacion();
        if (j != -1) {
            if (!sensor) {
                ocupacionCentros[j] -= sensores.get(i).getCapacidad();
                cantidadConexionesCentros[j] -= 1;
            } else {
                ocupacionSensores[j] -= sensores.get(i).getCapacidad();
                cantidadConexionesSensores[j] -= 1;
            }
        }
        asignacionSensores[i].setAssignacion(-1);

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
        for (int i = 0; i < sensores.size(); i++) {
            if (!connectsToCenter(i)) return false;
        }

        return true;
    }

    private boolean connectsToCenter(int i) {
        if (asignacionSensores[i].getAssignacion() == -1) return false;
        if (asignacionSensores[i].getConectaSensor()) {
            return connectsToCenter(asignacionSensores[i].getAssignacion());
        } else {
            return true;
        }
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

