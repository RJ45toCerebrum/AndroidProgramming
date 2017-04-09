package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.matchers.CTFileQueryBond;

import java.lang.annotation.ElementType;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Created by tylerheers on 3/10/17.
 * All about rendering molecules
 */

public class MoleRenderer2D extends View
{
    MainActivity moleculeActivity;

    // fields for Atom's
    Queue<MoleculeAtom> atomSelectionQ = new LinkedList<>();

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

    // rendering
    Bitmap rendererBitmap;
    CountDownTimer screenShotTimer;
    long timeToScreenShot = 500;       // milli-secs

    double dx, dy;
    double panX, panY;
    protected float focusX;
    protected float focusY;


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

        screenShotTimer = new CountDownTimer(timeToScreenShot, 1000)
        {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                screenShot();
            }
        };
    }

    public void addAtom(Elements atom)
    {
        int atomCount = MainActivity.getAtomCount();
        if(atomCount < MainActivity.maxAtoms)
        {
            atomSelectionQ.clear();
            MainActivity.addAtomCount(1);

            MoleculeAtom newAtom = new MoleculeAtom(atom);
            newAtom.setID("atom"+String.valueOf(atomCount+1));
            newAtom.setPoint2d(new Point2d(panX, panY));
            MainActivity.putAtom(newAtom);
            rendererBitmap = null;

            List<String> ids = new ArrayList<>();
            ids.add(newAtom.getID());
            sendAction(Action.ActionType.Add, ids, MoleculeAtom.class);

            postInvalidate();
        }
    }

    // adds a bond between currently queued atoms
    public boolean addBond(IBond.Order order)
    {
        if(atomSelectionQ.size() != 2)
            return false;

        MoleculeAtom a1 = atomSelectionQ.poll();
        MoleculeAtom a2 = atomSelectionQ.poll();
        if(!a1.canBond(order) || !a2.canBond(order)) {
            Toast.makeText(moleculeActivity, "Max number of bonds = 10", Toast.LENGTH_LONG).show();
            return false;
        }

        MainActivity.delAtom(a1.getID());
        MainActivity.delAtom(a2.getID());

        Molecule mole;
        // check if atom one is already a part of a molecule
        if(a1.isInMolecule())
        {
            mole = a1.getMolecule();
            Molecule atom2Mole = a2.getMolecule();
            if(atom2Mole != null)
                mole.addMolecule(atom2Mole);
            else
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
            mole.setID("molecule"+String.valueOf(MainActivity.getMoleculeCount()));
            mole.addAtom(a1);
            mole.addAtom(a2);
            a1.setMolecule(mole);
            a2.setMolecule(mole);
        }

        Bond newBond = new Bond(a1, a2);
        a1.addBond(order);
        a2.addBond(order);
        newBond.setOrder(order);
        MainActivity.addBondCount(order.numeric());
        MainActivity.addBondCount(1);
        newBond.setID("bond"+String.valueOf( MainActivity.getBondCount() ));
        mole.addBond(newBond);
        MainActivity.putMolecule(mole);

        atomSelectionQ.clear();
        rendererBitmap = null;
        postInvalidate();

        return true;
    }

    public boolean addMolecule(Molecule mole)
    {
        if (mole != null)
        {
            normalizePositions(mole);
            GeometryUtil.translate2DCenterTo(mole, new Point2d(getWidth() + panX, getHeight() + panY));

            MainActivity.putMolecule(mole);

            rendererBitmap = null;

            MainActivity.addAtomCount(mole.getAtomCount());
            MainActivity.addBondCount(mole.getBondCount());
            MainActivity.addMoleculeCount(1);

            List<String> ids = new ArrayList<>();
            ids.add(mole.getID());
            sendAction(Action.ActionType.Add, ids, Molecule.class);

            return true;
        }

        return false;
    }

    public void deleteSelected()
    {
        for (IAtom a: atomSelectionQ)
        {
            MoleculeAtom atom = (MoleculeAtom)a;
            MainActivity.delAtom(atom.getID());

            for(Molecule mole : MainActivity.getMolecules())
            {
                if(mole.contains(atom))
                    mole.removeAtom(atom);
            }
        }

        rendererBitmap = null;
        postInvalidate();
    }

    public void undoAdd(List<String> objIDs, Class<?> classType)
    {
        Log.e("Implementation", "Not implemented yet");
//        if(classType == MoleculeAtom.class)
//        {
//            for (String id: objIDs)
//            {
//                MoleculeAtom a = atoms.get(id);
//                if(a != null)
//                {
//                    Molecule m = a.getMolecule();
//                    if(m != null)
//                        m.removeAtom(a);
//
//                    atoms.remove(id);
//                }
//            }
//        }
//        else if(classType == Molecule.class)
//        {
//            for (String id: objIDs)
//                molecules.remove(id);
//        }
//
//        rendererBitmap = null;
//        postInvalidate();
    }

    void sendAction(Action.ActionType actionType, List<String> objID, Class<?> classType)
    {
        Action action = new Action();
        action.setActionType(actionType);

        if(objID == null || objID.size() == 0)
            throw new InvalidParameterException("Invalid ID list for action");

        action.setObjIDList(objID);
        action.setClassType(classType);

        moleculeActivity.addAction(action);
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

    void drawAtoms(Canvas canvas)
    {
        // for individually created atoms
        for (Atom a: MainActivity.getAtoms())
        {
            Point2d aPoint = a.getPoint2d();
            float x = (float)(aPoint.getX());
            float y = (float)(aPoint.getY());
            if(a == selectedAtom || atomSelectionQ.contains(a))
                canvas.drawCircle(x, y, atomCircleRadius * scaleFactor, selectedAtomPaint);
            else
                canvas.drawCircle(x, y, atomCircleRadius * scaleFactor, atomPaint);

            atomPaint.setTextSize(textSize * scaleFactor);
            canvas.drawText(a.getSymbol(),x, y + atomCircleRadius/2, atomPaint);
        }
    }

    void drawAtoms(Canvas canvas, IAtomContainer con)
    {
        for (IAtom a: con.atoms())
        {
            Point2d aPoint = a.getPoint2d();
            float x = (float)(aPoint.getX());
            float y = (float)(aPoint.getY());
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
        for (Molecule mole : MainActivity.getMolecules())
        {
            // go through all bonds
            for (IBond b : mole.bonds())
            {
                Point2d a1Pos = b.getAtom(0).getPoint2d();
                Point2d a2Pos = b.getAtom(1).getPoint2d();

                float xPos1 = (float) (a1Pos.getX());
                float yPos1 = (float) (a1Pos.getY());
                float xPos2 = (float) (a2Pos.getX());
                float yPos2 = (float) (a2Pos.getY());

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
        for (Molecule m: MainActivity.getMolecules())
            drawAtoms(canvas, m);

        drawBonds(canvas);
        postInvalidate();
    }

    private void screenShot() {
        rendererBitmap = loadBitmapFromView(getWidth(), getHeight());
    }

    private Bitmap loadBitmapFromView(int width, int height)
    {
        Bitmap b = Bitmap.createBitmap(width , height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        layout(getLeft(), getTop(), width, height);
        draw(c);
        return b;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.save();
        if (rendererBitmap == null)
        {
            canvas.scale(scaleFactor, scaleFactor, focusX, focusY);
            drawAtoms(canvas);
            drawMolecules(canvas);
            drawBonds(canvas);
        }
        else {
            Log.i("Bitmap", "Drawing");
            canvas.drawBitmap(rendererBitmap, 0, getTop(), atomPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        rendererBitmap = null;
        gestureDetector.onTouchEvent(event);
        scalerDetector.onTouchEvent(event);

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                screenShotTimer.cancel();
                lastPointerLoc.set(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                rendererBitmap = null;
                dx = (event.getX() - lastPointerLoc.getX());
                dy = (event.getY() - lastPointerLoc.getY());
                if (selectedAtom != null)
                {
                    for (IAtom a : atomSelectionQ)
                    {
                        Point2d atomPoint = a.getPoint2d();
                        atomPoint.setX(atomPoint.getX() + dx);
                        atomPoint.setY(atomPoint.getY() + dy);
                    }
                }
                if (selectedMolecule != null) {
                    Point2d newPoint = new Point2d(event.getX(), event.getY());
                    GeometryUtil.translate2DCenterTo(selectedMolecule, newPoint);
                }

                // Dragging
                if(selectedAtom == null && selectedMolecule == null)
                {
                    for (IAtom a: MainActivity.getAtoms()) {
                        Point2d atomPoint = a.getPoint2d();
                        atomPoint.setX(atomPoint.getX() + dx);
                        atomPoint.setY(atomPoint.getY() + dy);
                    }
                    for (Molecule m: MainActivity.getMolecules()) {
                        for (IAtom a: m.atoms()) {
                            Point2d atomPoint = a.getPoint2d();
                            atomPoint.setX(atomPoint.getX() + dx);
                            atomPoint.setY(atomPoint.getY() + dy);
                        }
                    }
                }

                lastPointerLoc.set(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                dx = 0;
                dy = 0;
                canMove = false;
                screenShotTimer.start();
                break;
        }

        postInvalidate();
        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        float minSelectionDistance = 100 + atomCircleRadius;
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
                        if(atom != alreadySelectedAtom || !atomSelectionQ.contains(atom))
                            atomSelectionQ.add((MoleculeAtom) atom);
                    }

                     //Testing here
//                    try {
//                        ModelBuilder3D builder3D = ModelBuilder3D.getInstance(DefaultChemObjectBuilder.getInstance());
//                        IAtomContainer con = builder3D.generate3DCoordinates(mole, false);
//                        for (IAtom newAtom: con.atoms()) {
//                            Log.d("new atom", newAtom.getPoint3d().toString());
//                        }
//                    }
//                    catch (CDKException ex) {
//                        Log.i("Building 3d exception", ex.getMessage());
//                    }
//                    catch (Exception io){
//                        Log.i("Clone or IO exception", io.getMessage());
//                    }
                     //end testing
                }
            }
        }

        private void selection(MotionEvent e)
        {
            float x = (e.getX() - (float)panX);
            float y = (e.getY() - (float)panY);
            IAtom sa = selectClosestAtom(x, y);
            if(sa == null)
                return;
            selectedAtom = (MoleculeAtom)sa;
            // add to the selection Q, if not null
            if(atomSelectionQ.size() < MainActivity.maxSelectedAtoms)
                if(!atomSelectionQ.contains(selectedAtom))
                    atomSelectionQ.add(selectedAtom);
        }

        @Nullable
        IAtom selectClosestAtom(float xPos, float yPos)
        {
            selectionPoint.set(xPos, yPos);
            IAtom closestAtom = null;
            double curShortestDistance = Double.MAX_VALUE;
            for (Atom a: moleculeActivity.getAtoms())
            {
                double distance = selectionPoint.distance(a.getPoint2d());
                if(distance <= (minSelectionDistance * scaleFactor) && distance < curShortestDistance){
                    closestAtom = a;
                    curShortestDistance = distance;
                }
            }
            for (Molecule mole: moleculeActivity.getMolecules())
            {
                IAtom a = GeometryUtil.getClosestAtom(xPos, yPos, mole);
                if (a == null)
                    continue;

                double distance = selectionPoint.distance(a.getPoint2d());
                if(distance <= (minSelectionDistance * scaleFactor) && distance < curShortestDistance){
                    closestAtom = a;
                    curShortestDistance = distance;
                }
            }

            return closestAtom;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            Log.i("Stuff", "Scalling");
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.3f, Math.min(scaleFactor, 1.5f));

            focusX = detector.getFocusX ();
            focusY = detector.getFocusY ();

            postInvalidate();
            return true;
        }

    }
}
