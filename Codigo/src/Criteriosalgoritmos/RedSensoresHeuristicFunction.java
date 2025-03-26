package Criteriosalgoritmos;

import aima.search.framework.HeuristicFunction;

public class RedSensoresHeuristicFunction implements HeuristicFunction {

    public double getHeuristicValue(Object n){
        Estado estado = (Estado) n;
        double heuristic = estado.getHeuristica();
        return heuristic;
    }
}
