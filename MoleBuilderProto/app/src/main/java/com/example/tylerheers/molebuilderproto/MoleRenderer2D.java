package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Created by tylerheers on 3/10/17.
 */

public class MoleRenderer2D extends View
{
    public static int maxAtoms = 30;
    public static int maxSelectedAtoms = 10;

    MainActivity moleculeActivity;

    // fields for Atom's
    int numCreatedAtoms = 0;
    ArrayList<MoleculeAtom> atoms = new ArrayList<>();
    Queue<MoleculeAtom> atomSelectionQ = new LinkedList<>();

    ArrayList<Molecule> molecules;
    MoleculeAtom selectedAtom = null;
    IAtomContainer selectedMolecule = null;
    float atomCircleRadius = 55.0f;
    float textSize = 60.0f;

    // for selection
    GestureDetector gestureDetector;
    ScaleGestureDetector scalerDetector;
    float scaleFactor = 1.0f;
    boolean canMove = false;
    Point2d lastPointerLoc = new Point2d();

    // line fields
    Paint atomPaint;
    Paint selectedAtomPaint;

    // efficiency
    Bitmap rendererBitmap;
    boolean takingScreenShot = false;
    float lastActive = 0;
    float maxInactiveTime = 2000.0f;


    public MoleRenderer2D(Context context){
        super(context);
        init(context);
    }

    public MoleRenderer2D(Context context, AttributeSet set) {
        super(context, set);
        setFocusable(true);
        setFocusableInTouchMode(true);
        init(context);
    }

    public MoleRenderer2D(Context context, AttributeSet set, int defStyle){
        super(context, set, defStyle);
        init(context);
    }


