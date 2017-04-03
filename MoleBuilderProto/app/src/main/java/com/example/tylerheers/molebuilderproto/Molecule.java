package com.example.tylerheers.molebuilderproto;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * Created by Tyler on 3/25/2017.
 */

class Molecule extends AtomContainer
{
    final float maxBondLength = 300f;
    final float maxScaleFactor = 1.5f;
    final float minScaleFactor = 0.5f;
    private float currScaleFactor = 1.0f;

    Molecule(){
        super();
    }
    Molecule(IAtomContainer aCon){
        super(aCon);
    }

    public float getScaleFactor(){return currScaleFactor; }

    void scaleMolecule(float sf)
    {
        sf = Math.max(minScaleFactor, Math.min(sf, maxScaleFactor));
        double avgBondDist = GeometryUtil.getBondLengthAverage(this);
        if(avgBondDist < maxBondLength)
            GeometryUtil.scaleMolecule(this, sf);
    }

    IBond.Order getBondOrderBetweenAtoms(IAtom a1, IAtom a2)
    {
        IBond b = getBond(a1, a2);
        return b.getOrder();
    }

    void removeAtom(MoleculeAtom atom)
    {
        // reduce bond number for all connected atoms
        for(IAtom conAtom : getConnectedAtomsList(atom)) {
            MoleculeAtom conMoleAtom = (MoleculeAtom)conAtom;
            conMoleAtom.delBond(getBondOrderBetweenAtoms(atom, conAtom));
        }
        for (IBond b: getConnectedBondsList(atom)) {
            removeBond(b);
            atom.delBond(b.getOrder());
        }

        super.removeAtom(atom);
    }

    void addMolecule(Molecule molecule)
    {
        for (IAtom a: molecule.atoms())
        {
            MoleculeAtom ma = (MoleculeAtom)a;
            ma.setMolecule(this);
            addAtom(a);
        }
        for (IBond b: molecule.bonds())
            addBond(b);


        molecule.removeAllElements();
    }
}
