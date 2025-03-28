package Criteriosalgoritmos;

import IA.Red.Sensores;
import IA.Red.CentrosDatos;
import aima.search.framework.Successor;

import java.util.ArrayList;
import java.util.Arrays;

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
    //private UnionFind UF;   // FALTA: hacer que el conectarA y el desconectarA tengan en cuenta al union find

    private double costo = 0;
    private double eficiencia = 0;

    //Operaciones del unionFind

    // Vamos a suponer que los primeros elementos del vector que representa el 'grafo del UF' son los centros y a partir de ahí empiezan los sensores

    // retorna la identificacion del arbol en el que está id, hay que tener en cuenta que este valor no está normalizado!!
    // cuando digo que no está normalizado es que si la identificación < numeroCentros es un centro y cuando no un sensor, por lo que está explicado arriba
    // La identificación  que retorna el find solo usarla para saber si formamos ciclos o no
    /*private int findUF(int i, boolean isSensor) {
        int id;
        if (!isSensor) id = i;
        else id = i + cantidadConexionesCentros.length;

        return UF.find(id);
    }*/

    // he metido el bool de isSensorI por si acaso lo necesitamos en un futuro,
    // aunque sé que solo podemos conectar sensores, los centros no se conectan a algo
    /*private void unionUF(int i, int j, boolean isSensorI, boolean isSensorJ) {
        int idI, idJ;

        if (!isSensorI) idI = i;
        else idI = i + cantidadConexionesCentros.length;

        if (!isSensorJ) idJ = j;
        else idJ = j + cantidadConexionesCentros.length;


        UF.union(idI, idJ);
    }*/

    public double getCosto() {
        return costo;
    }
    public double getEficiencia() {
        return eficiencia;
    }

    public double getHeuristica() {
        double ret = 0;
        for (int i = 0; i < sensores.size(); i++) {
            if (cantidadConexionesSensores[i] > 3) ret += 100000;

        }
        for (int i = 0; i < centrosDatos.size(); i++) {
            if (cantidadConexionesCentros[i] > 25) ret += 100000;
        }

        for (int i = 0; i < sensores.size(); i++) {
            ArrayList<Integer> og = new ArrayList<>();
            og.add(i);
            if (!connectsToCenter(i,og)) ret += 100000;
        }
        ret += costo;
        return ret;
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
        Estado nuevo = new Estado(true);
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
        //nuevo.UF = this.UF.clone();

        nuevo.costo = this.costo;
        nuevo.eficiencia = this.eficiencia;


        return nuevo;
    }

    void generarSolucionIngenua() { //meter algo aqui random ns, lo de abajo esta mal
        //System.out.println();
        //UF = new UnionFind(sensores.size());
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

    // incremento de coste de deconectar el sensor i y conectarlo a 'sensorAConectar
    int incrementoDeCoste(int i, int sensorAConectar) {
        int incCoste = 0;
        boolean sensor = asignacionSensores[i].getConectaSensor();
        int sensorADesconectar = asignacionSensores[i].getAssignacion();
        if (sensorADesconectar != -1) incCoste -= coste(i, sensorADesconectar, sensor);
        incCoste += coste(i, sensorAConectar, sensor);


        return incCoste;
    }


    //conecta i a j, el booleano indica si j es isSensor o centro, también desconecta del isSensor i el isSensor al que estaba conectado (si lo estaba)
    public void ConectarA(int i, int j, boolean isSensor) {
        //if (i != -1 && j != -1) {
        //System.out.println("CONECTARA" + i + " a " + (isSensor ?"Sensor ":"Centro ") +j);
        Desconectar(i);
        if (!isSensor) {
            //System.out.println("Ocupacion Centros antes: " + ocupacionCentros[j]);
            double cambio = Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
            propagarOcupacionyCoste(j, isSensor,cambio);
            cantidadConexionesCentros[j] += 1;
            //System.out.println("Despues: " + ocupacionCentros[j]);
        } else {
            //System.out.println("Ocupacion Sensores antes: "+ ocupacionSensores[j]);
            double cambio = Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
            propagarOcupacionyCoste(j, isSensor,cambio);
            //ocupacionSensores[j] += Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
            cantidadConexionesSensores[j] += 1;
            //System.out.println("Despues: " + ocupacionSensores[j]);
        }
        asignacionSensores[i].setAssignacion(j);
        asignacionSensores[i].setConectaSensor(isSensor);
        costo += coste(i, j, isSensor);
        //System.out.println("FIN CONECTARA");

        int idJUF = j; // idJUF es la id que tiene j en el UnionFind, la i se mantiene igual porque sabemos que es un sensor
        if (isSensor) idJUF += cantidadConexionesCentros.length;
        //UF.union(i,idJUF);
        //}
    }
    //desconecta lo que tenga conectado i
    public void Desconectar(int i) {
        boolean isSensor = asignacionSensores[i].getConectaSensor();
        int j = asignacionSensores[i].getAssignacion();
        //System.out.println("DESCONECTARA");
        if (j != -1) {
            if (!isSensor) {
                //System.out.println("Ocupacion Centros antes: "+ ocupacionCentros[j]);
                double cambio = Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
                propagarOcupacionyCoste(j,isSensor,-cambio);
                //ocupacionCentros[j] -= Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
                cantidadConexionesCentros[j] -= 1;
                //System.out.println("Despues: "+ ocupacionCentros[j]);
            } else {
                //System.out.println("Ocupacion Sensores antes: "+ocupacionSensores[j]);
                double cambio = Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
                propagarOcupacionyCoste(j,isSensor,-cambio);
                //ocupacionSensores[j] -= Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i] + sensores.get(i).getCapacidad());
                cantidadConexionesSensores[j] -= 1;
                //System.out.println("Despues: "+ ocupacionSensores[j]);
            }
            costo -= coste(i, j, isSensor);
            asignacionSensores[i].setAssignacion(-1);

            int idJUF = j; // idJUF es la id que tiene j en el UnionFind, la i se mantiene igual porque sabemos que es un sensor
            if (isSensor) idJUF += cantidadConexionesCentros.length;
            // UF.noSePuedeDesconectarUnUnionFindJEJEJEJEJ(i,idJUF); NO SE PUEDE DESCONECTAR EN UN UNIONFIND :-(
        }
        //System.out.println("FIN DESCONECTARA");
    }

    public void propagarOcupacionyCoste(int i, boolean isSensor, double cambio) {
        //System.out.println("Hola soy " + (isSensor?"Sensor":"Centro") + i + ", voy a cambiar " + cambio + " y mi capacidad es " + sensores.get(i).getCapacidad());
        if (isSensor) {
            int siguienteAsignado = asignacionSensores[i].getAssignacion();
            if (siguienteAsignado == -1) return;
            boolean isSensorsiguienteAsignado = asignacionSensores[i].getConectaSensor();
            double prevOcupacion = ocupacionSensores[i];
            double prevCoste = coste(i, siguienteAsignado,isSensorsiguienteAsignado);
            ocupacionSensores[i] += cambio;
            double impactocoste = coste(i, siguienteAsignado,isSensorsiguienteAsignado)-prevCoste;
            costo += impactocoste;
            //System.out.println("impacto de propagar coste en el global" + impactocoste);
            //System.out.println("Ahora mi (" + (isSensor?"Sensor":"Centro") + i + ") ocupacion es " + ocupacionSensores[i]);
            double ocupacionMaxima = sensores.get(i).getCapacidad()*2;
            double nCambio = Math.min(cambio, Math.max(0,ocupacionMaxima - prevOcupacion)); //Si el cambio que le ha llegado es positivo, el cambio a su hijo será el cambio anterior o el sobrante entre la ocupacion previa y la maxima.
            if (Math.signum(cambio) == -1) nCambio = Math.max(cambio,Math.min(0,ocupacionSensores[i] - ocupacionMaxima)); //Si el cambio es negativo, al siguiente le quitaré los megabytes que se me han quitado o la diferencia entre la nueva ocupacion y la maxima
            propagarOcupacionyCoste(asignacionSensores[i].getAssignacion(),asignacionSensores[i].getConectaSensor(),nCambio);



        } else {
            ocupacionCentros[i] += cambio;

           // System.out.println("Ahora mi (" + (isSensor?"Sensor":"Centro") + i + ") ocupacion es " + ocupacionCentros[i]);
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
            ArrayList<Integer> og = new ArrayList<>();
            og.add(i);
            if (!connectsToCenter(i,og)) return false;
        }
        return true;
    }

    private boolean connectsToCenter(int i, ArrayList<Integer> og) {

        if (asignacionSensores[i].getAssignacion() == -1 || og.contains(asignacionSensores[i].getAssignacion())) return false;
        if (asignacionSensores[i].getConectaSensor()) {
            og.add(i);
            return connectsToCenter(asignacionSensores[i].getAssignacion(), og);
        } else {
            return true;
        }
    }

    //mirar que no forme ciclos??
    public ArrayList<Successor> getSuccessors() {
        ArrayList<Successor> retVal = new ArrayList<>();
        //System.out.println("Empezamos generación de sucesores");
        for (int i = 0; i < sensores.size(); i++) { //PARA TODOS LOS SENSORES
            //System.out.println("Sucesores cambiando el sensor " + i);
            int actAssig = asignacionSensores[i].getAssignacion();
            boolean isSensor = asignacionSensores[i].getConectaSensor();
            //if (actAssig != -1) this.Desconectar(i);
            for (int j = 0; j < sensores.size(); j++) { //PRUEBAME TODOS LOS SENSORES
                double distance = get_distance(i, j, true);
                if ((distance < 70) && (j != i) && (!isSensor || j != actAssig)) {//QUE ESTEN CERCA, NO SEAN EL DE ANTES Y NO SEA YO
                    Estado successor = this.clone();
                        successor.ConectarA(i, j, true);
                        Successor newSuccessor = new Successor("sensor " + i + " conectado a sensor " + j, successor);

                        retVal.add(newSuccessor);
                    /*System.out.println();
                    System.out.println("sensor " + i + " conectado a sensor " + j);
                    System.out.println("Soy " + i + " y estaba conectado al " + (isSensor? "sensor " : "centro ") + actAssig);
                    successor.debugMostrarEstado();
                    System.out.print("Viene de: ");
                    debugMostrarEstado();*/
                }
            }

            for (int k = 0; k < centrosDatos.size(); k++) {//AHORA PARA TODOS LOS CENTROS LO MISMO
               double distance = get_distance(i, k, false);
                if ((distance < 70) && (isSensor || k != actAssig)) {
                    Estado successor = this.clone();
                    if (!isSensor && k != actAssig) {
                        successor.Desconectar(i);
                        successor.ConectarA(i, k, false);
                        Successor newSuccessor = new Successor("sensor " + i + " conectado a centro " + k, successor);
                        retVal.add(newSuccessor);
                    }
                    /*System.out.println();
                    System.out.println("Soy " + i + " y estaba conectado al " + (isSensor ? "sensor " : "centro ") + actAssig);
                    successor.debugMostrarEstado();
                    System.out.print("Viene de: ");
                    debugMostrarEstado();*/
                }
            }
        }
        return retVal;
    }

    public ArrayList<Successor> getSuccessorsSA() {
       return getSuccessors();
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
        return Math.pow(get_distance(i,j,sensor),2) * Math.min(sensores.get(i).getCapacidad()*3,ocupacionSensores[i]+sensores.get(i).getCapacidad());
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
        System.out.println("heuristica");
        System.out.println(getHeuristica());
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

/*
Esto estaba WOIP pero al final no hacia fata
void propagarCosteDesconectar(int i, boolean iSsensor){
        if(!iSsensor) return;
        int siguienteSensorAfectado = asignacionSensores[i].getAssignacion();
        boolean siguienteIsSensor = asignacionSensores[i].getConectaSensor();
        if(siguienteSensorAfectado == -1 || !siguienteIsSensor) return;
        int asignacionDelSiguiente = asignacionSensores[siguienteSensorAfectado].getAssignacion();
        boolean isSensorDelSiguiente = asignacionSensores[siguienteSensorAfectado].getConectaSensor();
        if(asignacionDelSiguiente == -1) return;
        double costeprevio = 0; //EHHHH COMO Q NO SE DE DONDE SACAR ESTO ASI Q A METERLO DENTRO DE
        DONDE TMB SE CAMBIA LA OCUPACION
        //al calcular este coste ya hemos actualizado ocupación de todos antes de la llamada
        double nuevocoste = coste(siguienteSensorAfectado, asignacionDelSiguiente, isSensorDelSiguiente);//lo que añadia el siguiente al total
        //el que empezamos en si, ya se resta en desconectar

    }*/