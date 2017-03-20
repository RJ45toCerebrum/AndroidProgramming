package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;


import org.openscience.cdk.interfaces.IElement;

/**
 * Created by Tyler on 3/10/2017.
 */

public class AtomImageButton extends ImageButton
{
   private IElement element;

    public AtomImageButton(Context context) {
        super(context);
    }

    public AtomImageButton(Context context, AttributeSet set){
        super(context, set);
    }

    public AtomImageButton(Context context, AttributeSet set, int defStyle){
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
