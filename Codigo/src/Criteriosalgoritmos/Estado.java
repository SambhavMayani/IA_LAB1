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
    private double ocupacionCentros[]; //cantidad de datos que recibe cada centro
    //ejemplo de para que nos sirve: centro recibe 152 (anque solo sean perceptibles 150).
    //cuando se desconecte un sensor que transmita 5 por ejemplo, tendremos que saber que
    //ahora estamos en 147 y no 145, que sería lo que nos daría sin guardarnos la ocupacion
    private int cantidadConexionesCentros[];
    private int cantidadConexionesSensores[];
    private double ocupacionSensores[];//lo mismo que los centros pero para los sensores
    private double costo = 0;
    private double eficiencia = 0;

    public double getCosto() {
        return costo;
    }
    public double getEficiencia() {
        return eficiencia;
    }

    public double getHeuristica() {
        return costo;
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
        nuevo.asignacionSensores = new AsignacionSensor[asignacionSensores.length];
        for (int i = 0; i< asignacionSensores.length ; i++) {
            nuevo.asignacionSensores[i] = new AsignacionSensor();
            nuevo.asignacionSensores[i].conectaSensor = asignacionSensores[i].conectaSensor;
            nuevo.asignacionSensores[i].assignacion = asignacionSensores[i].assignacion;
        }
        nuevo.ocupacionCentros = Arrays.copyOf(this.ocupacionCentros, centrosDatos.size());
        nuevo.cantidadConexionesCentros = Arrays.copyOf(this.cantidadConexionesCentros, centrosDatos.size());
        nuevo.cantidadConexionesSensores = Arrays.copyOf(this.cantidadConexionesSensores, sensores.size());
        nuevo.ocupacionSensores = Arrays.copyOf(this.ocupacionSensores, sensores.size());
        nuevo.costo = this.costo;
        nuevo.eficiencia = this.eficiencia;

        return nuevo;
    }

    void generarSolucionIngenua() { //meter algo aqui random ns, lo de abajo esta mal
        System.out.println();
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
    public void ConectarA(int i, int j, boolean sensor) {
        //if (i != -1 && j != -1) {
            Desconectar(i);
            if (!sensor) {
                ocupacionCentros[j] += ocupacionSensores[i] + sensores.get(i).getCapacidad(); //TODO Retocar esto
                cantidadConexionesCentros[j] += 1;
            } else {
                ocupacionSensores[j] += ocupacionSensores[i] + sensores.get(i).getCapacidad();
                cantidadConexionesSensores[j] += 1;
            }
            asignacionSensores[i].setAssignacion(j);
            asignacionSensores[i].setConectaSensor(sensor);
            costo += coste(i, j, sensor);
        //}
    }
    //desconecta lo que tenga conectado i
    public void Desconectar(int i) {
        boolean sensor = asignacionSensores[i].getConectaSensor();
        int j = asignacionSensores[i].getAssignacion();
        if (j != -1) {
            if (!sensor) {
                ocupacionCentros[j] -= sensores.get(i).getCapacidad() + ocupacionSensores[i];
                cantidadConexionesCentros[j] -= 1;
            } else {
                ocupacionSensores[j] -= sensores.get(i).getCapacidad() + ocupacionSensores[i];
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
            System.out.println("El Act assig aquí ha sido "+ asignacionSensores[i].getAssignacion() + " " + actAssig);
            boolean isSensor = asignacionSensores[i].getConectaSensor();
            if (actAssig != -1) this.Desconectar(i);
            for (int j = 0; j < sensores.size(); j++) {
                if (j != i) { //que no me conecta a mi mismo xD TODO Por alguna razón permite conectar un sensor a lo mismo a lo que estaba conectado otra vez.
                    //MIRAR QUE NO FORME CICLOS CONECTAR LAS COSAS!!!
                    if ( j != actAssig) { //Sin isSensor no me asigna multiples sensores de una (no se porque)
                        Estado successor = this.clone();
                        //System.out.println("Antes de crear sucesor: ");
                        //debugMostrarEstado();

                        successor.Desconectar(i);
                        successor.ConectarA(i, j, true);
                        Successor newSuccessor = new Successor("sensor " + i + " conectado a sensor " + j, successor);

                        retVal.add(newSuccessor);


                        System.out.println();
                        System.out.println("sensor " + i + " conectado a sensor " + j);
                        System.out.println("Soy " + i + " y estaba conectado al " + (isSensor? "sensor " : "centro ") + actAssig);
                        successor.debugMostrarEstado();
                        System.out.print("Viene de: ");
                        debugMostrarEstado();
                    } else if (!isSensor) {
                        Estado successor = this.clone();
                        successor.Desconectar(i);
                        successor.ConectarA(i, j, true);
                        Successor newSuccessor = new Successor("sensor " + i + " conectado a sensor " + j, successor);
                        retVal.add(newSuccessor);
                    }
                }System.out.println("aaa");
            }
            /*---------------------------------------------COMMENTDEBUG-------------------------------------------------------
            --             Parece que el programa se atasca en esta sección de código Y en la solución ingenua              ---
            -------------------------------------------------------------------------------------------------------------------
            for (int k = 0; k < centrosDatos.size(); k++) {
                //se podria hacer mejor lo del if y else if solo para no mirar de repetir
                //el caso actual (arriba lo mismo, pero bueno q estar esta bien supuestamente
                if (!isSensor && k != actAssig) {
                    Estado successor = this.clone();
                    successor.Desconectar(i);
                    successor.ConectarA(i,k,false);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a centro " + k, successor);
                    retVal.add(newSuccessor);
                }
                else if (isSensor) {
                    Estado successor = this.clone();
                    successor.Desconectar(i);
                    successor.ConectarA(i,k,false);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a centro " + k, successor);
                    retVal.add(newSuccessor);
                }
            }*/
            if (actAssig != -1) ConectarA(i,actAssig,isSensor);
            //'volver a dejarlo como estaba'
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
        return Math.pow(get_distance(i,j,sensor),2) * (ocupacionSensores[i]+sensores.get(i).getCapacidad());
    }

    public void debugMostrarEstado() {
        System.out.println();
        System.out.print("[");
        for (int i = 0; i < asignacionSensores.length; i++) {
            System.out.print("ass:" + asignacionSensores[i].assignacion + " con:" + asignacionSensores[i].conectaSensor);
            System.out.print(",");
        }
        System.out.println("]");
        System.out.println("ocupacionCentros");
        System.out.println(Arrays.toString(ocupacionCentros));
        System.out.println("cantidadConexionesCentros");
        System.out.println(Arrays.toString(cantidadConexionesCentros));

        System.out.println("cantidadConexionesSensores");
        System.out.println(Arrays.toString(cantidadConexionesSensores));
        System.out.println("ocupacionSensores");
        System.out.println(Arrays.toString(ocupacionSensores));

        System.out.println("costo");
        System.out.println(costo);
        System.out.println("eficiencia");
        System.out.println(eficiencia);
    }
}


class AsignacionSensor {
    int assignacion;
    boolean conectaSensor;

    public AsignacionSensor() {
        assignacion = -1;
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

