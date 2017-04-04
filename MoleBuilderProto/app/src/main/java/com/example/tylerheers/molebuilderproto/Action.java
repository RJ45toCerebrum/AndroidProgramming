package com.example.tylerheers.molebuilderproto;

import java.security.InvalidParameterException;
import java.util.List;


class Action
{
    enum ActionType
    {
        Add, Delete, Manipulation
    }

    public ActionType type;
    private List<String> objIDs;
    private Class<?> classType;

    Action(){}

    ActionType getActionType() { return type; }
    void setActionType(ActionType type) {
        this.type = type;
    }

    void setClassType(Class<?> classType)
    {
        if(classType != MoleculeAtom.class && classType != Molecule.class)
            throw new InvalidParameterException("Class type should be of MoleculeAtom or Molecule");

        this.classType = classType;
    }
    Class<?> getClassType() {return classType; }

    List<String> getObjIDList(){return objIDs; }
    void setObjIDList(List objIDs)
    {
        if( !(objIDs != null && objIDs.size() > 0) )
            throw new InvalidParameterException("the list must have ids in them");

        this.objIDs = objIDs;
    }
}
