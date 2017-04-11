package com.example.tylerheers.molebuilderproto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tylerheers on 4/11/17.
 * This is a container that holds all scene information
 */
class SceneContainer
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

    static SceneContainer getInstance() {
        return ourInstance;
    }

    private SceneContainer() {}

    private HashMap<String, MoleculeAtom> atoms = new HashMap<>();
    private HashMap<String, Molecule> molecules = new HashMap<>();

    Molecule selectedMolecule = null;


    private List<SceneChangeListener> sceneChangeListeners;

    boolean putAtom(MoleculeAtom ma)
    {
        if (ma != null)
        {
            atoms.put(ma.getID(), ma);
            updateSceneListeners(SceneChangeType.AtomNumber);
            return true;
        }

        return false;
    }
    Collection<MoleculeAtom> getAtoms() {
        return atoms.values();
    }


    boolean delAtom(String key)
    {
        if (atoms.remove(key) != null)
        {
            updateSceneListeners(SceneChangeType.AtomNumber);
            return true;
        }

        return false;
    }
    boolean delMolecule(String key)
    {
        Molecule mole = molecules.remove(key);
        if (mole != null)
        {
            updateSceneListeners(SceneChangeType.MoleculeNumber);
            return true;
        }

        return false;
    }

    boolean putMolecule(Molecule mole)
    {
        if (mole != null)
        {
            molecules.put(mole.getID(), mole);
            updateSceneListeners(SceneChangeType.MoleculeNumber);
            return true;
        }

        return false;
    }
    Collection<Molecule> getMolecules() {
        return molecules.values();
    }


    int getBondCount()
    {
        int bondCount = 0;
        for (Molecule m: molecules.values()) {
            bondCount += m.getBondCount();
        }
        return bondCount;
    }

    int getAtomCount() {
        return atoms.size();
    }

    int getMoleculeCount() {
        return molecules.size();
    }

    Molecule getMolecule(String key) {
        return molecules.get(key);
    }

    void addSceneChangeListener(SceneChangeListener listener)
    {
        if (sceneChangeListeners == null)
            sceneChangeListeners = new ArrayList<>();

        sceneChangeListeners.add(listener);
    }

    void updateSceneListeners(SceneChangeType type)
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

}
