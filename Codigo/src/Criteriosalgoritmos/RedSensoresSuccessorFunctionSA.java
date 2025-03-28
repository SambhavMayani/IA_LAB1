package Criteriosalgoritmos;

import IA.Red.Sensor;
import IA.Red.Sensores;
import IA.Red.Centro;
import IA.Red.CentrosDatos;

import java.util.*;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class RedSensoresSuccessorFunctionSA implements SuccessorFunction {
    public List<Successor> getSuccessors(Object a) {
        Estado estado = (Estado) a;
        ArrayList<Successor> retVal = estado.getSuccessorsSA();
        return retVal;
    }
}
