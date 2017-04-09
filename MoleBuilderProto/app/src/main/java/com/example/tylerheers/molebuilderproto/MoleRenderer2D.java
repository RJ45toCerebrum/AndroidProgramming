package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
 */

public class MoleRenderer2D extends View
{
    public static int maxAtoms = 30;
    public static int maxSelectedAtoms = 10;

    MainActivity moleculeActivity;

    // fields for Atom's
    int numCreatedAtoms = 0;
    int numCreatedBonds = 0;
    int numCreatedMolecules = 0;
    HashMap<String, MoleculeAtom> atoms = new HashMap<>();
    Queue<MoleculeAtom> atomSelectionQ = new LinkedList<>();

    HashMap<String, Molecule> molecules = new HashMap<>();
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

    public int getNumCreatedAtoms() {return numCreatedAtoms;}
    public int getNumCreatedBonds() {return numCreatedBonds;}
    public int getNumCreatedMolecules() {return numCreatedMolecules; }

    public void addAtom(Elements atom)
    {
        if(atoms.size() < maxAtoms)
        {
            atomSelectionQ.clear();
            numCreatedAtoms++;

            MoleculeAtom newAtom = new MoleculeAtom(atom);
            newAtom.setID("atom"+String.valueOf(numCreatedAtoms));
            newAtom.setPoint2d(new Point2d(panX, panY));
            atoms.put(newAtom.getID(), newAtom);
            rendererBitmap = null;

            List<String> ids = new ArrayList<>();
            ids.add(newAtom.getID());
            sendAction(Action.ActionType.Add, ids, MoleculeAtom.class);

            postInvalidate();
        }
    }

    // adds a bond between currently queued atoms
    public void addBond(IBond.Order order)
    {
        if(atomSelectionQ.size() != 2)
            return;

        MoleculeAtom a1 = atomSelectionQ.poll();
        MoleculeAtom a2 = atomSelectionQ.poll();
        if(!a1.canBond(order) || !a2.canBond(order)) {
            Toast.makeText(moleculeActivity, "Max number of bonds = 10", Toast.LENGTH_LONG).show();
            return;
        }

        atoms.remove(a1.getID());
        atoms.remove(a2.getID());

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
            mole.setID("molecule"+String.valueOf(numCreatedMolecules));
            mole.addAtom(a1);
            mole.addAtom(a2);
            a1.setMolecule(mole);
            a2.setMolecule(mole);
        }

        Bond newBond = new Bond(a1, a2);
        a1.addBond(order);
        a2.addBond(order);
        newBond.setOrder(order);
        numCreatedBonds++;
        newBond.setID("bond"+String.valueOf(numCreatedBonds));
        mole.addBond(newBond);
        molecules.put(mole.getID(), mole);

        atomSelectionQ.clear();
        rendererBitmap = null;
        postInvalidate();
    }

    public void addMolecule(Molecule mole)
    {
        if (mole != null)
        {
            normalizePositions(mole);
            GeometryUtil.translate2DCenterTo(mole, new Point2d(getWidth() + panX, getHeight() + panY));

            molecules.put(mole.getID(), mole);

            rendererBitmap = null;

            numCreatedAtoms += mole.getAtomCount();
            numCreatedBonds += mole.getBondCount();
            numCreatedMolecules += 1;

            List<String> ids = new ArrayList<>();
            ids.add(mole.getID());
            sendAction(Action.ActionType.Add, ids, Molecule.class);
        }
    }

    public void deleteSelected()
    {
        for (IAtom a: atomSelectionQ)
        {
            MoleculeAtom atom = (MoleculeAtom)a;
            atoms.remove(atom.getID());

            for(Molecule mole : molecules.values())
            {
                if(mole.contains(atom))
                    mole.removeAtom(atom);
            }
        }

        rendererBitmap = null;
        postInvalidate();
    }

    public boolean isSelection()
    {
        return !(atomSelectionQ.isEmpty() && selectedMolecule == null);
    }

    public void undoAdd(List<String> objIDs, Class<?> classType)
    {
        if(classType == MoleculeAtom.class)
        {
            for (String id: objIDs)
            {
                MoleculeAtom a = atoms.get(id);
                if(a != null)
                {
                    Molecule m = a.getMolecule();
                    if(m != null)
                        m.removeAtom(a);

                    atoms.remove(id);
                }
            }
        }
        else if(classType == Molecule.class)
        {
            for (String id: objIDs)
                molecules.remove(id);
        }

        rendererBitmap = null;
        postInvalidate();
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

    // DelETE
    private void printAtomPos()
    {
        for (IAtom a: atoms.values())
            Log.i("Atom Pos", a.getPoint2d().toString());
    }

    void drawAtoms(Canvas canvas)
    {
        // for individually created atoms
        for (Atom a: atoms.values())
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
        for (Molecule mole : molecules.values())
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
        for (Molecule m: molecules.values())
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
                    for (IAtom a: atoms.values()) {
                        Point2d atomPoint = a.getPoint2d();
                        atomPoint.setX(atomPoint.getX() + dx);
                        atomPoint.setY(atomPoint.getY() + dy);
                    }
                    for (Molecule m: molecules.values()) {
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
            for (Atom a: atoms.values())
            {
                double distance = selectionPoint.distance(a.getPoint2d());
                if(distance <= (minSelectionDistance * scaleFactor) && distance < curShortestDistance){
                    closestAtom = a;
                    curShortestDistance = distance;
                }
            }
            for (Molecule mole: molecules.values())
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
