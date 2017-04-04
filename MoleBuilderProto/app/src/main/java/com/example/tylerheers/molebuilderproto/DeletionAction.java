package com.example.tylerheers.molebuilderproto;

import org.openscience.cdk.Bond;

import java.util.ArrayList;
import java.util.List;

// Used for containing all information of a deletion action
// TODO: implement Deletion actions
public class DeletionAction extends Action
{

    MoleculeAtom deletedAtom;
    List<Pair<MoleculeAtom, Bond.Order>> conAtoms;

    public void setDeletedAtom(MoleculeAtom atom)
    {
        if(atom == null)
            throw new NullPointerException("The deleted atom mus not be null");

        deletedAtom = atom;
    }

    public void setConnectedAtoms(List<Pair<MoleculeAtom, Bond.Order>> conAtoms)
    {
        if(conAtoms == null)
            throw new NullPointerException("The deleted atom mus not be null");

        this.conAtoms = conAtoms;
    }

    public void addConnectedAtom(MoleculeAtom atom, Bond.Order order)
    {
        if(deletedAtom == null && atom == null)
            throw new NullPointerException("Deleted atom must be set and passed in atom must not be null");

        if(conAtoms == null)
            conAtoms = new ArrayList<>();

        Pair<MoleculeAtom, Bond.Order> info = new Pair<>();
        info.first = atom;
        info.second = order;
        conAtoms.add(info);
    }
}
