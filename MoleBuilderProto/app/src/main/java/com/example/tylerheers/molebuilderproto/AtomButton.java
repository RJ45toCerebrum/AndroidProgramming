package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import org.openscience.cdk.interfaces.IElement;

/**
 * Created by tylerheers on 3/25/17.
 */

public class AtomButton extends Button
{
    private IElement element;

    public AtomButton(Context context) {
        super(context);
    }

    public AtomButton(Context context, AttributeSet set){
        super(context, set);
    }

    public AtomButton(Context context, AttributeSet set, int defStyle){
        super(context, set, defStyle);
    }

    public void setAtom(IElement atom)
    {
        if(atom == null){
            throw new NullPointerException("Atom cannot be null");
        }
        this.element = atom;
    }

    public IElement getAtom(){
        return element;
    }
}
