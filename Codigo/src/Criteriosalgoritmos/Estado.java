package Criteriosalgoritmos;

import IA.Red.Sensores;
import IA.Red.CentrosDatos;
import aima.search.framework.Successor;

import java.util.*;

public class Estado {
    public static Sensores sensores;
    public static CentrosDatos centrosDatos;
    public static double a, b;
    public static int rango;

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


    public double getCosto() {
        return costo;
    }
    public double getEficiencia() {
        return eficiencia;
    }
    public int getInformacion(){
        int totalinfo = 0;
        for(int i = 0; i < ocupacionCentros.length; i++){
            totalinfo += ocupacionCentros[i];
        }
        return totalinfo;
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
        ret += costo* a ;
        ret -= getInformacion()*b*100;
        //el *10 para que afecte algo q en comparacion al costo es muy pequeño
        return ret;
    }

    public Estado(int modo) {
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

        if (modo == 1) generarSolucionAvariciosa();
        else if(modo == 0)generarSolucionIngenua();
        else if(modo == 2) generarSolucionRandom();
    }

    void generarSolucionAvariciosa() {
        double[] DistanciasensorMasCercanoAXcentro = new double[centrosDatos.size()]; // inicializado a false por defecto
        int[] SensorMasCercanoAXcentro = new int[centrosDatos.size()];
        // para todos los sensores los conectamos a los centros de datos que tienen más cerca
        Arrays.fill(SensorMasCercanoAXcentro, -1);
        double distanciarespectoelactual = 100000;
        int i = 0;
        boolean todosloscentrosllenos = false;
        int centrosllenos = 0;
        while (i < sensores.size() && !todosloscentrosllenos) { //conectamos todos sus sensores al centro que tengan mas cerca
            double mindist = 100000;
            int centroCandidato = -1;
            // miramos cual es el sensor más cercano
            for (int j = 0; j < centrosDatos.size(); j++) {
                if (cantidadConexionesCentros[j] < 25) {
                    distanciarespectoelactual = get_distance(i, j, false);
                    if (distanciarespectoelactual < mindist) {
                        mindist = distanciarespectoelactual;
                        centroCandidato = j;
                    }
                }
            }
            ConectarA(i, centroCandidato, false);
            if ((SensorMasCercanoAXcentro[centroCandidato] == -1) || (distanciarespectoelactual < DistanciasensorMasCercanoAXcentro[centroCandidato])) {
                DistanciasensorMasCercanoAXcentro[centroCandidato] = distanciarespectoelactual;
                SensorMasCercanoAXcentro[centroCandidato] = i;
            }//nos guardamos para cada centro cual es el sensor que tiene mas cerca
            ++i;
            if (cantidadConexionesCentros[centroCandidato] == 25) ++centrosllenos;
            if (centrosllenos == centrosDatos.size()) todosloscentrosllenos = true;
        }
        while (i < sensores.size()) {//para cada sensor que queda irlo conectando al sensor mas cercano a cada centro
            double mindist = 100000;
            int sensorCandidato = -1;
            // miramos cual es el sensor más cercano
            for (int j = 0; j < centrosDatos.size(); j++) {
                if (cantidadConexionesSensores[SensorMasCercanoAXcentro[j]] < 3) {
                    distanciarespectoelactual = get_distance(i, SensorMasCercanoAXcentro[j], true);
                    if (distanciarespectoelactual < mindist) {
                        mindist = distanciarespectoelactual;
                        sensorCandidato = SensorMasCercanoAXcentro[j];
                    }
                }
            }
            if(sensorCandidato != -1) ConectarA(i, sensorCandidato, true);
            //if(cantidadConexionesSensores[sensorCandidato] == 3) SensorMasCercanoAXcentro[sensorCandidato] =  //esto no es lo mejor posible evidentemente
            //si se llena de 3 el mas cercano es un caso muy raro, no creo que valga la pena
            //guardarse los otros 24 de antes para substituirlo
            ++i;
        }
    }


