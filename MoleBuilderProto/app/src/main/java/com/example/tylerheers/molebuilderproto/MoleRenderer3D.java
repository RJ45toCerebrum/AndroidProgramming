package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.Toast;

import org.openscience.cdk.interfaces.IAtom;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cylinder;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

/**
 * Created by Tyler on 4/9/2017.
 * All about rendering atoms in 3D
 */

class MoleRenderer3D extends Renderer
{
    private Context context;
    private List<Sphere> atomGeo;

    MoleRenderer3D(Context context)
    {
        super(context);
        // renderer needs a context; this is actually android specific
        this.context = context;
        // set the frame rate of the rendere
        this.setFrameRate(60);
    }

    @Override
    public void onTouchEvent(MotionEvent event){
        Toast.makeText(context, "Touching", Toast.LENGTH_SHORT).show();
    }

    public void onOffsetsChanged(float x, float y, float w, float h, int i, int j){

    }

    @Override
    public void onRender(final long elapsed, final double deltaTime)
    {
        super.onRender(elapsed, deltaTime);
    }


    public void initScene()
    {
//        atomGeo = new ArrayList<>();
//        DirectionalLight directionalLight = new DirectionalLight(1f, 0.2f, -1.0f);
//        directionalLight.setColor(1.0f, 1.0f, 1.0f);
//        directionalLight.setPower(2f);
//        getCurrentScene().addLight(directionalLight);
//
//        Scene scene = getCurrentScene();
//        for (IAtom a: MainActivity.getAtoms()) {
//            Sphere geo = new Sphere(0.1f, 8, 8);
//            atomGeo.add(geo);
//            Point2d placement = a.getPoint2d();
//            geo.setPosition(placement.getX()/200, placement.getY()/200, 0);
//            scene.addChild(geo);
//        }
    }

}
