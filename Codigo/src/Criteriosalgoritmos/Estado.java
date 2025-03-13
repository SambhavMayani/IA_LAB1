package Criteriosalgoritmos;

import IA.Red.Sensor;
import IA.Red.Sensores;
import IA.Red.Centro;
import IA.Red.CentrosDatos;

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
        for (int i = 0; i < centrosDatos.size(); ++i) {
            capacidadCentros[i] = centrosDatos.get(i).getCapacidadMax();
        }

        if (greedy) generarSolucionGreedy();
        else generarSolucionIngenua();
    }

    Estado(Estado e) {
        asignacionSensores = Arrays.copyOf(e.asignacionSensores, sensores.size());
        capacidadCentros = Arrays.copyOf(e.capacidadCentros, centrosDatos.size());
        this.costo = e.costo;
        this.eficiencia = e.eficiencia;
    }

    void generarSolucionIngenua() { //meter algo auqui random ns, lo de abajo est amal
        int iCentro = 0;
        int iSensor = 0;
        CentroDatos centro = centrosDatos.get(iCentro);
        while (iSensor < sensores.size()) {
            Sensor sensor = sensores.get(iSensor);
            if (capacidadCentros[iCentro] - sensor.getConsumo() >= 0) {
                asignacionSensores[iSensor] = iCentro;
                capacidadCentros[iCentro] -= sensor.getConsumo();
                costo += centro.getCosto() * sensor.getConsumo();
                eficiencia += calcularEficiencia(sensor, centro);
                ++iSensor;
            } else {
                ++iCentro;
                if (iCentro < centrosDatos.size()) {
                    centro = centrosDatos.get(iCentro);
                } else {
                    break;
                }
            }
        }
    }

    void generarSolucionGreedy() { //hacer la greedy lo de abajo eta mal
        for (int iSensor = 0; iSensor < sensores.size(); iSensor++) {
            Sensor sensor = sensores.get(iSensor);
            int mejorCentro = -1;
            double mejorCosto = Double.MAX_VALUE;
            for (int iCentro = 0; iCentro < centrosDatos.size(); iCentro++) {
                CentroDatos centro = centrosDatos.get(iCentro);
                if (capacidadCentros[iCentro] - sensor.getConsumo() >= 0) {
                    double costoActual = centro.getCosto() * sensor.getConsumo();
                    if (costoActual < mejorCosto) {
                        mejorCosto = costoActual;
                        mejorCentro = iCentro;
                    }
                }
            }
            if (mejorCentro != -1) {
                asignacionSensores[iSensor] = mejorCentro;
                capacidadCentros[mejorCentro] -= sensor.getConsumo();
                costo += mejorCosto;
                eficiencia += calcularEficiencia(sensor, centrosDatos.get(mejorCentro));
            }
        }
    }
}