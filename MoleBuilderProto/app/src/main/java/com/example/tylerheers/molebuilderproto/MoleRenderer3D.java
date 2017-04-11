package com.example.tylerheers.molebuilderproto;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.opengl.Matrix;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.formats.SMILESFormat;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/**
 * Created by Tyler on 4/9/2017.
 * All about rendering atoms in 3D
 */

class MoleRenderer3D extends Renderer
{
    IAtomContainer molecule;
    private boolean is3D = false;
    private SparseArray<Material> materials;
    private Context context;
    private Scene scene;
    private List<Sphere> atomGeo;

    private DirectionalLight directionalLight;
    private org.rajawali3d.cameras.Camera cam;
    private float xPos, yPos;
    private float scaleFactor = 60;
    private boolean isScaling = false;


    private ScaleGestureDetector scaleDetector;

    MoleRenderer3D(Context context, IAtomContainer mole3d) throws Exception
    {
        super(context);

        if(mole3d == null)
            throw new Exception("Molecule must be 3 Dimensional");

        if(GeometryUtil.has3DCoordinates(mole3d))
            is3D = true;

        molecule = mole3d;
        this.context = context;
        this.setFrameRate(30);

        scaleDetector = new ScaleGestureDetector(getContext(), new MoleRenderer3D.ScaleListener());
    }

    @Override
    public void onTouchEvent(MotionEvent event)
    {
        scaleDetector.onTouchEvent(event);
        int actionID = event.getAction();
        if(actionID == MotionEvent.ACTION_DOWN)
        {
            xPos = event.getX();
            yPos = event.getY();
        }
        else if(actionID == MotionEvent.ACTION_MOVE && !isScaling)
        {
            float dx = (event.getX() - xPos);
            float dy = (event.getY() - yPos);
//            GeometryUtil.rotate(molecule, new Point2d(0,0), dx * 0.001f);
            //rotateMolecule(dx *0.1, dy);
            //updateAtomPositions();
        }

        isScaling = false;
        xPos = event.getX();
        yPos = event.getY();
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
        scene = getCurrentScene();
        initCamera();
        initDirLight();
        initColorMap();

        atomGeo = new ArrayList<>();

        // render mole center; for debugging
        Sphere centerSphere = new Sphere(0.05f, 10, 10);
        centerSphere.setPosition(0,0,0);
        centerSphere.setMaterial(materials.get(Color.BLUE));
        scene.addChild(centerSphere);
        // end render center

        constructGeo();
    }

    private void constructGeo()
    {
        if(is3D)
        {
            for (IAtom a : molecule.atoms())
            {
                Point3d point = a.getPoint3d();
                double x = point.getX();
                double y = point.getY();
                double z = point.getZ();
                Sphere atom;
                if (a.getSymbol().equals("H")) {
                    atom = new Sphere(0.1f, 10, 10);
                    atom.setMaterial(materials.get(Color.LTGRAY));
                } else {
                    atom = new Sphere(0.2f, 10, 10);
                    atom.setMaterial(materials.get(Color.DKGRAY));
                }

                atom.setPosition(x, y, z);
                atomGeo.add(atom);
                scene.addChild(atom);
            }
        }
        else
        {
            for (IAtom a : molecule.atoms())
            {
                Point2d point = a.getPoint2d();
                double x = point.getX();
                double y = point.getY();
                Sphere atom;
                if (a.getSymbol().equals("H"))
                {
                    atom = new Sphere(0.1f, 10, 10);
                    atom.setMaterial(materials.get(Color.LTGRAY));
                } else {
                    atom = new Sphere(0.2f, 10, 10);
                    atom.setMaterial(materials.get(Color.DKGRAY));
                }

                atom.setPosition(x, y, 0);
                atomGeo.add(atom);
                scene.addChild(atom);
            }
        }
    }

    private void initCamera()
    {
        cam = new org.rajawali3d.cameras.Camera();
        cam.enableLookAt();
        cam.setPosition(0, 0, 5);
        cam.setLookAt(0.0f, 0.0f, 0.0f);
        cam.setFarPlane(1000);
        cam.setNearPlane(1);
        cam.setFieldOfView(scaleFactor);
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

    private void initColorMap()
    {
        materials = new SparseArray<>(9);
        materials.put(Color.RED, createMaterial(Color.RED));
        materials.put(Color.GREEN, createMaterial(Color.GREEN));
        materials.put(Color.BLUE, createMaterial(Color.BLUE));
        materials.put(Color.BLACK, createMaterial(Color.BLACK));
        materials.put(Color.GRAY, createMaterial(Color.GRAY));
        materials.put(Color.YELLOW, createMaterial(Color.YELLOW));
        materials.put(Color.LTGRAY, createMaterial(Color.LTGRAY));
        materials.put(Color.DKGRAY, createMaterial(Color.DKGRAY));
        materials.put(Color.MAGENTA, createMaterial(Color.MAGENTA));
    }

    private int getColorID()
    {
        //TODO: implement getColorID in MoleRenderer3D
        return 0;
    }

    private Material createMaterial(int color)
    {
        Material mat = new Material();
        mat.setDiffuseMethod(new DiffuseMethod.Lambert());
        mat.enableLighting(true);
        mat.setColor(color);
        return mat;
    }

    @Nullable
    private Sphere createAtom(int color, float size)
    {
        if(scene == null)
            return null;

        Sphere newAtom = new Sphere(size, 10, 10);
        newAtom.setMaterial(materials.get(color));
        scene.addChild(newAtom);
        return newAtom;
    }

    private void rotateMolecule(double angX, double angY)
    {
        Matrix4 rotMatX = Matrix4.createRotationMatrix(0,0,0, angX);
        Matrix4 rotMatZ = Matrix4.createRotationMatrix(Vector3.Axis.Z, angX);
        Matrix4 rotMat = rotMatX.multiply(rotMatZ);
//        rotMat = rotMat.multiply(rotMatX);

        for (IAtom a: molecule.atoms()) {
             Point2d p = a.getPoint2d();
             Vector3 v = new Vector3(p.getX(), p.getY(),0);
             v = rotMat.projectVector(v);
             p.setX(v.x);
             p.setY(v.y);
        }
    }

    private void updateAtomPositions()
    {
        for (int i = 0; i < atomGeo.size(); i++)
        {
            Point2d point = molecule.getAtom(i).getPoint2d();
            double x = point.getX();
            double y = point.getY();
            Sphere atom = atomGeo.get(i);
            atom.setPosition(x, y, 0);
        }
    }

    private void centerMolecule()
    {
        //Point3d point = GeometryUtil.get3DCenter(molecule);
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(20, Math.min(scaleFactor, 110));

            cam.setFieldOfView(scaleFactor);
            isScaling = true;
            return true;
        }
    }
}
