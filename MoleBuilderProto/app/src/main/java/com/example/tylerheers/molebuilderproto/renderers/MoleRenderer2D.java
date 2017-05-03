package com.example.tylerheers.molebuilderproto.renderers;

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

import com.example.tylerheers.molebuilderproto.MainActivity;
import com.example.tylerheers.molebuilderproto.Molecule;
import com.example.tylerheers.molebuilderproto.MoleculeAtom;
import com.example.tylerheers.molebuilderproto.Pair;
import com.example.tylerheers.molebuilderproto.SceneContainer;

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
import java.util.Collection;
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
    SceneContainer sceneContainer;

    // fields for Atom's
    Queue<MoleculeAtom> atomSelectionQ = new LinkedList<>();
    MoleculeAtom selectedAtom = null;
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
        sceneContainer = SceneContainer.getInstance();

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

    public void addAtom(Elements atom, Point2d placement)
    {
        int atomCount = sceneContainer.getAtomCount();
        if(atomCount < MainActivity.maxAtoms)
        {
            atomSelectionQ.clear();

            MoleculeAtom newAtom = new MoleculeAtom(atom);
            newAtom.setID("atom"+String.valueOf(atomCount+1));
            newAtom.setPoint2d(new Point2d(panX + placement.x, panY + placement.y));
            newAtom.setImplicitHydrogenCount(0);
            newAtom.setFormalCharge(0);
            sceneContainer.putAtom(newAtom);
            rendererBitmap = null;


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

        Molecule mole;
        // check if atom one is already a part of a molecule
        if(a1.isInMolecule())
        {
            mole = a1.getMolecule();
            if(alreadyBonded(mole, a1, a2))
                return false;

            Molecule atom2Mole = a2.getMolecule();
            if (atom2Mole != null)
            {
                //if the molecule is cyclic
                if(atom2Mole != mole)
                    mole.addMolecule(atom2Mole);
            }
            else
                mole.addAtom(a2);
        }
        else if(a2.isInMolecule())
        {
            mole = a2.getMolecule();
            mole.addAtom(a1);
        }
        else
        {
            mole = new Molecule();
            mole.setID("molecule"+String.valueOf(sceneContainer.getMoleculeCount()));
            mole.addAtom(a1);
            mole.addAtom(a2);
            sceneContainer.putMolecule(mole);
        }

        Bond newBond = new Bond(a1, a2);
        a1.addBond(order);
        a2.addBond(order);
        newBond.setOrder(order);
        newBond.setID("bond" + String.valueOf( sceneContainer.getBondCount() ));
        mole.addBond(newBond);

        sceneContainer.updateSceneListeners(SceneContainer.SceneChangeType.BondNumber);
        atomSelectionQ.clear();
        rendererBitmap = null;
        postInvalidate();

        return true;
    }

    private boolean alreadyBonded(Molecule m, MoleculeAtom a1, MoleculeAtom a2)
    {
        try
        {
            IBond b = m.getBond(a1, a2);
            if(b == null)
                return false;
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public boolean addMolecule(Molecule mole)
    {
        if (mole != null)
        {
            normalizePositions(mole);
            Point2d startPoint = new Point2d(getWidth() + panX, getHeight() + panY);
            Log.i("Add Atom Start Point", startPoint.toString());
            GeometryUtil.translate2DCenterTo(mole, startPoint);

            sceneContainer.putMolecule(mole);

            rendererBitmap = null;
            return true;
        }

        return false;
    }

    public void deleteSelected()
    {
        List<Pair<MoleculeAtom, Molecule>> atomsToDel = new ArrayList<>();
        for (IAtom a : atomSelectionQ)
        {
            MoleculeAtom atom = (MoleculeAtom) a;
            sceneContainer.delAtom(atom.getID());

            for (Molecule mole : sceneContainer.getMolecules())
            {
                if (mole.contains(atom))
                {
                    Pair<MoleculeAtom, Molecule> p = new Pair<>();
                    p.first = atom;
                    p.second = mole;
                    atomsToDel.add(p);
                }
            }
        }

        for (Pair<MoleculeAtom, Molecule> p : atomsToDel)
        {
            Molecule m = sceneContainer.getMolecule(p.second.getID());
            m.removeAtom(p.first);
        }

        rendererBitmap = null;
        sceneContainer.updateSceneListeners(SceneContainer.SceneChangeType.All);
        postInvalidate();
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
        for (Atom a: sceneContainer.getAtoms())
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
        for (Molecule mole : sceneContainer.getMolecules())
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
        for (Molecule m: sceneContainer.getMolecules())
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

    public void updateBitmap(){
        rendererBitmap = null;
        postInvalidate();
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

                if(moleculeActivity.getCurrentMode() == MainActivity.Mode.AddAtom)
                {
                    Elements e = moleculeActivity.getCurrentElement();
                    if(e != null)
                        addAtom(e, lastPointerLoc);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                rendererBitmap = null;
                dx = (event.getX() - lastPointerLoc.getX());
                dy = (event.getY() - lastPointerLoc.getY());

                // for moving selected molecules
                if(moleculeActivity.getCurrentMode() == MainActivity.Mode.Selection)
                {
                    if (selectedAtom != null) {
                        for (IAtom a : atomSelectionQ) {
                            Point2d atomPoint = a.getPoint2d();
                            atomPoint.setX(atomPoint.getX() + dx);
                            atomPoint.setY(atomPoint.getY() + dy);
                        }
                    }
                    if (sceneContainer.selectedMolecule != null) {
                        Point2d newPoint = new Point2d(event.getX(), event.getY());
                        GeometryUtil.translate2DCenterTo(sceneContainer.selectedMolecule, newPoint);
                    }
                }

                // Dragging; pan by translating atoms; No difference
                if(moleculeActivity.getCurrentMode() == MainActivity.Mode.PanZoom)
                {
                    if (selectedAtom == null && sceneContainer.selectedMolecule == null)
                    {
                        for (IAtom a : sceneContainer.getAtoms()) {
                            Point2d atomPoint = a.getPoint2d();
                            atomPoint.setX(atomPoint.getX() + dx);
                            atomPoint.setY(atomPoint.getY() + dy);
                        }
                        for (Molecule m : sceneContainer.getMolecules()) {
                            for (IAtom a : m.atoms()) {
                                Point2d atomPoint = a.getPoint2d();
                                atomPoint.setX(atomPoint.getX() + dx);
                                atomPoint.setY(atomPoint.getY() + dy);
                            }
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
        public boolean onDown(MotionEvent e)
        {
            if(moleculeActivity.getCurrentMode() == MainActivity.Mode.Selection) {
                selection(e);
                return true;
            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            atomSelectionQ.clear();
            selectedAtom = null;
            sceneContainer.selectedMolecule = null;

            return true;
        }

        public void onLongPress(MotionEvent e)
        {
            if(moleculeActivity.getCurrentMode() == MainActivity.Mode.Selection)
            {
                IAtom a = selectClosestAtom(e.getX(), e.getY());
                if (a != null)
                {
                    MoleculeAtom alreadySelectedAtom = (MoleculeAtom) a;
                    Molecule mole = alreadySelectedAtom.getMolecule();
                    if (mole != null)
                    {
                        sceneContainer.selectedMolecule = mole;
                        for (IAtom atom : mole.atoms()) {
                            if (atom != alreadySelectedAtom || !atomSelectionQ.contains(atom))
                                atomSelectionQ.add((MoleculeAtom) atom);
                        }
                    }
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
            for (Atom a: sceneContainer.getAtoms())
            {
                double distance = selectionPoint.distance(a.getPoint2d());
                if(distance <= (minSelectionDistance * scaleFactor) && distance < curShortestDistance){
                    closestAtom = a;
                    curShortestDistance = distance;
                }
            }
            for (Molecule mole: sceneContainer.getMolecules())
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
            if(moleculeActivity.getCurrentMode() == MainActivity.Mode.PanZoom)
            {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.3f, Math.min(scaleFactor, 1.5f));

                focusX = detector.getFocusX();
                focusY = detector.getFocusY();

                postInvalidate();
                return true;
            }

            return false;
        }

    }
}
