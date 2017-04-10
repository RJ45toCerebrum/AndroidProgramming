package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.openscience.cdk.interfaces.IAtom;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
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
    private Scene scene;
    private List<Sphere> atomGeo;

    DirectionalLight directionalLight;
    org.rajawali3d.cameras.Camera cam;
    float camPos;

    MoleRenderer3D(Context context)
    {
        super(context);
        this.context = context;
        this.setFrameRate(30);
    }

    @Override
    public void onTouchEvent(MotionEvent event){
        // this is where we can perform rotations around the molecule
        camPos += 0.1f;

        //cam.setLookAt(atomGeo.get(0).getPosition());
        cam.setY(Math.cos(camPos));
    }

    public void onOffsetsChanged(float x, float y, float w, float h, int i, int j){

    }

    @Override
    public void onRender(final long elapsed, final double deltaTime) {
        super.onRender(elapsed, deltaTime);
    }


    public void initScene()
    {
        scene = getCurrentScene();
        initCamera();
        initDirLight();

        atomGeo = new ArrayList<>();
        Material atomMat = new Material();
        atomMat.setDiffuseMethod(new DiffuseMethod.Lambert());
        atomMat.enableLighting(true);
        atomMat.setColor(Color.GRAY);

        for (IAtom a : MainActivity.getAtoms())
        {
            Point2d point = a.getPoint2d();
            Log.i("Atom Pos", point.toString());

            double x = point.getX();
            double y = point.getY();
            x = (x / getViewportWidth()) * 10;
            y = (y / getViewportHeight()) * 10;

            Sphere atom = new Sphere(0.5f, 10, 10);
            atom.setPosition(x, y, 0);
            atom.setMaterial(atomMat);
            atomGeo.add(atom);
            scene.addChild(atom);
        }
    }

    private void initCamera()
    {
        cam = new org.rajawali3d.cameras.Camera();
        cam.setPosition(0, 0, 5);
        cam.setLookAt(0.0f, 0.0f, 0.0f);
        cam.setFarPlane(1000);
        cam.setNearPlane(1);
        cam.setFieldOfView(120);
        scene.replaceAndSwitchCamera(cam, 0);
        scene.addCamera(cam);
    }

    private void initDirLight()
    {
        directionalLight = new DirectionalLight(1f, 0.2f, -1.0f);
        directionalLight.setColor(1.0f, 0.9f, 0.9f);
        directionalLight.setPower(1.5f);
        scene.addLight(directionalLight);
        directionalLight.setPosition(5, 0, 5);
    }

}
