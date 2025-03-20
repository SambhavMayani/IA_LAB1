package Criteriosalgoritmos;

import IA.Red.Sensor;
import IA.Red.Sensores;
import IA.Red.Centro;
import IA.Red.CentrosDatos;
import aima.search.csp.Assignment;
import aima.search.framework.Successor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        //inicializamos el vector de asignacionSensores
        asignacionSensores = new AsignacionSensor[sensores.size()];
        for (int i = 0; i < asignacionSensores.length; i++) {
            asignacionSensores[i] = new AsignacionSensor();
            asignacionSensores[i].setAssignacion(-1);
        }

        if (greedy) generarSolucionGreedy();
        else generarSolucionIngenua();
    }

    public Estado clone() {
        Estado nuevo = new Estado(false);
        nuevo.asignacionSensores = Arrays.copyOf(this.asignacionSensores, sensores.size());
        nuevo.ocupacionCentros = Arrays.copyOf(this.ocupacionCentros, centrosDatos.size());
        nuevo.cantidadConexionesCentros = Arrays.copyOf(this.cantidadConexionesCentros, centrosDatos.size());
        nuevo.cantidadConexionesSensores = Arrays.copyOf(this.cantidadConexionesSensores, sensores.size());
        nuevo.ocupacionSensores = Arrays.copyOf(this.ocupacionSensores, sensores.size());
        nuevo.costo = this.costo;
        nuevo.eficiencia = this.eficiencia;

        return nuevo;
    }

    void generarSolucionIngenua() { //meter algo aqui random ns, lo de abajo esta mal
        int centro = 0; boolean centrosLlenos = false;
        int sensor = 0;
        for (int i = 0; i < sensores.size(); i++) {
            ConectarA(i,centro, false);
            if (!centrosLlenos) { //si hay muy pocos centros y muchos sensores
                if (ocupacionCentros[centro] > 150 || cantidadConexionesCentros[centro] > 25) {
                    Desconectar(i);
                    centro++;
                    if (centro == centrosDatos.size()) { centrosLlenos = true; }
                }
            }
            else {
                ConectarA(i,sensor, true);
                ++sensor;
            }
        }
    }
    //conecta i a j, el booleano indica si j es sensor o centro, también desconecta del sensor i el sensor al que estaba conectado (si lo estaba)
    private void ConectarA(int i, int j, boolean sensor) {
        //if (i != -1 && j != -1) {
            Desconectar(i);
            if (!sensor) {
                ocupacionCentros[j] += Math.max(ocupacionSensores[i], sensores.get(i).getCapacidad());
                cantidadConexionesCentros[j] += 1;
            } else {
                ocupacionSensores[j] += Math.max(ocupacionSensores[i], sensores.get(i).getCapacidad());
                cantidadConexionesSensores[j] += 1;
            }
            asignacionSensores[i].setAssignacion(j);
            asignacionSensores[i].setConectaSensor(sensor);
            costo += coste(i, j, sensor);
        //}
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
            costo -= coste(i, j, sensor);
            asignacionSensores[i].setAssignacion(-1);
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

    public ArrayList<Successor> getSuccessors() {
        ArrayList<Successor> retVal = new ArrayList<>();
        for (int i = 0; i < sensores.size(); i++) {
            int actAssig = asignacionSensores[i].getAssignacion();
            boolean isSensor = asignacionSensores[i].getConectaSensor();
            if (actAssig != -1) this.Desconectar(i);
            for (int j = 0; j < sensores.size(); j++) {
                if (isSensor && j != actAssig) {
                    Estado successor = this.clone();
                    //successor.Desconectar(i); //mejora de eficiencia, desconecta tal cual del this y luego lo vuelves a conectar, así no desconectas para cada successor
                    successor.ConectarA(i,j,true);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a sensor " + j, successor);
                    retVal.add(newSuccessor);
                }
                else if (!isSensor) {
                    Estado successor = this.clone();
                    //successor.Desconectar(i); //mejora de eficiencia, desconecta tal cual del this y luego lo vuelves a conectar, así no desconectas para cada successor
                    successor.ConectarA(i,j,true);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a sensor " + j, successor);
                    retVal.add(newSuccessor);
                }
            }
            for (int k = 0; k < centrosDatos.size(); k++) {
                if (!isSensor && k != actAssig) {
                    Estado successor = this.clone();
                    //successor.Desconectar(i); //mejora de eficiencia, desconecta tal cual del this y luego lo vuelves a conectar, así no desconectas para cada successor
                    successor.ConectarA(i,k,false);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a centro " + k, successor);
                    retVal.add(newSuccessor);
                }
                else if (isSensor) {
                    Estado successor = this.clone();
                    //successor.Desconectar(i); //mejora de eficiencia, desconecta tal cual del this y luego lo vuelves a conectar, así no desconectas para cada successor
                    successor.ConectarA(i,k,false);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a centro " + k, successor);
                    retVal.add(newSuccessor);
                }
            }
            if (actAssig != -1) ConectarA(i,actAssig,isSensor);
        }

        return retVal;
    }

    public double get_distance(int i, int j, boolean sensor) {
        double xi = sensores.get(i).getCoordX();
        double yi = sensores.get(i).getCoordY();
        double xj;
        double yj;
        if(sensor){
            xj = sensores.get(j).getCoordX();
            yj = sensores.get(j).getCoordY();
        }
        else {
            xj = centrosDatos.get(j).getCoordX();
            yj = centrosDatos.get(j).getCoordY();
        }
        return Math.sqrt(Math.pow(xi-xj, 2) + Math.pow(yi-yj, 2));
    }

    public double coste(int i, int j, boolean sensor) {
        return Math.pow(get_distance(i,j,sensor),2) + Math.max(ocupacionSensores[i],sensores.get(i).getCapacidad());
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

