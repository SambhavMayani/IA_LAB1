package Criteriosalgoritmos;

import aima.search.framework.GoalTest;

public class RedSensoresGoalTest implements GoalTest {

    public boolean isGoalState(Object state){
        Estado estado = (Estado) state;

        return  estado.isGoal();;
    }
}

