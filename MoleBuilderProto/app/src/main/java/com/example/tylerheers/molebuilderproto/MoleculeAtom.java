package com.example.tylerheers.molebuilderproto;

import org.openscience.cdk.Atom;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

import java.security.InvalidParameterException;

/**
 * Created by Tyler on 3/26/2017.
 */

class MoleculeAtom extends Atom
{
    private Molecule molecule = null;
    private boolean isInMolecule = false;
    private int numBonds = 0;
    private int numConnectedAtoms;
    static final int maxNumBonds = 10;


    MoleculeAtom(Elements e){
        super(e.toIElement());
        this.setCovalentRadius(e.covalentRadius());
        this.setAtomicNumber(e.number());
    }

    MoleculeAtom(Elements e, IAtom a){
        super(a);
        this.setCovalentRadius(e.covalentRadius());
        this.setAtomicNumber(e.number());
    }

    void setMolecule(Molecule mole){
        molecule = mole;
        if(mole != null)
            isInMolecule = true;
    }

    boolean isInMolecule(){return isInMolecule; }

    Molecule getMolecule() {return molecule; }

    int getNumBonds() {return numBonds; }
    int getNumConnectedAtoms() {return numConnectedAtoms;}

    boolean addBond(IBond.Order order)
    {
        if(!canBond(order))
            return false;

        numBonds += order.numeric();
        numConnectedAtoms++;
        return true;
    }

    void delBond(IBond.Order order) throws InvalidParameterException
    {
        if(numBonds < order.numeric())
            throw new InvalidParameterException("Cannot delete a bond of order %1$d because not enough bonds");

        if(order == IBond.Order.SINGLE)
            numBonds--;
        else if(order == IBond.Order.DOUBLE)
            numBonds -= 2;
        else
            numBonds -=3;
    }

    boolean canBond(IBond.Order order) {
        return ( numBonds + order.numeric() ) <= maxNumBonds;
    }

    static Elements getElement(IAtom a)
    {
        Elements e;
        switch (a.getAtomicNumber())
        {
            case 1:
                e = Elements.Hydrogen;
                break;
            case 2:
                e = Elements.Helium;
                break;
            case 3:
                e = Elements.Lithium;
                break;
            case 4:
                e = Elements.Beryllium;
                break;
            case 5:
                e = Elements.Boron;
                break;
            case 6:
                e = Elements.Carbon;
                break;
            case 7:
                e = Elements.Nitrogen;
                break;
            case 8:
                e = Elements.Oxygen;
                break;
            case 9:
                e = Elements.Fluorine;
                break;
            case 10:
                e = Elements.Neon;
                break;
            case 11:
                e = Elements.Sodium;
                break;
            case 12:
                e = Elements.Magnesium;
                break;
            case 13:
                e = Elements.Aluminium;
                break;
            case 14:
                e = Elements.Silicon;
                break;
            case 15:
                e = Elements.Phosphorus;
                break;
            case 16:
                e = Elements.Sulfur;
                break;
            case 17:
                e = Elements.Chlorine;
                break;
            case 18:
                e = Elements.Argon;
                break;
            case 19:
                e = Elements.Potassium;
                break;
            case 20:
                e = Elements.Calcium;
                break;
            case 21:
                e = Elements.Scandium;
                break;
            case 22:
                e = Elements.Titanium;
                break;
            case 23:
                e = Elements.Vanadium;
                break;
            case 24:
                e = Elements.Chromium;
                break;
            case 25:
                e = Elements.Manganese;
                break;
            case 26:
                e = Elements.Iron;
                break;
            case 27:
                e = Elements.Cobalt;
                break;
            case 28:
                e = Elements.Nickel;
                break;
            case 29:
                e = Elements.Copper;
                break;
            case 30:
                e = Elements.Zinc;
                break;
            case 31:
                e = Elements.Gallium;
                break;
            case 32:
                e = Elements.Germanium;
                break;
            case 33:
                e = Elements.Arsenic;
                break;
            case 34:
                e = Elements.Selenium;
                break;
            case 35:
                e = Elements.Bromine;
                break;
            case 36:
                e = Elements.Krypton;
                break;
            case 40:
                e = Elements.Zirconium;
                break;
            case 46:
                e = Elements.Palladium;
                break;
            case 47:
                e = Elements.Silver;
                break;
            case 49:
                e = Elements.Indium;
                break;
            case 50:
                e = Elements.Tin;
                break;
            case 51:
                e = Elements.Antimony;
                break;
            case 52:
                e = Elements.Tellurium;
                break;
            case 53:
                e = Elements.Iodine;
                break;
            case 81:
                e = Elements.Thallium;
                break;
            case 82:
                e = Elements.Lead;
                break;
            case 83:
                e = Elements.Bismuth;
                break;
            case 84:
                e = Elements.Polonium;
                break;
            default:
                e = Elements.Hydrogen;
        }

        return e;
    }

    static int getMaxNumBonds(Elements e) {
        return maxNumBonds;
    }
}