    public Estado clone() {
        Estado nuevo = new Estado(3); //si es 3, no hace ninguna solucion inicial, moejor pq
        //total lo vamos a copiar del acutal todo asi que da igual
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

    // genera un numero random en el rango de min y max (ambos incluidos) excluyendo el numero exclude
    public static int getRandomExcluyendo(int min, int max, int exclude) {
        Random rand = new Random();
        int result;

        do {
            result = rand.nextInt(max - min + 1) + min; // Genera un número aleatorio entre min y max
        } while (result == exclude); // Repite si el número generado es el excluido

        return result;
    }


    void generarSolucionIngenua() { //meter algo aqui random ns, lo de abajo esta mal
        //System.out.println();
        //UF = new UnionFind(sensores.size());
        int centro = 0;
        boolean centrosLlenos = false;
        int sensor = 0;
        boolean sensoresLLenos = false;
        for (int i = 0; i < sensores.size(); i++) {
            if (!centrosLlenos) { //si hay muy pocos centros y muchos sensores
                ConectarA(i,centro, false);
                if (cantidadConexionesCentros[centro] == 25 || ocupacionCentros[centro] > 150 ) {
                    centro++;
                    if (centro == centrosDatos.size()) centrosLlenos = true;
                }
            }
            else if(!sensoresLLenos){
                ConectarA(i,sensor, true);
                if (cantidadConexionesSensores[sensor] == 3 || ocupacionSensores[sensor] > sensores.get(sensor).getCapacidad()*3) {
                    ++sensor;
                    if (sensor == sensores.size()) sensoresLLenos = true;
                }
            }
            else System.out.println("SE HA LLENADO TODO BARBARIDAD??!!");
        }
    }

    // genera una solucion random, con una "red" conexa y sin ciclos
    void generarSolucionRandom() {

        Random rand = new Random();

        ArrayList<Integer> sensoresSinAsignar = new ArrayList<>(); // al principio contiene todos los sensors, y cada vez que asigno un sensor en la solución inicial, se elimina de la lista
        ArrayList<Integer> sensoresAsignados = new ArrayList<>();

        for (int i = 0; i < sensores.size(); i++) {
            sensoresSinAsignar.add(i);
        }


        for (int c = 0; c < centrosDatos.size(); c++) {
            //  estos sensores seran los que se conectarán a los centros al principio
            int randNumSensoresCentroC = rand.nextInt(24) + 1;  // Número entre 1 y 25 (25 porque es el numero máximo de sensores que se pueden conectar a un centro)
            // no me fijo en la restriccion de ocupación xD (pero si en el numero de conexiones), que se encargue el hillclimbing
            // (tengo la desventaja de que la solucion inicial es menos buena pero más aleatoriedad de soluciones inciales, al final es un trade-off)
            // dejo tantos comentarios del estilo para luego justificar nuestras decisiones en el documento eh
            for (int j = 0; j < randNumSensoresCentroC && sensoresSinAsignar.size() != 0; ++j) {
                //System.out.println(sensoresSinAsignar.size());
                int indiceRandSensor1 = rand.nextInt(sensoresSinAsignar.size()); // un sensor random sin asignar
                int randSensor1 = sensoresSinAsignar.get(indiceRandSensor1);
                ConectarA(randSensor1, c, false);
                sensoresSinAsignar.remove(Integer.valueOf(randSensor1)); // como que ya asignamos el sensor, lo quitamos de los SinAsignar
                sensoresAsignados.add(Integer.valueOf(randSensor1));
            }
        }
        // ahora conecto los sensores sin asignar que me quedan a los asignados de manera random
        for (int i = 0; i < sensoresSinAsignar.size(); ++i) {

            int indiceRandSensor1 = rand.nextInt(sensoresSinAsignar.size()); // un sensor random sin asignar
            int randSensor1 = sensoresSinAsignar.get(indiceRandSensor1);

            int indiceRandSensor2 = rand.nextInt(sensoresAsignados.size()); // un sensor random de los asignados
            int randSensor2 = sensoresAsignados.get(indiceRandSensor2);                   // (asi al conectar no formo ciclos)

            // aquí, como a los centros, puedo escoger añadir la restricción de ocupacion (en Mb) o no, pasa lo mismo que antes,
            // poner ese condicional o no es un trade off entre aleatoriedad y calidad de la solucion inicial
            // he decidido en ambos casos no ponerlo ya que como el programa ejecuta mas o menos rapido, y el hill climbind en sí es un algoritmo rapido
            // interesa mas aleatoriedad que calidad de la solucion inicial
            while (cantidadConexionesSensores[randSensor2] >= 3) {
                indiceRandSensor2 = rand.nextInt(sensoresAsignados.size() - 2) + 1; // un sensor random de los asignados
                randSensor2 = sensoresAsignados.get(indiceRandSensor2);                   // (asi al conectar no formo ciclos)
            }

            ConectarA(randSensor1, randSensor2, true);
            sensoresSinAsignar.remove(Integer.valueOf(randSensor1));
            sensoresAsignados.add(Integer.valueOf(randSensor1));

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
            for (int j = 0; j < sensores.size(); j++) { //PRUEBAME TODOS LOS SENSORES
                double distance = get_distance(i, j, true);
                if ((distance < rango) && (j != i) && (!isSensor || j != actAssig)) {//QUE ESTEN CERCA, NO SEAN EL DE ANTES Y NO SEA YO
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
                if ((distance < rango) && (isSensor || k != actAssig)) {
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
        //ArrayList<Successor> temp = getSuccessors();
        Random rnd = new Random();
        //double r = rnd.nextDouble();
        //r *= temp.size() - 1;
        ArrayList<Successor> retVal = new ArrayList<>();
        //ret.add(temp.get((int)Math.round(r)));

        int i = (int)Math.round(rnd.nextDouble()*(sensores.size()-1));
        boolean connectToSensor = rnd.nextBoolean();
        int j = -1;
        while (j == -1 && !(connectToSensor && j == i)) {
            if (connectToSensor) j = (int)Math.round(rnd.nextDouble()*(sensores.size()-1));
            else j = (int)Math.floor(rnd.nextDouble()*(centrosDatos.size()-1));
        }

        int actAssig = asignacionSensores[i].getAssignacion();
        boolean isSensor = asignacionSensores[i].getConectaSensor();

        if (connectToSensor) { //Dependiendo de las variables generadas antes, crea un sucesor
            double distance = get_distance(i, j, true);
            if ((distance < rango) && (j != i) && (!isSensor || j != actAssig)) {//QUE ESTEN CERCA, NO SEAN EL DE ANTES Y NO SEA YO
                Estado successor = this.clone();
                successor.ConectarA(i, j, true);
                Successor newSuccessor = new Successor("sensor " + i + " conectado a sensor " + j, successor);
                retVal.add(newSuccessor);
            }
        } else {
            double distance = get_distance(i, j, false);
            if ((distance < rango) && (isSensor || j != actAssig)) {
                Estado successor = this.clone();
                if (!isSensor && j != actAssig) {
                    successor.Desconectar(i);
                    successor.ConectarA(i, j, false);
                    Successor newSuccessor = new Successor("sensor " + i + " conectado a centro " + j, successor);
                    retVal.add(newSuccessor);
                }
            }
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

        System.out.println("costo(con a aplicado)");
        System.out.println(costo);
        System.out.println("información(con b aplicado)");
        System.out.println(getInformacion());
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