package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.Bond;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.rajawali3d.math.vector.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    // fields for Atom's
    ArrayList<Atom> atoms = new ArrayList<>();
    Queue<Atom> atomSelectionQ = new LinkedList<>();
    AtomContainerSet atomContainerSet;
    Atom selectedAtom = null;
    IAtomContainer selectedMolecule = null;
    float atomCircleRadius = 55.0f;
    float textSize = 60.0f;

    float zoomFactor = 1.0f;
    double lastZoomDistance = 0;
    boolean setMoleScaleFactor = false;

    // Fields for Bond's
    HashMap<Long, IBond> bondHashMap = new HashMap<>();

    // selection specs
    GestureDetector gestureDetector;
    boolean canMove = false;
    float minSelectionDistance = 150;
    Point2d selectionPoint = new Point2d(0,0);

    // line fields
    Paint atomPaint;
    Paint selectedAtomPaint;
    Paint bondPaint;


    public MoleRenderer2D(Context context){
        super(context);
        init(null, 0);
    }

    public MoleRenderer2D(Context context, AttributeSet set) {
        super(context, set);
        setFocusable(true);
        setFocusableInTouchMode(true);
        init(set, 0);
    }

    public MoleRenderer2D(Context context, AttributeSet set, int defStyle){
        super(context, set, defStyle);
        init(set, 0);
    }

    private void init(AttributeSet set, int defStyle)
    {
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

        bondPaint = new Paint();
        bondPaint.setColor(Color.BLACK);
        bondPaint.setStrokeWidth(8);
        bondPaint.setStyle(Paint.Style.STROKE);

        gestureDetector = new GestureDetector(this.getContext(), new GestureListener());
    }

    public void addAtom(Atom atom)
    {
        if(atoms.size() < maxAtoms) {
            atom.setPoint2d(new Point2d(10, 10));
            atoms.add(atom);
            selectedAtom = atom;
            postInvalidate();
        }
    }

    public void addAtom(Atom atom, Point2d point)
    {
        atom.setPoint2d(point);
        atoms.add(atom);
    }

    // adds a bond between currently queued atoms
    public void addBond(IBond.Order order)
    {
        if(atomSelectionQ.size() != 2)
            return;

        Atom a1 = atomSelectionQ.poll();
        Atom a2 = atomSelectionQ.poll();

        // extremely small chance that this will collide
        // with the addition of other unique hash codes
        //TODO: make it to where there is 0 chance of collision
        long bondHashCode = a1.hashCode() + a2.hashCode();
        if(bondHashMap.get(bondHashCode) != null)
            return;

        Bond newBond = new Bond(a1, a2);
        newBond.setOrder(order);
        bondHashMap.put(bondHashCode, newBond);

        postInvalidate();
    }

    public void addBond(IBond b)
    {
        if(b.getAtomCount() != 0)
            return;

        IAtom a1 = b.getAtom(0);
        IAtom a2 = b.getAtom(1);
        long bondHashCode = getBondHashCode(b);
        if(bondHashMap.get(bondHashCode) != null)
            return;

        Bond newBond = new Bond(a1, a2);
        newBond.setOrder(b.getOrder());
        bondHashMap.put(bondHashCode, newBond);
    }

    public void addMolecule(IAtomContainer atomContainer){
        if (atomContainer != null)
        {
            normalizePositions(atomContainer);
            atomContainerSet.addAtomContainer(atomContainer);

            for (IBond b: atomContainer.bonds())
            {
                if(b.getAtomCount() == 2) {
                    bondHashMap.put(getBondHashCode(b), b);
                }
            }
        }
    }

    private void normalizePositions(IAtomContainer atomContainer)
    {
        for (IAtom a: atomContainer.atoms()) {
            Point2d point = a.getPoint2d();
            point.scale(300.0);
        }
    }

    public void setSelectionDistance(float distance){
        if(distance < 10){
            throw new IllegalArgumentException("distance must be greater than 10 pixels");
        }

        minSelectionDistance = distance;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawAtoms(canvas);
        drawMolecules(canvas);
        drawBonds(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        gestureDetector.onTouchEvent(event);

        // for the single pointer
        if (event.getPointerCount() == 1)
        {
            switch (event.getAction()) {
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
                    selectedAtom = null;
                    selectedMolecule = null;
                    break;
            }
        }
        else if(event.getPointerCount() == 2)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if(lastZoomDistance == 0)
                    {
                        Point2d p1= new Point2d(event.getX(0), event.getY(0));
                        Point2d p2= new Point2d(event.getX(1), event.getY(1));
                        lastZoomDistance = p1.distance(p2);
                    }

                    Point2d p1= new Point2d(event.getX(0), event.getY(0));
                    Point2d p2= new Point2d(event.getX(1), event.getY(1));
                    double newDistance = p1.distance(p2);
                    if (newDistance > lastZoomDistance)
                        zoomFactor += (newDistance / lastZoomDistance) / 20;
                    else
                        zoomFactor -= (newDistance / lastZoomDistance) / 20;

                    lastZoomDistance = newDistance;
                    zoomFactor = clampValue(zoomFactor, 1f, 3.0f);
                    setMoleScaleFactor = true;
                    break;
                case MotionEvent.ACTION_UP:
                    lastZoomDistance = 0;
                    break;
            }
        }

        postInvalidate();
        return true;
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
            }
        }

        return selectedMole;
    }

    void drawAtoms(Canvas canvas)
    {
        // for individually created atoms
        for (Atom a: atoms)
        {
            Point2d aPoint = a.getPoint2d();
            if(a == selectedAtom || atomSelectionQ.contains(a))
                canvas.drawCircle((float)aPoint.getX(), (float)aPoint.getY(),
                                    atomCircleRadius * zoomFactor, selectedAtomPaint);
            else
                canvas.drawCircle((float)aPoint.getX(), (float)aPoint.getY(),
                                  atomCircleRadius * zoomFactor, atomPaint);

            float x = (float)aPoint.getX();
            float y = (float)aPoint.getY();
            atomPaint.setTextSize(textSize * zoomFactor);
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
            canvas.drawCircle(x, y, atomCircleRadius * zoomFactor, p);

            atomPaint.setTextSize(textSize * zoomFactor);
            canvas.drawText(a.getSymbol(), x, y+atomCircleRadius/2, atomPaint);
        }
    }

    void drawBonds(Canvas canvas)
    {
        for (IBond b: bondHashMap.values())
        {
            Point2d bondPos = b.get2DCenter();
            Point2d a1Pos = b.getAtom(0).getPoint2d();
            Point2d a2Pos = b.getAtom(1).getPoint2d();

            float xPos1 = (float)a1Pos.getX();
            float yPos1 = (float)a1Pos.getY();
            float xPos2 = (float)a2Pos.getX();
            float yPos2 = (float)a2Pos.getY();

            switch (b.getOrder())
            {
                case SINGLE:
                    canvas.drawLine(xPos1, yPos1, xPos2, yPos2, bondPaint);
                    break;
                case DOUBLE:
                    Vector2d ortho = getOrthoVector(a1Pos, a2Pos);
                    ortho.scale(20);
                    canvas.drawLine(xPos1, yPos1, xPos2, yPos2, bondPaint);
                    canvas.drawLine(xPos1+(float)ortho.getX(), yPos1+(float)ortho.getY(),
                            xPos2+(float)ortho.getX(), yPos2+(float)ortho.getY(), bondPaint);
                    break;
                case TRIPLE:
                    Vector2d ortho2 = getOrthoVector(a1Pos, a2Pos);
                    ortho2.scale(20);
                    canvas.drawLine(xPos1, yPos1, xPos2, yPos2, bondPaint);
                    canvas.drawLine(xPos1+(float)ortho2.getX(), yPos1+(float)ortho2.getY(),
                            xPos2+(float)ortho2.getX(), yPos2+(float)ortho2.getY(), bondPaint);
                    canvas.drawLine(xPos1-(float)ortho2.getX(), yPos1-(float)ortho2.getY(),
                            xPos2-(float)ortho2.getX(), yPos2-(float)ortho2.getY(), bondPaint);
                    break;
                default:
                    break;
            }

            canvas.drawLine((float)a1Pos.getX(), (float)a1Pos.getY(),
                    (float)a2Pos.getX(), (float)a2Pos.getY(), bondPaint);
        }


    }

    void drawMolecules(Canvas canvas)
    {
        // draw each in atom container; for Molecules
        for (int i=0; i < atomContainerSet.getAtomContainerCount(); i++)
        {
            IAtomContainer con = atomContainerSet.getAtomContainer(i);
            if(setMoleScaleFactor)
            {
                scaleMole(con, 1.001f);
                setMoleScaleFactor = false;
            }
            drawAtoms(canvas, con);
        }
        drawBonds(canvas);
        postInvalidate();
    }

    private void scaleMole(IAtomContainer con, float scale)
    {
       GeometryUtil.scaleMolecule(con, scale);
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
        long bondHashCode = a1.hashCode() + a2.hashCode();
        return  bondHashCode;
    }

    private float clampValue(float value, float min, float max)
    {
        if(value < min)
            value = min;
        else if(value > max)
            value = max;

        return value;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onDown(MotionEvent event){
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            selectionPoint.set(e.getX(), e.getY());
            selectedAtom = selectClosestAtom(e.getX(), e.getY());
            selectedMolecule = selectClosestMolecule(e.getX(), e.getY());

            if(selectedAtom != null && selectedMolecule != null){
                Point2d geoCenter = GeometryUtil.get2DCenter(selectedMolecule);
                double dist = selectionPoint.distance(geoCenter);
                double dist2 = selectionPoint.distance(selectedAtom.getPoint2d());
                Log.d("Dist: ", String.valueOf(dist));
                Log.d("Dist 2: ", String.valueOf(dist2));
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


            return true;
        }
    }

}
