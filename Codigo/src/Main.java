//poner package
//seguro que hay muchas cosas mal pero es para guiarnos

import IA.RedSensores.CentrosDatos;
import IA.RedSensores.Centro;
import IA.RedSensores.Sensor;
import IA.RedSensores.Sensores;

import java.util.*;

import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;

import Criteriosalgoritmos.Estado;
import Criteriosalgoritmos.RedSensoresGoalTest;
import Criteriosalgoritmos.RedSensoresHeuristicFunction;
import Criteriosalgoritmos.RedSensoresSuccessorFunction;


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
            lambda = scanner.nextDouble();
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

        boolean greedy;
        System.out.print("¿Qué estrategia para generar la solución inicial quieres usar? [1 para avariciosa / 0 para ingenua]: ");
        scanner = new Scanner(System.in);
        greedy = (scanner.nextInt() == 1);

        //cambiar esto a las que haremos evidentemente
        boolean heuristicaEnergia;
        System.out.print("¿Qué función heurística quieres usar? [1 para solo energía / 0 para energía + fiabilidad]: ");
        scanner = new Scanner(System.in);
        heuristicaEnergia = (scanner.nextInt() == 1);

        double a = 0.1, b = 0.2;
        if (!heuristicaEnergia) {
            System.out.println("A continuación introduce los parámetros A (pondera la energía) y B (pondera la fiabilidad) de la heurística");
            System.out.print("Ponderación A [0.1 por defecto]: ");
            scanner = new Scanner(System.in);
            a = scanner.nextDouble();
            System.out.print("Ponderación B [0.2 por defecto]: ");
            scanner = new Scanner(System.in);
            b = scanner.nextDouble();
        }
        else {
            a = 1;
            b = 0;
        }

        long ini_time, end_time;
        ini_time = System.nanoTime();
        //cambiarlo para utilzar el .jar que nos dan
        CentrosDatos centros = new CentrosDatos(ncentros, seed);
        Sensores sensores = new Sensores(nsensores, seed);

        Estado.sensores = sensores;
        Estado.centros = centros;
        Estado.a = a;
        Estado.b = b;

        if (greedy) {
            sensores.sort(new Comparator<Sensor>() {
                @Override
                public int compare(Sensor s1, Sensor s2) {
                    // Ordenar por importancia y capacidad
                    if (s1.getImportancia() > s2.getImportancia()) return -1;
                    else if (s1.getImportancia() < s2.getImportancia()) return 1;
                    else {
                        if (s1.getCapacidad() > s2.getCapacidad()) return -1;
                        else if (s1.getCapacidad() < s2.getCapacidad()) return 1;
                        else return 0;
                    }
                }
            });
        }
        else {
            sensores.sort(new Comparator<Sensor>() {
                @Override
                public int compare(Sensor s1, Sensor s2) {
                    return s2.getImportancia() - s1.getImportancia();
                }
            });
        }

        Estado inicial = new Estado(greedy);

        if (hillClimb) redSensoresHillClimbingSearch(inicial);
        else redSensoresSimulatedAnnealingSearch(inicial, steps, stiter, k, lambda);

        end_time = System.nanoTime();
        int duracion = (int)(end_time-ini_time)/1000000;
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

            System.out.println("Valores de la solución final: (" + solucion.getEnergiaTotal() + " energía, " + solucion.getFiabilidad() + " fiabilidad)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//ya haremos esto despues del hill climbing casi lo mismo pero cambiamos literalmente la niea de
    //simulated annelaingsearch casi q encia ya esta implementado
 /*   private static void redSensoresSimulatedAnnealingSearch(Estado estado, int steps, int stiter, int k, double lambda) {
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

            System.out.println("Valores de la solución final: (" + solucion.getEnergiaTotal() + " energía, " + solucion.getFiabilidad() + " fiabilidad)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */
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