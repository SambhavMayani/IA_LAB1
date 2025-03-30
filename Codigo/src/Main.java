import Criteriosalgoritmos.*;
import IA.Red.CentrosDatos;
import IA.Red.Centro;
import IA.Red.Sensor;
import IA.Red.Sensores;

import java.util.*;

import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

public class Main {
    public static void main(String[] args) {
        System.out.print("Introduce número de sensores: ");
        Scanner scanner = new Scanner(System.in);
        int nsensores = scanner.nextInt();

        System.out.print("Introduce número de centros: ");
        scanner = new Scanner(System.in);
        int ncentros = scanner.nextInt();

        System.out.print("¿Qué algoritmo quieres usar? [1 para Hill Climbing / 0 para Simulated Annealing]: ");
        scanner = new Scanner(System.in);
        boolean hillClimb = (scanner.nextInt() == 1);

        int steps = 10000, stiter = 1000, k = 25;
        double lambda = 0.01;
        if (!hillClimb) { //YA HAREMOS EL SA DESPUE DEL HILL CLIMBING
            System.out.println("A continuación introduce los parámetros del Simulated annealing");
            System.out.print("Parámetro steps [10000 por defecto]: ");
            scanner = new Scanner(System.in);
            steps = scanner.nextInt();
            System.out.print("Parámetro stiter [1000 por defecto]: ");
            scanner = new Scanner(System.in);
            stiter = scanner.nextInt();
            System.out.print("Parámetro K [25 por defecto]: ");
            scanner = new Scanner(System.in);
            k = scanner.nextInt();
            System.out.print("Parámetro lambda [0.01 por defecto]: ");
            scanner = new Scanner(System.in);
            lambda = scanner.nextFloat();
        }

        int seed;
        System.out.print("¿Quieres semilla random? [1 para sí / 0 para no]: ");
        scanner = new Scanner(System.in);
        if (scanner.nextInt() == 1) {
            Random r = new Random();
            seed = r.nextInt();
            System.out.println("La semilla random es: " + seed);
        }
        else {
            System.out.print("Introduce la semilla: ");
            scanner = new Scanner(System.in);
            seed = scanner.nextInt();
        }
        System.out.print("¿Qué estrategia para generar la solución inicial quieres usar? [0 para ingenua/1 avariciosa/2 random]: ");
        scanner = new Scanner(System.in);
        int modo = scanner.nextInt();

        //cambiar esto a las que haremos evidentemente
        boolean heuristicaCoste;
        System.out.print("¿Qué función heurística quieres usar? [1 Coste / 0 Coste y información]: ");
        scanner = new Scanner(System.in);
        heuristicaCoste = (scanner.nextInt() == 1);
        double a = 0.1;
        double b = 0.2;
        if (!heuristicaCoste) {
            System.out.println("A continuación introduce los parámetros A (pondera el coste) y B (pondera la información) de la heurística (con ',' , no '.'. Que entre los 2 sumen 1!");
            System.out.print("Ponderación A [0,1 por defecto]: ");
            scanner = new Scanner(System.in);
            a = scanner.nextDouble();
            System.out.print("Ponderación B [0.,2 por defecto]: ");
            scanner = new Scanner(System.in);
            b = scanner.nextDouble();
        }
        else {
            a = 1;
            b = 0;
        }
        int rango;
        System.out.println("introuce el rango que quieres que tengan los intercambios: (max 141)");
        scanner = new Scanner(System.in);
        rango = scanner.nextInt();

        long ini_time, end_time;
        ini_time = System.nanoTime();
        //cambiarlo para utilzar el .jar que nos dan
        CentrosDatos centros = new CentrosDatos(ncentros, seed);
        Sensores sensores = new Sensores(nsensores, seed);

        Estado.sensores = sensores;
        Estado.centrosDatos = centros;
        Estado.a = a;
        Estado.b = b;
        Estado.rango = rango;



        Estado inicial = new Estado(modo);
        inicial.debugMostrarEstado();
        //----------------------------ESTO ES DEBUG-------------------------------------
        /*
        inicial.debugMostrarEstado();

        inicial.Desconectar(0);
        inicial.debugMostrarEstado();

        inicial.ConectarA(0,0,false);

        inicial.debugMostrarEstado();

        inicial.ConectarA(0,0,false);

        inicial.debugMostrarEstado();
        */
        //-------------------------------------------------------------------------------

        if (hillClimb) redSensoresHillClimbingSearch(inicial);
        else redSensoresSimulatedAnnealingSearch(inicial, steps, stiter, k, lambda);



        end_time = System.nanoTime();
        long duracion = (end_time - ini_time) / 1000000;
        System.out.println("Duración del algoritmo: " + duracion + " ms ");
    }

    private static void redSensoresHillClimbingSearch(Estado estado) {
        System.out.println("\nRedSensores Hill Climbing  -->");
        try {
            RedSensoresSuccessorFunction successorFunction = new RedSensoresSuccessorFunction();
            RedSensoresGoalTest goalTest = new RedSensoresGoalTest();
            RedSensoresHeuristicFunction heuristicFunction = new RedSensoresHeuristicFunction();
            Problem problem = new Problem(estado, successorFunction, goalTest, heuristicFunction);
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);

            System.out.println();
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());

            Estado solucion = (Estado) search.getGoalState();
            System.out.println("El final con resultado " + solucion.isGoal());
            solucion.debugMostrarEstado();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//ya haremos esto despues del hill climbing casi lo mismo pero cambiamos literalmente la niea de
    //simulated annelaingsearch casi q encia ya esta implementado
    private static void redSensoresSimulatedAnnealingSearch(Estado estado, int steps, int stiter, int k, double lambda) {
        System.out.println("\nRedSensores Simulated Annealing  -->");
        try {
            RedSensoresSuccessorFunctionSA successorFunction = new RedSensoresSuccessorFunctionSA();
            RedSensoresGoalTest goalTest = new RedSensoresGoalTest();
            RedSensoresHeuristicFunction heuristicFunction = new RedSensoresHeuristicFunction();
            Problem problem = new Problem(estado, successorFunction, goalTest, heuristicFunction);
            Search search = new SimulatedAnnealingSearch(steps, stiter, k, lambda);
            SearchAgent agent = new SearchAgent(problem, search);

            System.out.println();
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
            Estado solucion = (Estado) search.getGoalState();

            solucion.debugMostrarEstado();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//ni idea depa q esto
    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }
//esto para los pasos creo
    private static void printActions(List actions) {
        System.out.println(actions.toString());
    }
}