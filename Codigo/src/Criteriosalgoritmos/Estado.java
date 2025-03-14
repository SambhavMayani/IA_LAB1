package Criteriosalgoritmos;

import IA.Red.Sensor;
import IA.Red.Sensores;
import IA.Red.Centro;
import IA.Red.CentrosDatos;

import java.util.ArrayList;
import java.util.Arrays;

public class Estado {
    public static Sensores sensores;
    public static CentrosDatos centrosDatos;
    public static double a, b;

    private int asignacionSensores[];
    private double capacidadCentros[];
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
        asignacionSensores = new int[sensores.size()];
        capacidadCentros = new double[centrosDatos.size()];

        Arrays.fill(asignacionSensores, -1);

        if (greedy) generarSolucionGreedy();
        else generarSolucionIngenua();
    }

    public void clone(Estado e) {
        asignacionSensores = Arrays.copyOf(e.asignacionSensores, sensores.size());
        capacidadCentros = Arrays.copyOf(e.capacidadCentros, centrosDatos.size());
        this.costo = e.costo;
        this.eficiencia = e.eficiencia;
    }

    void generarSolucionIngenua() { //meter algo auqui random ns, lo de abajo est amal
        int j = 0;
        for (int i = 0; i < sensores.size(); i++) {
            double temp = capacidadCentros[j];
            temp += sensores.get(i).getCapacidad();
            if (temp > 150) {
                j++;
            }
            asignacionSensores[i] = j;
            capacidadCentros[j] += sensores.get(i).getCapacidad();
        }
    }

    void generarSolucionGreedy() { //hacer la greedy lo de abajo eta mal

    }
}