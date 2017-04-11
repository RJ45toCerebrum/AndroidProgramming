package com.example.tylerheers.molebuilderproto;


import android.support.annotation.Nullable;
import android.util.Log;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.MDLV2000Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

class SdfConverter
{
    @Nullable
    static IAtomContainer convertSDFString(String sdfString)
    {
        if(sdfString == null || sdfString.length() == 0)
            return null;

        MDLV2000Reader sdfReader = new MDLV2000Reader();
        IAtomContainer container = new AtomContainer();
        try
        {
            InputStream inputStream = new ByteArrayInputStream(sdfString.getBytes());
            sdfReader.setReader(inputStream);
            container = sdfReader.read(container);
        }
        catch (CDKException ex) {
            container = null;
            Log.e("CDK Exception", ex.getMessage());
        }
        finally
        {
            try {
                sdfReader.close();
            }
            catch (IOException ex) {
                Log.d("Error in IO", ex.getMessage());
            }
        }

        return container;
    }
}
