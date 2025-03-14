package Criteriosalgoritmos;

import IA.Red.Sensor;
import IA.Red.Sensores;
import IA.Red.Centro;
import IA.Red.CentrosDatos;

import java.util.*;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class RedSensoresSuccessorFunction implements SuccessorFunction{
    //tendrá errores se tiene que hacer pero para que sirva de guia
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList<>();
        Estado estado = (Estado) a;
        retVal = estado.getSuccessors();
        return retVal;
    }
}