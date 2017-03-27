package com.example.tylerheers.molebuilderproto;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtomContainer;

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

}