    private void init(Context con)
    {
        moleculeActivity = (MainActivity) con;
        molecules = new ArrayList<>();

        atomPaint = new Paint();
        atomPaint.setColor(Color.BLACK);
        atomPaint.setStrokeWidth(5);
        atomPaint.setStyle(Paint.Style.STROKE);
        atomPaint.setTextSize(textSize);
        atomPaint.setTextAlign(Paint.Align.CENTER);

        selectedAtomPaint = new Paint();
        selectedAtomPaint.setColor(Color.GREEN);
        selectedAtomPaint.setStrokeWidth(5);
        selectedAtomPaint.setStyle(Paint.Style.STROKE);
        selectedAtomPaint.setTextSize(textSize);
        atomPaint.setTextAlign(Paint.Align.CENTER);

        gestureDetector = new GestureDetector(this.getContext(), new GestureListener());
        scalerDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public void addAtom(Elements atom)
    {
        if(atoms.size() < maxAtoms)
        {
            atomSelectionQ.clear();
            numCreatedAtoms++;

            MoleculeAtom newAtom = new MoleculeAtom(atom);
            newAtom.setID("atom"+String.valueOf(numCreatedAtoms));
            newAtom.setPoint2d(new Point2d(200, 300));
            atoms.add(newAtom);
            rendererBitmap = null;
            lastActive = System.currentTimeMillis();
            postInvalidate();
        }
    }

    public void deleteSelected()
    {
        for (IAtom a: atomSelectionQ)
        {
            MoleculeAtom atom = (MoleculeAtom)a;
            atoms.remove(a);
            for(Molecule mole : molecules)
            {
                if(mole.contains(atom)) {
                    for (IBond b: mole.getConnectedBondsList(a)) {
                        mole.removeBond(b);
                        atom.delBond(b.getOrder());
                    }
                    mole.removeAtom(a);
                }
            }
        }

        rendererBitmap = null;
        postInvalidate();
    }

    // adds a bond between currently queued atoms
    public void addBond(IBond.Order order)
    {
        if(atomSelectionQ.size() != 2)
            return;

        MoleculeAtom a1 = atomSelectionQ.poll();
        MoleculeAtom a2 = atomSelectionQ.poll();
        if(!a1.canBond(order) || !a2.canBond(order))
            return;

        atoms.remove(a1);
        atoms.remove(a2);

        Molecule mole;
        // check if atom one is already a part of a molecule
        if(a1.isInMolecule())
        {
            Molecule atom2Mole = a2.getMolecule();
            if(atom2Mole != null)
                atom2Mole.removeAtom(a2);

            mole = a1.getMolecule();
            mole.addAtom(a2);
            a2.setMolecule(mole);
        }
        else if(a2.isInMolecule())
        {
            mole = a2.getMolecule();
            mole.addAtom(a1);
            a1.setMolecule(mole);
        }
        else
        {
            mole = new Molecule();
            mole.addAtom(a1);
            mole.addAtom(a2);
            a1.setMolecule(mole);
            a2.setMolecule(mole);
        }

        Bond newBond = new Bond(a1, a2);
        a1.addBond(order);
        a2.addBond(order);
        newBond.setOrder(order);
        mole.addBond(newBond);
        molecules.add(mole);
        atomSelectionQ.clear();

        rendererBitmap = null;
        lastActive = System.currentTimeMillis();
        postInvalidate();
    }

    public void addMolecule(Molecule mole)
    {
        if (mole != null)
        {
            normalizePositions(mole);
            GeometryUtil.translate2DCenterTo(mole, new Point2d(200, 200));
            molecules.add(mole);

            rendererBitmap = null;
            lastActive = System.currentTimeMillis();
        }
    }

    private void normalizePositions(IAtomContainer atomContainer)
    {
        for (IAtom a: atomContainer.atoms()) {
            Point2d point = a.getPoint2d();
            point.scale(300.0);
        }
    }

    @NonNull
    private Vector2d getOrthoVector(Point2d p1, Point2d p2)
    {
        Vector2d v = new Vector2d();
        v.setX(p1.getX()-p2.getX());
        v.setY(p1.getY()-p2.getY());

        double x = v.getX();
        double y = v.getY();
        v.setX(y);
        v.setY(-x);
        v.normalize();
        return v;
    }

    private long getBondHashCode(IBond b){
        IAtom a1 = b.getAtom(0);
        IAtom a2 = b.getAtom(1);
        return  a1.hashCode() + a2.hashCode();
    }

    void drawAtoms(Canvas canvas)
    {
        // for individually created atoms
        for (Atom a: atoms)
        {
            Point2d aPoint = a.getPoint2d();
            if(a == selectedAtom || atomSelectionQ.contains(a))
                canvas.drawCircle((float)aPoint.getX(), (float)aPoint.getY(),
                        atomCircleRadius * scaleFactor, selectedAtomPaint);
            else
                canvas.drawCircle((float)aPoint.getX(), (float)aPoint.getY(),
                        atomCircleRadius * scaleFactor, atomPaint);

            float x = (float)aPoint.getX();
            float y = (float)aPoint.getY();
            atomPaint.setTextSize(textSize * scaleFactor);
            canvas.drawText(a.getSymbol(),x, y+atomCircleRadius/2, atomPaint);
        }
    }

    void drawAtoms(Canvas canvas, IAtomContainer con)
    {
        for (IAtom a: con.atoms())
        {
            Point2d aPoint = a.getPoint2d();
            float x = (float)aPoint.getX();
            float y = (float)aPoint.getY();
            if(atomSelectionQ.contains(a))
                canvas.drawCircle(x, y, atomCircleRadius * scaleFactor, selectedAtomPaint);
            else
                canvas.drawCircle(x, y, atomCircleRadius * scaleFactor, atomPaint);

            atomPaint.setTextSize(textSize * scaleFactor);
            canvas.drawText(a.getSymbol(), x, y+atomCircleRadius/2, atomPaint);
        }
    }

    void drawBonds(Canvas canvas)
    {
        // for each molecule
        for (int i = 0; i < molecules.size(); i++)
        {
            IAtomContainer mole = molecules.get(i);
            // go through all bonds
            for (IBond b : mole.bonds())
            {
                Point2d a1Pos = b.getAtom(0).getPoint2d();
                Point2d a2Pos = b.getAtom(1).getPoint2d();

                float xPos1 = (float) a1Pos.getX();
                float yPos1 = (float) a1Pos.getY();
                float xPos2 = (float) a2Pos.getX();
                float yPos2 = (float) a2Pos.getY();

                switch (b.getOrder()) {
                    case SINGLE:
                        canvas.drawLine(xPos1, yPos1, xPos2, yPos2, atomPaint);
                        break;
                    case DOUBLE:
                        Vector2d ortho = getOrthoVector(a1Pos, a2Pos);
                        ortho.scale(20);
                        canvas.drawLine(xPos1, yPos1, xPos2, yPos2, atomPaint);
                        canvas.drawLine(xPos1 + (float) ortho.getX(), yPos1 + (float) ortho.getY(),
                                xPos2 + (float) ortho.getX(), yPos2 + (float) ortho.getY(), atomPaint);
                        break;
                    case TRIPLE:
                        Vector2d ortho2 = getOrthoVector(a1Pos, a2Pos);
                        ortho2.scale(20);
                        canvas.drawLine(xPos1, yPos1, xPos2, yPos2, atomPaint);
                        canvas.drawLine(xPos1 + (float) ortho2.getX(), yPos1 + (float) ortho2.getY(),
                                xPos2 + (float) ortho2.getX(), yPos2 + (float) ortho2.getY(), atomPaint);
                        canvas.drawLine(xPos1 - (float) ortho2.getX(), yPos1 - (float) ortho2.getY(),
                                xPos2 - (float) ortho2.getX(), yPos2 - (float) ortho2.getY(), atomPaint);
                        break;
                    default:
                        break;
                }

                canvas.drawLine((float) a1Pos.getX(), (float) a1Pos.getY(),
                        (float) a2Pos.getX(), (float) a2Pos.getY(), atomPaint);
            }

        }
    }

    void drawMolecules(Canvas canvas)
    {
        // draw each in atom container; for Molecules
        for (int i = 0; i < molecules.size(); i++)
        {
            IAtomContainer con = molecules.get(i);
            drawAtoms(canvas, con);
        }
        drawBonds(canvas);
        postInvalidate();
    }

    private void screenShot()
    {
        takingScreenShot = true;
        lastActive = System.currentTimeMillis();
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache(true);
        rendererBitmap = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);
        Log.i("Image created", "the image was created");
        takingScreenShot = false;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (rendererBitmap == null) {
            drawAtoms(canvas);
            drawMolecules(canvas);
            drawBonds(canvas);
            if ((System.currentTimeMillis() - lastActive) > maxInactiveTime && !takingScreenShot)
                screenShot();
        }
        else
            canvas.drawBitmap(rendererBitmap, 0, getTop(), atomPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        rendererBitmap = null;
        lastActive = System.currentTimeMillis();
        gestureDetector.onTouchEvent(event);
        //scalerDetector.onTouchEvent(event);

        switch (event.getAction())
        {
            case  MotionEvent.ACTION_DOWN:
                lastPointerLoc.set(event.getX(), event.getY());
            case MotionEvent.ACTION_MOVE:
                if (selectedAtom != null) {
                    for (IAtom a : atomSelectionQ)
                    {
                        Point2d atomPoint = a.getPoint2d();
                        // get change in x and y direction
                        double deltaX = event.getX() - lastPointerLoc.getX();
                        double deltaY = event.getY() - lastPointerLoc.getY();
                        atomPoint.setX(atomPoint.getX() + deltaX);
                        atomPoint.setY(atomPoint.getY() + deltaY);
                    }
                }
                if (selectedMolecule != null) {
                    Point2d newPoint = new Point2d(event.getX(), event.getY());
                    GeometryUtil.translate2DCenterTo(selectedMolecule, newPoint);
                }

                lastPointerLoc.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                canMove = false;
                screenShot();
                break;
        }

        postInvalidate();
        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        float minSelectionDistance = 150;
        Point2d selectionPoint = new Point2d(0,0);

        @Override
        public boolean onDown(MotionEvent e) {
            selection(e);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            atomSelectionQ.clear();
            selectedAtom = null;
            selectedMolecule = null;
            return true;
        }

        public void onLongPress(MotionEvent e) {

            IAtom a = selectClosestAtom(e.getX(), e.getY());
            if(a != null)
            {
                MoleculeAtom alreadySelectedAtom = (MoleculeAtom)a;
                Molecule mole = alreadySelectedAtom.getMolecule();
                if(mole != null)
                {
                    for (IAtom atom: mole.atoms()) {
                        if(atom != alreadySelectedAtom)
                            atomSelectionQ.add((MoleculeAtom) atom);
                    }
                }
            }
        }

        private void selection(MotionEvent e)
        {
            selectionPoint.set(e.getX(), e.getY());
            IAtom sa = selectClosestAtom(e.getX(), e.getY());
            if(sa == null)
                return;
            selectedAtom = (MoleculeAtom)sa;

            // add to the selection Q, if not null
            if(atomSelectionQ.size() < maxSelectedAtoms)
                if(!atomSelectionQ.contains(selectedAtom))
                    atomSelectionQ.add(selectedAtom);
        }

        @Nullable
        IAtom selectClosestAtom(float xPos, float yPos)
        {
            selectionPoint.set(xPos, yPos);
            IAtom closestAtom = null;
            double curShortestDistance = Double.MAX_VALUE;
            for (Atom a: atoms)
            {
                double distance = selectionPoint.distance(a.getPoint2d());
                if(distance <= minSelectionDistance && distance < curShortestDistance){
                    closestAtom = a;
                    curShortestDistance = distance;
                }
            }
            for (Molecule mole: molecules)
            {
                IAtom a = GeometryUtil.getClosestAtom(xPos, yPos, mole);
                if (a == null)
                    continue;

                double distance = selectionPoint.distance(a.getPoint2d());
                if(distance <= minSelectionDistance && distance < curShortestDistance){
                    closestAtom = a;
                    curShortestDistance = distance;
                }
            }

            return closestAtom;
        }

        @Nullable
        IAtomContainer selectClosestMolecule(float xPos, float yPos)
        {
            selectionPoint.set(xPos, yPos);
            double curShortestDistance = Double.MAX_VALUE;
            IAtomContainer selectedMole = null;
            for(int i = 0; i < molecules.size(); i++)
            {
                IAtomContainer con = molecules.get(i);
                IAtom a = GeometryUtil.getClosestAtom(xPos, yPos, con);
                if (a == null)
                    continue;

                Point2d p = a.getPoint2d();
                double d = p.distance(selectionPoint); // 8==D ~~~> DD
                if(d < curShortestDistance){
                    selectedMole = con;
                    curShortestDistance = d;
                }
            }

            if(curShortestDistance > minSelectionDistance)
                selectedMole = null;

            return selectedMole;
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        float lastScaleFactor = 0;

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
//            scaleFactor *= detector.getScaleFactor();
//            scaleFactor = Math.max(1, Math.min(scaleFactor, 1.2f));
//
//            scaleMoles();
//
//            postInvalidate();
            return true;
        }


        private void scaleMoles()
        {
            for (IAtomContainer mole: molecules)
            {
                Point2d geoCenter = new Point2d(GeometryUtil.get2DCenter(mole));
                ((Molecule)mole).scaleMolecule(scaleFactor);
            }
        }

    }
}
