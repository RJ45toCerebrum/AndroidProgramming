package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import org.openscience.cdk.Bond;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cylinder;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/**
 * Created by Tyler on 4/9/2017.
 * All about rendering atoms in 3D
 */

class MoleRenderer3D extends Renderer
{
    private IAtomContainer molecule;
    private boolean is3D = false;
    private SparseArray<Material> materials;
    private Context context;
    private Scene scene;
    private List<Sphere> atomGeo;
    private List<MoleRenderer3D.Bond> bondGeo;

    private DirectionalLight directionalLight;
    private org.rajawali3d.cameras.Camera cam;
    private float xPos, yPos;
    private float scaleFactor = 60;
    private boolean isScaling = false;
    private GestureDetector gestureDetector;
    private boolean autoRotation = false;


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
        gestureDetector = new GestureDetector(this.getContext(), new GestureListener());
    }

    @Override
    public void onTouchEvent(MotionEvent event)
    {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        int actionID = event.getAction();
        if(actionID == MotionEvent.ACTION_DOWN)
        {
            xPos = event.getX();
            yPos = event.getY();
        }
        else if(actionID == MotionEvent.ACTION_MOVE && !isScaling)
        {
            autoRotation = false;
            float dx = (event.getX() - xPos);
            float dy = (event.getY() - yPos);
            rotateMolecule(-dy * 0.1, -dx * 0.1);
            updateGeoPositions();
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
        if(autoRotation){
            rotateMolecule(0, 0.5);
            updateGeoPositions();
        }
    }


    public void initScene()
    {
        scene = getCurrentScene();
        initCamera();
        initDirLight();
        initColorMap();

        atomGeo = new ArrayList<>(molecule.getAtomCount());
        //bondGeo = new HashMap<>(molecule.getBondCount());
        bondGeo = new ArrayList<>(molecule.getBondCount());

        // render mole center; for debugging
        Sphere centerSphere = new Sphere(0.05f, 10, 10);
        centerSphere.setPosition(0,0,0);
        centerSphere.setMaterial(materials.get(Color.BLUE));
        scene.addChild(centerSphere);
        // end render center

        constructMoleculeGeo();
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
        materials.put(Color.BLUE & Color.RED, createMaterial(0xF075FF));
    }

    private int getColorID(String symbol)
    {
        switch (symbol)
        {
            case "H":
                return Color.LTGRAY;
            case "C":
                return Color.DKGRAY;
            case "N":
                return Color.BLUE;
            case "O":
                return Color.RED;
            case "F":
                return Color.MAGENTA;
            case "Cl":
                return Color.GREEN;
            case "S":
                return Color.YELLOW;
            case "P":
                return Color.BLUE & Color.RED;
            default:
                return Color.LTGRAY;
        }
    }

    private Material createMaterial(int color)
    {
        Material mat = new Material();
        mat.setDiffuseMethod(new DiffuseMethod.Lambert());
        mat.enableLighting(true);
        mat.setColor(color);
        return mat;
    }

    private void constructMoleculeGeo()
    {
        Set<IBond> bonds = new HashSet<>(molecule.getBondCount());
        if(is3D)
        {
            for (IAtom a : molecule.atoms())
            {
                Point3d point = a.getPoint3d();
                double x = point.getX();
                double y = point.getY();
                double z = point.getZ();
                Sphere atom = createAtom(getColorID(a.getSymbol()), 0.3f);
                if(a.getSymbol().equals("H"))
                    atom.setScale(0.2f);

                atom.setPosition(x, y, z);

                // bond creation
                for (IAtom conAtom: molecule.getConnectedAtomsList(a))
                {
                    IBond bond = molecule.getBond(a, conAtom);
                    if(bonds.contains(bond))
                        continue;

                    bonds.add(bond);
                    Vector3 vect = getVectorBetween(point, conAtom.getPoint3d());
                    Point3d bPoint = bond.get3DCenter();
                    //Cylinder bondGeo = createBond(0.05f, (float)vect.length(), conAtom, a, bond);
                    MoleRenderer3D.Bond b = new Bond(0.05f, (float)vect.length(), conAtom, a, bond);
                    b.setPosition(bPoint.x, bPoint.y, bPoint.z);
                    Point3d caP = conAtom.getPoint3d();
                    b.setLookAt(caP.x, caP.y, caP.z);
                    bondGeo.add(b);
                }
            }
        }
        // sometimes even 3D molecules only need 2D coordinates like C(triple_bond)N
        else
        {
            for (IAtom a : molecule.atoms())
            {
                Point2d point = a.getPoint2d();
                double x = point.getX();
                double y = point.getY();
                Sphere atom = createAtom(getColorID(a.getSymbol()), 0.3f);
                if(a.getSymbol().equals("H"))
                    atom.setScale(0.2f);

                atom.setPosition(x, y, 0);
                a.setPoint3d(new Point3d(x, y, 0));

                // bond creation
                for (IAtom conAtom: molecule.getConnectedAtomsList(a))
                {
                    IBond bond = molecule.getBond(a, conAtom);
                    if(bonds.contains(bond))
                        continue;

                    bonds.add(bond);
                    Point3d point3d = new Point3d(point.x, point.y, 0);
                    Point3d conAtomPoint = new Point3d(conAtom.getPoint2d().x, conAtom.getPoint2d().y, 0);
                    Vector3 vect = getVectorBetween(point3d, conAtomPoint);
                    Point2d bPoint = bond.get2DCenter();
                    MoleRenderer3D.Bond b = new Bond(0.05f, (float)vect.length(), conAtom, a, bond);
                    b.setPosition(bPoint.x, bPoint.y, 0);
                    Point2d caP = conAtom.getPoint2d();
                    b.setLookAt(caP.x, caP.y, 0);
                    bondGeo.add(b);
                }
            }
        }
    }

    private Sphere createAtom(int color, float size)
    {
        Sphere newAtom = new Sphere(size, 10, 10);
        newAtom.setMaterial(materials.get(color));
        atomGeo.add(newAtom);
        scene.addChild(newAtom);
        return newAtom;
    }

    private void rotateMolecule(double angX, double angY)
    {
        Matrix4 rotMatY = Matrix4.createRotationMatrix(Vector3.Axis.Y, angY);
        Matrix4 rotMatX = Matrix4.createRotationMatrix(Vector3.Axis.X, angX);
        Matrix4 rotMat = rotMatY.multiply(rotMatX);

        for (IAtom a: molecule.atoms())
        {
             Point3d p = a.getPoint3d();
             Vector3 v = new Vector3(p.getX(), p.getY(), p.getZ());
             v = rotMat.projectVector(v);
             p.setX(v.x);
             p.setY(v.y);
             p.setZ(v.z);

            for (IBond b: molecule.bonds())
            {
                Point3d bP = b.get3DCenter();
                Vector3 bV = new Vector3(bP.getX(), bP.getY(), bP.getZ());
                bV = rotMat.projectVector(bV);
                bP.set(bV.x, bV.y, bV.z);
            }
        }
    }

    private void updateGeoPositions()
    {
        for (int i = 0; i < atomGeo.size(); i++)
        {
            Point3d point = molecule.getAtom(i).getPoint3d();
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();
            Sphere atom = atomGeo.get(i);
            atom.setPosition(x, y, z);
        }

        for(int i = 0; i < bondGeo.size(); i++)
        {
            IBond b = molecule.getBond(i);
            Point3d point = b.get3DCenter();
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();

            bondGeo.get(i).setPosition(x, y, z);
            Point3d atomPoint = bondGeo.get(i).getAtoms()[1].getPoint3d();
            bondGeo.get(i).setLookAt(atomPoint.x, atomPoint.y, atomPoint.z);

//            Pair<Cylinder, IAtom[]> bond = bondGeo.get(b);
//            bond.first.setPosition(x, y, z);
//            Point3d atomPoint = bond.second[0].getPoint3d();
//            bond.first.setLookAt(atomPoint.x, atomPoint.y, atomPoint.z);
        }
    }

    // vector pointing from a --> B
    private Vector3 getVectorBetween(Point3d a, Point3d b)
    {
        Vector3 aVect = new Vector3(a.x, a.y, a.z);
        Vector3 bVect = new Vector3(b.x, b.y, b.z);
        return Vector3.subtractAndCreate(aVect, bVect);
    }

    private class Bond
    {
        private List<Cylinder> bondGeo;
        private IAtom[] atoms;
        private IBond bond;
        float length = 1;
        float radius = 1;
        final float bondOffset = 0.15f;

        Bond(float radius, float length, IAtom a1, IAtom a2, IBond b)
        {
            this.radius = radius;
            this.length = length;
            this.bond = b;
            atoms = new IAtom[]{a1, a2};
            bondGeo = new ArrayList<>();

            createBondGeo();
        }

        void createBondGeo()
        {
            Cylinder bondCylinder = new Cylinder(length, radius, 2, 5);
            bondCylinder.setPosition(0, 0, 0);
            bondCylinder.setMaterial(materials.get(Color.LTGRAY));
            bondGeo.add(bondCylinder);
            scene.addChild(bondCylinder);

            if(bond.getOrder().numeric() >= 2)
            {
                Cylinder bondCylinder2 = new Cylinder(length, radius, 2, 5);
                bondCylinder2.setPosition(0, bondOffset, 0);
                bondCylinder2.setMaterial(materials.get(Color.LTGRAY));
                bondCylinder.addChild(bondCylinder2);
                bondGeo.add(bondCylinder2);
                scene.addChild(bondCylinder2);

                if(bond.getOrder() == IBond.Order.TRIPLE)
                {
                    Cylinder bondCylinder3 = new Cylinder(length, radius, 2, 5);
                    bondCylinder3.setPosition(0, -bondOffset, 0);
                    bondCylinder3.setMaterial(materials.get(Color.LTGRAY));
                    bondCylinder.addChild(bondCylinder3);
                    bondGeo.add(bondCylinder3);
                    scene.addChild(bondCylinder3);
                }
            }

        }

        void setPosition(double x, double y, double z)
        {
            Cylinder b1 = bondGeo.get(0);
            b1.setPosition(x, y, z);
        }

        void setLookAt(double x, double y, double z)
        {
            // parenting should rotate others
            Cylinder b1 = bondGeo.get(0);
            b1.setLookAt(x, y, z);
        }

        IAtom[] getAtoms(){ return atoms;}
        List<Cylinder> getBonds() {return bondGeo;}
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            Log.i("R", "Rot");
            autoRotation = !autoRotation;
            return true;
        }
    }
}
