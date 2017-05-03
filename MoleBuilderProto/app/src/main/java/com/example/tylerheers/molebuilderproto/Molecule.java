package com.example.tylerheers.molebuilderproto;

import android.support.annotation.Nullable;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

import java.util.HashMap;

/**
 * Created by Tyler on 3/25/2017.
 *
 */

public class Molecule extends AtomContainer
{
    private SceneContainer sceneContainer;
    public Molecule() {
        super();
        sceneContainer = SceneContainer.getInstance();
    }

    public IBond.Order getBondOrderBetweenAtoms(IAtom a1, IAtom a2)
    {
        IBond b = getBond(a1, a2);
        return b.getOrder();
    }

    /**
     * Removes the Molecule atoms such that, all the bonds
     * on connected atoms are reduced in number
     * @param atom
     * @return: int
     */
    public void removeAtom(MoleculeAtom atom)
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
        if(getAtomCount() == 0)
            sceneContainer.delMolecule(getID());
    }

    /**
     * Adds the passed in molecule into to this Molecule
     * such that it is now one Molecule, and the passed in Molecule is emptied
     * @param molecule
     */
    public void addMolecule(Molecule molecule)
    {
        for (IAtom a: molecule.atoms())
        {
            MoleculeAtom ma = (MoleculeAtom)a;
            ma.setMolecule(this);
            addAtom(a);
        }
        for (IBond b: molecule.bonds())
            addBond(b);

        sceneContainer.delMolecule(molecule.getID());
        molecule.removeAllElements();
    }

    public void addAtom(MoleculeAtom a)
    {
        super.addAtom(a);
        a.setMolecule(this);
        sceneContainer.delAtom(a.getID());
    }

    /**
     * Converts the IAtomContainer into a Molecule
     * @param container
     * @return Molecule
     */
    public static Molecule convertAtomContainer(IAtomContainer container)
    {
        SceneContainer sceneContainer = SceneContainer.getInstance();
        HashMap<IBond, MoleculeAtom[]> bondAtoms = new HashMap<>();
        Molecule mole = new Molecule();

        /*
            they built it using IAtoms instead of MoleculeAtom
            which is what is needed;
            In order to perform the conversion:
            1) for each of the atoms, we make a new MoleculeAtom which is passed into constructor
            2) for each of the bonds connect to that atom:
                a) place it into a hashmap with key as bond. Why? because when i go to the next atom
                    then i can look up whether its already been added. I need to do this because
                    a bond is always connected to more than one atom.
                b) if no entry for the bond, then the algo has not seen it yet --> add it and put the
                    current MoleculeAtom into the first place of the MoleAtom[] of hashmap
                c) if there is an entry then this means we are at a bond but from the perspective of
                        a diff atom. Add atom into the second place of the MoleculeAtom[]

         */
        int numCreatedAtoms = sceneContainer.getAtomCount() + 1;
        int numCreatedBonds = sceneContainer.getBondCount() + 1;
        int numCreatedMolecules = sceneContainer.getMoleculeCount() + 1;
        for (int i = 0; i < container.getAtomCount(); i++)
        {
            IAtom a = container.getAtom(i);
            Elements e = MoleculeAtom.getElement(a);
            MoleculeAtom ma = new MoleculeAtom(e, a);
            ma.setID("atom"+ String.valueOf(numCreatedAtoms + i));
            mole.addAtom(ma);
            ma.setMolecule(mole);

            for (IBond b: container.getConnectedBondsList(a))
            {
                ma.addBond(b.getOrder());
                if(bondAtoms.containsKey(b)) {
                    bondAtoms.get(b)[1] = ma;
                }
                else {
                    MoleculeAtom[] conAtoms = new MoleculeAtom[2];
                    conAtoms[0] = ma;
                    bondAtoms.put(b, conAtoms);
                }
            }
        }

        for (IBond b: bondAtoms.keySet()) {
            b.setAtoms(bondAtoms.get(b));
            mole.addBond(b);
            b.setID("bond"+String.valueOf(numCreatedBonds));
            numCreatedBonds++;
        }

        mole.setID("mole"+String.valueOf(numCreatedMolecules));
        return  mole;
    }

    @Nullable
    public static String generateSmilesString(IAtomContainer container) throws CDKException
    {
        if(container == null)
            return null;

        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(container.getBuilder());
        for (IAtom atom: container.atoms())
        {
            IAtomType type = matcher.findMatchingAtomType(container, atom);
            AtomTypeManipulator.configure(atom, type);
        }
        CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(container.getBuilder());
        adder.addImplicitHydrogens(container);

        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Generic);
        String smiles = smilesGenerator.create(container);

        if(smiles.contains("#"))
            smiles = smiles.replace("#", "%23");

        return smiles;
    }
}
