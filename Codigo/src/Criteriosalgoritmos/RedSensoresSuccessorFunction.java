package Criteriosalgoritmos

import java.util.*;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class RedSensoresSuccessorFunction implements RedSEnsoresSuccessorFunction {
    //tendrá errores se tiene que hacer pero para que sirva de guia
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList<>();
        Estado estadoActual = (Estado) a;
        Sensores sensores = Estado.sensores;
        CentrosDatos centros = Estado.centros;

        // Intentar intercambiar sensores entre centros de datos
        for (int i = 0; i < sensores.size(); i++) {
            for (int j = i + 1; j < sensores.size(); j++) {
                Estado newState = new Estado(estadoActual);
                if (newState.swap(i, j)) {
                    String S = "INTERCAMBIO " + i + " " + j + " " + newState.toString() + "\n";
                    retVal.add(new Successor(S, newState));
                }
            }
        }

        // Intentar mover sensores a diferentes centros de datos
        for (int i = 0; i < sensores.size(); ++i) {
            for (int j = 0; j < centros.size(); ++j) {
                Estado newState = new Estado(estadoActual);
                if (newState.moverSensor(i, j)) {
                    String S = "MOVIDO sensor: " + i + " al centro: " + j + " " + newState.toString() + "\n";
                    retVal.add(new Successor(S, newState));
                }
            }
        }

        return retVal;
    }
}
