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
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.Bond;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
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
    public static int maxSelectedAtoms = 2;

    MainActivity moleculeActivity;

    // fields for Atom's
    ArrayList<Atom> atoms = new ArrayList<>();
    Queue<Atom> atomSelectionQ = new LinkedList<>();
    AtomContainerSet atomContainerSet;
    Atom selectedAtom = null;
    IAtomContainer selectedMolecule = null;
    float atomCircleRadius = 55.0f;
    float textSize = 60.0f;

    // for selection
    GestureDetector gestureDetector;
    ScaleGestureDetector scalerDetector;
    float scaleFactor = 1.0f;
    float maxScaleFactor = 5.0f;
    boolean canMove = false;

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
        init(context, null, 0);
    }

    public MoleRenderer2D(Context context, AttributeSet set) {
        super(context, set);
        setFocusable(true);
        setFocusableInTouchMode(true);
        init(context, set, 0);
    }

    public MoleRenderer2D(Context context, AttributeSet set, int defStyle){
        super(context, set, defStyle);
        init(context, set, 0);
    }


    private void init(Context con, AttributeSet set, int defStyle)
    {
        moleculeActivity = (MainActivity) con;
        atomContainerSet = new AtomContainerSet();

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

        gestureDetector = new GestureDetector(this.getContext(), new GestureListener(moleculeActivity));
        scalerDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public void addAtom(Atom atom)
    {
        if(atoms.size() < maxAtoms) {
            atom.setPoint2d(new Point2d(10, 10));
            atoms.add(atom);
            selectedAtom = atom;
            rendererBitmap = null;
            lastActive = System.currentTimeMillis();
            postInvalidate();
        }
    }

    public void deleteSelected()
    {
        for (IAtom a: atomSelectionQ) {
            atoms.remove(a);
            atomSelectionQ.remove(a);
        }
        for (int i =0; i < selectedMolecule.getBondCount(); i++) {
            selectedMolecule.getBond(i);
        }
        atomContainerSet.removeAtomContainer(selectedMolecule);
        selectedMolecule = null;

        postInvalidate();
    }

    // adds a bond between currently queued atoms
    public void addBond(IBond.Order order)
    {
        if(atomSelectionQ.size() != 2)
            return;

        Atom a1 = atomSelectionQ.poll();
        Atom a2 = atomSelectionQ.poll();
        atoms.remove(a1);
        atoms.remove(a2);

        IAtomContainer newMole = new AtomContainer();
        newMole.addAtom(a1);
        newMole.addAtom(a2);

        Bond newBond = new Bond(a1, a2);
        newBond.setOrder(order);
        newMole.addBond(newBond);
        atomContainerSet.addAtomContainer(newMole);
        atomSelectionQ.clear();

        rendererBitmap = null;
        lastActive = System.currentTimeMillis();
        postInvalidate();
    }

    public Iterator<Atom> getSelectedAtoms(){
        return atomSelectionQ.iterator();
    }

    public void addMolecule(IAtomContainer atomContainer)
    {
        if (atomContainer != null)
        {
            normalizePositions(atomContainer);
            atomContainerSet.addAtomContainer(atomContainer);

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
        Paint p = atomPaint;
        if(con == selectedMolecule){
            p = selectedAtomPaint;
        }

        for (IAtom a: con.atoms())
        {
            Point2d aPoint = a.getPoint2d();
            float x = (float)aPoint.getX();
            float y = (float)aPoint.getY();
            canvas.drawCircle(x, y, atomCircleRadius * scaleFactor, p);

            atomPaint.setTextSize(textSize * scaleFactor);
            canvas.drawText(a.getSymbol(), x, y+atomCircleRadius/2, atomPaint);
        }
    }

    void drawBonds(Canvas canvas)
    {
        // for each molecule
        for (int i = 0; i < atomContainerSet.getAtomContainerCount(); i++)
        {
            IAtomContainer mole = atomContainerSet.getAtomContainer(i);
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
        for (int i=0; i < atomContainerSet.getAtomContainerCount(); i++)
        {
            IAtomContainer con = atomContainerSet.getAtomContainer(i);
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
        scalerDetector.onTouchEvent(event);

        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                if (selectedAtom != null) {
                    Point2d atomPoint = selectedAtom.getPoint2d();
                    atomPoint.setX(event.getX());
                    atomPoint.setY(event.getY());
                }
                if (selectedMolecule != null) {
                    Point2d newPoint = new Point2d(event.getX(), event.getY());
                    GeometryUtil.translate2DCenterTo(selectedMolecule, newPoint);
                }
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
        private MainActivity ma;
        private int numClicks = 0;

        GestureListener(MainActivity ma){
            if(ma == null)
                throw new InvalidParameterException("main activity cannot be null");

            this.ma = ma;
        }

        public boolean onDoubleTap(MotionEvent e)
        {
            selection(e);
            return true;
        }

        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            //selection(e);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e)
        {
            selection(e);
            return true;
        }

        private void selection(MotionEvent e)
        {
            selectionPoint.set(e.getX(), e.getY());
            selectedAtom = selectClosestAtom(e.getX(), e.getY());
            selectedMolecule = selectClosestMolecule(e.getX(), e.getY());

            if(selectedAtom != null && selectedMolecule != null)
            {
                Point2d geoCenter = GeometryUtil.get2DCenter(selectedMolecule);
                double dist = selectionPoint.distance(geoCenter);
                double dist2 = selectionPoint.distance(selectedAtom.getPoint2d());
                if(dist < dist2)
                    selectedAtom = null;
                else
                    selectedMolecule = null;
            }

            // add to the selection Q, if not null
            if(selectedAtom != null && atomSelectionQ.size() < maxSelectedAtoms)
                atomSelectionQ.add(selectedAtom);
            else
                atomSelectionQ.clear();
        }


        @Nullable
        Atom selectClosestAtom(float xPos, float yPos)
        {
            selectionPoint.set(xPos, yPos);
            Atom closestAtom = null;
            double curShortestDistance = Double.MAX_VALUE;
            for (Atom a: atoms)
            {
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
            for(int i =0; i < atomContainerSet.getAtomContainerCount(); i++)
            {
                IAtomContainer con = atomContainerSet.getAtomContainer(i);
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
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(1, Math.min(scaleFactor, 1.2f));

            scaleMoles();

            postInvalidate();
            return true;
        }


        private void scaleMoles()
        {
            for (IAtomContainer mole: atomContainerSet.atomContainers())
            {
                Point2d geoCenter = new Point2d(GeometryUtil.get2DCenter(mole));
                double currMoleScale = GeometryUtil.getScaleFactor(mole, GeometryUtil.getBondLengthAverage(mole));
                if(currMoleScale < 2) {
                    GeometryUtil.scaleMolecule(mole, scaleFactor);
                    GeometryUtil.translate2DCenterTo(mole, geoCenter);
                }
            }
        }

    }
}
