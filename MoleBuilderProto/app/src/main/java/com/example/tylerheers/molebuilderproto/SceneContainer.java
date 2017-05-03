package com.example.tylerheers.molebuilderproto;

import org.openscience.cdk.interfaces.IAtom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tylerheers on 4/11/17.
 * This is a container that holds all scene information
 */
public class SceneContainer
{
    public enum SceneChangeType
    {
        AtomNumber, BondNumber, MoleculeNumber, All
    }

    // For those who want to know about scene changes
    interface SceneChangeListener
    {
        void atomNumberChanged();
        void bondNumberChanged();
        void moleculeNumberChanged();
    }

    private static SceneContainer ourInstance = new SceneContainer();

    public static SceneContainer getInstance() {
        return ourInstance;
    }

    private SceneContainer() {}

    private HashMap<String, MoleculeAtom> atoms = new HashMap<>();
    private HashMap<String, Molecule> molecules = new HashMap<>();

    static final int maxAtomsForMolecule = 100;

    public Molecule selectedMolecule = null;


    private List<SceneChangeListener> sceneChangeListeners;

    public boolean putAtom(MoleculeAtom ma)
    {
        if (ma != null)
        {
            atoms.put(ma.getID(), ma);
            updateSceneListeners(SceneChangeType.AtomNumber);
            return true;
        }

        return false;
    }
    public Collection<MoleculeAtom> getAtoms() {
        return atoms.values();
    }


    public boolean delAtom(String key)
    {
        if (atoms.remove(key) != null)
        {
            updateSceneListeners(SceneChangeType.AtomNumber);
            return true;
        }

        return false;
    }
    public boolean delMolecule(String key)
    {
        Molecule mole = molecules.remove(key);
        if (mole != null)
        {
            updateSceneListeners(SceneChangeType.MoleculeNumber);
            return true;
        }

        return false;
    }

    public boolean putMolecule(Molecule mole)
    {
        if (mole != null)
        {
            for (IAtom a: mole.atoms()) {
                MoleculeAtom ma = (MoleculeAtom)a;
                atoms.remove(ma.getID());
            }

            molecules.put(mole.getID(), mole);
            updateSceneListeners(SceneChangeType.MoleculeNumber);
            return true;
        }

        return false;
    }
    public Collection<Molecule> getMolecules() {
        return molecules.values();
    }

    public void clearScene()
    {
        atoms.clear();
        molecules.clear();

        updateSceneListeners(SceneChangeType.All);
    }


    public int getBondCount()
    {
        int bondCount = 0;
        for (Molecule m: molecules.values()) {
            bondCount += m.getBondCount();
        }
        return bondCount;
    }

    public int getAtomCount()
    {
        int numAtoms = atoms.size();
        for (Molecule m: molecules.values()) {
            numAtoms += m.getAtomCount();
        }
        return numAtoms;
    }

    public int getMoleculeCount() {
        return molecules.size();
    }

    public Molecule getMolecule(String key) {
        return molecules.get(key);
    }

    public void addSceneChangeListener(SceneChangeListener listener)
    {
        if (sceneChangeListeners == null)
            sceneChangeListeners = new ArrayList<>();

        sceneChangeListeners.add(listener);
    }

    public void updateSceneListeners(SceneChangeType type)
    {

        if(type == SceneChangeType.AtomNumber)
            for (SceneChangeListener listener : sceneChangeListeners)
                listener.atomNumberChanged();
        else if(type == SceneChangeType.BondNumber)
            for (SceneChangeListener listener : sceneChangeListeners)
                listener.bondNumberChanged();
        else if(type == SceneChangeType.MoleculeNumber)
            for (SceneChangeListener listener : sceneChangeListeners)
                listener.moleculeNumberChanged();
        else
        {
            for (SceneChangeListener listener : sceneChangeListeners)
            {
                listener.atomNumberChanged();
                listener.bondNumberChanged();
                listener.moleculeNumberChanged();
            }
        }

    }

    public boolean isAtomInMolecule(IAtom a)
    {
        for (Molecule m: molecules.values()) {
            if(m.contains(a))
                return true;
        }

        return false;
    }
}
