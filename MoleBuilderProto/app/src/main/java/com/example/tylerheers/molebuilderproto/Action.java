package com.example.tylerheers.molebuilderproto;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Created by Tyler on 4/3/2017.
 */

class Action
{
    public enum ActionType
    {
        Add, Remove, Delete, Manipulation
    }

    private ActionType type;
    private List<String> objIDs;

    Action(){}

    Action(ActionType type, List objIDs)
    {
        setObjList(objIDs);
        this.type = type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    void setObjList(List objIDs)
    {
        if( !(objIDs != null && objIDs.size() > 0) )
            throw new InvalidParameterException("the list must have ids in them");

        this.objIDs = objIDs;
    }
}
