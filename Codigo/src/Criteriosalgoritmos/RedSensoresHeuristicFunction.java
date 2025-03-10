package Criteriosalgoritmos; //cambiar esto

import aima.search.framework.HeuristicFunction;

public class RedSensoresHeuristicFunction implements HeuristicFunction {

    public double getHeuristicValue(Object n){

        return ((RedSensores) n).heuristic();
    }
}
