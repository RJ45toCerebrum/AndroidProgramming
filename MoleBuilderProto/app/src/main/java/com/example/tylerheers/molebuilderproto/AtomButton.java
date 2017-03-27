package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IElement;

/**
 * Created by tylerheers on 3/25/17.
 */

public class AtomButton extends Button
{
    private Elements element;

    public AtomButton(Context context) {
        super(context);
    }

    public AtomButton(Context context, AttributeSet set){
        super(context, set);
    }

    public AtomButton(Context context, AttributeSet set, int defStyle){
        super(context, set, defStyle);
    }

    public void setElement(Elements element)
    {
        if(element == null){
            throw new NullPointerException("Atom cannot be null");
        }
        this.element = element;
    }


    public Elements getElement(){
        return element;
    }
}
